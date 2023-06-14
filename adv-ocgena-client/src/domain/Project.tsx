import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { OCDotToDOTConverter } from '../ocdot/converter';
import { PeggySyntaxError } from 'ocdot-parser/lib/ocdot.peggy';
import { AST } from 'ocdot-parser';
import * as rxops from 'rxjs/operators';
import { ErrorsMessage, HtmlMessage, IPCMesage, LaunchMessage, OcelMessage, SimulationClientStatus, SimulationStatusMessage, isIpcMessage } from '../main/shared';

import {
  OcDotContent,
  ProjectWindowStructure,
  ProjectWindow,
  SimulationArgument,
  ProjectWindowId,
  SimulationConfig,
} from '../main/domain';
import { GraphvizView } from './views/GraphvizView';
import { SimulatorEditor } from './SimulatorEditor';
import { EditorHolder, ModelEditor } from './views/ModelEditor';
import {
  StructureNode,
  StructureWithTabs,
  StructureParent,
  ProjectWindowManager,
} from './StructureNode';
import { produce } from 'immer';
import { StartButtonMode } from 'renderer/allotment-components/actions-bar';
import { ErrorConsole } from './views/ErrorConsole';
import { ExecutionConsole } from './views/ExecutionConsole';
import { OcelConsole, OcelObj } from './views/OcelConsole';

export type ProjectState = {
  canStartSimulation: boolean;
  isSimulating: boolean;
  startButtonMode: StartButtonMode;
  windowStructure: StructureNode<ProjectWindow>;
};


export interface ProjectWindowProvider {
  getProjectWindow(projectWindowId: ProjectWindowId): ProjectWindow | undefined;
}

export class Project implements ProjectWindowProvider {

  updateSimulationStatus(simulationStatus: SimulationClientStatus) {
    if (!simulationStatus) return;
    this.simulationClientStatus$.next(simulationStatus)
  }
  writeErrors(errors: string[] | undefined) {
    this.errors$.next(errors)
  }

  writeHtmlConsole(htmlLines: string[] | undefined) {
    if (htmlLines) {
      this.executionConsole.writeLines(htmlLines)
    } else {
      this.executionConsole.clean();
    }
  }

  writeOcelConsole(ocel: any) {
    this.ocelConsole.clean();
    this.ocelConsole.ocel = ocel;
  }

  readonly errors$ = new BehaviorSubject<string[] | undefined>(undefined);
  readonly simulationClientStatus$ =
    new BehaviorSubject<SimulationClientStatus>({
      canLaunchNewSimulation: false,
      ongoingSimulation: false,
    });



  private graphvizLoading = new Subject<boolean>();
  private graphvizDot = new Subject<string>();
  private internalOcDotEditorSubject$ = new Subject<string>();
  private internalSimConfigEditorInput$ = new Subject<string>();

  readonly modelEditor;
  readonly simulationConfigEditor;
  readonly graphvizView;
  readonly errorConsole;
  readonly executionConsole;
  readonly ocelConsole;
  
  private initialState;
  readonly projectState$;
  private projectWindowManager;

  private updateOcDot: (ocdot: string | null) => void                     
  private updateSimConfig: (simConfi: string | null) => void 
  private launchSimulation: () => void
  private stopSimulation : () => void;

  constructor(
    updateOcDot: (ocdot: string | null) => void,
    updateSimConfig: (simConfi: string | null) => void,
    launchSimulation: () => void ,
    stopSimulation : () => void
  ) {
    this.updateOcDot = updateOcDot;
    this.updateSimConfig = updateSimConfig;
    this.launchSimulation = launchSimulation;
    this.initialState = this.createInitialState();
    this.projectState$ = new BehaviorSubject<ProjectState>(this.initialState);
    this.stopSimulation = stopSimulation
    this.modelEditor = new ModelEditor(ModelEditor.id);
    this.simulationConfigEditor = new SimulatorEditor();
    this.graphvizView = new GraphvizView(
      this.graphvizDot,
      this.graphvizLoading
    );
    this.errorConsole = new ErrorConsole();
    this.executionConsole = new ExecutionConsole();
    this.ocelConsole = new OcelConsole((ocelObj: OcelObj) => {
      console.log('requesting ocel save')

      window.electron.ipcRenderer.sendMessage('transform-ocel', [
        ocelObj
      ]);
    })
    this.projectWindowManager = this.createProjectWindowManager();
    this.startProcessingProjectState();
    this.startProcessingOcDotInput();
    this.startProcessingSimConfigInput();
    this.startObservingSimulationStatus();
    this.startObservingErrors();
  }

  getLastFocusedEditor() : EditorHolder | undefined {
    return this.projectWindowManager.lastFocusedEditor
  }

  private startProcessingProjectState() {
    this.projectWindowManager.projectWindowStructure$.subscribe(
      (newStructure) => {
        let currentProjectState = this.projectState$.getValue();
        let newProjectState = produce(currentProjectState, (draft) => {
          draft.windowStructure = newStructure;
        });

        console.log(
          'Project: projectWindowStructure$.subscribe : emitting new structure ' +
            JSON.stringify(newStructure)
        );
        console.log(
          'project: projectWindowStructure$: state shallow equal to previous: ' +
            (newProjectState === currentProjectState)
        );
        this.projectState$.next(newProjectState);
      }
    );
  }

  private startObservingErrors() {
    this.errors$
      .pipe(rxops.debounceTime(200))
      .subscribe((errors: string[] | undefined) => {
        console.log("Project: received new errors " + errors)
        if (!errors) {
          this.errorConsole.clean()
          return
        }
        this.errorConsole.writeLines(errors)
      });
  }

  private startProcessingOcDotInput() {
    this.internalOcDotEditorSubject$
      .pipe(
        rxops.tap((value) => this.onNewOcDotContents(value)),
        rxops.debounceTime(500),
        rxops.map((rawOcDot) => {
          console.log('accepting ocdot value ' + rawOcDot);
          return this.convertRawOcDotToDot(rawOcDot);
        })
      )
      .subscribe((newDot) => {
        console.log('new dot: ' + newDot);
        if (newDot) {
          this.graphvizDot.next(newDot);
        }
        this.hideLoading();
      });

    this.modelEditor.getEditorCurrentInput$().subscribe((input) => {
      this.internalOcDotEditorSubject$.next(input);
    });
  }

  private startProcessingSimConfigInput() {
    this.internalSimConfigEditorInput$
      .pipe(
        rxops.debounceTime(500)
      )
      .subscribe((newConfig) => {
        console.log('accepting sim config value ' + newConfig);

        this.updateSimConfig(newConfig)
      });

    this.simulationConfigEditor.getEditorCurrentInput$().subscribe((input) => {
      this.internalSimConfigEditorInput$.next(input);
    });
  }

  private startObservingSimulationStatus() {
    this.simulationClientStatus$
      .pipe(rxops.debounceTime(500), rxops.distinctUntilChanged())
      .subscribe((newValue) => {
        let currentProjectState = this.projectState$.getValue();
        let newProjectState = produce(currentProjectState, (draft) => {
          draft.canStartSimulation = newValue.canLaunchNewSimulation;
          draft.isSimulating = newValue.ongoingSimulation;
          
          let valueMode: StartButtonMode = 'disabled';

          if (!newValue.canLaunchNewSimulation && !newValue.ongoingSimulation) {
            valueMode = 'disabled';
          } else if (
            !newValue.canLaunchNewSimulation &&
            newValue.ongoingSimulation
          ) {
            valueMode = 'executing';
          } else if (newValue.canLaunchNewSimulation) {
            valueMode = 'start';
          }

          draft.startButtonMode = valueMode;
        });
        console.log(
          'Project: startObservingSimulationReadiness : emitting new structure ' +
            JSON.stringify(newProjectState)
        );
        this.projectState$.next(newProjectState);
      });
  }

  private createProjectWindowManager() {
    return new ProjectWindowManager(this.initialState.windowStructure, {
      [ModelEditor.id]: this.modelEditor,
      [SimulatorEditor.id]: this.simulationConfigEditor,
      [GraphvizView.id]: this.graphvizView,
      [ErrorConsole.id]: this.errorConsole,
      [ExecutionConsole.id]: this.executionConsole,
      [OcelConsole.id] : this.ocelConsole
    });
  }

  onClickStart() {
    let currentProjectStatus = this.projectState$.getValue();
    let buttonMode = currentProjectStatus.startButtonMode;
    console.log('start button clicked');
    if (buttonMode == 'start') {
      console.log("starting new simulation, g'luck to us all");
      this.executionConsole.clean();
      this.launchSimulation();
    }
  }

  onClickStop() {
    let currentProjectStatus = this.projectState$.getValue();
    let isSimulating = currentProjectStatus.isSimulating;
    console.log('stop button clicked');
    if (isSimulating) {
      this.stopSimulation();
    }
  }

  getProjectWindow(
    projectWindowId: ProjectWindowId
  ): ProjectWindow | undefined {
    return this.projectWindowManager.windowsMap[projectWindowId];
  }

  clickTab(projectWindowId: ProjectWindowId) {
    this.projectWindowManager.clickTabOfProjectWindow(projectWindowId);
  }

  private onNewOcDotContents(onNewOcDotContents: OcDotContent | null) {
    this.showLoading();
    this.updateOcDot(onNewOcDotContents)
  }

  private convertRawOcDotToDot(rawOcDot: string): string | null {
    let result = null;
    try {
      const ast = AST.parse(rawOcDot, { rule: AST.Types.OcDot });
      const converter = new OCDotToDOTConverter(ast);

      result = converter.compileDot();
    } catch (e: PeggySyntaxError | any) {
      console.log(e);
    }
    return result;
  }

  private createSimpleStructure(): ProjectWindowStructure {
    return {
      id: 'root',
      direction: 'column',
      children: [
        {
          id: 'workspace',
          direction: 'row',
          children: [
            {
              id: 'editors',
              tabs: [SimulatorEditor.id, ModelEditor.id],
              currentTabIndex: 0,
            } as StructureWithTabs<ProjectWindowId>,
            {
              id: 'view',
              tabs: [GraphvizView.id],
              currentTabIndex: 0,
            } as StructureWithTabs<ProjectWindowId>,
          ],
        } as StructureParent<ProjectWindowId>,
        {
          id: 'terminal',
          tabs: [ErrorConsole.id, ExecutionConsole.id, OcelConsole.id],
          currentTabIndex: 0,
        } as StructureWithTabs<ProjectWindowId>,
      ],
    } as StructureParent<ProjectWindowId>;
  }

  showLoading() {
    this.graphvizLoading.next(true);
  }

  hideLoading() {
    this.graphvizLoading.next(false);
  }

  setModelOcDotContentsFromFile(modelOcDotContents: string, filePath : string) {
    this.modelEditor.updateEditorWithContents(modelOcDotContents, filePath);
    this.internalOcDotEditorSubject$.next(modelOcDotContents)
  }
  
  setSimConfigContentsFromFile(simConfigContents: string, filePath: string) {
    this.simulationConfigEditor.updateEditorWithContents(simConfigContents, filePath);
    this.internalSimConfigEditorInput$.next(simConfigContents)
  }

  private createInitialState(): ProjectState {
    return {
      canStartSimulation: false,
      isSimulating: false,
      startButtonMode: 'disabled',
      windowStructure: this.createSimpleStructure(),
      // windowStructure: this.createInitialStructure()
    };
  }

  createSimulationArgument(): SimulationArgument {
    let simConfig = this.simulationConfigEditor.collectSimulationConfig();
    let modelConfig = this.modelEditor.collectOcDot();

    return {
      ocDot: modelConfig,
      simulationConfig: simConfig,
    };
  }


  getGraphvizObservable(): Observable<string> {
    return this.graphvizDot;
  }

  getGraphvizLoading(): Observable<boolean> {
    return this.graphvizLoading;
  }
}

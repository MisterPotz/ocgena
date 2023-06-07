import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { OCDotToDOTConverter } from 'ocdot/converter';
import { PeggySyntaxError } from 'ocdot-parser/lib/ocdot.peggy';
import { AST } from 'ocdot-parser';
import * as rxops from 'rxjs/operators';
import {
  OcDotContent,
  ProjectWindowStructure,
  ProjectWindow,
  SimulationArgument,
  ProjectWindowId,
  SimulationConfig,
} from './domain';
import { GraphvizView } from './views/GraphvizView';
import { SimulatorEditor } from './SimulatorEditor';
import { ClickHandler, ModelEditor } from './views/ModelEditor';
import {
  ProjectSingleSimulationExecutor,
  SimulationClientStatus,
} from './ProjectSingleSimulationExecutor';
import {
  StructureNode,
  StructureWithTabs,
  StructureParent,
  ProjectWindowManager,
  WindowsMap,
} from './StructureNode';
import { produce } from 'immer';
import * as yaml from 'js-yaml';
import Ajv from 'ajv';
import {
  TimeRangeClass,
  createAjv,
  simconfigSchema,
  simconfigSchemaId,
} from 'simconfig/simconfig_yaml';
import { SimConfigCreator } from '../simconfig/SimConfigCreator';
import { StartButtonMode } from 'renderer/allotment-components/actions-bar';
import { ErrorConsole } from './views/ErrorConsole';
import { error, model, simulation } from 'ocgena';

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
  private graphvizLoading = new Subject<boolean>();
  private graphvizDot = new Subject<string>();
  private internalOcDotEditorSubject$ = new Subject<string>();
  private internalSimConfigEditorInput$ = new Subject<string>();

  readonly modelEditor;
  readonly simulationConfigEditor;
  readonly graphvizView;
  readonly errorConsole;

  private projectSimulationExecutor = new ProjectSingleSimulationExecutor();
  private initialState;
  readonly projectState$;
  private projectWindowManager;
  private ajv = createAjv();

  private simConfigCreator = new SimConfigCreator();

  constructor() {
    this.initialState = this.createInitialState();
    this.projectState$ = new BehaviorSubject<ProjectState>(this.initialState);

    this.modelEditor = new ModelEditor(ModelEditor.id);
    this.simulationConfigEditor = new SimulatorEditor();
    this.graphvizView = new GraphvizView(
      this.graphvizDot,
      this.graphvizLoading
    );
    this.errorConsole = new ErrorConsole();

    this.projectWindowManager = this.createProjectWindowManager();
    this.startProcessingProjectState();
    this.startProcessingOcDotInput();
    this.startProcessingSimConfigInput();
    this.startObservingSimulationStatus();
    this.startObservingErrors();
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
    this.projectSimulationExecutor.errors$
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
        rxops.debounceTime(500),
        rxops.map((rawSimConfig) => {
          console.log('accepting sim config value ' + rawSimConfig);
          return this.convertRawSimConfigToSimConfig(rawSimConfig);
        })
      )
      .subscribe((newConfig) => {
        this.projectSimulationExecutor.updateSimulationConfig(newConfig);
        console.log(
          'new successfully mapped config ' + JSON.stringify(newConfig)
        );
      });

    this.simulationConfigEditor.getEditorCurrentInput$().subscribe((input) => {
      this.internalSimConfigEditorInput$.next(input);
    });
  }

  private startObservingSimulationStatus() {
    this.getCanStartSimulationObservable()
      .pipe(rxops.debounceTime(500), rxops.distinctUntilChanged())
      .subscribe((newValue) => {
        let currentProjectState = this.projectState$.getValue();
        let newProjectState = produce(currentProjectState, (draft) => {
          draft.canStartSimulation = newValue.canLaunchNewSimulation;
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
    });
  }

  onClickStart() {
    let currentProjectStatus = this.projectState$.getValue();
    let buttonMode = currentProjectStatus.startButtonMode;
    console.log('start button clicked');
    if (buttonMode == 'start') {
      console.log("starting new simulation, g'luck to us all");
      this.projectSimulationExecutor.tryStartSimulation();
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
    this.projectSimulationExecutor.updateModel(onNewOcDotContents);
  }

  private createClickHandler() {
    let project = this;

    return {
      clickTab(projectWindowId) {
        if (project.projectWindowManager) {
          project.projectWindowManager.clickTabOfProjectWindow(projectWindowId);
        }
      },
    } as ClickHandler;
  }

  private convertRawSimConfigToSimConfig(
    rawSimConfig: string
  ): SimulationConfig | null {
    let yamlObj: any;

    try {
      yamlObj = yaml.load(rawSimConfig);
    } catch (e) {
      console.log('oops, yaml error ' + e);
      return null;
    }

    console.log(
      'Project: convertRawSimConfigToSimConfig: yamlObj: ' +
        JSON.stringify(yamlObj)
    );

    let isValid = this.ajv.validate(simconfigSchemaId, yamlObj);

    if (!isValid) {
      console.log(
        'Project: convertRawSimConfigToSimConfig: have errors: ' +
          JSON.stringify(this.ajv.errors)
      );
      return null;
    }

    return this.simConfigCreator.createFromObj(yamlObj);
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
          tabs: [ErrorConsole.id],
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

  setModelOcDotContents(modelOcDotContents: string) {
    this.modelEditor.updateEditorWithContents(modelOcDotContents);
    this.internalOcDotEditorSubject$.next(modelOcDotContents)
  }
  
  setSimConfigContents(simConfigContents: string) {
    this.simulationConfigEditor.updateEditorWithContents(simConfigContents);
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

  updateModel(ocDotContent: OcDotContent | null) {
    if (ocDotContent != null) {
      this.modelEditor.updateEditorWithContents(ocDotContent);
    }
  }

  startSimulation() {
    this.projectSimulationExecutor.tryStartSimulation();
  }

  createSimulationArgument(): SimulationArgument {
    let simConfig = this.simulationConfigEditor.collectSimulationConfig();
    let modelConfig = this.modelEditor.collectOcDot();

    return {
      ocDot: modelConfig,
      simulationConfig: simConfig,
    };
  }

  getCanStartSimulationObservable(): Observable<SimulationClientStatus> {
    return this.projectSimulationExecutor.simulationClientStatus$;
  }

  getGraphvizObservable(): Observable<string> {
    return this.graphvizDot;
  }

  getGraphvizLoading(): Observable<boolean> {
    return this.graphvizLoading;
  }
}

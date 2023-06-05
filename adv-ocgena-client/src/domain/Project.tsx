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
} from './domain';
import { GraphvizView } from './GraphvizView';
import { SimulatorEditor } from './SimulatorEditor';
import { ClickHandler, ModelEditor } from './ModelEditor';
import { ProjectSingleSimulationExecutor } from './ProjectSingleSimulationExecutor';
import {
  StructureNode,
  StructureWithTabs,
  StructureParent,
  ProjectWindowManager,
  WindowsMap,
} from './StructureNode';
import { produce } from 'immer';

export type ProjectState = {
  canStartSimulation: boolean;
  isSimulating: boolean;
  windowStructure: StructureNode<ProjectWindow>;
};

export interface ProjectWindowProvider { 
  getProjectWindow(projectWindowId : ProjectWindowId) : ProjectWindow | undefined
}

export class Project implements ProjectWindowProvider {
  private graphvizLoading = new Subject<boolean>();
  private graphvizDot = new Subject<string>();
  private internalEditorSubject = new Subject<string>();
  private ocDotFileSourceObservable = new Subject<OcDotContent>();

  readonly modelEditor;
  readonly simulationConfigEditor;
  readonly graphvizView;

  private projectSimulationExecutor = new ProjectSingleSimulationExecutor();
  private initialState;
  readonly projectState$;
  private projectWindowManager;
  private clickHandler = this.createClickHandler();

  constructor() {
    this.initialState = this.createInitialState();
    this.projectState$ = new BehaviorSubject<ProjectState>(this.initialState);
    
    this.modelEditor = new ModelEditor(ModelEditor.id);
    this.simulationConfigEditor = new SimulatorEditor();
    this.graphvizView = new GraphvizView(
      this.graphvizDot,
      this.graphvizLoading,
    );

    this.projectWindowManager = new ProjectWindowManager(
      this.initialState.windowStructure,
      {
        [ModelEditor.id] : this.modelEditor,
        [SimulatorEditor.id] : this.simulationConfigEditor,
        [GraphvizView.id] : this.graphvizView,
      }
    );
    
    this.projectWindowManager.projectWindowStructure$.subscribe(
      (newStructure) => {
        let currentProjectState = this.projectState$.getValue();
        let newProjectState = produce(currentProjectState, (draft) => {
          draft.windowStructure = newStructure
        })
        console.log(
          'Project: projectWindowStructure$.subscribe : emitting new structure ' + JSON.stringify(newStructure)
        );
        console.log("project: projectWindowStructure$: state shallow equal to previous: " + (newProjectState === currentProjectState))
        this.projectState$.next(newProjectState);
      }
    );

    this.internalEditorSubject
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
  }

  getProjectWindow(projectWindowId: ProjectWindowId): ProjectWindow | undefined {
      return this.projectWindowManager.windowsMap[projectWindowId]
  }

  clickTab(projectWindowId : ProjectWindowId) {
    this.projectWindowManager.clickTabOfProjectWindow(projectWindowId)
  }

  private onNewOcDotContents(onNewOcDotContents: OcDotContent | null) {
    this.showLoading();
  }

  private createClickHandler() { 
    let project = this

    return {
      clickTab(projectWindowId) {
          if (project.projectWindowManager) {
            project.projectWindowManager.clickTabOfProjectWindow(projectWindowId)
          }
      },
    } as ClickHandler
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

  private createInitialStructure(): ProjectWindowStructure {
    return {
      id: 'root',
      direction: 'row',
      children: [
        {
          id: 'editors',
          tabs: [this.modelEditor, this.simulationConfigEditor],
          currentTabIndex: 0,
        },
        {
          id: 'view',
          tabs: [this.graphvizView],
          currentTabIndex: 0,
        } as StructureWithTabs<ProjectWindow>,
      ],
    } as StructureParent<ProjectWindow>;
  }

  private createSimpleStructure(): ProjectWindowStructure {
    return {
      id: 'root',
      direction: 'row',
      children: [
        {
          id: 'editors',
          tabs: [SimulatorEditor.id, ModelEditor.id],
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

  onNewOcDotEditorValue(newValue: string) {
    this.internalEditorSubject.next(newValue);
  }

  getOcDotFileSourceObservable(): Observable<string> {
    return this.ocDotFileSourceObservable;
  }

  onFileOpened(fileOcDotContents: string) {
    this.ocDotFileSourceObservable.next(fileOcDotContents);
  }

  private createInitialState(): ProjectState {
    return {
      canStartSimulation: false,
      isSimulating: false,
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

  getCanStartSimulationObservable(): Observable<boolean> {
    return this.projectSimulationExecutor.isLaunchAllowed$;
  }

  getGraphvizObservable(): Observable<string> {
    return this.graphvizDot;
  }

  getGraphvizLoading(): Observable<boolean> {
    return this.graphvizLoading;
  }
}

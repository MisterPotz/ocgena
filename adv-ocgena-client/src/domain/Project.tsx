import { Observable, Subject } from "rxjs";
import { OCDotToDOTConverter } from "ocdot/converter";
import { PeggySyntaxError } from "ocdot-parser/lib/ocdot.peggy";
import { AST } from "ocdot-parser";
import * as rxops from 'rxjs/operators';
import { OcDotContent, ProjectWindowStructure, ProjectWindow, SimulationArgument } from "./domain";
import { GraphvizView } from "./GraphvizView";
import { SimulatorEditor } from "./SimulatorEditor";
import { ModelEditor } from "./ModelEditor";
import { ProjectSingleSimulationExecutor } from "./ProjectSingleSimulationExecutor";
import { StructureNode, StructureWithTabs } from "./StructureNode";

export type ProjectState = {
    canStartSimulation: boolean,
    isSimulating: boolean,
    windowStructure: StructureNode<ProjectWindow>
}

export class Project {
    private graphvizLoading = new Subject<boolean>();
    private graphvizDot = new Subject<string>();
    private internalEditorSubject = new Subject<string>();
    private ocDotFileSourceObservable = new Subject<OcDotContent>();

    readonly modelEditor = new ModelEditor();
    readonly simulationConfigEditor = new SimulatorEditor();
    readonly graphvizView = new GraphvizView(
        this.graphvizDot,
        this.graphvizLoading
    );
    private projectSimulationExecutor = new ProjectSingleSimulationExecutor();
    readonly projectState$ = new Subject<ProjectState>();

    constructor() {
        this.projectState$.next(this.createInitialState());

        this.internalEditorSubject.pipe(
            rxops.tap(value => this.onNewOcDotContents(value)),
            rxops.debounceTime(500),
            rxops.map(((rawOcDot) => {
                console.log("accepting ocdot value " + rawOcDot);
                return this.convertRawOcDotToDot(rawOcDot);
            })))
            .subscribe((newDot) => {
                console.log("new dot: " + newDot);
                if (newDot) {
                    this.graphvizDot.next(newDot);
                }
                this.hideLoading();
            });
    }

    private onNewOcDotContents(onNewOcDotContents: OcDotContent | null) {
        this.showLoading();
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
            direction: "row",
            children: [
                {
                    tabs: [
                        this.modelEditor,
                        this.simulationConfigEditor,
                    ],
                    currentTabIndex: 0
                },
                ({
                    tabs: [
                        this.graphvizView
                    ],
                    currentTabIndex: 0
                } as StructureWithTabs<ProjectWindow>)
            ]
        };
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

    createInitialState(): ProjectState {
        return {
            canStartSimulation: false,
            isSimulating: false,
            windowStructure: this.createInitialStructure()
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
            simulationConfig: simConfig
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

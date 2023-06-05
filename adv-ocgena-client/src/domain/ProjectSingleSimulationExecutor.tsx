import { simulation } from "ocgena";
import { Subject, tap } from "rxjs";
import { OcDotContent, SimulationConfig } from "./domain";

export class ProjectSingleSimulationExecutor {
    private simulationClient = new simulation.client.Client({
        readyToCalc: (ready) => {
            this.simulationReadiness$.next(ready);
        }
    } as simulation.client.OnReadinessCallback);

    private simulationReadiness$ = new Subject<boolean>();
    private currentSimulationReadiness = false;

    readonly isLaunchAllowed$ = new Subject<boolean>();

    private currentSimulationTask: simulation.client.ClientSimTask | null = null;

    constructor() {
        this.simulationReadiness$.pipe(
            tap((value) => {
                this.currentSimulationReadiness = value;
                this.isLaunchAllowed$.next(this.internalIsLaunchAllowed(
                    value,
                    this.currentSimulationTask != null
                ));
            })
        ).subscribe();
    }

    updateModel(ocDotContent: OcDotContent | null) {
        if (ocDotContent == null)
            return;
        this.simulationClient.updateOcDot(ocDotContent);
    }

    updateSimulationConfig(simulationConfig: SimulationConfig | null) {
        if (simulationConfig == null)
            return;
        this.simulationClient.updateConfig(simulationConfig);
    }

    tryStartSimulation() {
        this.requireLaunchAllowed();

        let simTaskFactory = this.simulationClient.createClientSimTaskFactory();


        if (simTaskFactory != null) {
            let newSimulationTask = simTaskFactory.create(
                this.createSimTaskClientCallback(),
                this.createHtmlTraceWriter(),
                this.createOcelWriter()
            );
            this.currentSimulationTask = newSimulationTask;
            newSimulationTask.launch();
        }
    }

    private createOcelWriter(): simulation.client.OcelWriter {
        return new simulation.client.OcelWriter();
    }

    private createHtmlTraceWriter(): simulation.client.Writer {
        return {
            end: () => {
                console.log("writer: end");
            },
            writeLine: (line) => {
                console.log("writer: " + line);
            }
        } as simulation.client.Writer;
    }

    private createSimTaskClientCallback(): simulation.client.SimTaskClientCallback {
        let onFinish = () => {
            this.currentSimulationTask = null;
        };
        return {
            onExecutionFinish: () => {
                console.log("execution finished");
                onFinish();
            },
            onExecutionStart: () => {
                console.log("execution started");
            },
            onExecutionTimeout: () => {
                console.log("execution timeout");
                onFinish();
            }
        } as simulation.client.SimTaskClientCallback;
    }

    private requireLaunchAllowed(): boolean {
        if (!this.isLaunchAllowed()) {
            throw new Error("launch is not allowed");
        }
        return true;
    }

    private internalIsLaunchAllowed(simulationReadiness: boolean, ongoingCalculation: boolean): boolean {
        return simulationReadiness && !ongoingCalculation;
    }

    isLaunchAllowed(): boolean {
        return this.internalIsLaunchAllowed(this.currentSimulationReadiness, this.currentSimulationTask != null);
    }
}

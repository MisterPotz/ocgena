import { simulation } from 'ocgena';
import { BehaviorSubject, Subject, tap } from 'rxjs';
import { OcDotContent, SimulationConfig } from './domain';
import { produce } from 'immer';

export type SimulationClientStatus = {
  canLaunchNewSimulation: boolean;
  ongoingSimulation: boolean;
};

export class ProjectSingleSimulationExecutor {
  private simulationClient = new simulation.client.Client(
    simulation.client.toOnReadinessCallback({
      readyToCalc: (ready) => {
        this.simulationReadiness$.next(ready);
      },
    } as simulation.client.JsOnReadinessCallback)
  );

  private simulationReadiness$ = new Subject<boolean>();
  private currentSimulationReadiness = false;

  readonly simulationClientStatus$ =
    new BehaviorSubject<SimulationClientStatus>({
      canLaunchNewSimulation: false,
      ongoingSimulation: false,
    });

  private currentSimulationTask: simulation.client.ClientSimTask | null = null;

  constructor() {
    this.startObservingSimulationReadiness();
  }

  private startObservingSimulationReadiness() {
    this.simulationReadiness$
      .pipe(
        tap((value) => {
          this.currentSimulationReadiness = value;

          let currentStatus = this.simulationClientStatus$.getValue();
          let newStatus = produce(currentStatus, (draft) => {
            draft.canLaunchNewSimulation = this.isLaunchAllowed();
          });

          this.simulationClientStatus$.next(newStatus);
        })
      )
      .subscribe();
  }

  updateModel(ocDotContent: OcDotContent | null) {
    if (ocDotContent == null) return;
    this.simulationClient.updateOcDot(ocDotContent);
  }

  private onSimulationStart() {
    let currentStatus = this.simulationClientStatus$.getValue();

    let newStatus = produce(currentStatus, (draft) => {
      draft.canLaunchNewSimulation = this.isLaunchAllowed();
      draft.ongoingSimulation = true;
    });

    this.simulationClientStatus$.next(newStatus);
  }

  private onSimulationFinished() {
    let currentStatus = this.simulationClientStatus$.getValue();

    let newStatus = produce(currentStatus, (draft) => {
      draft.canLaunchNewSimulation = this.isLaunchAllowed();
      draft.ongoingSimulation = false;
    });

    this.simulationClientStatus$.next(newStatus);
  }

  updateSimulationConfig(simulationConfig: SimulationConfig | null) {
    if (simulationConfig == null) return;
    this.simulationClient.updateConfig(simulationConfig);
  }

  tryStartSimulation() {
    if (!this.isLaunchAllowed()) return;

    let simTaskFactory = this.simulationClient.createClientSimTaskFactory();

    if (simTaskFactory != null) {
      let newSimulationTask = simTaskFactory.create(
        this.createSimStatusListener(),
        this.createSimTaskClientCallback(),
        this.createHtmlTraceWriter(),
        this.createAnsiTraceWriter(),
        this.createOcelWriter()
      );
      this.startSimulation(newSimulationTask);
    }
  }

  private startSimulation(simulationTask : simulation.client.ClientSimTask) {
    this.currentSimulationTask = simulationTask;
    this.onSimulationStart();
    this.currentSimulationTask?.launch();
  }

  private finishSimulation() {
    this.currentSimulationTask = null;
    this.onSimulationFinished();
  }

  private createSimStatusListener() {
    return (simStatus: simulation.client.ClientSimTaskStatus) => {
      if (simStatus == simulation.client.ClientSimTaskStatus.FINISHED) {
        this.finishSimulation();
      }
    };
  }

  private createOcelWriter(): simulation.client.OcelWriter {
    return new simulation.client.OcelWriter();
  }

  private createHtmlTraceWriter(): simulation.client.Writer {
    return new simulation.client.HtmlDebugTraceBuilderWriter();
  }

  private createAnsiTraceWriter(): simulation.client.Writer {
    return new simulation.client.CallbackStringWriter((line) => {
      console.log(line);
    });
  }

  private createSimTaskClientCallback(): simulation.client.SimTaskClientCallback {
    // let onFinish = () => {
    //   this.currentSimulationTask = null;
    // };
    return simulation.client.toSimTaskClientCallback({
      onExecutionFinish: () => {
        console.log('execution finished');
      },
      onExecutionStart: () => {
        console.log('execution started');
      },
      onExecutionTimeout: () => {
        console.log('execution timeout');
      },
    } as simulation.client.JsSimTaskClientCallback);
  }

  private requireLaunchAllowed(): boolean {
    if (!this.isLaunchAllowed()) {
      throw new Error('launch is not allowed');
    }
    return true;
  }

  private internalIsLaunchAllowed(
    simulationReadiness: boolean,
    ongoingCalculation: boolean
  ): boolean {
    return simulationReadiness && !ongoingCalculation;
  }

  isLaunchAllowed(): boolean {
    return this.internalIsLaunchAllowed(
      this.currentSimulationReadiness,
      this.currentSimulationTask != null
    );
  }
}

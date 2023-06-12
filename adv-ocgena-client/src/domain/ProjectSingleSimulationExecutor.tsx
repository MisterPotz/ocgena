import { error, model, simulation } from 'ocgena';
import { BehaviorSubject, Subject, tap } from 'rxjs';
import { OcDotContent, SimulationConfig } from './domain';
import { produce } from 'immer';
import { red, reset, yellow } from 'renderer/allotment-components/panel';
import * as rxjs from 'rxjs'
export type SimulationClientStatus = {
  canLaunchNewSimulation: boolean;
  ongoingSimulation: boolean;
};

export class ProjectSingleSimulationExecutor {
  private simulationClient = new simulation.client.Client(
    simulation.client.toOnReadinessCallback({
      readyToCalc: (ready: boolean) => {
        this.simulationReadiness$.next(ready);
      },
      ocDotParseResult: (ocDotParseResult: model.OcDotParseResult) => {
        console.log(ocDotParseResult)
      },
      onCurrentErrorsChange: (errors) => {
          console.log("have currently these errors: " + errors?.map((eror: { message?: any; }) =>  `${Object.keys(eror)} ${eror.message}`)?.join('\n'));
          if (errors) {
            this.errors$.next(this.buildErrorMessageChunk(errors))
          } else {
            this.errors$.next(undefined)
          }
      },
    } )
    );

  private buildErrorMessageChunk(errors : error.Error[]) {
    return errors.map(
        (error) =>  { 
          let errorColor = (error.errorLevel.name == "CRITICAL") ? red : yellow
          let errorTag = `[${error.errorLevel.name}]`.padEnd(10)
          return `${errorColor}${errorTag}${reset} : ${error.message}`
        }
    )
  }

  private simulationReadiness$ = new Subject<boolean>();
  private currentSimulationReadiness = false;
  readonly errors$ = new BehaviorSubject<string[] | undefined>(undefined);

  readonly simulationClientStatus$ =
    new BehaviorSubject<SimulationClientStatus>({
      canLaunchNewSimulation: false,
      ongoingSimulation: false,
    });

  private currentSimulationTask: simulation.client.ClientSimTask | null = null;
  
  private executionTraceLineWriter: (line: string) => void;
  private simTaskClientCallback: simulation.client.JsSimTaskClientCallback
  private resultingOcel : (any: any) => void;
  private currentCalculation: rxjs.Subscription | undefined

  constructor(executionTraceLineWriter : (line: string)=>void, 
            simTaskClientCallback: simulation.client.JsSimTaskClientCallback,
            resultingOcel: (any: any) => void) {
    this.startObservingSimulationReadiness();
    this.startClient();
    this.executionTraceLineWriter = executionTraceLineWriter;
    this.simTaskClientCallback = simTaskClientCallback;
    this.resultingOcel = resultingOcel;
    this.simulationClient.loggingEnabled = true;
  } 
  
  private startClient() {
    this.simulationClient.start();
  }

  private startObservingSimulationReadiness() {
    this.simulationReadiness$
      .pipe(
        tap((value) => {
          this.currentSimulationReadiness = value;
          console.log("ProjectSingleSimulationExecutor: new simulation readiness state")
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
    console.log("ProjectSingleSimulationExecutor: setting new ocdot contents")
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
    console.log("ProjectSingleSimulationExecutor: setting new config")
    this.simulationClient.updateConfig(simulationConfig);
  }

  tryStartSimulation() {
    if (!this.isLaunchAllowed() || (this.currentCalculation && !this.currentCalculation.closed)) return;

    let simTaskFactory = this.simulationClient.createClientSimTaskFactory();

    let observable = new rxjs.Observable((observer) => {
      if (simTaskFactory != null) {
        let newSimulationTask = simTaskFactory.create(
          this.createSimStatusListener(),
          this.createSimTaskClientCallback(),
          this.createHtmlTraceWriter(),
          this.createAnsiTraceWriter(),
          this.createOcelWriter()
        );
        this.startSimulation(newSimulationTask);
        observer.complete();
      }
    }).pipe(
      rxjs.observeOn(rxjs.asyncScheduler)
    );
    this.currentCalculation = observable.subscribe()
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
    return new simulation.client.OcelWriter((ocel) => {

      this.resultingOcel(ocel)
    });
  }

  private createHtmlTraceWriter(): simulation.client.Writer {
    return new simulation.client.HtmlDebugTraceCallbackBuilderWriter(
      (line) => {
        console.log("received line %s", line)
        this.executionTraceLineWriter(line);
        // console.log(line);
      }
    );
  }

  private createAnsiTraceWriter(): simulation.client.Writer {
    return new simulation.client.CallbackStringWriter((line) => {
      // this.executionTraceLineWriter(line);
      console.log(line);
    });
  }

  private createSimTaskClientCallback(): simulation.client.SimTaskClientCallback {
    // let onFinish = () => {
    //   this.currentSimulationTask = null;
    // };
    return simulation.client.toSimTaskClientCallback(this.simTaskClientCallback);
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

import { error, model, simulation } from 'ocgena';
import { parentPort } from 'worker_threads';
import { produce } from 'immer';
import { Subject, BehaviorSubject, tap } from 'rxjs';
import { OcDotContent, SimulationConfig } from './domain';
import * as rxjs from 'rxjs';
const processSend = parentPort?.postMessage.bind(parentPort) || function () {};
let subscribedToMain = false;
import {
  ErrorsMessage,
  SimConfigUpdateMessage,
  OcDotUpdateMessage,
  HtmlMessage,
  IPCMesage,
  LaunchMessage,
  OcelMessage,
  SimulationClientStatus,
  SimulationStatusMessage,
  isIpcMessage,
} from '../main/shared';
import { createAjv } from '../simconfig/createAjv';
import { SimConfigCreator } from '../simconfig/SimConfigCreator';
import * as yaml from 'js-yaml';
import { simconfigSchemaId } from '../simconfig/simconfigSchema';
import { link } from 'fs';
import { netLog } from 'electron';
import { red, yellow, reset } from './red';

export class SimConfigMapper {
  private ajv = createAjv();

  private simConfigCreator = new SimConfigCreator();

  convertRawSimConfigToSimConfig(
    rawSimConfig: string
  ): SimulationConfig | null {
    let yamlObj: any;

    try {
      yamlObj = yaml.load(rawSimConfig);
    } catch (e) {
      console.log('oops, yaml error ' + e);
      return null;
    }

    try {
      let isValid = this.ajv.validate(simconfigSchemaId, yamlObj);

      if (!isValid) {
        console.log(
          'Project: convertRawSimConfigToSimConfig: have errors: ' +
            JSON.stringify(this.ajv.errors)
        );
        return null;
      }

      let simConfig = this.simConfigCreator.createFromObj(yamlObj);
      console.log('created simconfig %s', simConfig.serialize());
      return simConfig
    } catch (e) {
      return null;
    }
  }
}

const simConfigMapper = new SimConfigMapper();

console.log('worker started');
parentPort!.on('message', (message) => {
  console.log('received message in ocgena_client ', message);

  if (isIpcMessage(message)) {
    switch (message.type) {
      case 'update-simconfig':
        executor.updateSimulationConfig(
          (message as SimConfigUpdateMessage)?.rawSimConfig || null
        );
        break;
      case 'update-ocdot':
        executor.updateModel((message as OcDotUpdateMessage)?.ocdot || null);
        break;
      case 'launch-sim':
        executor.tryStartSimulation();
        break;
      case 'stop-sim':
        executor.tryStopSimulation();
        break;
      default:
        break;
    }
  } else if (!subscribedToMain) {
    subscribedToMain = true;
    processSend({ message: 'worker is ready' });
    executor.simulationClientStatus$.subscribe(
      (SimulationClientStatus: SimulationClientStatus) => {
        processSend({
          type: 'execution-state',
          simulationStatus: SimulationClientStatus,
        } as SimulationStatusMessage);
      }
    );

    executor.errors$.subscribe((errors: any) => {
      processSend({ type: 'errors', errors: errors } as ErrorsMessage);
    });
  }
});

const convertRawSimConfigToSimConfig: (
  rawSimConfig: string
) => SimulationConfig | null = (rawSimConfig: string) => {
  return simConfigMapper.convertRawSimConfigToSimConfig(rawSimConfig);
};

export class ProjectSingleSimulationExecutor {
  private simulationClient = new simulation.client.Client(
    simulation.client.toOnReadinessCallback({
      readyToCalc: (ready: boolean) => {
        this.simulationReadiness$.next(ready);
      },
      ocDotParseResult: (ocDotParseResult: model.OcDotParseResult) => {
        console.log(ocDotParseResult);
      },
      onCurrentErrorsChange: (errors) => {
        console.log(
          'have currently these errors: ' +
            errors
              ?.map(
                (eror: { message?: any }) =>
                  `${Object.keys(eror)} ${eror.message}`
              )
              ?.join('\n')
        );
        if (errors) {
          this.errors$.next(this.buildErrorMessageChunk(errors));
        } else {
          this.errors$.next(undefined);
        }
      },
    })
  );

  private buildErrorMessageChunk(errors: error.Error[]) {
    return errors.map((error) => {
      let errorColor = error.errorLevel.name == 'CRITICAL' ? red : yellow;
      let errorTag = `[${error.errorLevel.name}]`.padEnd(10);
      return `${errorColor}${errorTag}${reset} : ${error.message}`;
    });
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
  private simTaskClientCallback: simulation.client.JsSimTaskClientCallback;
  private resultingOcel: (any: any) => void;
  private currentCalculation: rxjs.Subscription | undefined;
  private simulationStopRequested: boolean = false;

  constructor(
    executionTraceLineWriter: (line: string) => void,
    simTaskClientCallback: simulation.client.JsSimTaskClientCallback,
    resultingOcel: (any: any) => void
  ) {
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
          console.log(
            'ProjectSingleSimulationExecutor: new simulation readiness state'
          );
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
    console.log('ProjectSingleSimulationExecutor: setting new ocdot contents');
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

  updateSimulationConfig(simulationConfig: string | null) {
    if (simulationConfig == null) return;
    console.log('ProjectSingleSimulationExecutor: setting new config');
    let yamlSimConfig =
      simConfigMapper.convertRawSimConfigToSimConfig(simulationConfig);
    if (yamlSimConfig) {
      this.simulationClient.updateConfig(yamlSimConfig);
    }
  }

  tryStopSimulation() {
    this.simulationStopRequested = true;
  }

  // async to let event loop process possible external events
  async checkSimulationPermit() {
    return await new Promise((resolve) =>
      setTimeout(() => {
        if (this.simulationStopRequested) {
          this.currentSimulationTask?.finish();
        }
        resolve(!this.simulationStopRequested);
      }, 0)
    );
  }

  tryStartSimulation() {
    if (
      !this.isLaunchAllowed() ||
      (this.currentCalculation && !this.currentCalculation.closed)
    )
      return;

    let simTaskFactory = this.simulationClient.createClientSimTaskFactory();

    let observable = rxjs.from(
      new Promise<boolean>(async (resolve) => {
        if (simTaskFactory != null) {
          let finished = false;
          let newSimulationTask = simTaskFactory.create(
            this.createSimStatusListener(() => {
              finished = true;
            }),
            this.createSimTaskClientCallback(),
            this.createHtmlTraceWriter(),
            this.createAnsiTraceWriter(),
            this.createOcelWriter()
          );
          this.startSimulation(newSimulationTask);

          while (!finished && (await this.checkSimulationPermit())) {
            this.currentSimulationTask?.performStep();
          }

          // allow to collect last state
          this.currentSimulationTask?.performStep()

          resolve(true);
        } else {
          resolve(false);
        }
      })
    );

    this.currentCalculation = observable.subscribe(() => {
      this.finishSimulation();
    });
  }

  private startSimulation(simulationTask: simulation.client.ClientSimTask) {
    this.currentSimulationTask = simulationTask;
    this.onSimulationStart();
  }

  private finishSimulation() {
    this.currentSimulationTask = null;
    this.currentCalculation = undefined;
    this.simulationStopRequested = false;
    this.onSimulationFinished();
  }

  private createSimStatusListener(onFinish: () => void) {
    return (simStatus: simulation.client.ClientSimTaskStatus) => {
      if (simStatus == simulation.client.ClientSimTaskStatus.FINISHED) {
        onFinish();
      }
    };
  }

  private createOcelWriter(): simulation.client.OcelWriter {
    return new simulation.client.OcelWriter((ocel) => {
      this.resultingOcel(ocel);
    });
  }

  private createHtmlTraceWriter(): simulation.client.Writer {
    return new simulation.client.HtmlDebugTraceCallbackBuilderWriter(
      (line) => {
        // console.log("received line %s", line)
        // console.log(line);
      },
      (fullLog) => {
        console.log("received full log")
        this.executionTraceLineWriter(fullLog);
        // this.executionTraceLineWriter(fullLog)
      },
      1000 // limit
    );
  }

  // ansi trace writer
  // html trace writer
  // ocel writer
  // state

  private createAnsiTraceWriter(): simulation.client.Writer {
    return new simulation.client.CallbackStringWriter((line) => {
      // this.executionTraceLineWriter(line);
      // console.log(line);
    });
  }

  private createSimTaskClientCallback(): simulation.client.SimTaskClientCallback {
    // let onFinish = () => {
    //   this.currentSimulationTask = null;
    // };
    return simulation.client.toSimTaskClientCallback(
      this.simTaskClientCallback
    );
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

const executor = new ProjectSingleSimulationExecutor(
  (line: string) => {
    console.log("sending execution lines")
    processSend({ type: 'html', htmlLines: [line] });
  },
  {
    onExecutionFinish: () => {
      console.log('execution finished');
    },
    onExecutionStart: () => {
      console.log('execution started');
    },
    onExecutionTimeout: () => {
      console.log('execution timeout');
    },
  } as simulation.client.JsSimTaskClientCallback,
  (ocel: any) => {
    if (!ocel) return;
    processSend({ type: 'ocel', ocel: ocel } as OcelMessage);
  }
);

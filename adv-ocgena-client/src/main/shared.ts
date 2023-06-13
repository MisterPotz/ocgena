export type SimulationClientStatus = {
    canLaunchNewSimulation: boolean;
    ongoingSimulation: boolean;
};
  
export function isIpcMessage(message: any): message is IPCMesage {
  return 'type' in message;
}

export type IPCMesage = {
    type:
      | 'ocel'
      | 'execution-state'
      | 'ansi'
      | 'html'
      | 'update-simconfig'
      | 'update-ocdot'
      | 'errors'
      | 'launch-sim'
      | 'stop-sim';
  };
  
  export type SimConfigUpdateMessage = IPCMesage & {
    rawSimConfig: string | undefined;
    type: 'update-simconfig';
  };
  
  export type OcDotUpdateMessage = IPCMesage & {
    ocdot: string | undefined;
    type: 'update-ocdot';
  };
  
  export type OcelMessage = IPCMesage & {
    ocel: any;
    type: 'ocel';
  };
  
  export type SimulationStatusMessage = IPCMesage & {
    simulationStatus: SimulationClientStatus;
    type: 'execution-state';
  };
  
  export type HtmlMessage = IPCMesage & {
    htmlLines: string[] | undefined;
    type: 'html';
  };
  
  export type ErrorsMessage = IPCMesage & {
    errors: string[] | undefined;
    type: 'errors';
  };
  
  export type LaunchMessage = IPCMesage & {
    type: 'launch-sim';
  };

  export type StopMessage = IPCMesage & {
    type: 'stop-sim';
  };
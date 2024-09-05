export interface SimulationRun {
  id: string;
  name: string;
  simulationConfigPath: string;
  modelPath: string;
}

export interface RunsToPaths {
  [id: string]: string;
}

export interface Project {
  id: string;
  userName: String;
  simulationRuns: SimulationRun[];
  simulationConfigPaths: string[];
  modelPaths: string[];
  runToSimulationDB: RunsToPaths;
  runToOcel: RunsToPaths;
}

export interface AppDomainState {
  recentProjects: Project[];
}

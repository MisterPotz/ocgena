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
  current: boolean;
}

export interface AppDomainState {
  currentProject: Project | null;
  recentProjects: Project[];
}

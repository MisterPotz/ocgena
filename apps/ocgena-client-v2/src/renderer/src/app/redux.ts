import { PayloadAction, createAsyncThunk } from "@reduxjs/toolkit";
import { createAppSlice } from "./createAppSlice";
import { v4 as uuidv4 } from "uuid";
import { DEFAULT_PROJECT_NAME, makeDefaultName } from "../utils/defaults";
import { AppDomainState, Project, SimulationRun } from "../../../shared/domain";
import { event } from "../../../shared/events";
import { RootState } from "./store";
import "redux-observable";
import { loadFulfilled } from "../features/epicsTypes";

interface DynamicSimRun {
  simulationConfigPath?: string;
  modelPath?: string;
  validated?: boolean;
  isExecuting?: boolean;
}

function canStart(dynamic: DynamicSimRun) {
  return (
    !!dynamic.simulationConfigPath && !!dynamic.modelPath && !!dynamic.validated
  );
}

function toExecutionState(dynamic : DynamicSimRun): Execution {
  if (canStart(dynamic)) return 'ready'
  if (!!dynamic.isExecuting) return 'in_progress'
  return "blocked"
}

type Execution = 'ready' | 'in_progress' | 'blocked'

interface CurrentProject extends Project {
  runConfig: DynamicSimRun;
}

interface AppState {
  projects: AppDomainState;
  loaded: boolean;
  runConfig: DynamicSimRun;
}

const initialState: AppState = {
  projects: { recentProjects: [] },
  loaded: false,
  runConfig: {},
};

// export const createNewProject = createAsyncThunk(
//   "domain/newproject",
//   async (_, thunkApi): Promise<Project> => {
//     // need to add sql interaction here with the node
//     const defaultNames = countDefaultNames(
//       (thunkApi.getState() as RootState).domain.recentProjects
//     );

//     const newProj: Project = {
//       id: uuidv4(),
//       modelPaths: [],
//       runToOcel: {},
//       simulationRuns: [],
//       simulationConfigPaths: [],
//       runToSimulationDB: {},
//       current: true,
//       userName: makeDefaultName(defaultNames + 1),
//     };

//     // dbStore.set(newProj.id, newProj);

//     return newProj;
//   }
// );

function countDefaultNames(any: any) {
  let count = 0;
  Object.keys(any).forEach((element) => {
    if (element.startsWith(DEFAULT_PROJECT_NAME)) {
      count++;
    }
  });
  return count;
}

export const appSlice = createAppSlice({
  name: "domain",
  initialState,
  reducers: (create) => ({
    projectSelected: create.reducer(
      (state, payloadAction: PayloadAction<Project | null>) => {}
    ),
  }),
  extraReducers: (builder) => {
    builder.addCase(
      loadFulfilled,
      (state, action: PayloadAction<AppDomainState>) => {
        state.projects = action.payload;
        state.loaded = true;
      }
    );
  },
  selectors: {
    executionModeSelector: (state) => toExecutionState(state.runConfig)
  },
});

export const { projectSelected } = appSlice.actions;
export const { executionModeSelector } = appSlice.selectors;

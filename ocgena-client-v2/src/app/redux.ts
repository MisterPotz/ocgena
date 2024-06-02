import { PayloadAction, createAsyncThunk } from "@reduxjs/toolkit";
import { createAppSlice } from "./createAppSlice.ts";
import { v4 as uuidv4 } from "uuid";
import { RootState } from "./store.ts";
import { DEFAULT_PROJECT_NAME, makeDefaultName } from "../utils/defaults.tsx";
import { StoreHolder } from "../db.ts";

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

const initialState: AppDomainState = {
  currentProject: null,
  recentProjects: [],
};

export const createNewProject = createAsyncThunk(
  "domain/newproject",
  async (_, thunkApi): Promise<Project> => {
    // need to add sql interaction here with the node
    const defaultNames = countDefaultNames(StoreHolder.getInstance().store.store);

    const newProj: Project = {
      id: uuidv4(),
      modelPaths: [],
      runToOcel: {},
      simulationRuns: [],
      simulationConfigPaths: [],
      runToSimulationDB: {},
      current: true,
      userName: makeDefaultName(defaultNames + 1),
    };

    // dbStore.set(newProj.id, newProj);

    return newProj;
  }
);

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
      (state, payloadAction: PayloadAction<Project | null>) => {
        state.currentProject = payloadAction.payload;
      }
    ),
  }),
  extraReducers: (builder) => {
    builder.addCase(createNewProject.fulfilled, (state, action) => {
      const newProject = action.payload;
      state.currentProject = newProject;
      state.recentProjects = [newProject, ...state.recentProjects];
    });
  },
  selectors: {},
});

export const { projectSelected } = appSlice.actions;
export const {} = appSlice.selectors;

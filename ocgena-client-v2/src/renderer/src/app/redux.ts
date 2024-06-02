import { PayloadAction, createAsyncThunk } from "@reduxjs/toolkit";
import { createAppSlice } from "./createAppSlice.ts";
import { v4 as uuidv4 } from "uuid";
import { DEFAULT_PROJECT_NAME, makeDefaultName } from "../utils/defaults.tsx";
import { AppDomainState, Project } from "../../../shared/domain.ts";
import { App, ipcRenderer } from "electron";
import { event } from "../../../shared/events.ts";
import { RootState } from "./store.ts";

const initialState: AppDomainState & { loaded: boolean } = {
  currentProject: null,
  recentProjects: [],
  loaded: false,
};

export const createNewProject = createAsyncThunk(
  "domain/newproject",
  async (_, thunkApi): Promise<Project> => {
    // need to add sql interaction here with the node
    const defaultNames = countDefaultNames(
      await ipcRenderer.invoke(event("getStoreAll"))
    );

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

export const load = createAsyncThunk<AppDomainState, void, { rejectValue : string }>(
  "domain/load",
  async (_, api) => {
    if ((api.getState() as RootState).domain.loaded) {
      return api.rejectWithValue("already loaded");
    }
    const allProjects = (await ipcRenderer.invoke(
      event("getStoreAll")
    )) as Project[];

    const current = allProjects.find((el) => el.current);

    return {
      currentProject: current,
      recentProjects: allProjects,
    };
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
    builder.addCase(load.fulfilled, (state, action) => {
      console.log("load fulfilled")
      state.loaded = true;
      state.currentProject = action.payload.currentProject;
      state.recentProjects = action.payload.recentProjects;
    });
  },
  selectors: {},
});

export const { projectSelected } = appSlice.actions;
export const {} = appSlice.selectors;

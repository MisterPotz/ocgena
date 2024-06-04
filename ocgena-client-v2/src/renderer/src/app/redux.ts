import { PayloadAction, createAsyncThunk } from "@reduxjs/toolkit";
import { createAppSlice } from "./createAppSlice";
import { v4 as uuidv4 } from "uuid";
import { DEFAULT_PROJECT_NAME, makeDefaultName } from "../utils/defaults";
import { AppDomainState, Project } from "../../../shared/domain";
import { event } from "../../../shared/events";
import { RootState } from "./store";
import "redux-observable";
import { loadFulfilled } from "../features/epicsTypes";

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
      (thunkApi.getState() as RootState).domain.recentProjects
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

export const load = createAsyncThunk<
  AppDomainState,
  void,
  { rejectValue: string }
>("domain/load", async (_, api) => {
  if ((api.getState() as RootState).domain.loaded) {
    return api.rejectWithValue("already loaded");
  }
  const allProjects = (await window.api.request(
    event("getStoreAll")
  )) as Project[];

  const current = allProjects.find((el) => el.current);

  return {
    currentProject: current,
    recentProjects: allProjects,
  };
});

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
    builder.addCase(
      loadFulfilled,
      (state, action: PayloadAction<AppDomainState>) => {
        state.currentProject = !action.payload.currentProject ? null : action.payload.currentProject;
        state.recentProjects = action.payload.recentProjects;
        state.loaded = true;
      }
    );
  },
  selectors: {},
});

export const { projectSelected } = appSlice.actions;
export const {} = appSlice.selectors;
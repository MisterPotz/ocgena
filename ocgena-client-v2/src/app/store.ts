import type { Action, ThunkAction } from "@reduxjs/toolkit";
import "@reduxjs/toolkit/dist/combineSlices.ts";
import { combineSlices, configureStore } from "@reduxjs/toolkit";
import { setupListeners } from "@reduxjs/toolkit/query";
import { combineEpics, createEpicMiddleware } from "redux-observable";
import { createFilteringMiddleware } from "../utils/redux_utils.ts";
import {
  editorActionFilter,
  editorHandleDragEpic,
  editorSlice,
} from "../features/editor/redux.ts";

// `combineSlices` automatically combines the reducers using
// their `reducerPath`s, therefore we no longer need to call `combineReducers`.
const rootReducer = combineSlices(editorSlice);
const epicMiddleware = createEpicMiddleware<Action, Action, void, any>();
const rootEpic = combineEpics(editorHandleDragEpic);
const filteringMiddleware = createFilteringMiddleware(editorActionFilter);

// Infer the `RootState` type from the root reducer
export type RootState = ReturnType<typeof rootReducer>;

// The store setup is wrapped in `makeStore` to allow reuse
// when setting up tests that need the same store config
export const makeStore = (preloadedState?: Partial<RootState>) => {
  const store = configureStore({
    reducer: rootReducer,
    // Adding the api middleware enables caching, invalidation, polling,
    // and other useful features of `rtk-query`.
    middleware: (getDefaultMiddleware) => {
      return getDefaultMiddleware()
        .concat(epicMiddleware)
        .concat(filteringMiddleware);
    },
    preloadedState,
  });
  // configure listeners using the provided defaults
  // optional, but required for `refetchOnFocus`/`refetchOnReconnect` behaviors
  setupListeners(store.dispatch);

  epicMiddleware.run(rootEpic);
  return store;
};

export const store = makeStore();

// Infer the type of `store`
export type AppStore = typeof store;
// Infer the `AppDispatch` type from the store itself
export type AppDispatch = AppStore["dispatch"];
export type AppThunk<ThunkReturnType = void> = ThunkAction<
  ThunkReturnType,
  RootState,
  unknown,
  Action
>;

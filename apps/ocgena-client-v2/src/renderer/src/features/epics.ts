import { TypedActionCreator } from "@reduxjs/toolkit/dist/mapBuilders";
import { combineEpics, ofType } from "redux-observable";
import { exhaustMap, EMPTY, from, map } from "rxjs";
import { Project, AppDomainState } from "../../../shared/domain";
import { loadFulfilled, loadType, loadTypeFinished } from "./epicsTypes";
import { event } from "../../../shared/events";
import { convertState } from "../app/store";

export const createNewProjectEpic = combineEpics((action$, state$) =>
  action$.pipe(
    ofType(loadType),
    exhaustMap(() => {
      if (convertState(state$.value).domain.loaded) {
        return EMPTY;
      }
      return from(
        (async () => {
          return (await window.api.request(event("getStoreAll"))) as Project[];
        })()
      ).pipe(
        map((projects) => {
          return loadFulfilled({
            recentProjects: projects,
          } as AppDomainState);
        })
      );
    })
  )
);

export const allEpics = combineEpics(createNewProjectEpic);

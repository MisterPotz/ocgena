import {
  Action,
  ActionCreatorWithPayload,
  Middleware,
  PayloadAction,
  ReducerCreators,
} from "@reduxjs/toolkit/react";
import { Epic, ofType } from "redux-observable";
import { Observable, OperatorFunction, filter, map } from "rxjs";

export function ofTypeAndMap<P, R extends PayloadAction<P>>(
  actionCreator: ActionCreatorWithPayload<P, string>
): OperatorFunction<Action, ReturnType<typeof actionCreator>> {
  return function (action$: Observable<Action>) {
    return action$.pipe(
      ofType(actionCreator.type),
      map((action) => action as R)
    );
  };
}

function isActionWithTypes(action: any): action is Action {
  return typeof action === "object" && action !== null && "type" in action;
}

export function createFilteringMiddleware(
  ...actionFilters: ActionFilter[]
): Middleware {
  return (store) => (next) => (action) => {
    if (isActionWithTypes(action)) {
      if (!actionFilters.find((el) => el.shouldFilterAction(action))) {
        return next(action);
      }
    } else {
      return next(action);
    }
  };
}

export interface ActionFilter {
  shouldFilterAction(action: Action): boolean;
}

export function createActionFilter(
  ...actionCreators: ActionCreatorWithPayload<any, any>[]
): ActionFilter {
  const filteredValues = actionCreators.map(
    (actionCreator) => actionCreator.type
  );

  return {
    shouldFilterAction: (action: Action) => {
      return filteredValues.find((e) => e == action.type);
    },
  };
}

export function createEmptyPayloadReducer<Payload, State = any>(
  create: ReducerCreators<State>
) {
  return create.reducer((state, action: PayloadAction<Payload>) => undefined);
}

export function createEmptyReducer<State>(create: ReducerCreators<State>) {
  return create.reducer((state, action: Action) => undefined);
}

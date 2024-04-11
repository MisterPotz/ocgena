import {
  Action,
  ActionCreatorWithPayload,
  Middleware,
  PayloadAction,
  ReducerCreators,
} from "@reduxjs/toolkit/react"
import { Epic, ofType } from "redux-observable"
import { Observable, OperatorFunction, filter, map } from "rxjs"
import { every, exists } from "fp-ts/Array"

export function ofTypeAndMap<P, R extends PayloadAction<P>>(
  actionCreator: ActionCreatorWithPayload<P, string>,
): OperatorFunction<Action, ReturnType<typeof actionCreator>> {
  return function (action$: Observable<Action>) {
    return action$.pipe(
      ofType(actionCreator.type),
      map(action => action as R),
    )
  }
}

export function createEpic(
  actionPipeCreator: (action$: Observable<Action>) => Observable<Action>,
): Epic<
  Action,
  Action,
  void,
  any
> /* (action$: Observable<Action>) => Observable<Action> */ {
  return actionPipeCreator
}

function isActionWithTypes(action: any): action is Action {
  return typeof action === "object" && action !== null && "type" in action
}

export function createFilteringMiddleware(
  ...actionFilters: ActionFilter[]
): Middleware {
  return store => next => action => {
    if (isActionWithTypes(action)) {
      if (
        !exists<ActionFilter>(el => el.shouldFilterAction(action))(actionFilters)
      ) {
        return next(action)
      }
    } else {
      return next(action)
    }
  }
}

export interface ActionFilter {
  shouldFilterAction(action: Action): boolean
}

export function createActionFilter<P, T extends string = string>(
  ...actionCreators: ActionCreatorWithPayload<P, T>[]
): ActionFilter {
  const filteredValues = actionCreators.map(actionCreator => actionCreator.type)

  return {
    shouldFilterAction: (action: Action<T>) => {
      return exists<T>(e => e == action.type)(filteredValues)
    },
  }
}

export function createEmptyPayloadReducer<Payload, State = any>(
    create: ReducerCreators<State>,
  ) {
    return create.reducer((state, action: PayloadAction<Payload>) => {})
  }
  
  export function createEmptyReducer<State>(create: ReducerCreators<State>) {
    return create.reducer((state, action: Action) => {})
  }
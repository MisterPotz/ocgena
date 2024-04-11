import {
  Action,
  ActionCreatorWithPayload,
  PayloadAction,
  ReducerCreators,
  Tuple,
  createAction,
  createReducer,
  createSelector,
} from "@reduxjs/toolkit"
import { createAppSlice } from "../../app/createAppSlice"
import { combineEpics, ofType } from "redux-observable"
import {
  Observable,
  OperatorFunction,
  filter,
  interval,
  map as rxMap,
  sample,
  switchMap,
  takeUntil,
  throttleTime,
} from "rxjs"
import {
  createActionFilter,
  createEmptyPayloadReducer,
  createEpic,
  ofTypeAndMap,
} from "../../utils/redux_utils"
import "fp-ts/lib/Array"
import { findIndex, map, modifyAt, replicate } from "fp-ts/lib/Array"
import { Option, fold, getOrElse, isSome, some } from "fp-ts/lib/Option"
import { replace } from "fp-ts/lib/string"
import { pipe } from "fp-ts/lib/function"
import { updateAt } from "fp-ts/lib/ReadonlyNonEmptyArray"
import { ap } from "fp-ts/lib/Apply"
import { modify } from "fp-ts/lib/FromState"
import { mapLeft } from "fp-ts/lib/EitherT"
import { eq } from "fp-ts"

export type Element<Shape extends SpecificShape = SpecificShape> = {
  //   type: Tool
  x: number
  y: number
  shape: Shape
  fill?: string
  stroke?: string
  id: string
  selected?: boolean
}

export type ShapeType = "rect" | "circle"

export type AnyElement = Element<SpecificShape>

interface ElementShape {
  type: ShapeType
}
export type RectangleShape = {
  type: "rect"
  width: number
  height: number
}
export interface CircleShape extends ElementShape {
  type: "circle"
  radius: number
}

export type SpecificShape = RectangleShape | CircleShape

export type Elements = Element<SpecificShape>[]

export interface EditorSliceState {
  elements: Elements
  selected: string[]
}

const initialState: EditorSliceState = {
  elements: [
    {
      id: "s1",
      x: 50,
      y: 50,
      shape: { type: "rect", width: 100, height: 100 },
      fill: "black",
    },
    {
      id: "s2",
      x: 200,
      y: 200,
      shape: { type: "circle", radius: 50 },
      stroke: "black",
    },
  ],
  selected: [],
}

export type PositionUpdatePayload = {
  id: string
  x: number
  y: number
}

export type SizeUpdatePayload = {
  id : string
  x : number
  y : number
  width: number
  height: number
}

export type ClickPayload = {
  id?: string
}

export const editorHandleDragEpic = combineEpics(
  createEpic(action$ =>
    action$.pipe(ofTypeAndMap(elementDragEpicTrigger), sample(interval(500))),
  ),
  createEpic(action$ =>
    action$.pipe(
      ofTypeAndMap(elementDragEndEpicTrigger),
      rxMap(action => {
        return elementPositionUpdate(action.payload)
      }),
    ),
  ),
)

export const editorSlice = createAppSlice({
  name: "editor",
  initialState,
  reducers: create => ({
    elementSelected: create.reducer((state, action: PayloadAction<string>) => {
      state.selected = [action.payload]
    }),
    elementPositionUpdate: create.reducer(
      (state, action: PayloadAction<PositionUpdatePayload>) => {
        state.elements = pipe(
          state.elements,
          map(el =>
            el.id === action.payload.id
              ? {
                  ...el,
                  x: action.payload.x,
                  y: action.payload.y,
                }
              : el,
          ),
        )
      },
    ),
    // elementSizeUpdate: create.reducer(
    //   (state, action : PayloadAction)
    // )
    mouseClick: create.reducer((state, action: PayloadAction<ClickPayload>) => {
      if (action.payload.id) {
        state.elements
      }
    }),
    elementDragEpicTrigger:
      createEmptyPayloadReducer<PositionUpdatePayload>(create),
    elementDragEndEpicTrigger:
      createEmptyPayloadReducer<PositionUpdatePayload>(create),
    // elementMoved: create.asyncThunk({

    // })
  }),
  selectors: {
    elementSelector: state => state.elements,
    selectedIdsSelector: state => state.selected,
    // elementIdSelector: createSelector(
    //   (state: EditorSliceState) => state.elements,
    //   (elements: Elements) => elements.map(el => el.id),
    // ),
  },
})

export const {
  elementSelected,
  elementPositionUpdate,
  elementDragEpicTrigger,
  elementDragEndEpicTrigger,
} = editorSlice.actions

export const { elementSelector, selectedIdsSelector } = editorSlice.selectors

export const editorActionFilter = createActionFilter(
  elementDragEpicTrigger,
  elementDragEndEpicTrigger,
)

function findElementIndex(elements: Elements, id: string): Option<number> {
  return findIndex<Element>(el => el.id == id)(elements)
}

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
  mergeMap,
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
import { findIndex, map, modifyAt, reduce, replicate } from "fp-ts/lib/Array"
import { Option, fold, getOrElse, isSome, none, some } from "fp-ts/lib/Option"
import { replace } from "fp-ts/lib/string"
import { pipe } from "fp-ts/lib/function"
import { ap } from "fp-ts/lib/Apply"
import { modify } from "fp-ts/lib/FromState"
import { mapLeft } from "fp-ts/lib/EitherT"
import { eq, nonEmptyArray, option } from "fp-ts"
import { max } from "fp-ts/lib/NonEmptyArray"

export type Element<Shape extends SpecificShape = SpecificShape> = {
  //   type: Tool
  x: number
  y: number
  shape: Shape
  fill?: string
  stroke?: string
  id: string
  selected?: boolean
  text?: string
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

export type SpecificShapeType = SpecificShape["type"]

export type Elements = Element<SpecificShape>[]

export interface EditorSliceState {
  elements: Elements
  selected: string[]
  contextMenuElement: string | null
}

class Counter {
  i: number

  constructor() {
    this.i = 0
  }

  next(): number {
    return ++this.i
  }
}

const keyWord: string = "el_"

class IdFactory {
  counter: Counter

  constructor() {
    this.counter = new Counter()
  }

  next(): string {
    return `${keyWord}${this.counter.next().toString()}`
  }

  isNumber(str: string) {
    return Number.isFinite(+str)
  }

  getNumber(id: string): number | null {
    let substring = id.substring(keyWord.length)
    if (this.isNumber(substring)) {
      return Number.parseInt(substring)
    } else {
      return null
    }
  }
}

const idFactory = new IdFactory()

const initialState: EditorSliceState = {
  elements: [
    {
      id: "test_rect",
      shape: {
        height: 200,
        width: 400,
        type: "rect",
      },
      x: 300,
      y: 200,
      selected: false,
      fill: "orange",
      text: "kek lol arbidol",
    },
    {
      id: idFactory.next(),
      x: 50,
      y: 50,
      shape: { type: "rect", width: 100, height: 100 },
      fill: "black",
    },
    {
      id: idFactory.next(),
      x: 200,
      y: 200,
      shape: { type: "circle", radius: 50 },
      stroke: "black",
    },
  ],
  selected: [],
  contextMenuElement: null,
}

export type PositionUpdatePayload = {
  id: string
  x: number
  y: number
}

export type SizeUpdatePayload = {
  id: string
  x: number
  y: number
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
  // createEpic(action$ =>
  //   action$.pipe(
  //     ofTypeAndMap(contextMenuAddTransition),
  //     mergeMap(action => {

  //     })
  //   )
  // )
)

function defaultRect(x: number, y: number): Element<RectangleShape> {
  return {
    id: idFactory.next(),
    shape: {
      type: "rect",
      height: 75,
      width: 120,
    },
    x,
    y,
    text: "transition",
  }
}

function deselectElements(elements: Elements): Elements {
  return pipe(
    elements,
    map(el => (el.selected ? { ...el, selected: false } : el)),
  )
}

export const editorSlice = createAppSlice({
  name: "editor",
  initialState,
  reducers: create => ({
    elementSelected: create.reducer((state, action: PayloadAction<string>) => {
      state.elements = pipe(
        state.elements,
        map(el =>
          el.id === action.payload
            ? {
                ...el,
                selected: true,
              }
            : el.selected
              ? { ...el, selected: false }
              : el,
        ),
      )
      state.selected = [action.payload]
    }),
    elementSelectionCancelled: create.reducer(state => {
      state.elements = deselectElements(state.elements)
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
    elementContextOpened: (state, action: PayloadAction<string>) => {
      state.contextMenuElement = action.payload
    },
    contextMenuAddTransition: create.reducer(state => {
      if (state.contextMenuElement) {
        const contextMenuElement = state.contextMenuElement
        const el = state.elements.find(el => el.id === contextMenuElement)

        if (el) {
          const x = el.x // need to find proper start position for new rect
          const y = el.y
          const newRect: AnyElement = { ...defaultRect(x, y), selected: true }
          state.elements = deselectElements(state.elements).concat(newRect)
          state.contextMenuElement = null
        }
      }
    }),
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
  elementSelectionCancelled,
  elementPositionUpdate,
  elementDragEpicTrigger,
  elementDragEndEpicTrigger,
  elementContextOpened,
  contextMenuAddTransition,
} = editorSlice.actions

export const { elementSelector, selectedIdsSelector } = editorSlice.selectors

export const editorActionFilter = createActionFilter(
  elementDragEpicTrigger,
  elementDragEndEpicTrigger,
)

import {
  PayloadAction,
} from "@reduxjs/toolkit"
import { createAppSlice } from "../../app/createAppSlice"
import { combineEpics } from "redux-observable"
import {
  interval,
  map as rxMap,
  sample,
} from "rxjs"
import {
  createActionFilter,
  createEmptyPayloadReducer,
  createEpic,
  ofTypeAndMap,
} from "../../utils/redux_utils"
import "fp-ts/lib/Array"
import { map } from "fp-ts/lib/Array"
import { pipe } from "fp-ts/lib/function"
import "./CoordinatesExt"

export interface Element<Shape extends SpecificShape = SpecificShape> {
  //   type: Tool
  x: number
  y: number
  rawX: number
  rawY: number
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
export interface RectangleShape extends ElementShape {
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

export interface ContextMenu {
  x: number
  y: number
  targetElement: string
}

export interface EditorSliceState {
  elements: Elements
  selected: string[]
  contextMenu?: ContextMenu | null
  // contextMenuElement: string | null
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
      rawX: 300,
      rawY: 200,
      x: (300).closestDotX(),
      y: (200).closestDotY(),
      selected: false,
      fill: "orange",
      text: "kek lol arbidol",
    },
    {
      id: idFactory.next(),
      rawX: 50,
      rawY: 50,
      x: (50).closestDotX(),
      y: (50).closestDotY(),
      shape: { type: "rect", width: 100, height: 100 },
      fill: "black",
    },
    {
      id: idFactory.next(),
      rawX: 200,
      rawY: 200,
      x: (200).closestDotX(),
      y: (200).closestDotY(),
      shape: { type: "circle", radius: 50 },
      stroke: "black",
    },
  ],
  selected: [],
  // contextMenuElement: null,
}

function heightFromStart(element: Element): number {
  switch (element.shape.type) {
    case "circle":
      return element.shape.radius
    case "rect":
      return element.shape.height
  }
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
      height: (75).closestSize(),
      width: (120).closestSize(),
    },
    x: x.closestDotX(),
    y: y.closestDotY(),
    rawX: x,
    rawY: y,
    text: "transition",
    stroke: "black",
    fill: "white",
  }
}

function defaultCircle(x: number, y: number): Element<CircleShape> {
  return {
    id: idFactory.next(),
    shape: {
      type: "circle",
      radius: (75).closestSize(),
    },
    x: (x + 75).closestDotX(),
    y: y.closestDotY(),
    rawX: x,
    rawY: y,
    text: "transition",
    stroke: "black",
    fill: "white",
  }
}

function deselectElements(elements: Elements): Elements {
  return pipe(
    elements,
    map(el => (el.selected ? { ...el, selected: false } : el)),
  )
}
const PADDING = 20

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
    elementContextOpened: create.reducer(
      (state, action: PayloadAction<ContextMenu>) => {
        state.contextMenu = action.payload
      },
    ),
    elementContextMenuClosed: create.reducer(state => {
      state.contextMenu = null
    }),
    contextMenuAddTransition: create.reducer(state => {
      if (state.contextMenu) {
        const contextMenuElement = state.contextMenu.targetElement
        const el = state.elements.find(el => el.id === contextMenuElement)

        if (el) {
          const x = el.x + PADDING // need to find proper start position for new rect
          const y = el.y + heightFromStart(el) + PADDING
          const newRect: AnyElement = { ...defaultRect(x, y), selected: true }
          state.elements = deselectElements(state.elements).concat(newRect)
          state.contextMenu = null
        }
      }
    }),
    contextMenuAddPlace: create.reducer(state => {
      if (state.contextMenu) {
        const contextMenuElement = state.contextMenu.targetElement
        const el = state.elements.find(el => el.id === contextMenuElement)

        if (el) {
          const x = el.x + PADDING // need to find proper start position for new rect
          const y = el.y + heightFromStart(el) + PADDING
          const newRect: AnyElement = { ...defaultCircle(x, y), selected: true }
          state.elements = deselectElements(state.elements).concat(newRect)
          state.contextMenu = null
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
    contextMenuSelector: state => state.contextMenu,
  },
})

export const {
  elementSelected,
  elementSelectionCancelled,
  elementPositionUpdate,
  elementDragEpicTrigger,
  elementDragEndEpicTrigger,
  elementContextOpened,
  elementContextMenuClosed,
  contextMenuAddTransition,
  contextMenuAddPlace,
} = editorSlice.actions

export const { elementSelector, selectedIdsSelector, contextMenuSelector } =
  editorSlice.selectors

export const editorActionFilter = createActionFilter(
  elementDragEpicTrigger,
  elementDragEndEpicTrigger,
)

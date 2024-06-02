import { PayloadAction, createSelector } from "@reduxjs/toolkit"
import { createAppSlice } from "../../app/createAppSlice.js"
import { combineEpics } from "redux-observable"
import {
  EMPTY,
  interval,
  map as rxMap,
  sample,
  sampleTime,
  switchMap,
} from "rxjs"
import {
  createActionFilter,
  createEmptyPayloadReducer,
  createEpic,
  ofTypeAndMap,
} from "../../utils/redux_utils.ts"
import "./CoordinatesExt"
import { ELEMENT_PREFIX } from "./Keywords.js"
import {
  AnyElement,
  CircleShape,
  Element,
  Elements,
  PositionUpdatePayload,
  RectangleShape,
  SelectionWindow,
  SelectionWindowPayload,
} from "./Models.js"
import {
  bottomBound,
  elementInSelectionWindow,
  getUpdatedShape,
  rightBound,
} from "./primitiveShapeUtils.js"

export interface ContextMenu {
  x: number
  y: number
  targetElement: string
}

export interface EditorSliceState {
  elements: Elements
  // selected: string[]
  contextMenu?: ContextMenu | null
  selectionWindow?: SelectionWindow | null
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

const keyWord: string = ELEMENT_PREFIX

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
    const substring = id.substring(keyWord.length)
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
      id: idFactory.next(),
      shape: {
        height: (75).closestSize(),
        width: (150).closestSize(),
        type: "rect",
      },
      rawX: 0,
      rawY: 0,
      rawHeight: 0,
      rawWidth: 0,
      x: (0).closestDotX(),
      y: (0).closestDotY(),
      selectedAtClick: false,
      fill: "white",
      stroke: "black",
      text: "ITEMS" //"ITEMS ARE SORTeD",
    },
    // {
    //   id: idFactory.next(),
    //   shape: {
    //     height: (75).closestSize(),
    //     width: (150).closestSize(),
    //     type: "rect",
    //   },
    //   rawX: 0,
    //   rawY: 0,
    //   rawHeight: 0,
    //   rawWidth: 0,
    //   x: (110).closestDotX(),
    //   y: dots.getClosestY(0),
    //   selectedAtClick: false,
    //   fill: "white",
    //   stroke: "black",
    //   text: "orders are issued",
    // },
    // {
    //   id: idFactory.next(),
    //   shape: {
    //     height: (75).closestSize(),
    //     width: (150).closestSize(),
    //     type: "rect",
    //   },
    //   rawX: 0,
    //   rawY: 0,
    //   rawHeight: 0,
    //   rawWidth: 0,
    //   x: (0).closestDotX(),
    //   y: (110).closestDotY(),
    //   selectedAtClick: false,
    //   fill: "white",
    //   stroke: "black",
    //   text: "transaction accepted",
    // },

    // {
    //   id: idFactory.next(),
    //   shape: {
    //     height: (75).closestSize(),
    //     width: (150).closestSize(),
    //     type: "rect",
    //   },
    //   rawX: 0,
    //   rawY: 0,
    //   rawHeight: 0,
    //   rawWidth: 0,
    //   x: (110).closestDotX(),
    //   y: (110).closestDotY(),
    //   selectedAtClick: false,
    //   fill: "white",
    //   stroke: "black",
    //   text: "package delivered",
    // },

    // {
    //   id: idFactory.next(),
    //   rawX: 50,
    //   rawY: 50,
    //   x: (50).closestDotX(),
    //   y: (50).closestDotY(),
    //   shape: { type: "rect", width: 100, height: 100 },
    //   fill: "black",
    // },
    // {
    //   id: idFactory.next(),
    //   rawX: 200,
    //   rawY: 200,
    //   x: (200).closestDotX(),
    //   y: (200).closestDotY(),
    //   shape: { type: "circle", radius: 50 },
    //   stroke: "black",
    // },
  ],
  // selected: [],
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

export type MouseMovePayload = {
  clientX: number
  clientY: number
  stageX: number
  stageY: number
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
  createEpic(action$ =>
    action$.pipe(
      ofTypeAndMap(mouseMoveEpicTrigger),
      sampleTime(1000),
      switchMap(action => {
        console.log("mousemove", action.payload)
        return EMPTY
      }),
    ),
  ),
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
    rawWidth: 120,
    rawHeight: 75,
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
    x: x.closestDotX(),
    y: y.closestDotY(),
    rawX: x,
    rawY: y,
    rawHeight: 75 * 2,
    rawWidth: 75 * 2,
    text: "transition",
    stroke: "black",
    fill: "white",
  }
}

function deselectElements(elements: Elements): Elements {
  return elements.map(el =>
    el.selectedAtClick || el.selectedWithWindow
      ? { ...el, selectedAtClick: false, selectedWithWindow: false }
      : el,
  )
}

function recalculateSelectionWindow(state : EditorSliceState) {
  if (state.elements.length == 0 || !state.elements.find((el => el.selectedWithWindow))) {
    state.selectionWindow = null
  } else {
    const selLeftBound = state.elements.reduce((prev, curr) => {
      return curr.selectedWithWindow ? Math.min(curr.x, prev) : prev
    }, Number.MAX_VALUE)
    const selTopBound = state.elements.reduce((prev, curr) => {
      return curr.selectedWithWindow ? Math.min(curr.y, prev) : prev
    }, Number.MAX_VALUE)
    const selRightBound = state.elements.reduce((prev, curr) => {
      return curr.selectedWithWindow
        ? Math.max(rightBound(curr), prev)
        : prev
    }, Number.MIN_VALUE)
    const selBottomBound = state.elements.reduce((prev, curr) => {
      return curr.selectedWithWindow
        ? Math.max(bottomBound(curr), prev)
        : prev
    }, Number.MIN_VALUE)

    state.selectionWindow = {
      x: Math.max(selLeftBound - SELECTION_WINDOW_PADDING, 0),
      y: Math.max(0, selTopBound - SELECTION_WINDOW_PADDING),
      height:
        Math.max(0, selBottomBound - selTopBound) +
        SELECTION_WINDOW_PADDING * 2,
      width:
        Math.max(0, selRightBound - selLeftBound) +
        SELECTION_WINDOW_PADDING * 2,
      selectedElementIds: state.elements.filter((el) => !!el.selectedWithWindow).map((el) => el.id)
    }
  }
}

const NEW_PLACEMENT_PADDING = 20

const SELECTION_WINDOW_PADDING = 10

export const editorSlice = createAppSlice({
  name: "editor",
  initialState,
  reducers: create => ({
    elementSelected: create.reducer((state, action: PayloadAction<string>) => {
      state.elements = state.elements.map(el =>
        el.id === action.payload
          ? {
              ...el,
              selectedAtClick: true,
            }
          : el.selectedAtClick
            ? { ...el, selectedAtClick: false }
            : el,
      )
    }),
    selectionUpdated: create.reducer(
      (state, action: PayloadAction<SelectionWindowPayload>) => {
        state.elements = state.elements.map((el: Element) => {
          return elementInSelectionWindow(el, action.payload)
            ? { ...el, selectedWithWindow: true, selectedAtClick: false }
            : el.selectedAtClick || el.selectedWithWindow
              ? { ...el, selectedAtClick: false, selectedWithWindow: false }
              : el
        })
        recalculateSelectionWindow(state)
      },
    ),
    elementSelectionCancelled: create.reducer(state => {
      state.elements = deselectElements(state.elements)
      state.selectionWindow = null
    }),
    elementPositionUpdate: create.reducer(
      (state, action: PayloadAction<PositionUpdatePayload>) => {
        state.elements = state.elements.map(el =>
          el.id === action.payload.id
            ? {
                ...el,
                x: action.payload.x.closestDotX(),
                y: action.payload.y.closestDotY(),
                rawX: action.payload.x,
                rawY: action.payload.y,
                rawHeight: action.payload.height,
                rawWidth: action.payload.width,
                shape: getUpdatedShape(el.shape, action.payload)
              }
            : el,
        )
        // recalculateSelectionWindow(state)
      },
    ),
    // elementSizeUpdate: create.reducer(
    //   (state, action : PayloadAction)
    // )
    mouseClick: create.reducer((state, action: PayloadAction<ClickPayload>) => {
      // if (action.payload.id) {
      //   state.elements
      // }
    }),
    elementDragEpicTrigger:
      createEmptyPayloadReducer<PositionUpdatePayload>(create),
    elementDragEndEpicTrigger:
      createEmptyPayloadReducer<PositionUpdatePayload>(create),
    mouseMoveEpicTrigger: createEmptyPayloadReducer<MouseMovePayload>(create),
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
          const x = el.x + NEW_PLACEMENT_PADDING // need to find proper start position for new rect
          const y = el.y + heightFromStart(el) + NEW_PLACEMENT_PADDING
          const newRect: AnyElement = {
            ...defaultRect(x, y),
            selectedAtClick: true,
          }
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
          // const x = el.x + PADDING // need to find proper start position for new rect
          // const y = el.y + heightFromStart(el) + PADDING
          const newRect: AnyElement = {
            ...defaultCircle(0, 0),
            selectedAtClick: true,
          }
          state.elements = deselectElements(state.elements).concat(newRect)
          state.contextMenu = null
        }
      }
    }),
  }),
  selectors: {
    elementSelector: state => state.elements,
    // selectedIdsSelector: state => state.selected,
    // elementIdSelector: createSelector(
    //   (state: EditorSliceState) => state.elements,
    //   (elements: Elements) => elements.map(el => el.id),
    // ),
    // smartSelectionWindowElementsSelector : createSelector(
    //   (state: EditorSliceState) => {
    //     state.elements
    //   },
    //   (elements: Elements) => {elements.filter()}
    // ),
    selectionWindowElementsSelector: state => {
      if (!state.selectionWindow) return null
      
      return {
        selectedElements: state.elements.filter(el =>
          el.selectedWithWindow ? el.selectedWithWindow : false,
        ),
        window: state.selectionWindow,
      }
    },
    selectedElementIdsSelector: createSelector(
      (state : EditorSliceState) => state.selectionWindow?.selectedElementIds,
      (elements: string[] | undefined) => elements ? elements : undefined
    ),
    contextMenuSelector: state => state.contextMenu,
  },
})

export const {
  elementSelected,
  selectionUpdated,
  elementSelectionCancelled,
  elementPositionUpdate,
  elementDragEpicTrigger,
  elementDragEndEpicTrigger,
  elementContextOpened,
  elementContextMenuClosed,
  mouseMoveEpicTrigger,
  contextMenuAddTransition,
  contextMenuAddPlace,
} = editorSlice.actions

export const {
  elementSelector,
  selectionWindowElementsSelector,
  /* selectedIdsSelector,*/ contextMenuSelector,
  selectedElementIdsSelector,
} = editorSlice.selectors

export const editorActionFilter = createActionFilter(
  elementDragEpicTrigger,
  elementDragEndEpicTrigger,
  mouseMoveEpicTrigger,
)

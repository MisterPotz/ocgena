import { PayloadAction } from "@reduxjs/toolkit"
import { createAppSlice } from "../../app/createAppSlice"
import {
    Space,
    SpaceViewer,
    Navigator,
    Keys,
    MouseKeys,
    ButtonKeys,
    containsXY,
    Positionable,
    leftBorder,
    rightBorder,
    topBorder,
    bottomBorder,
    Rect,
    PositionableShape,
    PositionablesIndex,
} from "./SpaceModel"
import {
    compareBottomRight,
    compareTopLeft,
    PositionablesPositionIndexImpl,
} from "./PositionablesIndexImpl"
import { CombinedPressedKeyChecker } from "./CombinedPressedKeyChecker"
import { PositionablesRepository } from "./PositionablesMap"
type EditorV2State = {
    space: Space
    spaceViewer: SpaceViewer
    navigator: Navigator
    selectionCommands: SelectionCommandOld[]
}

const initialState: EditorV2State = {
    space: {
        positionables: [],
        selector: null,
    },
    navigator: {
        areaSelection: null,
        pressedKeys: new Set<Keys>(),
        x: 0,
        y: 0,
    },
    spaceViewer: {
        offsetX: 0,
        offsetY: 0,
        startOffsetX: undefined,
        startOffsetY: undefined,
    },
    selectionCommands: [],
}

type MouseDownPayload = {
    targetId?: string
    x: number
    y: number
    key: MouseKeys
}

type MouseRelease = {
    releaseX: number
    releaseY: number
    key: MouseKeys
}

type MouseMove = {
    newX: number
    newY: number
}

type ButtonDownPayload = {
    key: ButtonKeys
}

type ButtonUpPayload = {
    key: ButtonKeys
}

const keysChecker = new CombinedPressedKeyChecker()

// what if i join selector and transformer?

type SelectItems = {
    items: PositionableShape[]
    topLeftItem: PositionableShape
    bottomRightItem: PositionableShape
    type: "select"
}

type MoveItems = {
    items: PositionableShape[]
    moveX: number
    moveY: number
    type: "move"
}

type Command = {
    applyToState(context: Context): void
    undoToState(context: Context): void
}

type Context = {
    editorV2State: EditorV2State
    positionablesMap: PositionablesRepository
    positionablesIndex: PositionablesIndex
}

// function getCommand(key: SelectItems | MoveItems): Command {
//     switch (key.type) {
//         case "move": {
//             return {
//                 applyToState(context) {},
//                 undoToState(context) {
//                     for (const pos of context.positionablesMap.getPositionables(key.items)) {
//                         pos.x -= key.moveX
//                         pos.y -= key.moveY
//                     }
//                     context.editorV2State.space.positionables =
//                         context.positionablesMap.mapToShapes()
//                 },
//             }
//         }
//         case "select": {
//             return {
//                 applyToState(context) {
//                     context.editorV2State.space.selector = {
//                         elements: context.positionablesMap.getPositionables(key.items),
//                         topLeftElement: this.topLeftItem,
//                         bottomRightElement: this.bottomRightItem,
//                         borders: getBorders(this.topLeftItem, this.bottomRightItem),
//                     }
//                 },
//             }
//         }
//     }
// }

interface SelectionCommandOld {
    applyToState(state: EditorV2State): void
    undoToState(state: EditorV2State): void
}

// class SelectItems implements SelectionCommandOld {
//     items: PositionableShape[]
//     topLeftItem: PositionableShape
//     bottomRightItem: PositionableShape

//     constructor(items: PositionableShape[], topLeftItem: PositionableShape, bottomRightItem: PositionableShape) {
//         this.items = items
//         this.topLeftItem = topLeftItem
//         this.bottomRightItem = bottomRightItem
//     }

//     applyToState(state: EditorV2State) {
//         state.space.selector = {
//             elements: this.items,
//             topLeftElement: this.topLeftItem,
//             bottomRightElement: this.bottomRightItem,
//             borders: getBorders(this.topLeftItem, this.bottomRightItem),
//         }
//     }

//     undoToState(state: EditorV2State) {
//         state.space.selector = null
//     }
// }

// class MoveItems implements SelectionCommandOld {
//     items: Positionable[]
//     moveX: number
//     moveY: number

//     constructor(items: Positionable[], moveX: number, moveY: number) {
//         this.items = items
//         this.moveX = moveX
//         this.moveY = moveY
//     }

//     applyToState(state: EditorV2State) {}

//     undoToState(state: EditorV2State) {
//         for (const pos of this.items) {
//             pos.x -= this.moveX
//             pos.y -= this.moveY
//         }
//     }
// }

function finishSelectedElementSelection(state: EditorV2State) {
    if (state.navigator.areaSelection) {
        // if (state.space.selector) {
        //     const selectCommand = new SelectItems(
        //         state.space.selector.elements,
        //         state.space.selector.topLeftElement,
        //         state.space.selector.bottomRightElement,
        //     )
        //     selectCommand.applyToState(state)
        //     state.selectionCommands.push(selectCommand)
        // }
        state.navigator.areaSelection = null
    }
}

const positionableIndex = new PositionablesPositionIndexImpl()
const positionableMap = new PositionablesRepository()

export const editorV2Slice = createAppSlice({
    name: "editorv2",
    initialState: initialState,
    reducers: create => ({
        mouseDown: create.reducer((state, action: PayloadAction<MouseDownPayload>) => {
            state.navigator.x = action.payload.x
            state.navigator.y = action.payload.y

            const key = mouseKeyToKeys(action.payload.key)
            keysChecker
                .updatePressedKeys(state.navigator.pressedKeys)
                .updatePlusKeys(action.payload.key)

            if (keysChecker.checkBecamePressed("space", "left")) {
                state.spaceViewer.startOffsetX = action.payload.x
                state.spaceViewer.startOffsetY = action.payload.y
            } else if (keysChecker.checkBecamePressed("left")) {
                if (
                    state.space.selector &&
                    containsXY(state.space.selector.borders, action.payload.x, action.payload.y)
                ) {
                    state.space.selector.startX = action.payload.x
                    state.space.selector.startY = action.payload.y
                } else {
                    const positionable =
                        /* state.space.positionables */ positionableIndex.getByCoordinate(
                            action.payload.x,
                            action.payload.y,
                        )

                    if (positionable != null) {
                        // const newSelectionCommand = new SelectItems(
                        //     [positionable],
                        //     positionable,
                        //     positionable,
                        // )
                        // newSelectionCommand.applyToState(state)
                        // state.selectionCommands.push(newSelectionCommand)
                        // state.space.selector = {
                        //     state.space.selector!.startX = action.payload.x
                        //     state.space.selector!.startY = action.payload.y
                        // }
                    } else {
                        state.space.selector = null
                        state.navigator.areaSelection = {
                            startX: action.payload.x,
                            startY: action.payload.y,
                        }
                    }
                }
            } else if (keysChecker.checkBecamePressed("right")) {
            }
            state.navigator.pressedKeys.add(key)
        }),
        mouseRelease: create.reducer((state, action: PayloadAction<MouseRelease>) => {
            const key = mouseKeyToKeys(action.payload.key)

            keysChecker.updatePressedKeys(state.navigator.pressedKeys).updateMinusKeys(key)

            if (
                keysChecker.checkArePressed("space", "left") &&
                keysChecker.checkBecameUnpressed("left")
            ) {
                state.spaceViewer.startOffsetX = undefined
                state.spaceViewer.startOffsetY = undefined
            } else if (keysChecker.checkBecameUnpressed("left")) {
                if (state.navigator.areaSelection) {
                    finishSelectedElementSelection(state)
                } else if (state.space.selector) {
                    if (state.space.selector.startX && state.space.selector.startY) {
                        // const moveCommand = new MoveItems(
                        //     state.space.selector.elements,
                        //     action.payload.releaseX - state.space.selector.startX,
                        //     action.payload.releaseY - state.space.selector.startY,
                        // )
                        // state.selectionCommands.push(moveCommand)
                        state.space.selector.startX = undefined
                        state.space.selector.startY = undefined
                        state.space.selector = null
                    }
                }
            } else if (keysChecker.checkBecameUnpressed("right")) {
                // if there is transformer or selection (and mouse over such elements?), open popup menu
            }

            state.navigator.pressedKeys.delete(key)
        }),
        mouseMove: create.reducer((state, action: PayloadAction<MouseMove>) => {
            keysChecker.updatePressedKeys(state.navigator.pressedKeys)
            const newX = action.payload.newX
            const newY = action.payload.newY
            const oldX = state.navigator.x
            const oldY = state.navigator.y

            if (keysChecker.checkArePressed("space", "left")) {
                if (state.spaceViewer.startOffsetX && state.spaceViewer.startOffsetY) {
                    state.spaceViewer.offsetY = newY - state.spaceViewer.startOffsetY!
                    state.spaceViewer.offsetX = newX - state.spaceViewer.startOffsetX!
                }
            } else if (keysChecker.checkArePressed("left")) {
                if (state.navigator.areaSelection) {
                    // const capturedMoreElements =
                    //     Math.abs(newX - state.navigator.areaSelection.startX) >=
                    //         Math.abs(oldX - state.navigator.areaSelection.startX) ||
                    //     Math.abs(newY - state.navigator.areaSelection.startY) >=
                    //         Math.abs(oldY - state.navigator.areaSelection.startY)

                    // // update selected figures
                    // const leftSelect = Math.min(newX, state.navigator.areaSelection!.startX)
                    // const rightSelect = Math.max(newX, state.navigator.areaSelection!.startX)
                    // const topSelect = Math.min(newY, state.navigator.areaSelection!.startY)
                    // const bottomSelect = Math.max(newY, state.navigator.areaSelection!.startY)
                    // const selectedPositionables = positionableIndex.getPositionablesInRange(
                    //     leftSelect,
                    //     topSelect,
                    //     rightSelect,
                    //     bottomSelect,
                    // )
                    // const edgeElements = getEdgeElements(selectedPositionables)
                    // if (selectedPositionables.length > 0 && edgeElements) {
                    //     state.space.selector = {
                    //         elements: selectedPositionables,
                    //         topLeftElement: edgeElements.topLeft,
                    //         bottomRightElement: edgeElements.bottomRight,
                    //         borders: getBorders(edgeElements.topLeft, edgeElements.bottomRight),
                    //     }
                    // } else {
                    //     state.space.selector = null
                    // }
                }
            }
        }),
        buttonDown: create.reducer((state, action: PayloadAction<ButtonDownPayload>) => {
            keysChecker
                .updatePressedKeys(state.navigator.pressedKeys)
                .updatePlusKeys(action.payload.key)

            if (keysChecker.checkBecamePressed("space", "left")) {
                if (state.navigator.areaSelection) {
                    finishSelectedElementSelection(state)
                }
                state.spaceViewer.startOffsetX = state.navigator.x
                state.spaceViewer.startOffsetY = state.navigator.y
            }

            state.navigator.pressedKeys.add(action.payload.key)
        }),
        buttonUp: create.reducer((state, action: PayloadAction<ButtonUpPayload>) => {
            keysChecker
                .updatePressedKeys(state.navigator.pressedKeys)
                .updateMinusKeys(action.payload.key)

            if (keysChecker.checkBecameUnpressed("space", "left")) {
                state.spaceViewer.startOffsetX = undefined
                state.spaceViewer.startOffsetY = undefined
            }

            state.navigator.pressedKeys.delete(action.payload.key)
        }),
    }),
})

function mouseKeyToKeys(mouseKey: MouseKeys): Keys {
    switch (mouseKey) {
        case "left":
            return "left"
        case "right":
            return "right"
    }
}

function getBorders(topLeftElement: Positionable, bottomRightElement: Positionable): Rect {
    return {
        left: leftBorder(topLeftElement),
        top: topBorder(topLeftElement),
        right: rightBorder(bottomRightElement),
        bottom: bottomBorder(bottomRightElement),
    }
}

function getEdgeElements(
    positionable: Positionable[],
): { topLeft: Positionable; bottomRight: Positionable } | null {
    if (positionable.length == 0) return null
    var topLeft = positionable[0]
    var bottomRight = positionable[positionable.length - 1]

    for (const pos of positionable) {
        if (compareTopLeft(topLeft, pos) == 1) {
            topLeft = pos
        }
        if (compareBottomRight(pos, bottomRight) == 1) {
            bottomRight = pos
        }
    }

    return {
        topLeft,
        bottomRight,
    }
}

export const { mouseDown, buttonDown, buttonUp, mouseRelease, mouseMove } = editorV2Slice.actions

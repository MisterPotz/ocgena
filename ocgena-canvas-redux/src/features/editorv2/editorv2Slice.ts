import { PayloadAction } from "@reduxjs/toolkit"
import { createAppSlice } from "../../app/createAppSlice"
import {
    Space,
    SpaceViewer,
    Navigator,
    Keys,
    MouseKeys,
    ButtonKeys,
} from "./SpaceModel"
import { number } from "fp-ts"
import { PositionablesIndexImpl } from "./PositionablesIndexImpl"
import { CombinedPressedKeyChecker } from "./CombinedPressedKeyChecker"

type EditorV2State = {
    space: Space
    spaceViewer: SpaceViewer
    navigator: Navigator
}

const initialState: EditorV2State = {
    space: {
        positionables: new PositionablesIndexImpl(),
        selector: null,
        transformer: null,
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
                state.navigator.pressedKeys.add(key)
            } else if (keysChecker.checkBecamePressed("left")) {
                // need to test whether down is on selection
                if ((state.space.selector || state.space.transformer ) && action.payload.x) {

                } else if (action.payload.targetId != null) {
                    const positionable = state.space.positionables.getById(action.payload.targetId)
                    if (positionable != null) {
                        state.space.transformer = {
                            element: positionable,
                        }
                    }
                } else {
                    state.navigator.areaSelection = {
                        startX: action.payload.x,
                        startY: action.payload.y,
                    }
                    state.navigator.pressedKeys.add("left")
                    state.space.transformer = null
                    state.space.selector = null
                }
            } else if (keysChecker.checkBecamePressed("right")) {
                state.navigator.pressedKeys.add("right")
            }
        }),
        mouseRelease: create.reducer((state, action: PayloadAction<MouseRelease>) => {
            const key = mouseKeyToKeys(action.payload.key)

            keysChecker.updatePressedKeys(state.navigator.pressedKeys).updateMinusKeys(key)

            if (keysChecker.checkBecameUnpressed("left")) {
                state.navigator.areaSelection = null
                state.spaceViewer.startOffsetX = undefined
                state.spaceViewer.startOffsetY = undefined
            } else if (keysChecker.checkBecameUnpressed('right')) {
                // if there is transformer or selection (and mouse over such elements?), open popup menu
            }

            state.navigator.pressedKeys.delete(key)
        }),
        mouseMove: create.reducer((state, action: PayloadAction<MouseMove>) => {
            keysChecker.updatePressedKeys(state.navigator.pressedKeys)
            const newX = action.payload.newX
            const newY = action.payload.newY

            if (keysChecker.checkArePressed("space", "left")) {
                state.spaceViewer.offsetY = newY - state.spaceViewer.startOffsetY!
                state.spaceViewer.offsetX = newX - state.spaceViewer.startOffsetX!
            } else if (keysChecker.checkArePressed("left") && state.navigator.areaSelection) {
                // update selected figures
                const leftSelect = Math.min(newX, state.navigator.areaSelection!.startX)
                const rightSelect = Math.max(newX, state.navigator.areaSelection!.startX)
                const topSelect = Math.min(newY, state.navigator.areaSelection!.startY)
                const bottomSelect = Math.max(newY, state.navigator.areaSelection!.startY)
                const selectedPositionables = state.space.positionables.getPositionablesInRange(
                    leftSelect,
                    topSelect,
                    rightSelect,
                    bottomSelect,
                )

                if (selectedPositionables.length == 1) {
                    state.space.selector = null
                    state.space.transformer = {
                        element: selectedPositionables[0],
                    }
                } else if (selectedPositionables.length > 1) {
                    state.space.transformer = null
                    state.space.selector = {
                        elements: selectedPositionables,
                        moveX: 0,
                        moveY: 0,
                    }
                }
            } else if (
                keysChecker.checkArePressed("left") &&
                (state.space.selector || state.space.transformer)
            ) {
                // move the items
            }
        }),
        buttonDown: create.reducer((state, action: PayloadAction<ButtonDownPayload>) => {
            keysChecker
                .updatePressedKeys(state.navigator.pressedKeys)
                .updatePlusKeys(action.payload.key)

            if (keysChecker.checkBecamePressed("space", "left")) {
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

function pressedKeys(state: EditorV2State, ...keys: Keys[]) {
    for (const key of keys) {
        if (!state.navigator.pressedKeys.has(key)) {
            return false
        }
    }
    return true
}
import { PayloadAction } from "@reduxjs/toolkit";
import { createAppSlice } from "../../app/createAppSlice";
import { Space, SpaceViewer, Navigator, Keys, MouseKeys, ButtonKeys } from "./SpaceModel";

type EditorV2State {
    space: Space,
    spaceViewer: SpaceViewer,
    navigator: Navigator
}

const initialState : EditorV2State = {
    space: {
        positionables: [],
        selector: null,
        transformer: null
    },
    navigator: {
        areaSelection: null,
        pressedKeys: new Set<Keys>(),
        x: 0,
        y: 0
    },
    spaceViewer: {
        offsetX: 0,
        offsetY: 0 
    }
}

type MouseDownPayload = {
    targetId ?: string
    x: number
    y: number
    key: MouseKeys
}

type MouseRelease = {
    releaseX: number,
    releaseY: number,
    key: MouseKeys
}

type MouseMove = {
    newX: number,
    newY: number
}

type ButtonDownPaylaod = {
    key: ButtonKeys
}

const editorV2Slice = createAppSlice({
    name: "editorv2",
    initialState: initialState,
    reducers: create => ({
        mouseDown: create.reducer((state, action: PayloadAction<MouseDownPayload>) => {
            state.navigator.x = action.payload.x
            state.navigator.y = action.payload.y

            if (action.payload.key == "left") {
                if (action.payload.targetId != null) {
                    const positionable = getPositionableById(state.space, action.payload.targetId)
                    if (positionable != null) {
                        state.space.transformer = {
                            element: positionable
                        }
                    }
                } else {
                    state.navigator.areaSelection = {
                        startX: action.payload.x,
                        startY: action.payload.y
                    }
                    state.navigator.pressedKeys.add("left")
                }
            } else if (action.payload.key == "right") {
                state.navigator.pressedKeys.add("right")
            }
        }),
        mouseRelease: create.reducer((state, action: PayloadAction<MouseRelease>) => {
        }),
        mouseMove: create.reducer((state, action: PayloadAction<MouseMove>) => {

        }),
        buttonDown: create.reducer((state, action: PayloadAction<ButtonDownPaylaod>) => {
            state.navigator.pressedKeys.add(action.payload.key)
        })
    })
})

function getPositionableById(space: Space, id : string) {
    for (const positionable of space.positionables) {
        if (positionable.id === id) {
            return positionable
        }
    }
    return null
}
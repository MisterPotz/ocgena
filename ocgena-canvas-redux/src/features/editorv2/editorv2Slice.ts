import { PayloadAction } from "@reduxjs/toolkit"
import { createAppSlice } from "../../app/createAppSlice"
import {
    Space,
    SpaceViewer,
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
    Shape,
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
    positionables: Shape[]
}

const initialState: EditorV2State = {
    positionables: [],
}

type MouseDownPayload = {
    targetId?: string
    key: MouseKeys
}

type MouseReleasePayload = {
    key: MouseKeys
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
        mouseMove: create.reducer((state, action: PayloadAction<MouseDownPayload>) => {
        }),
        mouseDown: create.reducer((state, action: PayloadAction<MouseDownPayload>) => {
        }),
        mouseRelease: create.reducer((state, action: PayloadAction<MouseReleasePayload>) => {
        }),
        buttonDown: create.reducer((state, action: PayloadAction<ButtonDownPayload>) => {
        }),
        buttonUp: create.reducer((state, action: PayloadAction<ButtonUpPayload>) => {
        }),
    }),
})


export const { mouseDown, buttonDown, buttonUp, mouseRelease, mouseMove } = editorV2Slice.actions

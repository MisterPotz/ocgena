import { EditorState, getClickAreaByPoint, log, ViewerData } from "./EditorV2"
import { ButtonKeys, MouseKeys, Rect } from "./SpaceModel"
import { RBBox } from "./Views"
import _ from "lodash"

interface MouseMoveUpdate {
    x: number
    y: number
}

interface MouseButtonUpdate {
    type: "release" | "down"
    key: MouseKeys
    x: number
    y: number
}

interface ButtonUpdate {
    type: "release" | "down"
    key: ButtonKeys
}

export interface State {
    type: EditorState
    context: Context
    init(state: ViewerData): void
    onMouseKeyChange(state: ViewerData, keyUpdate: MouseButtonUpdate): void
    onMouseMove(state: ViewerData, update: MouseMoveUpdate): void
}

export interface Context {
    searchIntersecting(rect: Rect): string[]
    setState(state: State): void
}

class BaseMouseMoveDelegate {
    onMouseMove(state: ViewerData, update: MouseMoveUpdate) {
        state.x = update.x
        state.y = update.y
    }
}

abstract class BaseState implements State {
    moveDelegate = new BaseMouseMoveDelegate()
    context: Context
    abstract type: EditorState

    constructor(context: Context) {
        this.context = context
    }

    abstract init(state: ViewerData): void
    abstract onMouseKeyChange(state: ViewerData, keyUpdate: MouseButtonUpdate): void
    updateContextState(newState: State, dataState: ViewerData) {
        this.cleanUpState(dataState)
        newState.init(dataState)
        this.context.setState(newState)
    }

    onMouseMove(state: ViewerData, update: MouseMoveUpdate): void {
        this.moveDelegate.onMouseMove(state, update)
    }
    cleanUpState(state: ViewerData): void {

    }
}

export class TrueIdleState extends BaseState {
    type: EditorState = "trueidle"
    constructor(context: Context) {
        super(context)
    }
    init(state: ViewerData): void {
    }

    onMouseKeyChange(state: ViewerData, keyUpdate: MouseButtonUpdate): void {
        switch (keyUpdate.type) {
            case "release":
                break
            case "down":
                switch (keyUpdate.key) {
                    case "left":
                        const itemsPressed = this.context.searchIntersecting(
                            getClickAreaByPoint(keyUpdate.x, keyUpdate.y),
                        )
                        if (itemsPressed.length > 0) {
                            this.updateContextState(
                                new DragState(this.context, itemsPressed, keyUpdate.x, keyUpdate.y),
                                state,
                            )
                        } else if (itemsPressed.length == 0) {
                            // selecting area mode
                            this.updateContextState(
                                new SelectingAreaState(this.context, keyUpdate.x, keyUpdate.y),
                                state,
                            )
                        }
                        break
                    case "right":
                        break
                }
                break
        }
    }
}

export class SelectIdleState extends BaseState {
    type: EditorState = "selectidle"
    selectedItems: string[]

    constructor(context: Context, selectedItems: string[]) {
        super(context)
        this.selectedItems = selectedItems
    }

    init(state: ViewerData): void {
        state.selectorArea = {
            currentlySelected: [...this.selectedItems],
            dragOffsetX: 0,
            dragOffsetY: 0,
            state: {
                type: "idle",
            },
        }
    }

    onMouseKeyChange(state: ViewerData, keyUpdate: MouseButtonUpdate): State {
        const selectorArea = state.selectorArea
        if (!selectorArea) return this

        switch (keyUpdate.type) {
            case "down": {
                switch (keyUpdate.key) {
                    case "left":
                        if (!state.selectorArea) return this

                        const itemsPressed = this.context.searchIntersecting(
                            getClickAreaByPoint(keyUpdate.x, keyUpdate.y),
                        )
                        if (
                            itemsPressed.length > 0 &&
                            !_.isEqual(selectorArea.currentlySelected, itemsPressed)
                        ) {
                            state.selectorArea = {
                                currentlySelected: [...itemsPressed],
                                state: {
                                    type: "idle",
                                },
                            }
                        } else if (itemsPressed.length == 0) {
                            this.updateContextState(
                                new SelectingAreaState(this.context, keyUpdate.x, keyUpdate.y),
                                state,
                            )
                        } else if (
                            itemsPressed.length > 0 &&
                            itemsPressed.some(el => selectorArea.currentlySelected.indexOf(el) > 0)
                        ) {
                            this.updateContextState(
                                new DragState(
                                    this.context,
                                    [...state.selectorArea.currentlySelected],
                                    keyUpdate.x,
                                    keyUpdate.y,
                                ),
                                state,
                            )
                        }
                        break
                    case "right":
                        break
                }
                break
            }
            case "release":
                break
        }
        return this
    }
}

export class DragState extends BaseState {
    type: EditorState = "drag"
    startX: number
    startY: number
    selectingItems: string[]

    constructor(context: Context, selectingItems: string[], startX: number, startY: number) {
        super(context)
        this.startX = startX
        this.startY = startY
        this.selectingItems = selectingItems
    }
    init(state: ViewerData): void {
        // if (!state.selectorArea?.currentlySelected) return
        state.selectorArea = {
            ...state.selectorArea,
            currentlySelected: this.selectingItems,
            state: {
                type: "dragging",
                dragStartX: this.startX,
                dragStartY: this.startY,
            },
        }
    }

    onMouseMove(state: ViewerData, update: MouseMoveUpdate): void {
        super.onMouseMove(state, update)
        if (state.selectorArea && state.selectorArea.state.type === "dragging") {
            state.selectorArea.dragOffsetX = update.x - state.selectorArea.state.dragStartX
            state.selectorArea.dragOffsetY = update.y - state.selectorArea.state.dragStartY
        }
    }

    onMouseKeyChange(state: ViewerData, keyUpdate: MouseButtonUpdate): void {
        switch (keyUpdate.type) {
            case "down":
                break
            case "release": {
                switch (keyUpdate.key) {
                    case "left": {
                        if (state.selectorArea)
                            this.updateContextState(
                                new SelectIdleState(
                                    this.context,
                                    [...state.selectorArea.currentlySelected],
                                ),
                                state,
                            )
                        break
                    }
                    case "right":
                        break
                }
                break
            }
        }
    }
}

export class SelectingAreaState extends BaseState {
    type: EditorState = "selectarea"
    startX: number
    startY: number
    constructor(context: Context, startX: number, startY: number) {
        super(context)
        this.startX = startX
        this.startY = startY
    }

    init(state: ViewerData): void {
        state.selectorArea = {
            currentlySelected: [],
            state: {
                type: "selectingarea",
                selectionDrawStartX: this.startX,
                selectionDrawStartY: this.startY,
            },
        }
    }

    onMouseMove(state: ViewerData, update: MouseMoveUpdate): void {
        super.onMouseMove(state, update)
        if (!state.selectorArea || state.selectorArea.state.type !== "selectingarea") return

        const leftSelect = Math.min(update.x, state.selectorArea.state.selectionDrawStartX)
        const rightSelect = Math.max(update.x, state.selectorArea.state.selectionDrawStartX)
        const topSelect = Math.min(update.y, state.selectorArea.state.selectionDrawStartY)
        const bottomSelect = Math.max(update.y, state.selectorArea.state.selectionDrawStartY)

        const intersectingItems = this.context.searchIntersecting({
            left: leftSelect,
            top: topSelect,
            right: rightSelect,
            bottom: bottomSelect,
        })

        if (!_.isEqual(state.selectorArea.currentlySelected, intersectingItems)) {
            state.selectorArea.currentlySelected = intersectingItems

            log(`selection area: ${intersectingItems.join(", ")}`, "intersection")
        }
    }

    cleanUpState(state: ViewerData): void {
        state.selectorArea = undefined
    }

    onMouseKeyChange(state: ViewerData, keyUpdate: MouseButtonUpdate): State {
        switch (keyUpdate.type) {
            case "release":
                switch (keyUpdate.key) {
                    case "left":
                        if (
                            !!state.selectorArea &&
                            state.selectorArea.currentlySelected.length > 0
                        ) {
                            this.updateContextState(
                                new SelectIdleState(
                                    this.context,
                                    [...state.selectorArea.currentlySelected],
                                ),
                                state
                            )
                        } else {
                            this.updateContextState(
                                new TrueIdleState(this.context),
                                state
                            )
                        }
                        break
                    case "right":
                        break
                }
                break
            case "down":
                break
        }
        return this
    }
}

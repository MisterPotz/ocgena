import { EditorState, getClickAreaByPoint, ViewerData } from "./EditorV2"
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

interface State {
    type: ViewerData["state"]
    init(context: Context, state: ViewerData): void
    onMouseKeyChange(context: Context, state: ViewerData, keyUpdate: MouseButtonUpdate): State
    onMouseMove(state: ViewerData, update: MouseMoveUpdate): State
}

interface Context {
    // selectViews(views: string[] | null)
    searchIntersecting(rect: Rect): string[]
}

class BaseMouseMoveDelegate {
    onMouseMove(state: ViewerData, update: MouseMoveUpdate) {
        state.x = update.x
        state.y = update.y
    }
}

class TrueIdleState implements State {
    type: EditorState = "trueidle"
    moveDelegate = new BaseMouseMoveDelegate()

    onMouseMove(state: ViewerData, update: MouseMoveUpdate): State {
        this.moveDelegate.onMouseMove(state, update)
        return this
    }

    init(context: Context, state: ViewerData): void {
        state.state = "trueidle"
    }

    onMouseKeyChange(context: Context, state: ViewerData, keyUpdate: MouseButtonUpdate): State {
        switch (keyUpdate.type) {
            case "release":
                break
            case "down":
                {
                    switch (keyUpdate.key) {
                        case "left": {
                            // state.selectorArea = {
                            //     currentlySelected: [],
                            //     dragOffsetX: 0,
                            //     dragOffsetY: 0,
                            //     state: {
                            //         type: "idle",
                            //     },
                            // }
                            const itemsPressedAtMain = context.searchIntersecting(
                                getClickAreaByPoint(keyUpdate.x, keyUpdate.y),
                            )
                            if (itemsPressedAtMain.length > 0) {
                                return new SelectIdleState(itemsPressedAtMain)
                            } else {
                                state.selectorArea.state = {
                                    type: "selectingarea",
                                    selectionDrawStartX: keyUpdate.x,
                                    selectionDrawStartY: keyUpdate.y,
                                }
                            }
                            break
                        }
                        case "right":
                            break
                    }
                }
                break
        }
    }
}

class SelectIdleState implements State {
    type: EditorState = "selectidle"
    selectedItems: string[]
    mouseMove = new BaseMouseMoveDelegate()

    constructor(selectedItems: string[]) {
        this.selectedItems = selectedItems
    }

    init(context: Context, state: ViewerData): void {
        state.state = "selectidle"
        state.selectorArea = {
            currentlySelected: [...this.selectedItems],
            dragOffsetX: 0,
            dragOffsetY: 0,
            state: {
                type: "idle",
            },
        }
    }

    onMouseKeyChange(context: Context, state: ViewerData, keyUpdate: MouseButtonUpdate): State {
        const selectorArea = state.selectorArea
        if (!selectorArea) return this

        switch (keyUpdate.type) {
            case "down": {
                switch (keyUpdate.key) {
                    case "left": {
                        const itemsPressed = context.searchIntersecting(
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
                            // selecting area mode
                            return new TrueIdleState()
                        } else if (
                            itemsPressed.length > 0 &&
                            itemsPressed.some(el => selectorArea.currentlySelected.indexOf(el) > 0)
                        ) {
                            return new DragState(keyUpdate.x, keyUpdate.y)
                        }
                        return this
                    }
                    case "right":
                        break
                }
                break
            }
            case "release": {
                switch (keyUpdate.key) {
                    case "left": {
                        break
                    }
                    case "right":
                        break
                }
                break
            }
        }
    }

    onMouseMove(state: ViewerData, update: MouseMoveUpdate): State {
        this.mouseMove.onMouseMove(state, update)
        return this
    }
}

class DragState implements State {
    type: EditorState = "drag"
    moveDelegate = new BaseMouseMoveDelegate()
    startX: number
    startY: number

    constructor(startX: number, startY: number) {
        this.startX = startX
        this.startY = startY
    }
    init(context: Context, state: ViewerData): void {
        if (!state.selectorArea?.currentlySelected) return
        state.selectorArea.state = {
            type: "dragging",
            dragStartX: this.startX,
            dragStartY: this.startY,
        }
    }

    onMouseMove(state: ViewerData, update: MouseMoveUpdate): State {
        this.moveDelegate.onMouseMove(state, update)
        if (state.selectorArea && state.selectorArea.state.type === "dragging") {
            state.selectorArea.dragOffsetX = update.x - state.selectorArea.state.dragStartX
            state.selectorArea.dragOffsetY = update.y - state.selectorArea.state.dragStartY
        }
        return this
    }

    onMouseKeyChange(context: Context, state: ViewerData, keyUpdate: MouseButtonUpdate): State {
        switch (keyUpdate.type) {
            case "down":
                break
            case "release": {
                switch (keyUpdate.key) {
                    case "left": {
                        if (state.selectorArea)
                            return new SelectIdleState(state.selectorArea.currentlySelected)
                        break;
                    }
                    case "right":
                        break
                }
                break
            }
        }
        return this
    }
}

class SelectingAreaState implements State {
    
}
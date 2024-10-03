import deepEquals, {
    InteractionMode,
    ShapeLayerFacade,
    ViewerData,
    getClickAreaByPoint,
    log,
    InteractionModeEvent,
} from "./EditorV2"
import { Keys } from "./SpaceModel"

export class SelectInteractionMode implements InteractionMode {
    type: "panning" | "selection" = "selection"

    mainLayerFacade: ShapeLayerFacade
    selectionLayerFacade: ShapeLayerFacade
    constructor(mainLayerFacade: ShapeLayerFacade, selectionLayerFacade: ShapeLayerFacade) {
        this.mainLayerFacade = mainLayerFacade
        this.selectionLayerFacade = selectionLayerFacade
    }

    private pullElementsFromSelection(state: ViewerData) {
        if (!!state.selectorArea?.currentlySelected) {
            this.selectionLayerFacade.moveItemsTo(
                state.selectorArea.currentlySelected,
                this.mainLayerFacade,
            )
        }
    }
    private pushElementsToSelection(state: ViewerData) {
        if (!!state.selectorArea?.currentlySelected) {
            this.mainLayerFacade.moveItemsTo(
                state.selectorArea.currentlySelected,
                this.selectionLayerFacade,
            )
        }
    }
    private transitionNullState(
        state: ViewerData,
        event: InteractionModeEvent,
    ) {
        if (!state.selectorArea) return

        switch (event.type) {
            case "modeactive": {
                const itemsPressedAtMain = this.mainLayerFacade.searchIntersecting(
                    getClickAreaByPoint(event.x, event.y),
                )
                if (itemsPressedAtMain.length > 0) {
                    state.selectorArea.currentlySelected = [itemsPressedAtMain[0]]
                    this.pushElementsToSelection(state)
                    state.selectorArea.state = {
                        type: "idle",
                    }
                } else {
                    state.selectorArea.state = {
                        type: "selectingarea",
                        selectionDrawStartX: event.x,
                        selectionDrawStartY: event.y,
                    }
                }
                break
            }
        }
    }
    private transitionSelectingAreaState(
        state: ViewerData,
        event: InteractionModeEvent,
    ) {
        if (state.selectorArea?.state.type !== "selectingarea") return

        switch (event.type) {
            case "mousemove": {
                const leftSelect = Math.min(
                    event.newX,
                    state.selectorArea.state.selectionDrawStartX,
                )
                const rightSelect = Math.max(
                    event.newX,
                    state.selectorArea.state.selectionDrawStartX,
                )
                const topSelect = Math.min(
                    event.newY,
                    state.selectorArea.state.selectionDrawStartY,
                )
                const bottomSelect = Math.max(
                    event.newY,
                    state.selectorArea.state.selectionDrawStartY,
                )

                const intersectingItems = this.mainLayerFacade.searchIntersecting({
                    left: leftSelect,
                    top: topSelect,
                    right: rightSelect,
                    bottom: bottomSelect,
                })

                if (!deepEquals(state.selectorArea.currentlySelected, intersectingItems)) {
                    state.selectorArea.currentlySelected = intersectingItems

                    log(
                        `selection area: ${intersectingItems.filter(el => el.id).join(", ")}`,
                        "intersection",
                    )
                }
                break
            }
            case "modeoff": {
                log(
                    `commencing intersection with ${state.selectorArea.currentlySelected.map(el => el.id).join(",")}`,
                    "intersection",
                )
                this.pushElementsToSelection(state)

                state.selectorArea.state = {
                    type: "idle",
                }
            }
            default:
                break
        }
    }

    private transitionIdleState(
        state: ViewerData,
        event: InteractionModeEvent,
    ) {
        if (state.selectorArea?.state?.type !== "idle") return

        switch (event.type) {
            case "modeactive": {
                const itemsAtSelectionLayer = this.selectionLayerFacade.searchIntersecting(
                    getClickAreaByPoint(event.x, event.y),
                )
                if (itemsAtSelectionLayer.length == 0) {
                    this.pullElementsFromSelection(state)
                }
                const itemsPressedAtMain = this.mainLayerFacade.searchIntersecting(
                    getClickAreaByPoint(event.x, event.y),
                )
                if (itemsPressedAtMain.length > 0) {
                    state.selectorArea.currentlySelected = [itemsPressedAtMain[0]]
                    this.pushElementsToSelection(state)
                    state.selectorArea.state = {
                        type: "idle",
                    }
                } else {
                    state.selectorArea = undefined
                }
                break
            }
            default:
                break
        }
    }
    private transitionDragState(
        state: ViewerData,
        event: InteractionModeEvent,
    ) {
        if (state.selectorArea?.state.type !== "dragging") return

        switch (event.type) {
            case "modeactive":
                break
            case "modeoff": {
                state.selectorArea.state = {
                    type: "idle",
                }
                break
            }
            case "mousemove": {
                state.selectorArea.dragOffsetX = event.newX - state.selectorArea.state.dragStartX
                state.selectorArea.dragOffsetY = event.newY - state.selectorArea.state.dragStartY
                break
            }
        }
    }

    transitionToIdleIfCan(state: ViewerData): void {
        switch (state.selectorArea?.state?.type) {
            case "idle":
            case undefined:
            case null:
                break
            case "selectingarea":
            case "dragging": {
                this.pushElementsToSelection(state)

                state.selectorArea.state = {
                    type: "idle",
                }
            }
        }
    }
    
    activationKeys(): Keys[] {
        return ["left"]
    }

    onEvent(state: ViewerData, event: InteractionModeEvent): void {
        switch (state.selectorArea?.state?.type) {
            case "dragging":
                this.transitionDragState(state, event)
                break
            case "idle":
                this.transitionIdleState(state, event)
                break
            case "selectingarea":
                this.transitionSelectingAreaState(state, event)
                break
            case undefined:
            case null:
                this.transitionNullState(state, event)
                break
        }
    }
}

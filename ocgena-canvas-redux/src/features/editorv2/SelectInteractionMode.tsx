import { CombinedPressedKeyChecker } from "./CombinedPressedKeyChecker"
import deepEquals, {
    InteractionMode,
    ShapeLayerFacade,
    ViewerData,
    MouseEventData,
    getClickAreaByPoint,
    log,
    KeyEventData,
    UpdateContext,
} from "./EditorV2"
import { ButtonKeys } from "./SpaceModel"

export class SelectInteractionMode implements InteractionMode {
    type: "panning" | "selection" = "selection"

    mainLayerFacade: ShapeLayerFacade
    selectionLayerFacade: ShapeLayerFacade
    keyChecker: CombinedPressedKeyChecker = new CombinedPressedKeyChecker()
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
        event: MouseEventData | KeyEventData,
        updateContext: UpdateContext,
    ) {
        if (!state.selectorArea) return

        switch (event.type) {
            case "down": {
                const itemsPressedAtMain = this.mainLayerFacade.searchIntersecting(
                    getClickAreaByPoint(event.canvasX, event.canvasY),
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
                        selectionDrawStartX: event.canvasX,
                        selectionDrawStartY: event.canvasY,
                    }
                }
                break
            }
        }
    }
    private transitionSelectingAreaState(
        state: ViewerData,
        event: MouseEventData | KeyEventData,
        updateContext: UpdateContext,
    ) {
        if (state.selectorArea?.state.type !== "selectingarea") return

        switch (event.type) {
            case "move": {
                const leftSelect = Math.min(
                    event.canvasX,
                    state.selectorArea.state.selectionDrawStartX,
                )
                const rightSelect = Math.max(
                    event.canvasX,
                    state.selectorArea.state.selectionDrawStartX,
                )
                const topSelect = Math.min(
                    event.canvasY,
                    state.selectorArea.state.selectionDrawStartY,
                )
                const bottomSelect = Math.max(
                    event.canvasY,
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
            case "release": {
                log(
                    `commencing intersection with ${state.selectorArea.currentlySelected.map(el => el.id).join(",")}`,
                    "intersection",
                )
                this.pushElementsToSelection(state)

                state.selectorArea.state = {
                    type: "idle",
                }
            }
            case "keydown": {
                if (updateContext.keyChecker.checkBecamePressed("space", "left")) {
                    this.pushElementsToSelection(state)

                    state.selectorArea.state = {
                        type: "idle",
                    }
                }
            }
            default:
                break
        }
    }

    private transitionIdleState(
        state: ViewerData,
        event: MouseEventData | KeyEventData,
        updateContext: UpdateContext,
    ) {
        if (state.selectorArea?.state?.type !== "idle") return

        switch (event.type) {
            case "down": {
                const itemsAtSelectionLayer = this.selectionLayerFacade.searchIntersecting(
                    getClickAreaByPoint(event.canvasX, event.canvasY),
                )
                if (itemsAtSelectionLayer.length == 0) {
                    this.pullElementsFromSelection(state)
                }
                const itemsPressedAtMain = this.mainLayerFacade.searchIntersecting(
                    getClickAreaByPoint(event.canvasX, event.canvasY),
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
        event: MouseEventData | KeyEventData,
        updateContext: UpdateContext,
    ) {
        if (state.selectorArea?.state.type !== "dragging") return

        switch (event.type) {
            case "down":
                break
            case "release": {
                state.selectorArea.state = {
                    type: "idle",
                }
                break
            }
            case "move": {
                state.selectorArea.dragOffsetX = event.canvasX - state.selectorArea.state.dragStartX
                state.selectorArea.dragOffsetY = event.canvasY - state.selectorArea.state.dragStartY
                break
            }
            case "keydown": {
                if (updateContext.keyChecker.checkBecamePressed("space", "left")) {
                    state.selectorArea.state = {
                        type: "idle",
                    }
                }
            }
        }
    }

    onMouseEvent(state: ViewerData, event: MouseEventData, updateContext: UpdateContext): void {
        switch (state.selectorArea?.state?.type) {
            case "dragging":
                this.transitionDragState(state, event, updateContext)
                break
            case "idle":
                this.transitionIdleState(state, event, updateContext)
                break
            case "selectingarea":
                this.transitionSelectingAreaState(state, event, updateContext)
                break
            case undefined:
            case null:
                this.transitionNullState(state, event, updateContext)
                break
        }
    }

    onButtonEvent(state: ViewerData, event: KeyEventData, updateContext: UpdateContext): void {
        switch (state.selectorArea?.state?.type) {
            case "dragging":
                this.transitionDragState(state, event, updateContext)
                break
            case "idle":
                this.transitionIdleState(state, event, updateContext)
                break
            case "selectingarea":
                this.transitionSelectingAreaState(state, event, updateContext)
                break
            case undefined:
            case null:
                this.transitionNullState(state, event, updateContext)
                break
        }
    }
}

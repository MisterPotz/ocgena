import { StateObservable } from "redux-observable"
import deepEquals, {
    InteractionMode,
    ViewerData,
    getClickAreaByPoint,
    log,
    InteractionModeEvent,
    nlog,
} from "./EditorV2"
import { Keys } from "./SpaceModel"
import { LayerViewCollection, LayerViewCollectionDelegate, SelectionLayerViewCollection } from "./Views"

export class SelectInteractionMode implements InteractionMode {
    type: "panning" | "selection" = "selection"

    mainLayerFacade: LayerViewCollection
    selectionLayerFacade: SelectionLayerViewCollection
    constructor(mainLayerFacade: LayerViewCollection, selectionLayerFacade: SelectionLayerViewCollection) {
        this.mainLayerFacade = mainLayerFacade
        this.selectionLayerFacade = selectionLayerFacade
    }

    private pullElementsFromSelection(state: ViewerData) {
        if (!!state.selectorArea?.currentlySelected) {
            const removedChildren = this.selectionLayerFacade.removeChildren(state.selectorArea.currentlySelected)
            this.mainLayerFacade.addChildren(removedChildren)   
        }
    }
    private pushElementsToSelection(state: ViewerData) {
        if (!!state.selectorArea?.currentlySelected) {
            const removedChildren = this.mainLayerFacade.removeChildren(state.selectorArea.currentlySelected)
            this.selectionLayerFacade.addChildren(removedChildren)   
        }
    }
    private transitionNullState(
        state: ViewerData,
        event: InteractionModeEvent,
    ) {
        switch (event.type) {
            case "modeactive": {
                state.selectorArea = {
                    currentlySelected: [],
                    dragOffsetX: 0,
                    dragOffsetY: 0,
                    state: {
                        type: 'idle'
                    }
                }
                const itemsPressedAtMain = this.mainLayerFacade.searchIntersecting(
                    // getClickAreaByPoint(event.x, event.y),
                    {
                        left: -1,
                        top: -1,
                        bottom: 1000,
                        right: 1000
                    }
                )
                nlog(['debug'], "itemsPressedAtMain", itemsPressedAtMain, "coordinates", event.x, event.y)
                if (itemsPressedAtMain.length > 0) {
                    state.selectorArea.currentlySelected = [itemsPressedAtMain[0]].map(el => el.id)
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
                    state.selectorArea.currentlySelected = intersectingItems.map(el => el.id)

                    log(
                        `selection area: ${intersectingItems.filter(el => el.id).join(", ")}`,
                        "intersection",
                    )
                }
                break
            }
            case "modeoff": {
                log(
                    `commencing intersection with ${state.selectorArea.currentlySelected.join(",")}`,
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
                    state.selectorArea.currentlySelected = [itemsPressedAtMain[0]].map(el => el.id)
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
        log(`[selectinteractionmode] received ${event.type}`, 'intersection')
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

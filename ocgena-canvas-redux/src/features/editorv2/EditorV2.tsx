import Konva from "konva"
import { useEffect, useRef, useState } from "react"
import { Layer, Rect as KonvaRect, Stage } from "react-konva"
import { ButtonKeys, Keys, MouseKeys, Shape, Rect } from "./SpaceModel"
import RBush, { BBox } from "rbush"
import {
    BehaviorSubject,
    catchError,
    filter,
    fromEvent,
    map,
    merge,
    scan,
    Subject,
    Subscription,
    throttleTime,
} from "rxjs"
import { CombinedPressedKeyChecker } from "./CombinedPressedKeyChecker"
import { produce, produceWithPatches } from "immer"
import { SelectInteractionMode } from "./SelectInteractionMode"
import { ActiveModeDeterminer } from "./ActiveModeDeterminer"
import { prettyPrintJson } from "pretty-print-json"
import _ from "lodash"
import { LayerViewCollectionDelegate, RectangleView, SelectionLayerViewCollection } from "./Views"
import { Context, State, TrueIdleState } from "./EditorStates"

function mouseBtnToKey(button: number): MouseKeys | undefined {
    switch (button) {
        case 0:
            return "left"
        case 2:
            return "right"
        default:
            return undefined
    }
}

function keyboardBtnToKey(button: string): ButtonKeys | null {
    switch (button) {
        case " ":
            return "space"
        default:
            return null
    }
}

export default function deepEquals(object1: any, object2: any) {
    const keys1 = Object.keys(object1)
    const keys2 = Object.keys(object2)
    if (keys1.length !== keys2.length) {
        return false
    }
    for (const key of keys1) {
        const val1 = object1[key]
        const val2 = object2[key]
        const areObjects = isObject(val1) && isObject(val2)
        if ((areObjects && !deepEquals(val1, val2)) || (!areObjects && val1 !== val2)) {
            return false
        }
    }
    return true
}
function isObject(object: Object) {
    return object != null && typeof object === "object"
}

function selectionAreaXStart(viewerData: ViewerData) {
    if (viewerData?.selectorArea?.state?.type !== "selectingarea") return

    return Math.min(viewerData.selectorArea.state.selectionDrawStartX, viewerData.x)
}

function selectionAreaXEnd(viewerData: ViewerData) {
    if (viewerData?.selectorArea?.state?.type !== "selectingarea") return

    return Math.max(viewerData.selectorArea.state.selectionDrawStartX, viewerData.x)
}

function selectionAreaYStart(viewerData: ViewerData) {
    if (viewerData?.selectorArea?.state?.type !== "selectingarea") return

    return Math.min(viewerData.selectorArea.state.selectionDrawStartY, viewerData.y)
}

function selectionAreaYEnd(viewerData: ViewerData) {
    if (viewerData?.selectorArea?.state?.type !== "selectingarea") return

    return Math.max(viewerData.selectorArea.state.selectionDrawStartY, viewerData.y)
}

type SelectingAreaState = {
    selectionDrawStartX: number
    selectionDrawStartY: number
    type: "selectingarea"
}

type DraggingState = {
    dragStartX: number
    dragStartY: number
    type: "dragging"
}

type Idle = {
    type: "idle"
}
type SelectState = SelectingAreaState | DraggingState | Idle

type ViewerSelectorArea = {
    dragOffsetX?: number
    dragOffsetY?: number
    state: SelectState
    currentlySelected: string[]
}

type ViewerOffset = {
    dragStartX?: number
    dragStartY?: number
    offsetX: number
    offsetY: number
}

export type EditorState = "trueidle" | "pan" | "selectidle" | "drag" | "selectarea"

export type ViewerData = {
    x: number
    y: number
    pressedKeys: Keys[]
    selectorArea?: ViewerSelectorArea
    offset: ViewerOffset
    stateDelegate: State
}

export type MouseEventButtonData = {
    canvasX: number
    canvasY: number
    type: "down" | "release"
    button: MouseKeys
}
export type MouseEventMoveData = {
    canvasX: number
    canvasY: number
    type: "move"
}
export type MouseEventData = MouseEventButtonData | MouseEventMoveData

export type KeyEventData = {
    type: "keydown" | "keyrelease"
    button: Keys
}

type StartDragging = {
    type: "startdrag"
}

type ExternalEvent = {
    type: "ext"
}

class SelectionBuffer {
    buffer: Shape[] = []
}

export type UpdateContext = {
    keyChecker: CombinedPressedKeyChecker
}

export type InteractButtonsDownEvent = {
    type: "modeactive"
    x: number
    y: number
}

export type InteractButtonsReleaseEvent = {
    type: "modeoff"
}

export type InteractMouseMoveEvent = {
    type: "mousemove"
    newX: number
    newY: number
}

export type InteractionModeEvent =
    | InteractButtonsDownEvent
    | InteractButtonsReleaseEvent
    | InteractMouseMoveEvent

export type InteractionMode = {
    type: "panning" | "selection"
    activationKeys(): Keys[]
    onEvent(state: ViewerData, event: InteractionModeEvent): void
}

export type InteractionModeType = InteractionMode["type"]

class PanningInteractionMode implements InteractionMode {
    type: "panning" | "selection" = "panning"
    keysChecker: CombinedPressedKeyChecker = new CombinedPressedKeyChecker()
    constructor() {}

    activationKeys(): Keys[] {
        return ["space", "left"]
    }

    onEvent(state: ViewerData, event: InteractionModeEvent): void {
        log(`[paninteractionmode] received ${event.type}`, "pan")

        switch (event.type) {
            case "modeactive": {
                state.offset.dragStartX = event.x + state.offset.offsetX
                state.offset.dragStartY = event.y + state.offset.offsetY
                break
            }
            case "modeoff": {
                state.offset.dragStartX = undefined
                state.offset.dragStartY = undefined
                break
            }
            case "mousemove":
                if (state.offset.dragStartX && state.offset.dragStartY) {
                    state.offset.offsetX = state.offset.dragStartX! - event.newX
                    state.offset.offsetY = state.offset.dragStartY! - event.newY
                }
                break
        }
    }
}

type LogCategories = "buttons" | "intersection" | "pan" | "select" | "debug"
const loggingEvents: LogCategories[] = [
    /* "intersection", "pan", "select" */ "debug",
    "select",
    "intersection",
]
export function log(message: string, ...levels: LogCategories[]) {
    var allLevels = true
    for (const level of levels) {
        if (!loggingEvents.includes(level)) {
            allLevels = false
        }
    }
    if (allLevels) {
        console.log("[", levels.join(", "), "]", message)
    }
}

export function nlog(levels: LogCategories[], message: String, ...others: any[]) {
    var allLevels = true
    for (const level of levels) {
        if (!loggingEvents.includes(level)) {
            allLevels = false
        }
    }
    if (allLevels) {
        console.log("[", levels.join(", "), "]", message, ...others)
    }
    return false
}

export function dlog(message: String, ...others: any[]) {
    nlog(["debug"], message, others)
    return false
}

export function getClickAreaByPoint(x: number, y: number) {
    return {
        left: x - 2,
        top: y - 2,
        right: x + 2,
        bottom: y + 2,
    }
}

export function isMouseEvent(
    mouseEvent: ExternalEvent | MouseEventData | KeyEventData,
): mouseEvent is MouseEventData {
    return mouseEvent.type === "down" || mouseEvent.type === "release" || mouseEvent.type === "move"
}

export function isKeyboardEvent(
    keyboardEvent: ExternalEvent | MouseEventData | KeyEventData,
): keyboardEvent is KeyEventData {
    return keyboardEvent.type === "keydown" || keyboardEvent.type === "keyrelease"
}

function DebugView(props: { viewerData: ViewerData }) {
    const debugRef = useRef<HTMLPreElement | null>(null)
    useEffect(() => {
        debugRef.current!.innerHTML = prettyPrintJson.toHtml(props.viewerData, {
            indent: 3,
            trailingCommas: false,
            quoteKeys: false,
            lineNumbers: false,
        })
    }, [props.viewerData])

    return (
        <>
            <pre className="json-container" ref={debugRef}></pre>
        </>
    )
}

type ViewerDataWithStateMachine = {
    viewerData: ViewerData
    currentState: State
}

// todo debug coordinates and text
class ViewFacade {
    // viewerData = new BehaviorSubject<ViewerData>(this.initialState)
    keysChecker = new CombinedPressedKeyChecker()
    externalEvents = new Subject<ExternalEvent>()
    disposable: Subscription | null = null
    stage: Konva.Stage
    stageContainer: HTMLElement
    mainLayerFacade: LayerViewCollectionDelegate = new LayerViewCollectionDelegate(
        "rbush-debug-main",
    )
    selectionLayerFacade: SelectionLayerViewCollection = new SelectionLayerViewCollection(
        "rbush-debug-selection",
    )
    modes: InteractionMode[] = []
    activeModeDeterminer: ActiveModeDeterminer
    private lastRenderedValue: ViewerData | null = null

    constructor(
        stageContainer: HTMLElement,
        stage: Konva.Stage,
        shapesLayer: Konva.Layer,
        selectionLayer: Konva.Layer,
    ) {
        this.stage = stage
        this.stageContainer = stageContainer
        this.mainLayerFacade.attach(shapesLayer)
        this.selectionLayerFacade.attach(selectionLayer)
        this.mainLayerFacade.addChild(new RectangleView("rect1", 100, 40))
        this.modes = [
            new PanningInteractionMode(),
            new SelectInteractionMode(this.mainLayerFacade, this.selectionLayerFacade),
        ]
        this.activeModeDeterminer = new ActiveModeDeterminer(
            this.modes.map(el => ({ type: el.type, activationKeys: el.activationKeys() })),
        )
    }

    render(oldData: ViewerData | null, newData: ViewerData) {
        if (!_.isEqual(oldData?.selectorArea, newData.selectorArea)) {
            switch (newData.selectorArea?.state?.type) {
                case "dragging":
                    break
                case "selectingarea": {
                    break
                }
                case "idle":
                    break
            }
        }
    }

    start(updateViewerData?: (v: ViewerData) => void) {
        this.disposable?.unsubscribe()

        const subs = new Subscription()

        const context: Context = {
            searchIntersecting: (rect: Rect) => {
                return [
                    ...this.selectionLayerFacade.searchIntersecting(rect),
                    ...this.mainLayerFacade.searchIntersecting(rect),
                ].map(el => el.id)
            },
            setState: (state: State) => {},
        }
        const startState = new TrueIdleState(context)
        const initialState: ViewerData = {
            x: 0,
            y: 0,
            pressedKeys: [],
            offset: {
                offsetX: 0,
                offsetY: 0,
            },
            stateDelegate: startState,
        }

        const viewerData$ = merge(
            this.externalEvents,
            this.mouseDownObservable(),
            this.mouseMoveObservable(),
            this.mouseUpObservable(),
            this.keyDownObservable(),
            this.keyReleaseObservable(),
        )
            .pipe(
                scan((acc, value, idx) => {
                    if (!value) return acc
                    const newState = produce(acc, state => {
                        const viewerDataState = state
                        context.setState = (state: State) => {
                            viewerDataState.stateDelegate = state
                            nlog(
                                ["debug"],
                                "switching state to",
                                state.type,
                                "currently selected",
                                viewerDataState.selectorArea?.currentlySelected,
                            )
                        }
                        this.reduce(state, value, idx)
                    })
                    return newState
                }, initialState),
                map((state: ViewerData) => {
                    if (!!updateViewerData) {
                        updateViewerData(state)
                    }
                    this.render(this.lastRenderedValue, state)
                    this.lastRenderedValue = state
                    return state
                }),
                catchError((err, caught) => {
                    console.log(err)
                    return caught
                }),
            )
            .subscribe()

        subs.add(viewerData$)
        this.disposable = subs
    }

    reduce(state: ViewerData, event: ExternalEvent | MouseEventData | KeyEventData, idx: number) {
        switch (event.type) {
            case "down":
            case "release":
                state.stateDelegate.onMouseKeyChange(state, {
                    key: event.button,
                    type: event.type,
                    x: event.canvasX,
                    y: event.canvasY,
                })
                break
            case "move":
                state.stateDelegate.onMouseMove(state, {
                    x: event.canvasX,
                    y: event.canvasY,
                })
                break
            case "keydown":
            case "keyrelease":
                break
        }
    }

    stop() {
        this.disposable?.unsubscribe()
        this.disposable = null
    }

    private getMouseCoords(mouseEvent: MouseEvent): { x: number; y: number } {
        var { x, y } = this.stageContainer.getBoundingClientRect()

        return {
            x: mouseEvent.clientX - x,
            y: mouseEvent.clientY - y,
        }
    }

    private mouseDownObservable() {
        return fromEvent(this.stage, "mousedown", (evt: MouseEvent) => {
            var { x, y } = this.stageContainer.getBoundingClientRect()

            log(
                `stage coords at x:${x}, y:${y}, clientX:${evt.clientX}, clientY:${evt.clientY}`,
                "buttons",
            )
            x = evt.clientX - x
            y = evt.clientY - y
            // const { x, y } = this.getMouseCoords(evt)
            const btn = mouseBtnToKey(evt.button)
            if (!btn) return
            log(`mouse down ${btn} at x:${x}, y:${y},`, "buttons")
            const mouseEvent: MouseEventData = {
                canvasX: x,
                canvasY: y,
                type: "down",
                button: btn,
            }
            return mouseEvent
        }).pipe(filter(el => !!el))
    }

    private mouseMoveObservable() {
        return fromEvent(this.stage, "mousemove", (evt: MouseEvent) => {
            const { x, y } = this.getMouseCoords(evt)

            // dlog(`x:${x},y:${y}`)
            const mouseEvent: MouseEventData = {
                canvasX: x,
                canvasY: y,
                type: "move",
            }
            return mouseEvent
        }).pipe(throttleTime(10))
    }

    private mouseUpObservable() {
        return fromEvent(this.stage, "mouseup", (evt: MouseEvent) => {
            const { x, y } = this.getMouseCoords(evt)
            const btn = mouseBtnToKey(evt.button)
            if (!btn) return
            log(`mouse release ${btn}`, "buttons")

            const mouseEvent: MouseEventData = {
                canvasX: x,
                canvasY: y,
                type: "release",
                button: btn,
            }
            return mouseEvent
        })
    }

    private keyDownObservable() {
        return fromEvent(window, "keydown", (evt: KeyboardEvent) => {
            const key: ButtonKeys | null = keyboardBtnToKey(evt.key)
            if (key) {
                evt.preventDefault()
                evt.cancelable
                if (evt.repeat) return
                log(`key down ${key}`, "buttons")
                const keyEventData: KeyEventData = {
                    button: key,
                    type: "keydown",
                }
                return keyEventData
            }
            return null
        }).pipe(filter(el => el != null))
    }

    private keyReleaseObservable() {
        return fromEvent(window, "keyup", (evt: KeyboardEvent) => {
            const key = keyboardBtnToKey(evt.key)
            if (key) {
                log(`key up ${key}`, "buttons")
                const keyEventData: KeyEventData = {
                    button: key,
                    type: "keyrelease",
                }
                return keyEventData
            }
            return null
        }).pipe(filter(el => el != null))
    }
}

export function EditorV2() {
    const stage = useRef<Konva.Stage | null>(null)
    const mainLayer = useRef<Konva.Layer | null>(null)
    const selectionLayer = useRef<Konva.Layer | null>(null)
    const viewFacade = useRef<ViewFacade | null>(null)
    const stageParent = useRef<HTMLDivElement | null>(null)
    const [viewerData, setViewerData] = useState<ViewerData>()

    useEffect(() => {
        if (stage.current && mainLayer.current && selectionLayer.current && stageParent.current) {
            viewFacade.current = new ViewFacade(
                stageParent.current,
                stage.current,
                mainLayer.current,
                selectionLayer.current,
            )

            viewFacade.current.start(el => {
                setViewerData(el)
            })
            console.log("started view facade")

            return () => {
                viewFacade.current?.stop()
                console.log("stopped view facade")
            }
        }
    }, [stageParent.current, stage.current, mainLayer.current, selectionLayer.current])

    return (
        <>
            <div style={{ width: "100%" }}>
                <div
                    style={{
                        width: "100%",
                        display: "flex",
                        flexDirection: "row",
                        alignItems: "start",
                        justifyContent: "flex-start",
                    }}
                >
                    <div ref={stageParent} style={{ position: "relative" }}>
                        <Stage
                            style={{
                                border: "solid",
                                borderWidth: "1px",
                            }}
                            ref={stage}
                            width={800}
                            height={600}
                        >
                            <PatternBackground
                                offsetX={viewerData?.offset?.offsetX ?? 0}
                                offsetY={viewerData?.offset?.offsetY ?? 0}
                            />
                            <Layer ref={mainLayer} listening={false} />
                            <Layer ref={selectionLayer} listening={false}>
                                
                                {viewerData?.selectorArea?.state?.type === "selectingarea" && (
                                    <KonvaRect
                                        id="selection"
                                        x={selectionAreaXStart(viewerData)}
                                        y={selectionAreaYStart(viewerData)}
                                        width={
                                            selectionAreaXEnd(viewerData)! -
                                            selectionAreaXStart(viewerData)!
                                        }
                                        height={
                                            selectionAreaYEnd(viewerData)! -
                                            selectionAreaYStart(viewerData)!
                                        }
                                        stroke={'#239EF4'}
                                    />
                                )}
                            </Layer>
                        </Stage>
                    </div>
                    <div
                        id="debugging"
                        // ref={debuggingText}
                        style={{ fontSize: "1rem", textAlign: "start", paddingBottom: "10px" }}
                    >
                        {!!viewerData ? <DebugView viewerData={viewerData} /> : <></>}
                    </div>
                </div>

                <div
                    style={{
                        width: "100%",
                        alignContent: "start",
                        alignItems: "start",
                        justifyContent: "start",
                        textAlign: "start",
                    }}
                >
                    <h5>Main layer rbush</h5>
                    <pre id="rbush-debug-main" className="json-container"></pre>
                </div>

                <div
                    style={{
                        width: "100%",
                        alignContent: "start",
                        alignItems: "start",
                        justifyContent: "start",
                        textAlign: "start",
                    }}
                >
                    <h5>Selection layer rbush</h5>
                    <pre id="rbush-debug-selection" className="json-container"></pre>
                </div>
            </div>
        </>
    )
}

function KonvaDiagram(props : { bBoxes: BlackBox[]}) {
    return <Layer>
        {props.bBoxes.map(el => el.createReactNode())}
    </Layer>=
}

function PatternBackground(props: { offsetX: number; offsetY: number }) {
    const [patternImage, setPatternImage] = useState(new window.Image())

    const { offsetX, offsetY } = props

    useEffect(() => {
        const dotPatternCanvas = document.createElement("canvas")
        const context = dotPatternCanvas.getContext("2d")!

        const spacing = 20 // Spacing between dots
        const dotRadius = 1 // Radius of the dots

        // Set the canvas size to the size of the pattern
        dotPatternCanvas.width = spacing
        dotPatternCanvas.height = spacing

        // Draw the dot pattern once
        context.beginPath()
        context.arc(spacing / 2, spacing / 2, dotRadius, 0, 2 * Math.PI)
        context.fillStyle = "black"
        context.fill()
        // Convert canvas to an image
        const pattern = new Image()
        pattern.src = dotPatternCanvas.toDataURL()
        pattern.onload = () => {
            setPatternImage(pattern)
        }
    }, [])

    return (
        <>
            <Layer>
                <KonvaRect
                    draggable={false}
                    listening={false}
                    x={0}
                    y={0}
                    width={1200}
                    height={800}
                    fillPatternImage={patternImage}
                    fillPatternOffset={{
                        x: offsetX,
                        y: offsetY,
                    }}
                    fillPatternRepeat="repeat"
                />
            </Layer>
        </>
    )
}

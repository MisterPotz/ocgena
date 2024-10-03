import Konva from "konva"
import { useEffect, useRef, useState } from "react"
import { Layer, Rect, Stage } from "react-konva"
import { useAppDispatch, useAppSelector } from "../../app/hooks"
import {
    buttonDown,
    buttonUp,
    editorV2Slice,
    mouseDown,
    mouseMove,
    mouseRelease,
} from "./editorv2Slice"
import {
    ButtonKeys,
    Circle,
    Keys,
    MouseKeys,
    Positionable,
    PositionableShape,
    Rectangle,
} from "./SpaceModel"
import RBush, { BBox } from "rbush"
import { ShapeConfig, shapes } from "konva/lib/Shape"
import {
    BehaviorSubject,
    filter,
    fromEvent,
    merge,
    Observable,
    scan,
    Subject,
    Subscription,
    zip,
} from "rxjs"
import { KonvaEventObject } from "konva/lib/Node"
import { state, string } from "fp-ts"
import { CombinedPressedKeyChecker } from "./CombinedPressedKeyChecker"
import { produce } from "immer"
import { SelectInteractionMode } from "./SelectInteractionMode"
import { aC } from "vitest/dist/reporters-P7C2ytIv.js"

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

class PositionableShapeRBush extends RBush<PositionableShape> {
    toBBox(shape: PositionableShape): BBox {
        switch (shape.type) {
            case "circle": {
                return {
                    minX: shape.x - shape.radius,
                    maxX: shape.x + shape.radius,
                    minY: shape.y - shape.radius,
                    maxY: shape.y + shape.radius,
                }
            }
            case "rectangle": {
                return {
                    minX: shape.left,
                    maxX: shape.left + shape.width,
                    minY: shape.top,
                    maxY: shape.top + shape.height,
                }
            }
        }
    }

    compareMinX(a: PositionableShape, b: PositionableShape): number {
        return this.minX(a) - this.minX(b)
    }

    compareMinY(a: PositionableShape, b: PositionableShape): number {
        return this.minY(a) - this.minY(b)
    }

    minX(a: PositionableShape) {
        switch (a.type) {
            case "circle": {
                return a.x - a.radius
            }
            case "rectangle": {
                return a.left
            }
        }
    }

    minY(a: PositionableShape) {
        switch (a.type) {
            case "circle": {
                return a.y - a.radius
            }
            case "rectangle": {
                return a.top
            }
        }
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
function test() {}
function isObject(object: Object) {
    return object != null && typeof object === "object"
}

class ShapesRepository {
    tree = new PositionableShapeRBush()
    layer = new Konva.Layer({ listening: false })
    idToItem: Map<String, PositionableShape> = new Map()

    // setupListeners() {
    //     // this.layer.addevenaddEventListener()
    // }

    addElements(shapes: PositionableShape[]) {
        for (const shape of shapes) {
            this.idToItem.set(shape.id, shape)
        }
        this.tree.load(shapes)
    }

    addElement(shape: PositionableShape) {
        this.idToItem.set(shape.id, shape)
        this.tree.insert(shape)
    }

    removeElement(shape: PositionableShape) {
        const item = this.idToItem.get(shape.id)
        if (!!item) {
            this.idToItem.delete(shape.id)
            this.tree.remove(shape)
        }
    }

    removeElements(shapes: PositionableShape[]) {
        for (const shape of shapes) {
            this.removeElement(shape)
        }
    }

    updateElement(shape: PositionableShape) {
        const prevVersion = this.idToItem.get(shape.id)

        if (!!prevVersion) {
            this.tree.remove(prevVersion)
        }

        this.idToItem.set(shape.id, shape)
        this.tree.insert(shape)
    }

    updateElements(shapes: PositionableShape[]) {
        for (const shape of shapes) {
            const prevVersion = this.idToItem.get(shape.id)

            if (!!prevVersion) {
                this.tree.remove(prevVersion)
            }
            this.idToItem.set(shape.id, shape)
        }
        this.tree.load(shapes)
    }

    searchIntersecting(
        left: number,
        top: number,
        right: number,
        bottom: number,
    ): PositionableShape[] {
        return this.tree.search({
            minY: top,
            maxY: bottom,
            minX: left,
            maxX: right,
        })
    }
}

export class ShapeLayerFacade {
    viewDelegate: ShapesViewDelegate
    repository: ShapesRepository

    constructor(viewUpdater: ShapesViewDelegate, repository: ShapesRepository) {
        this.viewDelegate = viewUpdater
        this.repository = repository
    }

    addElements(shapes: PositionableShape[]) {
        this.viewDelegate.addElements(shapes)
        this.repository.addElements(shapes)
    }

    removeElements(shapes: PositionableShape[]) {
        this.viewDelegate.removeElements(shapes)
        this.repository.removeElements(shapes)
    }

    updateElements(shapes: PositionableShape[]) {
        this.repository.updateElements(shapes)
        this.viewDelegate.updateElements(shapes)
    }

    // searchIntersecting(left: number, top: number, right: number, bottom: number) {
    //     return this.repository.searchIntersecting(left, top, right, bottom)
    // }

    searchIntersecting(area: { left: number; top: number; right: number; bottom: number }) {
        return this.repository.searchIntersecting(area.left, area.top, area.right, area.bottom)
    }

    moveItemsTo(shapes: PositionableShape[], dst: ShapeLayerFacade) {
        this.removeElements(shapes)
        dst.addElements(shapes)
    }
}

class ShapesViewDelegate {
    layer: Konva.Layer

    constructor(elementsLayer: Konva.Layer) {
        this.layer = elementsLayer
    }

    addElements(shapes: PositionableShape[]) {
        // this.repository.addElements(shapes)
        for (const shape of shapes) {
            this.layer.add(this.createKonvaNode(shape))
        }
    }

    removeElements(shapes: PositionableShape[]) {
        // this.repository.removeElements(shapes)
        for (const shape of shapes) {
            const child = this.getKonvaNode(shape)
            if (!!child) {
                child.remove()
            }
        }
    }

    updateElements(shapes: PositionableShape[]) {
        // this.repository.updateElements(shapes)

        for (const shape of shapes) {
            const child = this.getKonvaNode(shape)
            if (!!child) {
                this.updateKonvaNode(child, shape)
            }
        }
    }

    nodeAsCircle(node: Konva.Node): node is Konva.Circle {
        return true
    }

    nodeAsRect(node: Konva.Node): node is Konva.Rect {
        return true
    }

    updateKonvaNode(konvaNode: Konva.Shape, shape: PositionableShape) {
        switch (shape.type) {
            case "circle": {
                if (this.nodeAsCircle(konvaNode)) {
                    konvaNode.setAttrs({
                        x: shape.x,
                        y: shape.y,
                        radius: shape.radius,
                    })
                }
                break
            }
            case "rectangle": {
                if (this.nodeAsRect(konvaNode)) {
                    konvaNode.setAttrs({
                        x: shape.left,
                        y: shape.top,
                        width: shape.width,
                        height: shape.height,
                    })
                }
            }
        }
    }

    getKonvaNode(shape: PositionableShape): Konva.Shape | null {
        const child = this.layer.getChildren(el => el.id() == shape.id)
        if (!!child && child.length > 0) {
            return child[0] as Konva.Shape
        }
        return null
    }

    private createKonvaNode(shape: PositionableShape): Konva.Shape {
        switch (shape.type) {
            case "circle": {
                return new Konva.Circle({
                    id: shape.id,
                    x: shape.x,
                    y: shape.y,
                    radius: shape.radius,
                })
            }
            case "rectangle": {
                return new Konva.Rect({
                    x: shape.left,
                    y: shape.top,
                    width: shape.width,
                    height: shape.height,
                    id: shape.id,
                })
            }
        }
    }

    private createPositionable(shape: PositionableShape): Positionable {
        switch (shape.type) {
            case "circle": {
                return new Circle(shape.id, shape.radius)
            }
            case "rectangle": {
                return new Rectangle(shape.id, shape.width, shape.height)
            }
        }
    }
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
    dragOffsetX: number
    dragOffsetY: number
    state: SelectState
    currentlySelected: PositionableShape[]
}

type ViewerOffset = {
    dragStartX?: number
    dragStartY?: number
    offsetX: number
    offsetY: number
}

export type ViewerData = {
    x: number
    y: number
    pressedKeys: Set<Keys>
    selectorArea?: ViewerSelectorArea
    offset: ViewerOffset
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
    button: ButtonKeys
}

type StartDragging = {
    type: "startdrag"
}

type ExternalEvent = {
    type: "ext"
}

class SelectionBuffer {
    buffer: PositionableShape[] = []
}

export type UpdateContext = {
    keyChecker: CombinedPressedKeyChecker
}
export type InteractionMode = {
    type: "panning" | "selection"
    onMouseEvent(state: ViewerData, event: MouseEventData, updateContext: UpdateContext): void
    onButtonEvent(state: ViewerData, event: KeyEventData, updateContext: UpdateContext): void
}

class PanningInteractionMode implements InteractionMode {
    type: "panning" | "selection" = "panning"
    keysChecker: CombinedPressedKeyChecker = new CombinedPressedKeyChecker()
    constructor() {}
    private onMouseMove(state: ViewerData, event: MouseEventData): void {
        if (state.offset.dragStartX && state.offset.dragStartY) {
            state.offset.offsetX = event.canvasX - state.offset.dragStartX!
            state.offset.offsetY = event.canvasY - state.offset.dragStartY!
        }
    }
    private onMouseKeyDown(state: ViewerData, event: MouseEventData): void {
        state.offset.dragStartX = event.canvasX
        state.offset.dragStartY = event.canvasY
    }
    private onMouseKeyRelease(state: ViewerData, event: MouseEventData): void {
        state.offset.dragStartX = undefined
        state.offset.dragStartY = undefined
    }

    onMouseEvent(state: ViewerData, event: MouseEventData): void {
        switch (event.type) {
            case "down":
                this.onMouseKeyDown(state, event)
                break
            case "release":
                this.onMouseKeyRelease(state, event)
                break
            case "move":
                this.onMouseMove(state, event)
                break
        }
    }

    onButtonEvent(state: ViewerData, event: KeyEventData): void {
        switch (event.type) {
            case "keydown": {
                this.keysChecker.updatePressedKeys(state.pressedKeys).updatePlusKeys(event.button)

                if (this.keysChecker.checkBecamePressed("space", "left")) {
                    state.offset.dragStartX = state.x
                    state.offset.dragStartY = state.y
                }
                break
            }
            case "keyrelease": {
                this.keysChecker
                    .updatePressedKeys(state.pressedKeys)
                    .checkBecameUnpressed(event.button)

                if (this.keysChecker.checkBecameUnpressed("space", "left")) {
                    state.offset.dragStartX = undefined
                    state.offset.dragStartY = undefined
                }
                break
            }
        }
    }
}

type LogCategories = "buttons" | "intersection"
const loggingEvents: LogCategories[] = ["buttons", "intersection"]
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

export function getClickAreaByPoint(x: number, y: number) {
    return {
        left: x - 2,
        top: x - 2,
        right: x + 2,
        bottom: x + 2,
    }
}

// todo encapsulate the updates into 'Mode' class (selection / panning)
class ViewFacade {
    initialState: ViewerData = {
        x: 0,
        y: 0,
        pressedKeys: new Set<ButtonKeys>(),
        offset: {
            offsetX: 0,
            offsetY: 0,
        },
    }
    viewerData = new BehaviorSubject<ViewerData>(this.initialState)
    keysChecker = new CombinedPressedKeyChecker()
    externalEvents = new Subject<ExternalEvent>()
    disposable: Subscription | null = null
    stage: Konva.Stage
    mainLayerFacade: ShapeLayerFacade
    selectionLayerFacade: ShapeLayerFacade
    panInteractionMode = new PanningInteractionMode()
    selectInteractionMode: SelectInteractionMode
    activeInteractionMode: InteractionMode | null = null
    keysToMods: { [key: string]: { keys: Keys[]; mode: InteractionMode } }
    modes : InteractionMode[] = []

    constructor(stage: Konva.Stage, shapesLayer: Konva.Layer, selectionLayer: Konva.Layer) {
        this.stage = stage
        const shapesLayerRepository = new ShapesRepository()
        const selectionLayerRepository = new ShapesRepository()
        const shapesView = new ShapesViewDelegate(shapesLayer)
        const selectionView = new ShapesViewDelegate(selectionLayer)
        this.mainLayerFacade = new ShapeLayerFacade(shapesView, shapesLayerRepository)
        this.selectionLayerFacade = new ShapeLayerFacade(selectionView, selectionLayerRepository)
        this.selectInteractionMode = new SelectInteractionMode(
            this.mainLayerFacade,
            this.selectionLayerFacade,
        )
        this.keysToMods = {
            panning: { keys: ["space", "left"], mode: this.panInteractionMode },
            selection: { keys: ["left"], mode: this.selectInteractionMode },
        }
        this.modes = [this.panInteractionMode, this.selectInteractionMode]
    }

    start() {
        this.disposable?.unsubscribe()
        this.disposable = merge(
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
                    return produce(acc, state => {
                        this.reduce(state, value, idx)
                    })
                }, this.initialState),
            )
            .subscribe(this.viewerData)
    }

    isMouseEvent(
        mouseEvent: ExternalEvent | MouseEventData | KeyEventData,
    ): mouseEvent is MouseEventData {
        return (
            mouseEvent.type === "down" ||
            mouseEvent.type === "release" ||
            mouseEvent.type === "move"
        )
    }

    isKeyboardEvent(
        keyboardEvent: ExternalEvent | MouseEventData | KeyEventData,
    ): keyboardEvent is KeyEventData {
        return (
            keyboardEvent.type === "keydown" ||
            keyboardEvent.type === "keyrelease"
        )
    }

    reduce(state: ViewerData, event: ExternalEvent | MouseEventData | KeyEventData, idx: number) {
        this.keysChecker.updatePressedKeys(state.pressedKeys)
        switch (event.type) {
            case "down": this.keysChecker.updatePlusKeys(event.button); break;
            case "release": state.pressedKeys.delete(event.button); break;
            case 'keydown': state.pressedKeys.add(event.button); break;
            case 'keyrelease': state.pressedKeys.delete(event.button); break;
            default: {
                break
            }
        }

        if (this.isMouseEvent(event)) {
            this.modes.forEach(el => el.onMouseEvent(state, event))
        } else if (this.isKeyboardEvent(event)) {
            this.modes.forEach(el => el.onButtonEvent(state, event))
        }
        switch (event.type) {
            case "move": {
                state.x = event.canvasX
                state.y = event.canvasY
                break
            }
            case "down": state.pressedKeys.add(event.button); break;
            case "release": state.pressedKeys.delete(event.button); break;
            case 'keydown': state.pressedKeys.add(event.button); break;
            case 'keyrelease': state.pressedKeys.delete(event.button); break;
            default: {
                break
            }
        }
    }

    stop() {
        this.disposable?.unsubscribe()
        this.disposable = null
    }

    private getMouseCoords(mouseEvent: MouseEvent): { x: number; y: number } {
        const { x, y } = this.stage.getClientRect({ skipTransform: true })

        return {
            x: mouseEvent.clientX - x,
            y: mouseEvent.clientY - y,
        }
    }

    private mouseDownObservable() {
        return fromEvent(this.stage, "mousedown", (evt: KonvaEventObject<MouseEvent>) => {
            const { x, y } = this.getMouseCoords(evt.evt)
            const btn = mouseBtnToKey(evt.evt.button)
            if (!btn) return
            log(`mouse down ${btn}`, "buttons")
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
        return fromEvent(this.stage, "mousemove", (evt: KonvaEventObject<MouseEvent>) => {
            const { x, y } = this.getMouseCoords(evt.evt)
            const mouseEvent: MouseEventData = {
                canvasX: x,
                canvasY: y,
                type: "move",
            }
            return mouseEvent
        })
    }

    private mouseUpObservable() {
        return fromEvent(this.stage, "mouseup", (evt: KonvaEventObject<MouseEvent>) => {
            const { x, y } = this.getMouseCoords(evt.evt)
            const btn = mouseBtnToKey(evt.evt.button)
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
    const dispatch = useAppDispatch()

    useEffect(() => {
        if (stage.current && mainLayer.current && selectionLayer.current) {
            viewFacade.current = new ViewFacade(
                stage.current,
                mainLayer.current,
                selectionLayer.current,
            )

            viewFacade.current.start()

            return () => {
                viewFacade.current?.stop()
            }
        }
    }, [stage.current, mainLayer.current, , selectionLayer.current])

    return (
        <>
            <Stage
                style={{
                    border: "solid",
                    borderWidth: "1px",
                }}
                ref={stage}
                width={1200}
                height={800}
            >
                <PatternBackground />
                <Layer ref={mainLayer} />
                <Layer ref={selectionLayer} />
                {/* <Layer ref={elementsLayerRef}>
              {elements.map((el, index) => {
                return (
                  <AutoSizeTextShape
                    key={el.id}
                    element={el}
                    updatePosition={payload => {
                      dispatch(elementPositionUpdate(payload))
                    }}
                  />
                )
              })}
            </Layer> */}
            </Stage>
        </>
    )
}

function PatternBackground() {
    const [patternImage, setPatternImage] = useState(new window.Image())

    const offsetX = useAppSelector(state => state.editorv2.spaceViewer.offsetX)
    const offsetY = useAppSelector(state => state.editorv2.spaceViewer.offsetY)

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
                <Rect
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

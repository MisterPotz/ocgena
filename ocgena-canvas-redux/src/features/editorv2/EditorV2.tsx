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
import { string } from "fp-ts"
import { CombinedPressedKeyChecker } from "./CombinedPressedKeyChecker"
import { produce } from "immer"

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
function test() {
    
}
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

class ShapeLayerFacade {
    viewUpdater: ShapesViewUpdater
    repository: ShapesRepository

    constructor(viewUpdater: ShapesViewUpdater, repository: ShapesRepository) {
        this.viewUpdater = viewUpdater
        this.repository = repository
    }

    addElements(shapes: PositionableShape[]) {
        this.viewUpdater.addElements(shapes)
        this.repository.addElements(shapes)
    }

    removeElements(shapes: PositionableShape[]) {
        this.viewUpdater.removeElements(shapes)
        this.repository.removeElements(shapes)
    }

    updateElements(shapes: PositionableShape[]) {
        this.repository.updateElements(shapes)
        this.viewUpdater.updateElements(shapes)
    }

    // searchIntersecting(left: number, top: number, right: number, bottom: number) {
    //     return this.repository.searchIntersecting(left, top, right, bottom)
    // }

    searchIntersecting(area: { left: number; top: number; right: number; bottom: number }) {
        return this.repository.searchIntersecting(area.left, area.top, area.right, area.bottom)
    }
}

class ShapesViewUpdater {
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

type ViewerSelectorArea = {
    selectionDrawStartX?: number
    selectionDrawStartY?: number
    dragStartX?: number
    dragStartY?: number
    dragOffsetX?: number
    dragOffsetY?: number
    currentlySelected: PositionableShape[]
}

type ViewerOffset = {
    dragStartX?: number
    dragStartY?: number
    offsetX: number
    offsetY: number
}

type ViewerData = {
    x: number
    y: number
    pressedKeys: Set<Keys>
    selectorArea?: ViewerSelectorArea
    offset: ViewerOffset
}

type MouseEventData = {
    canvasX: number
    canvasY: number
    type: "down" | "release" | "move"
    button?: MouseKeys
}

type KeyEventData = {
    type: "keydown" | "keyrelease"
    button?: ButtonKeys
}

type StartDragging = {
    type: "startdrag"
}

type ExternalEvent = {
    type: "ext"
}

type LogCategories = "buttons" | "intersection"

class SelectionBuffer {
    buffer: PositionableShape[] = []
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
    loggingEvents: LogCategories[] = ["buttons"]

    constructor(stage: Konva.Stage, shapesLayer: Konva.Layer, selectionLayer: Konva.Layer) {
        this.stage = stage
        const shapesLayerRepository = new ShapesRepository()
        const selectionLayerRepository = new ShapesRepository()
        const shapesView = new ShapesViewUpdater(shapesLayer)
        const selectionView = new ShapesViewUpdater(selectionLayer)
        this.mainLayerFacade = new ShapeLayerFacade(shapesView, shapesLayerRepository)
        this.selectionLayerFacade = new ShapeLayerFacade(selectionView, selectionLayerRepository)
    }

    mouseDownObservable() {
        return fromEvent(this.stage, "mousedown", (evt: KonvaEventObject<MouseEvent>) => {
            const { x, y } = this.getMouseCoords(evt.evt)
            const btn = mouseBtnToKey(evt.evt.button)
            this.log(`mouse down ${btn}`, "buttons")
            const mouseEvent: MouseEventData = {
                canvasX: x,
                canvasY: y,
                type: "down",
                button: btn,
            }
            return mouseEvent
        })
    }

    mouseMoveObservable() {
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

    mouseUpObservable() {
        return fromEvent(this.stage, "mouseup", (evt: KonvaEventObject<MouseEvent>) => {
            const { x, y } = this.getMouseCoords(evt.evt)
            const btn = mouseBtnToKey(evt.evt.button)
            this.log(`mouse release ${btn}`, "buttons")

            const mouseEvent: MouseEventData = {
                canvasX: x,
                canvasY: y,
                type: "release",
                button: btn,
            }
            return mouseEvent
        })
    }

    keyDownObservable() {
        return fromEvent(window, "keydown", (evt: KeyboardEvent) => {
            const key: ButtonKeys | null = keyboardBtnToKey(evt.key)
            if (key) {
                evt.preventDefault()
                this.log(`key down ${key}`, "buttons")
                const keyEventData: KeyEventData = {
                    button: key,
                    type: "keydown",
                }
                return keyEventData
            }
            return null
        }).pipe(filter(el => el != null))
    }

    keyReleaseObservable() {
        return fromEvent(window, "keyup", (evt: KeyboardEvent) => {
            const key = keyboardBtnToKey(evt.key)
            if (key) {
                this.log(`key up ${key}`, "buttons")
                const keyEventData: KeyEventData = {
                    button: key,
                    type: "keyrelease",
                }
                return keyEventData
            }
            return null
        }).pipe(filter(el => el != null))
    }

    log(message: string, ...levels: LogCategories[]) {
        var allLevels = true
        for (const level of levels) {
            if (!this.loggingEvents.includes(level)) {
                allLevels = false
            }
        }
        if (allLevels) {
            console.log("[", levels.join(", "), "]", message)
        }
    }

    start() {
        merge(
            this.externalEvents,
            this.mouseDownObservable(),
            this.mouseMoveObservable(),
            this.mouseUpObservable(),
            this.keyDownObservable(),
            this.keyReleaseObservable(),
        )
            .pipe(
                scan((acc, value, idx) => {
                    return produce(acc, state => {
                        this.reduce(state, value, idx)
                    })
                }, this.initialState),
            )
            .subscribe(this.viewerData)
    }

    reduce(state: ViewerData, event: ExternalEvent | MouseEventData | KeyEventData, idx: number) {
        switch (event.type) {
            case "move": {
                this.keysChecker.updatePressedKeys(state.pressedKeys)
                state.x = event.canvasX
                state.y = event.canvasY

                if (this.keysChecker.checkArePressed("space", "left")) {
                    if (state.offset.dragStartX && state.offset.dragStartY) {
                        state.offset.offsetX = event.canvasX - state.offset.dragStartX!
                        state.offset.offsetY = event.canvasY - state.offset.dragStartY!
                    }
                } else if (this.keysChecker.checkArePressed("left")) {
                    if (
                        state.selectorArea &&
                        state.selectorArea.selectionDrawStartX &&
                        state.selectorArea.selectionDrawStartY
                    ) {
                        const leftSelect = Math.min(
                            event.canvasX,
                            state.selectorArea.selectionDrawStartX,
                        )
                        const rightSelect = Math.max(
                            event.canvasX,
                            state.selectorArea.selectionDrawStartX,
                        )
                        const topSelect = Math.min(
                            event.canvasY,
                            state.selectorArea.selectionDrawStartY,
                        )
                        const bottomSelect = Math.max(
                            event.canvasY,
                            state.selectorArea.selectionDrawStartY,
                        )

                        const intersectingItems = this.mainLayerFacade.searchIntersecting({
                            left: leftSelect,
                            top: topSelect,
                            right: rightSelect,
                            bottom: bottomSelect,
                        })

                        if (!deepEquals(state.selectorArea.currentlySelected, intersectingItems)) {
                            state.selectorArea.currentlySelected = intersectingItems

                            this.log(
                                `selection area: ${intersectingItems.filter(el => el.id).join(", ")}`,
                                "intersection",
                            )
                        }
                    } else if (
                        state.selectorArea &&
                        state.selectorArea.dragStartX &&
                        state.selectorArea.dragStartY
                    ) {
                        state.selectorArea.dragOffsetX =
                            event.canvasX - state.selectorArea.dragStartX
                        state.selectorArea.dragOffsetY =
                            event.canvasY - state.selectorArea.dragStartY
                    }
                }
                break;
            }
            case "down": {
                if (!event.button) return
                this.keysChecker.updatePressedKeys(state.pressedKeys).updatePlusKeys(event.button)

                if (this.keysChecker.checkBecamePressed("space", "left")) {
                    state.offset.dragStartX = event.canvasX
                    state.offset.dragStartY = event.canvasY
                } else if (this.keysChecker.checkBecamePressed("left")) {
                    const elementsAtSelection = this.selectionLayerFacade.searchIntersecting(
                        this.getClickAreaByPoint(event.canvasX, event.canvasY),
                    )

                    if (
                        state.selectorArea?.currentlySelected &&
                        !state.selectorArea.selectionDrawStartX &&
                        !state.selectorArea.selectionDrawStartY &&
                        elementsAtSelection.length > 0
                    ) {
                        state.selectorArea.dragStartX = event.canvasX
                        state.selectorArea.dragStartY = event.canvasY
                    } else {
                        const elementsAtMain = this.mainLayerFacade.searchIntersecting(
                            this.getClickAreaByPoint(event.canvasX, event.canvasY),
                        )
                        if (elementsAtMain.length > 0) {
                            const selectedElement = elementsAtMain[0]

                            state.selectorArea = {
                                currentlySelected: [selectedElement],
                            }
                        } else {
                            state.selectorArea = undefined
                        }
                    }
                } else if (this.keysChecker.checkBecamePressed("right")) {
                }

                state.pressedKeys.add(event.button)
                break;
            }
            case "release": {
                if (!event.button) return
                this.keysChecker.updatePressedKeys(state.pressedKeys).updateMinusKeys(event.button)

                if (
                    this.keysChecker.checkArePressed("space", "left") &&
                    this.keysChecker.checkBecameUnpressed("left")
                ) {
                    state.offset.dragStartX = undefined
                    state.offset.dragStartY = undefined
                } else if (this.keysChecker.checkBecameUnpressed("left")) {
                    if (
                        state.selectorArea?.selectionDrawStartX &&
                        state.selectorArea?.selectionDrawStartY
                    ) {
                        state.selectorArea.selectionDrawStartX = undefined
                        state.selectorArea.selectionDrawStartY = undefined
                    } else if (state.selectorArea?.dragStartX && state.selectorArea?.dragStartY) {
                        // drag offset already applied during move events
                        state.selectorArea.dragStartX = undefined
                        state.selectorArea.dragStartY = undefined
                    }
                } else if (this.keysChecker.checkBecameUnpressed("right")) {
                    // if there is transformer or selection (and mouse over such elements?), open popup menu
                }

                state.pressedKeys.delete(event.button)
                break;
            }
            default: {
                break;
            }
        }
    }

    stop() {
        if (this.disposable) {
            this.disposable.unsubscribe()
            this.disposable = null
        }
    }

    private pushViewsToSelectionBuffer(positionable: PositionableShape[]) {
        this.shapesView.removeElements(positionable)
        this.shapesLayerRepository.removeElements(positionable)
        this.shapesView.addElements(positionable)
        this.selectionLayerRepository.addElements(positionable)
    }

    private pushViewsFromSelectionBuffer(positionable: PositionableShape[]) {
        this.shapesView.removeElements(positionable)
        this.shapesLayerRepository.removeElements(positionable)
        this.shapesView.addElements(positionable)
        this.selectionLayerRepository.addElements(positionable)
    }

    private getMouseCoords(mouseEvent: MouseEvent): { x: number; y: number } {
        const { x, y } = this.stage.getClientRect({ skipTransform: true })

        return {
            x: mouseEvent.clientX - x,
            y: mouseEvent.clientY - y,
        }
    }

    private getClickAreaByPoint(x: number, y: number) {
        return {
            left: x - 2,
            top: x - 2,
            right: x + 2,
            bottom: x + 2,
        }
    }
}

export function EditorV2() {
    const stage = useRef<Konva.Stage | null>(null)
    const dispatch = useAppDispatch()

    useEffect(() => {
        const handleKeyDown: (ev: WindowEventMap["keydown"]) => any = evt => {
            const key = keyboardBtnToKey(evt.key)
            if (key) {
                evt.preventDefault()
                console.log("sending button down ", key)
                dispatch(buttonDown({ key }))
            }
        }

        const handleKeyUp: (ev: WindowEventMap["keyup"]) => any = evt => {
            const key = keyboardBtnToKey(evt.key)
            if (key) {
                dispatch(buttonUp({ key }))
            }
        }

        window.addEventListener("keydown", handleKeyDown)
        window.addEventListener("keyup", handleKeyUp)
        window.addEventListener("mousedown", handleKeyUp)

        // Cleanup function to remove the event listeners
        return () => {
            window.removeEventListener("keydown", handleKeyDown)
            window.removeEventListener("keyup", handleKeyUp)
        }
    }, [dispatch])

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
                onMouseDown={evt => {
                    const mouseKey = mouseBtnToKey(evt.evt.button)
                    const stageloc = stage.current
                    const pointerPos = stageloc?.getPointerPosition()
                    if (mouseKey && stageloc && pointerPos) {
                        dispatch(
                            mouseDown({
                                key: mouseKey,
                                x: pointerPos.x,
                                y: pointerPos.y,
                                targetId: undefined,
                            }),
                        )
                    }
                }}
                onMouseUp={evt => {
                    const mouseKey = mouseBtnToKey(evt.evt.button)
                    const stageloc = stage.current
                    const pointerPos = stageloc?.getPointerPosition()
                    if (mouseKey && stageloc && pointerPos) {
                        const stageCoord = stageloc.container().getBoundingClientRect()

                        dispatch(
                            mouseRelease({
                                key: mouseKey,
                                releaseX: evt.evt.clientX - stageCoord.x,
                                releaseY: evt.evt.clientY - pointerPos.y,
                            }),
                        )
                    }
                }}
                onMouseMove={evt => {
                    const stageloc = stage.current
                    const pointerPos = stageloc?.getPointerPosition()
                    if (stageloc && pointerPos) {
                        const stageCoord = stageloc.container().getBoundingClientRect()
                        dispatch(
                            mouseMove({
                                newX: evt.evt.clientX - stageCoord.x,
                                newY: evt.evt.clientY - stageCoord.y,
                            }),
                        )
                    }
                }}
            >
                <PatternBackground />
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

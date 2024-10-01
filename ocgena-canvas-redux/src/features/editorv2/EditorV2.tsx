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
import { ShapeConfig } from "konva/lib/Shape"
import {
    BehaviorSubject,
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

class ShapesRepository {
    tree = new PositionableShapeRBush()
    layer = new Konva.Layer({ listening: false })
    idToItem: Map<String, PositionableShape> = new Map()

    setupListeners() {
        // this.layer.addevenaddEventListener()
    }

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
}

class DiagramView {
    layer: Konva.Layer
    repository = new ShapesRepository()

    constructor(elementsLayer: Konva.Layer) {
        this.layer = elementsLayer
    }

    addElements(shapes: PositionableShape[]) {
        this.repository.addElements(shapes)
        for (const shape of shapes) {
            this.layer.add(this.createKonvaNode(shape))
        }
    }

    removeElements(shapes: PositionableShape[]) {
        this.repository.removeElements(shapes)
        for (const shape of shapes) {
            const child = this.getKonvaNode(shape)
            if (!!child) {
                child.remove()
            }
        }
    }

    updateElements(shapes: PositionableShape[]) {
        this.repository.updateElements(shapes)

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

type ViewerData = {
    canvasX: number
    canvasY: number
    pressedKeys: Set<Keys>,
    capturedDragId?: string
}

type MouseEventData = {
    canvasX: number
    canvasY: number
    type: "down" | "release" | "move"
    button?: MouseKeys
}

type StartDragging = {
    type : 'startdrag'
}

type ExternalEvent = StartDragging

type CanDragEffect = {
    // element data here, etc
    type: "candrag",
}

type Effect = CanDragEffect

class ViewFacade {
    initialState: ViewerData = {
        canvasX: 0,
        canvasY: 0,
        pressedKeys: new Set<ButtonKeys>(),
    }
    viewerData = new BehaviorSubject<ViewerData>(this.initialState)

    externalEvents = new Subject<ExternalEvent>()

    disposable: Subscription | null = null
    stage: Konva.Stage

    effectCb: (effect: Effect) => void;

    constructor(stage: Konva.Stage, effectCb: (effect: Effect) => void) {
        this.stage = stage
        this.effectCb = effectCb
    }

    start() {
        merge(
            this.externalEvents,
            fromEvent(this.stage, "mousedown", (evt: KonvaEventObject<MouseEvent>) => {
                const { x, y } = this.stage.getClientRect({ skipTransform: true })
                const mouseEvent: MouseEventData = {
                    canvasX: evt.evt.clientX - x,
                    canvasY: evt.evt.clientY - y,
                    type: "down",
                    button: mouseBtnToKey(evt.evt.button),
                }
                return mouseEvent
            }),
            fromEvent(this.stage, "mousemove", (evt: KonvaEventObject<MouseEvent>) => {
                const { x, y } = this.getMouseCoords(evt.evt)
                const mouseEvent: MouseEventData = {
                    canvasX: x,
                    canvasY: y,
                    type: "move",
                }
                return mouseEvent
            }),
            fromEvent(this.stage, "mouseup", (evt: KonvaEventObject<MouseEvent>) => {
                const { x, y } = this.getMouseCoords(evt.evt)
                const mouseEvent: MouseEventData = {
                    canvasX: x,
                    canvasY: y,
                    type: "release",
                    button: mouseBtnToKey(evt.evt.button),
                }
                return mouseEvent
            }),
        )
            .pipe(
                scan((viewerData: ViewerData, value: ExternalEvent | MouseEventData, idx) => {
                    switch (value.type) {
                        case "move": {
                            return {
                                ...viewerData,
                                canvasX: value.canvasX,
                                canvasY: value.canvasY,
                            }
                        }
                        case "down": {
                            if (value.button) {
                                viewerData.pressedKeys.add(value.button)
                            }
                            this.effectCb({

                            })
                            return {
                                ...viewerData,
                                canvasX: value.canvasX,
                                canvasY: value.canvasY,
                            }
                        }
                        case "release": {
                            if (value.button) {
                                viewerData.pressedKeys.delete(value.button)
                            }

                            return {
                                ...viewerData,
                                canvasX: value.canvasX,
                                canvasY: value.canvasY,
                            }
                        }
                        case "startdrag": {
                        
                        }
                    }
                    return viewerData
                }, this.initialState),
            )
            .subscribe(this.viewerData)
    }

    startDragging() {

    }

    stop() {
        if (this.disposable) {
            this.disposable.unsubscribe()
            this.disposable = null
        }
    }

    private getMouseCoords(mouseEvent: MouseEvent): { x: number; y: number } {
        const { x, y } = this.stage.getClientRect({ skipTransform: true })

        return {
            x: mouseEvent.clientX - x,
            y: mouseEvent.clientY - y,
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

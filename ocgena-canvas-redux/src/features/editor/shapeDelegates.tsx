import { NodeConfig } from "konva/lib/Node"
import { Coord } from "./Coord"
import { MutableRefObject, useEffect } from "react"
import Konva from "konva"
import { PositionUpdatePayload, Element, CircleShape } from "./editorSlice"
import { Circle, KonvaNodeEvents, Rect } from "react-konva"

class ShapeDelegateCommon {
  private static _instance: ShapeDelegateCommon

  private consumeScaleToDimens(node: Konva.Node) {
    const width = node.width()
    const height = node.height()
    const scaleX = node.scaleX()
    const scaleY = node.scaleY()
    const newWidth = scaleX * width
    const newHeight = scaleY * height
    // we will reset it back
    node.scaleX(1)
    node.scaleY(1)
    node.width(newWidth)
    node.height(newHeight)
  }

  createCommonProps(
    nodeConfig: NodeConfig,
    shapeRef: MutableRefObject<Konva.Shape | null>,
    localCoord: Coord,
    syncPositions: () => void,
    updatePosition: (positionUpdatePayload: PositionUpdatePayload) => void,
  ): Konva.NodeConfig & KonvaNodeEvents {
    return {
      ...nodeConfig,
      x: localCoord.x,
      y: localCoord.y,
      draggable: false,
      onTransform(evt: Konva.KonvaEventObject<Event>) {
        syncPositions()
      },
      onTransformEnd: (evt: Konva.KonvaEventObject<Event>) => {
        this.consumeScaleToDimens(shapeRef.current!)
        console.log(
          "circle group pos before",
          (shapeRef.current!.getParent() as Konva.Group).getAbsolutePosition(),
        )
        syncPositions()
        const groupAbsolutePosition = (
          shapeRef.current!.getParent() as Konva.Group
        ).getAbsolutePosition()

        console.log("circle group pos after", groupAbsolutePosition)

        updatePosition({
          id: nodeConfig.id!,
          x: groupAbsolutePosition.x,
          y: groupAbsolutePosition.y,
        })
      },
    }
  }

  static get Instance(): ShapeDelegateCommon {
    return this._instance || (this._instance = new ShapeDelegateCommon())
  }
}

type PossibleShapes = Konva.Rect | Konva.Circle | Konva.Shape

export interface ShapeDelegateNew<Type extends PossibleShapes = Konva.Shape> {
  createShape(
    element: Element,
    nodeConfig: NodeConfig,
    shapeRef: MutableRefObject<Type | null>,
    text: () => Konva.Text,
    updatePosition: (positionUpdatePayload: PositionUpdatePayload) => void,
  ): JSX.Element

  synchronizeTextAreaPosition(shape: Type, textArea: HTMLTextAreaElement): void
}

class RectShapeDelegate implements ShapeDelegateNew<Konva.Rect> {
  private static _instance: RectShapeDelegate

  private syncPositions(shape: Konva.Rect, textSelector: () => Konva.Text) {
    const group = shape.getParent() as Konva.Group
    const text = textSelector()
    text.setAttrs({
      width: shape.scaleX() * shape.width(),
      height: shape.scaleY() * shape.height(),
      x: 0,
      y: 0,
    })
    group?.setAbsolutePosition(shape.getAbsolutePosition())
    shape.setPosition({ x: 0, y: 0 })
  }

  synchronizeTextAreaPosition(
    shape: Konva.Rect,
    textArea: HTMLTextAreaElement,
  ): void {
    const rectangle = shape
    const xOffset = 0
    const yOffset = 0
    const width = rectangle.width()
    const height = rectangle.height()
    synchronizeTextAreaPosition(
      xOffset,
      yOffset,
      width,
      height,
      rectangle,
      textArea,
    )
  }

  createShape(
    element: Element,
    nodeConfig: NodeConfig,
    shapeRef: MutableRefObject<Konva.Rect | null>,
    text: () => Konva.Text,
    updatePosition: (positionUpdatePayload: PositionUpdatePayload) => void,
  ) {
    const props = ShapeDelegateCommon.Instance.createCommonProps(
      nodeConfig,
      shapeRef,
      { x: 0, y: 0 },
      () => {
        this.syncPositions(shapeRef.current!, text)
      },
      updatePosition,
    )
    useEffect(() => {
      this.syncPositions(shapeRef.current!, text)
    }, [])
    return <Rect ref={shapeRef} {...props} />
  }

  static get Instance(): RectShapeDelegate {
    if (!this._instance) {
      this._instance = new RectShapeDelegate()
    }
    return this._instance
  }
}

class CircleShapeDelegate implements ShapeDelegateNew<Konva.Circle> {
  private static _instance: CircleShapeDelegate

  private synchronizeSizeAndLocalCoord(shape: Konva.Circle) {
    const realRadius = (shape.width() * shape.scaleX()) / 2
    const closestSize = (shape.getWidth() * shape.scaleX()).closestSize()
    const closestRadius = closestSize / 2
    
    shape.setAttrs({
        x: closestRadius,
        y: closestRadius,
        radius: closestRadius
    })
  }

  private syncPositions(shape: Konva.Circle, textSelector: () => Konva.Text) {
    const group = shape.getParent() as Konva.Group
    const text = textSelector()
    const realRadius = (shape.width() * shape.scaleX()) / 2

    text.setAttrs({
      width: shape.scaleX() * shape.radius() * 2,
      height: shape.scaleY() * shape.radius() * 2,
      x: 0,
      y: 0,
    })
    console.log("shape absolute position", shape.getAbsolutePosition())
    const absoluteCirclePosition = shape.getAbsolutePosition()
    group?.setAbsolutePosition({
      x: absoluteCirclePosition.x - realRadius,
      y: absoluteCirclePosition.y - realRadius,
    })
    console.log(
      "circle group abs position",
      shape.getAbsolutePosition(),
      "shape radius",
      shape.radius(),
      "local",
      shape.position(),
    )
    shape.setAttrs({
      x: realRadius,
      y: realRadius,
    })
  }

  synchronizeTextAreaPosition(
    shape: Konva.Circle,
    textArea: HTMLTextAreaElement,
  ): void {
    const circleShape = shape
    const srcWidth = circleShape.scaleX() * circleShape.width()

    const innerRectSize = 2 * Math.cos((45 * Math.PI) / 180) * (srcWidth / 2)
    const xOffset = -innerRectSize / 2
    const yOffset = -innerRectSize / 2
    const width = innerRectSize
    const height = innerRectSize
    synchronizeTextAreaPosition(
      xOffset,
      yOffset,
      width,
      height,
      circleShape,
      textArea,
    )
  }

  createShape(
    element: Element<CircleShape>,
    nodeConfig: NodeConfig,
    shapeRef: MutableRefObject<Konva.Circle | null>,
    text: () => Konva.Text,
    updatePosition: (positionUpdatePayload: PositionUpdatePayload) => void,
  ) {
    const props = ShapeDelegateCommon.Instance.createCommonProps(
      nodeConfig,
      shapeRef,
      { x: element.shape.radius, y: element.shape.radius },
      () => {
        this.syncPositions(shapeRef.current!, text)
      },
      updatePosition,
    )
    useEffect(() => {
      this.synchronizeSizeAndLocalCoord(shapeRef!.current!)
    }, [nodeConfig])

    useEffect(() => {
      this.syncPositions(shapeRef.current!, text)
    }, [])
    return <Circle ref={shapeRef} {...props} />
  }

  static get Instance(): CircleShapeDelegate {
    if (!this._instance) {
      this._instance = new CircleShapeDelegate()
    }
    return this._instance
  }
}

const synchronizeTextAreaPosition = (
  xOffset: number,
  yOffset: number,
  width: number,
  height: number,
  srcNode: Konva.Node,
  targetElement: HTMLElement,
) => {
  const srcAbsolutePosition = srcNode.getAbsolutePosition()
  const stage = srcNode.getLayer()?.getStage()
  // then lets find position of stage container on the page:
  var stageBox = stage!.container().getBoundingClientRect()
  targetElement.style.position = "absolute"
  targetElement.style.top =
    yOffset + stageBox.top + window.scrollY + srcAbsolutePosition.y + "px"
  targetElement.style.left =
    xOffset + stageBox.left + window.scrollX + srcAbsolutePosition.x + "px"
  targetElement.style.width = width - 5 + "px"
  targetElement.style.height = height - 5 + "px"
}

export function selectShapeDelegate(element: Element): ShapeDelegateNew {
  switch (element.shape.type) {
    case "rect":
      return RectShapeDelegate.Instance
    case "circle":
      return CircleShapeDelegate.Instance
  }
}

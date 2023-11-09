import { NodeConfig } from "konva/lib/Node"
import { Coord } from "./Coord"
import { MutableRefObject, useEffect } from "react"
import Konva from "konva"
import { Circle, KonvaNodeEvents, Rect } from "react-konva"
import { ELEMENT_CHILD_SHAPE_PREFIX } from "./Keywords"
import { CircleShape, Element, PositionUpdatePayload } from "./Models"
import { Text } from "konva/lib/shapes/Text"
import { Vector2d } from "konva/lib/types"
import { getRealHeight, getRealWidth } from "./primitiveShapeUtils"

class ShapeDelegateCommon {
  private static _instance: ShapeDelegateCommon
  createCommonShapeProps(
    nodeConfig: NodeConfig,
    localCoord: Coord,
  ): Konva.NodeConfig & KonvaNodeEvents {
    return {
      ...nodeConfig,
      key: ELEMENT_CHILD_SHAPE_PREFIX + nodeConfig.id,
      id: ELEMENT_CHILD_SHAPE_PREFIX + nodeConfig.id,
      x: localCoord.x,
      y: localCoord.y,
      draggable: false,
    }
  }

  static get Instance(): ShapeDelegateCommon {
    return this._instance || (this._instance = new ShapeDelegateCommon())
  }
}

type PossibleShapes = Konva.Rect | Konva.Circle | Konva.Shape

export interface ShapeDelegateNew<Type extends PossibleShapes = Konva.Shape> {
  createShape(
    nodeConfig: NodeConfig,
    shapeRef: MutableRefObject<Type | null>,
  ): JSX.Element

  synchronizeTextAreaPosition(shape: Type, textArea: HTMLTextAreaElement): void
  synchronizeTextAndShape(shape: Type, text: Konva.Text): void
  updateShapeToMatchText(shape: Type, text: Konva.Text): void
  updateShapeSize(shape: Type, size: Vector2d): void
}

class RectShapeDelegate implements ShapeDelegateNew<Konva.Rect> {
  private static _instance: RectShapeDelegate

  synchronizeTextAndShape(shape: Konva.Rect, text: Text): void {
    text.setAttrs({
      width: getRealWidth(shape),
      height: getRealHeight(shape),
      x: 0,
      y: 0,
    })
    shape.setAttrs({
      x: 0,
      y: 0,
    })
  }

  updateShapeToMatchText(shape: Konva.Rect, text: Text): void {
    shape.setAttrs({
      height: text.height(),
      width: text.width(),
    })
  }

  updateShapeSize(shape: Konva.Rect, size: Vector2d): void {
    shape.setAttrs({
      height: size.y,
      width: size.x,
    })
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
    nodeConfig: NodeConfig,
    shapeRef: MutableRefObject<Konva.Rect | null>,
  ) {
    const props = ShapeDelegateCommon.Instance.createCommonShapeProps(
      nodeConfig,
      { x: 0, y: 0 },
    )
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

  synchronizeTextAndShape(shape: Konva.Circle, text: Text): void {
    const radius = shape.radius()
    const innerRectSize = 2 * Math.cos((45 * Math.PI) / 180) * radius

    text.setAttrs({
      width: innerRectSize,
      height: innerRectSize,
      x: radius - innerRectSize / 2,
      y: radius - innerRectSize / 2,
    })
    shape.setAttrs({
      x: radius,
      y: radius,
    })
  }

  updateShapeToMatchText(shape: Konva.Circle, text: Text): void {
    const shapeRadius = text.width() / ( 2 * Math.cos((45 * Math.PI) / 180))

    shape.setAttrs({
      radius: shapeRadius,
      x: text.width() / 2,
      y: text.height() / 2
    })
  }

  updateShapeSize(shape: Konva.Circle, size: Vector2d): void {
    const radius = size.x / 2
    shape.setAttrs({
      x: radius,
      y: radius,
      radius: radius,
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
    nodeConfig: NodeConfig,
    shapeRef: MutableRefObject<Konva.Circle | null>,
  ) {
    const props = ShapeDelegateCommon.Instance.createCommonShapeProps(
      nodeConfig,
      { x: nodeConfig.radius, y: nodeConfig.radius },
    )
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

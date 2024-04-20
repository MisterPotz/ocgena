import Konva from "konva"
import { MutableRefObject, useEffect, useMemo, useRef, useState } from "react"
import {
  RectangleShape,
  Element,
  AnyElement,
  ShapeType,
  SpecificShape,
  SpecificShapeType,
  CircleShape,
  PositionUpdatePayload,
} from "./editorSlice"
import React from "react"
import {
  Circle,
  Group,
  KonvaNodeEvents,
  Rect,
  Text,
  Transformer,
} from "react-konva"
import { elementToNodeConfig } from "./Utils"
import { KonvaEventObject, NodeConfig } from "konva/lib/Node"
import { useAppDispatch } from "../../app/hooks"
import { s } from "vitest/dist/reporters-P7C2ytIv.js"
import { elem } from "fp-ts/lib/Option"

const MIN_WIDTH = 50
const MIN_HEIGHT = 50

interface Coord {
  x: number
  y: number
}

// interface ShapeDelegate<
//   T extends SpecificShape = SpecificShape,
//   ShapeType extends SpecificShapeType = T["type"],
// > {
//   type: ShapeType
//   convertElement(element: AnyElement): NodeConfig
//   createShape(
//     config: NodeConfig,
//     text: React.MutableRefObject<Konva.Node | null>,
//   ): JSX.Element
//   synchronizeTextAndShapePosition(text: Konva.Node): void
//   synchronizeTextAreaPosition(textArea: HTMLTextAreaElement): void
// }

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

interface ShapeDelegateNew<Type extends PossibleShapes = Konva.Shape> {
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

  private synchronizeRelativeShapeCoord(
    shape: Konva.Circle,
  ) {
    const realRadius = (shape.width() * shape.scaleX()) / 2
    shape.setPosition({ x: realRadius, y: realRadius })
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
      this.synchronizeRelativeShapeCoord(shapeRef!.current!)
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

function selectShapeDelegate(element: Element): ShapeDelegateNew {
  switch (element.shape.type) {
    case "rect":
      return RectShapeDelegate.Instance
    case "circle":
      return CircleShapeDelegate.Instance
  }
}

interface TextShapeProps {
  element: AnyElement
  updatePosition: (payload: PositionUpdatePayload) => void
}

/*
 * Text is synchronized with the shape
 * Text is editable.
 * The whole shape is rezisable.
 */
export function TextShape(
  { element, updatePosition }: TextShapeProps,
  // element: AnyElement,
  // updatePosition: (payload: PositionUpdatePayload) => void,
) {
  const selected = element.selected || false
  const trRef = useRef<Konva.Transformer | null>(null)
  const groupRef = useRef<Konva.Group | null>(null)
  const textRef = useRef<Konva.Text | null>(null)
  const shapeRef = useRef<Konva.Shape | null>(null)
  const editableTextAreaRef = useRef<HTMLTextAreaElement | null>(null)

  const shapeDelegate = useMemo<ShapeDelegateNew>(() => {
    return selectShapeDelegate(element)
  }, [element.shape.type])

  useEffect(() => {
    setupRemovableTextArea(
      groupRef.current!,
      textRef.current!,
      shapeRef.current!,
      editableTextAreaRef,
      textArea => {
        shapeDelegate.synchronizeTextAreaPosition(shapeRef.current!, textArea)
      },
    )
  }, [])

  const nodeConfig = useMemo<NodeConfig>(() => {
    return elementToNodeConfig(element)
  }, [element])

  useEffect(() => {
    groupRef.current!.setAbsolutePosition({
      x: nodeConfig.x!,
      y: nodeConfig.y!,
    })
  }, [nodeConfig])

  // const coord = useMemo<Coord>(() => {
  //   return shapeDelegate.getStartCoord(element)
  // }, [element.x, element.y])

  React.useEffect(() => {
    if (selected) {
      if (trRef.current && groupRef.current) {
        let transformableNodes = [shapeRef.current!]
        trRef.current.nodes(transformableNodes)
        trRef.current.getLayer()?.batchDraw()
      }
    }
  }, [selected])

  return (
    <React.Fragment key={element.id}>
      <Group
        id={element.id}
        key={element.id}
        ref={groupRef}
        draggable={true}
        listening={true}
        x={nodeConfig.x}
        y={nodeConfig.y}
      >
        {shapeDelegate.createShape(
          element,
          nodeConfig,
          shapeRef,
          () => {
            return textRef.current!
          },
          updatePosition,
        )}
        <Text
          id={"text"}
          ref={textRef}
          fontSize={24}
          ellipsis
          align="center"
          verticalAlign="middle"
          wrap="word"
          text="kek lol arbidol"
          draggable={false}
          listening={false}
        />
      </Group>
      {selected && (
        <Transformer
          id="transformer"
          padding={5}
          ref={trRef}
          rotateEnabled={false}
          flipEnabled={false}
          boundBoxFunc={(oldBox, newBox) => {
            // limit resize
            if (
              Math.abs(newBox.width) < MIN_WIDTH ||
              Math.abs(newBox.height) < MIN_HEIGHT
            ) {
              return oldBox
            }
            const newX = newBox.x.closestDotX()
            const newY = newBox.y.closestDotY()
            const newWidth = newBox.width.closestSize()
            const newHeight = newBox.height.closestSize()

            console.log(
              "new box raw",
              newBox,
              "snapped new box",
              {
                newX,
                newY,
                newWidth,
                newHeight,
              },
              "oldbox",
              oldBox,
            )
            // if (
            //   newX != newBox.x ||
            //   newY != newBox.y ||
            //   newWidth != newBox.width ||
            //   newHeight != newBox.height
            // ) {
            //   return {
            //     x: newX,
            //     y: newY,
            //     width: newWidth,
            //     height: newHeight,
            //     rotation: newBox.rotation,
            //   }
            // }
            return newBox
          }}
        />
      )}
    </React.Fragment>
  )
}

const setupRemovableTextArea = (
  groupNode: Konva.Node,
  textNode: Konva.Text,
  rectRef: Konva.Node,
  textAreaRef: React.MutableRefObject<HTMLTextAreaElement | null>,
  synchronizeTextAreaPosition: (textArea: HTMLTextAreaElement) => void,
) => {
  groupNode.on("dblclick dbltap", (e: KonvaEventObject<MouseEvent>) => {
    if (textAreaRef.current) return
    if (e.evt.button !== 0) return
    // create textarea and style it
    const textarea = document.createElement("textarea")
    textAreaRef.current = textarea

    const removeTextArea = () => {
      if (textAreaRef.current) {
        document.body.removeChild(textAreaRef.current)
        textAreaRef.current = null
      }
    }
    let escapeListener = createEscapeListener(removeTextArea)
    const fullRemoveTextArea = () => {
      document.removeEventListener("keydown", escapeListener)
      removeTextArea()
    }
    textarea.addEventListener("blur", () => {
      fullRemoveTextArea()
    })
    document.addEventListener("keydown", escapeListener)
    textarea.addEventListener("keydown", function (e) {
      // hide on enter
      if (e.key === "Enter") {
        textNode.text(textarea.value)
        fullRemoveTextArea()
      }
    })

    document.body.appendChild(textarea)
    textarea.style.fontSize = 22 + "px"
    textarea.value = textNode.text()

    synchronizeTextAreaPosition(textarea)
    textarea.focus()
  })
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

function createEscapeListener(onEscapeCallback: () => void) {
  // Return the actual event handler function from this closure
  return function selfRemovableOnEscape(
    this: Document,
    ev: KeyboardEvent,
  ): any {
    console.log("keypress event", ev.key)
    if (ev.key === "Escape") {
      onEscapeCallback()
      // Remove the event listener using the function reference
      document.removeEventListener("keydown", selfRemovableOnEscape)
      return true
    }
  }
}

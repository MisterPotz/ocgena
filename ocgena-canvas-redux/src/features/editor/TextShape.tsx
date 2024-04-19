import Konva from "konva"
import { useEffect, useMemo, useRef, useState } from "react"
import {
  RectangleShape,
  Element,
  AnyElement,
  ShapeType,
  SpecificShape,
  SpecificShapeType,
  CircleShape,
} from "./editorSlice"
import React from "react"
import { Circle, Group, Rect, Text, Transformer } from "react-konva"
import { elementToNodeConfig } from "./Utils"
import { KonvaEventObject, NodeConfig } from "konva/lib/Node"
import { useAppDispatch } from "../../app/hooks"

const MIN_WIDTH = 50
const MIN_HEIGHT = 50

interface Coord {
  x: number
  y: number
}

interface ShapeDelegate<
  T extends SpecificShape = SpecificShape,
  ShapeType extends SpecificShapeType = T["type"],
> {
  type: ShapeType
  convertElement(element: AnyElement): NodeConfig
  createShape(
    config: NodeConfig,
    text: React.MutableRefObject<Konva.Node | null>,
  ): JSX.Element
  synchronizeTextAndShapePosition(text: Konva.Node): void
  synchronizeTextAreaPosition(textArea: HTMLTextAreaElement): void
  getStartCoord(element: Element<T>): Coord
}

function createShapeDelegate(
  shapeType: ShapeType,
  shapeRef: React.MutableRefObject<any | null>,
): ShapeDelegate {
  switch (shapeType) {
    case "rect": {
      const synchronizeTextAndShape = (text: Konva.Node) =>
        synchronizeRectAndTextPosition(shapeRef.current!, text)
      return {
        type: shapeType,
        synchronizeTextAndShapePosition: synchronizeTextAndShape,
        convertElement: elementToNodeConfig,
        getStartCoord: (el: Element<RectangleShape>) => {
          return { x: el.x, y: el.y }
        },
        synchronizeTextAreaPosition: (textArea: HTMLTextAreaElement) => {
          const rectangle = shapeRef.current! as Konva.Rect
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
        },
        createShape: (
          config: NodeConfig,
          textRef: React.MutableRefObject<Konva.Node | null>,
        ) => {
          return (
            <Rect
              ref={shapeRef}
              {...config}
              draggable={false}
              onTransform={() => {
                synchronizeTextAndShape(textRef.current!)
              }}
              onTransformEnd={() => {
                consumeScaleToDimens(shapeRef.current!, MIN_WIDTH, MIN_HEIGHT)
                synchronizeTextAndShape(textRef.current!)
              }}
            />
          )
        },
      }
    }
    case "circle": {
      const synchronizeTextAndShape = (text: Konva.Node) => {
        synchronizeCircleAndTextPosition(shapeRef.current!, text)
      }
      return {
        type: shapeType,
        synchronizeTextAndShapePosition: synchronizeTextAndShape,
        convertElement: elementToNodeConfig,
        getStartCoord: (el: Element<CircleShape>) => {
          return { x: el.x, y: el.y }
        },
        synchronizeTextAreaPosition: (textArea: HTMLTextAreaElement) => {
          const circleShape = shapeRef.current! as Konva.Circle
          const srcWidth = circleShape.scaleX() * circleShape.width()

          const innerRectSize =
            2 * Math.cos((45 * Math.PI) / 180) * (srcWidth / 2)
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
        },
        createShape: (
          config: NodeConfig,
          textRef: React.MutableRefObject<Konva.Node | null>,
        ) => {
          return (
            <Circle
              ref={shapeRef}
              {...config}
              draggable={false}
              onTransform={() => {
                synchronizeTextAndShape(textRef.current!)
              }}
              onTransformEnd={() => {
                consumeScaleToDimens(shapeRef.current!, MIN_WIDTH, MIN_HEIGHT)
                synchronizeTextAndShape(textRef.current!)
              }}
            />
          )
        },
      }
    }
  }
}

/*
 * Text is synchronized with the shape
 * Text is editable.
 * The whole shape is rezisable.
 */
export function TextShape(element: AnyElement) {
  const dispatch = useAppDispatch()

  const selected = element.selected || false
  // const [selected, setSelected] = useState(false)

  const trRef = useRef<Konva.Transformer | null>(null)
  const groupRef = useRef<Konva.Group | null>(null)
  const textRef = useRef<Konva.Text | null>(null)
  const shapeRef = useRef<Konva.Shape | null>(null)
  const editableTextAreaRef = useRef<HTMLTextAreaElement | null>(null)

  const shapeDelegate = useMemo<ShapeDelegate>(() => {
    return createShapeDelegate(element.shape.type, shapeRef)
  }, [element.shape.type])

  useEffect(() => {
    shapeDelegate.synchronizeTextAndShapePosition(textRef.current!)
    setupRemovableTextArea(
      groupRef.current!,
      textRef.current!,
      shapeRef.current!,
      editableTextAreaRef,
      shapeDelegate.synchronizeTextAreaPosition,
    )
  }, [])
  const nodeConfig = useMemo<NodeConfig>(() => {
    return shapeDelegate.convertElement(element)
  }, [element])

  const coord = useMemo<Coord>(() => {
    return shapeDelegate.getStartCoord(element)
  }, [element.x, element.y])

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
    <React.Fragment>
      <Group
        id={element.id}
        key={element.id}
        // onDragMove={() => {
        //   if (editableTextAreaRef.current) {
        //     synchronizeTextAreaPosition(
        //       textRef.current!,
        //       editableTextAreaRef.current!,
        //     )
        //   }
        // }}
        ref={groupRef}
        draggable={true}
        x={coord.x}
        y={coord.y}
      >
        {shapeDelegate.createShape(nodeConfig, textRef)}
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

            console.log("new box raw", newBox, "snapped new box", {
              newX,
              newY,
              newWidth,
              newHeight,
            })
            if (
              newX != newBox.x ||
              newY != newBox.y ||
              newWidth != newBox.width ||
              newHeight != newBox.height
            ) {
              return {
                x: newX,
                y: newY,
                width: newWidth,
                height: newHeight,
                rotation: newBox.rotation,
              }
            }
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

const synchronizeRectAndTextPosition = (
  srcNode: Konva.Node,
  targetNode: Konva.Node,
) => {
  targetNode.setAttrs({
    width: srcNode.scaleX() * srcNode.width(),
    height: srcNode.scaleY() * srcNode.height(),
    x: srcNode.x(),
    y: srcNode.y(),
  })
}

const synchronizeCircleAndTextPosition = (
  srcNode: Konva.Circle,
  targetNode: Konva.Node,
) => {
  const srcWidth = srcNode.scaleX() * srcNode.width()
  const srcHeight = srcNode.scaleY() * srcNode.height()
  const innerRectSize = 2 * Math.cos((45 * Math.PI) / 180) * (srcWidth / 2)
  targetNode.setAttrs({
    width: innerRectSize,
    height: innerRectSize,
    x: srcNode.x() - innerRectSize / 2,
    y: srcNode.y() - innerRectSize / 2,
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
  const textPosition = srcNode.getAbsolutePosition()
  const stage = srcNode.getLayer()?.getStage()
  // then lets find position of stage container on the page:
  var stageBox = stage!.container().getBoundingClientRect()
  targetElement.style.position = "absolute"
  targetElement.style.top =
    yOffset + stageBox.top + window.scrollY + textPosition.y + "px"
  targetElement.style.left =
    xOffset + stageBox.left + window.scrollX + textPosition.x + "px"
  targetElement.style.width = width - 5 + "px"
  targetElement.style.height = height - 5 + "px"
}

// const synchronizeTextAreaPosition = (
//   srcNode: Konva.Node,
//   targetElement: HTMLElement,
// ) => {
//   const textPosition = srcNode.getAbsolutePosition()
//   const stage = srcNode.getLayer()?.getStage()
//   // then lets find position of stage container on the page:
//   var stageBox = stage!.container().getBoundingClientRect()
//   // so position of textarea will be the sum of positions above:
//   var areaPosition = {
//     x: stageBox.left + window.scrollX + textPosition.x,
//     y: stageBox.top + window.scrollY + textPosition.y,
//   }
//   targetElement.style.position = "absolute"
//   targetElement.style.top = areaPosition.y + "px"
//   targetElement.style.left = areaPosition.x + "px"
//   targetElement.style.width = srcNode.width() - 5 + "px"
//   targetElement.style.height = srcNode.height() - 5 + "px"
// }

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

const consumeScaleToDimens = (
  node: Konva.Node,
  minWidth: number,
  minHeight: number,
) => {
  const width = node.width()
  const height = node.height()
  const scaleX = node.scaleX()
  const scaleY = node.scaleY()
  const newWidth = Math.max(scaleX * width, minWidth).closestSize()
  const newHeight = Math.max(scaleY * height, minHeight).closestSize()
  // we will reset it back
  node.scaleX(1)
  node.scaleY(1)
  node.width(newWidth)
  node.height(newHeight)
  node.width()
}

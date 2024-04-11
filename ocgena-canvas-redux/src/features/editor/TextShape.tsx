import Konva from "konva"
import { useEffect, useMemo, useRef, useState } from "react"
import { RectangleShape, Element, AnyElement, ShapeType } from "./editorSlice"
import React from "react"
import { Circle, Group, Rect, Text, Transformer } from "react-konva"
import { elementToNodeConfig } from "./Utils"
import { NodeConfig } from "konva/lib/Node"

const MIN_WIDTH = 50
const MIN_HEIGHT = 50

interface ShapeDelegate {
  type: ShapeType
  convertElement(element: AnyElement): NodeConfig
  createShape(
    config: NodeConfig,
    text: React.MutableRefObject<Konva.Node | null>,
  ): JSX.Element
  synchronizeTextAndShapePosition(text: Konva.Node): void
}

function createShapeDelegate(
  shapeType: ShapeType,
  shapeRef: React.MutableRefObject<any | null>,
): ShapeDelegate {
  switch (shapeType) {
    case "rect": {
      const synchronizeTextAndShape = (text: Konva.Node) => {
        synchronizeRectAndTextPosition(shapeRef.current!, text)
      }
      return {
        type: shapeType,
        synchronizeTextAndShapePosition: synchronizeTextAndShape,
        convertElement: (element: AnyElement) => {
          return elementToNodeConfig(element)
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
        convertElement: (element: AnyElement) => {
          return elementToNodeConfig(element)
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
  const [selected, setSelected] = useState(false)

  const trRef = useRef<Konva.Transformer | null>(null)
  const groupRef = useRef<Konva.Group | null>(null)
  const textRef = useRef<Konva.Text | null>(null)
  const shapeRef = useRef<Konva.Shape | null>(null)
  const editableTextAreaRef = useRef<HTMLTextAreaElement | null>(null)

  useEffect(() => {
    synchronizeRectAndTextPosition(shapeRef.current!, textRef.current!)
    setupRemovableTextArea(
      textRef.current!,
      shapeRef.current!,
      editableTextAreaRef,
    )
  }, [])

  const shapeDelegate = useMemo<ShapeDelegate>(() => {
    return createShapeDelegate(element.shape.type, shapeRef)
  }, [element.shape.type])

  const nodeConfig = useMemo<NodeConfig>(() => {
    return shapeDelegate.convertElement(element)
  }, [element])

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
        onDragMove={() => {
          if (editableTextAreaRef.current) {
            synchronizeTextAreaPosition(
              textRef.current!,
              editableTextAreaRef.current!,
            )
          }
        }}
        ref={groupRef}
        draggable={true}
      >
        {shapeDelegate.createShape(nodeConfig, textRef)}
        <Text
          ref={textRef}
          fontSize={24}
          ellipsis
          align="center"
          verticalAlign="middle"
          wrap="word"
          text="kek lol arbidol"
          draggable={false}
        />
      </Group>
      {selected && (
        <Transformer
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
            return newBox
          }}
        />
      )}
    </React.Fragment>
  )
}

const setupRemovableTextArea = (
  textNode: Konva.Text,
  rectRef: Konva.Node,
  textAreaRef: React.MutableRefObject<HTMLTextAreaElement | null>,
) => {
  textNode.on("dblclick dbltap", () => {
    if (textAreaRef.current) return
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

    synchronizeTextAreaPosition(rectRef, textarea)
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

// TODO: 2024-04-11 redefine this function for circle shapes to look better with text
const synchronizeCircleAndTextPosition = (
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

const synchronizeTextAreaPosition = (
  srcNode: Konva.Node,
  targetElement: HTMLElement,
) => {
  const textPosition = srcNode.getAbsolutePosition()
  const stage = srcNode.getLayer()?.getStage()
  // then lets find position of stage container on the page:
  var stageBox = stage!.container().getBoundingClientRect()
  // so position of textarea will be the sum of positions above:
  var areaPosition = {
    x: stageBox.left + window.scrollX + textPosition.x,
    y: stageBox.top + window.scrollY + textPosition.y,
  }
  targetElement.style.position = "absolute"
  targetElement.style.top = areaPosition.y + "px"
  targetElement.style.left = areaPosition.x + "px"
  targetElement.style.width = srcNode.width() - 5 + "px"
  targetElement.style.height = srcNode.height() - 5 + "px"
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

const consumeScaleToDimens = (
  node: Konva.Node,
  minWidth: number,
  minHeight: number,
) => {
  const width = node.width()
  const height = node.height()
  const scaleX = node.scaleX()
  const scaleY = node.scaleY()
  const newWidth = Math.max(scaleX * width, minWidth)
  const newHeight = Math.max(scaleY * height, minHeight)
  // we will reset it back
  node.scaleX(1)
  node.scaleY(1)
  node.width(newWidth)
  node.height(newHeight)
  node.width()
}

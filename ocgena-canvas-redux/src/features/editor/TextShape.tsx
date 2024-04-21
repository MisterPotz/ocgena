import Konva from "konva"
import { MutableRefObject, useEffect, useMemo, useRef, useState } from "react"
import React from "react"
import { Circle, Group, Rect, Text, Transformer } from "react-konva"
import { elementToNodeConfig, elementToSize, isGroup } from "./Utils"
import { KonvaEventObject, NodeConfig } from "konva/lib/Node"
import { ShapeDelegateNew, selectShapeDelegate } from "./shapeDelegates"
import {
  ELEMENT_CHILD_PREFIX,
  MARK_SELECTION_PREFIX,
  TRANSFORMER_PREFIX,
} from "./Keywords"
import { AnyElement, PositionUpdatePayload } from "./Models"
import {
  getRealHeight,
  getRealWidth,
  height,
  width,
} from "./primitiveShapeUtils"

const MIN_WIDTH = 50
const MIN_HEIGHT = 50

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
) {
  const selectedAtClick = element.selectedAtClick || false
  const selectedWithWindow = element.selectedWithWindow || false
  const trRef = useRef<Konva.Transformer | null>(null)
  const markSelectionRef = useRef<Konva.Rect | null>(null)
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

  React.useEffect(() => {
    if (selectedAtClick) {
      if (trRef.current && groupRef.current) {
        let transformableNodes = [groupRef.current!]
        trRef.current.nodes(transformableNodes)
        trRef.current.getLayer()?.batchDraw()
      }
    }
  }, [selectedAtClick])

  function synchronizeGroupWithShape() {
    const group = groupRef.current!
    const scaleX = group.scaleX()
    const scaleY = group.scaleY()
    const shape = shapeRef.current!
    const width = shape.width() * scaleX
    const height = shape.height() * scaleY

    group.scaleX(1)
    group.scaleY(1)

    shapeDelegate.updateShapeSize(shapeRef.current!, {
      x: width,
      y: height,
    })

    const markSelection = markSelectionRef.current
    if (markSelection) {
      markSelection.setAttrs({
        x: -10,
        y: -10,
        // x: shape.x(),
        // y: shape.y(),
        width: width + 20,
        height: height + 20,
      })
    }
  }

  function synchronizeTextWithShape() {
    shapeDelegate.synchronizeTextAndShape(shapeRef.current!, textRef.current!)
  }

  function synchronizeSelectionMark() {
    const markSelection = markSelectionRef.current
    const shape = shapeRef.current!
    if (markSelection) {
      markSelection.setAttrs({
        x: - 10,
        y: -10,
        // x: shape.x(),
        // y: shape.y(),
        width: shape.width() + 20,
        height: shape.height() + 20,
      })
    }
  }

  useEffect(() => {
    synchronizeTextWithShape()
    synchronizeSelectionMark()
  }, [])

  return (
    <React.Fragment key={element.id}>
      <Group
        id={element.id}
        key={element.id}
        ref={groupRef}
        draggable={true}
        listening={true}
        onTransform={(evt: KonvaEventObject<Event>) => {
          synchronizeGroupWithShape()
          synchronizeTextWithShape()
          synchronizeSelectionMark()
        }}
        onTransformEnd={(evt: KonvaEventObject<Event>) => {
          synchronizeGroupWithShape()
          synchronizeTextWithShape()
          synchronizeSelectionMark()
          const group = groupRef.current!

          const shape = shapeRef.current!
          const groupAbsolutePosition = group.absolutePosition()
          console.log("on group transform end", evt.target.id())

          updatePosition({
            id: element.id,
            x: groupAbsolutePosition.x,
            y: groupAbsolutePosition.y,
            height: shape.height(),
            width: shape.width(),
          })
        }}
        x={nodeConfig.x}
        y={nodeConfig.y}
      >
        {shapeDelegate.createShape(element, nodeConfig, shapeRef)}
        <Text
          id={ELEMENT_CHILD_PREFIX + "text_" + element.id}
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
        {selectedWithWindow && (
          <Rect
            ref={markSelectionRef}
            id={MARK_SELECTION_PREFIX + element.id}
            x={-10}
            y={-10}
            width={width(element) + 20}
            height={height(element) + 20}
            draggable={false}
            listening={false}
            opacity={1}
            strokeWidth={1}
            stroke={"#239EF4"}
          />
        )}
      </Group>
      {selectedAtClick && (!selectedWithWindow) && (
        <Transformer
          id={TRANSFORMER_PREFIX + element.id}
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

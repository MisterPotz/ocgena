import Konva from "konva"
import { MutableRefObject, useEffect, useMemo, useRef, useState } from "react"
import {
  AnyElement,
  PositionUpdatePayload,
} from "./editorSlice"
import React from "react"
import {
  Circle,
  Group,
  Text,
  Transformer,
} from "react-konva"
import { elementToNodeConfig } from "./Utils"
import { KonvaEventObject, NodeConfig } from "konva/lib/Node"
import { ShapeDelegateNew, selectShapeDelegate } from "./shapeDelegates"

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

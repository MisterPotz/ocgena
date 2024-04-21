import Konva from "konva"
import { useEffect, useMemo, useRef, useState } from "react"
import React from "react"
import { Group, Rect, Text, Transformer } from "react-konva"
import {
  elementToNodeConfigWithSize,
} from "./Utils"
import { KonvaEventObject } from "konva/lib/Node"
import { ShapeDelegateNew, selectShapeDelegate } from "./shapeDelegates"
import {
  ELEMENT_CHILD_PREFIX,
  MARK_SELECTION_PREFIX,
  TRANSFORMER_PREFIX,
} from "./Keywords"
import { MIN_HEIGHT, MIN_WIDTH, TextShapeProps } from "./Models"
import {
  height,
  width,
} from "./primitiveShapeUtils"


/*
 * Text is synchronized with the shape
 * Text is editable.
 * The whole shape is rezisable.
 */
export function AutoSizeTextShape({ element, updatePosition }: TextShapeProps) {
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
  const [textState, setTextState] = useState(() => element.text)


  // const [fontSize, setFontSize] = useState(30)
  // const rectSize = useRef<{ width: number; height: number }>({
  //   width: 100,
  //   height: (100 / 5) * 3,
  // })

  useEffect(() => {
    setupRemovableTextArea(
      groupRef.current!,
      textRef.current!,
      shapeRef.current!,
      editableTextAreaRef,
      textArea => {
        shapeDelegate.synchronizeTextAreaPosition(shapeRef.current!, textArea)
      },
      text => {
        setTextState(text)
      }
    )
  }, [])

  useEffect(() => {
    if (selectedAtClick) {
      if (trRef.current && groupRef.current) {
        let transformableNodes = [groupRef.current!]
        trRef.current.nodes(transformableNodes)
        trRef.current.getLayer()?.batchDraw()
      }
    }
  }, [selectedAtClick])

  useEffect(() => {
    const text = textRef.current!
    if (text && shapeRef.current) {
      let newWidth = text.width()
      let newHeight = text.height()
      let shouldUpdate = false
            // let currentFontSize = fontSize
      while (
        text.getTextWidth() > newWidth ||
        text.getTextHeight() > newHeight
      ) {
        // currentFontSize -= 1
        // textRef.current!.fontSize(currentFontSize)
        newWidth = textRef.current!.getTextWidth()
        newHeight = (newWidth / 5) * 3
        shouldUpdate = true
      }
      if (shouldUpdate) {
        text.setAttrs({
          width: newWidth,
          height: newHeight
        })
        synchronizeGroup()
        // setFontSize(currentFontSize)
      }
      text.getLayer()!.batchDraw()
    }
  }, [element.text, textState])

  function synchronizeSelectionMark() {
    const markSelection = markSelectionRef.current
    const text = textRef.current!
    const shape = shapeRef.current!

    if (markSelection) {
      markSelection.setAttrs({
        x: -10,
        y: -10,
        width: text.width() + 20,
        height: text.height() + 20,
      })
    }
  }

  function synchronizeTextWithShape() {
    if (textRef.current && shapeRef.current) {
      shapeDelegate.updateShapeToMatchText(shapeRef.current!, textRef.current!)
    }
  }

  function synchronizeGroup() {
    const group = groupRef.current!
    const scaleX = group.scaleX()
    const scaleY = group.scaleY()
    const shape = shapeRef.current!
    const width = shape.width() * scaleX
    const height = shape.height() * scaleY

    group.scaleX(1)
    group.scaleY(1)

    synchronizeSelectionMark()
    synchronizeTextWithShape()
  }

  useEffect(() => {
    groupRef.current!.setAbsolutePosition({
      x: element.x,
      y: element.y,
    })
    synchronizeGroup()
    if (trRef.current) {
      trRef.current?.nodes([groupRef.current!])
      trRef.current?.update()
    }
  }, [element.x, element.y, element.rawX, element.rawY, element.shape])

  useEffect(() => {
    console.log(
      "element width",
      width(element),
      "transformer size",
      trRef.current?.getSize?.(),
      "group size",
      groupRef.current!.size(),
    )
    console.log("shape size ", shapeRef.current!.size())
    if (trRef.current) {
      trRef.current.width(width(element))
      trRef.current.height(height(element))
      trRef.current.clearCache()
      trRef.current.forceUpdate()
      trRef.current._clearCaches()
      trRef.current._clearCaches()
      trRef.current.getLayer()!.batchDraw()
    }
  }, [width(element), height(element)])

  const nodeConfig = useMemo(() => {
    return {
      ...elementToNodeConfigWithSize(element, 100, (100 / 5) * 3),
      // draggable: false,
      // listening: false,
    }
  }, [element])

  return (
    <React.Fragment key={element.id}>
      <Group
        id={element.id}
        key={element.id}
        ref={groupRef}
        draggable={true}
        listening={true}
        onTransform={(evt: KonvaEventObject<Event>) => {
          synchronizeGroup()
        }}
        onTransformEnd={(evt: KonvaEventObject<Event>) => {
          synchronizeGroup()
          const group = groupRef.current!

          const shape = shapeRef.current!
          const groupAbsolutePosition = group.absolutePosition()

          updatePosition({
            id: element.id,
            x: groupAbsolutePosition.x,
            y: groupAbsolutePosition.y,
            width: 100,
            height: 100,
          })
        }}
        x={element.x}
        y={element.y}
      >
        {shapeDelegate.createShape(nodeConfig, shapeRef)}
        <Text
          id={ELEMENT_CHILD_PREFIX + "text_" + element.id}
          ref={textRef}
          fontSize={24}
          // ellipsis
          width={width(element)}
          height={width(element) * 3 / 5}
          align="center"
          verticalAlign="middle"
          wrap="word"
          text={textState}
          draggable={false}
          listening={true}
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
      {selectedAtClick && !selectedWithWindow && (
        <Transformer
          id={TRANSFORMER_PREFIX + element.id}
          ref={trRef}
          rotateEnabled={false}
          flipEnabled={false}
          draggable={true}
          listening={true}
          shouldOverdrawWholeArea={false}
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
  setText: (text : string) => void
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
        // textNode.text(textarea.value)
        setText(textarea.value)
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

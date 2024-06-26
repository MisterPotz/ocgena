import Konva from "konva"
import { useEffect, useMemo, useRef, useState } from "react"
import React from "react"
import { Group, Rect, Text, Transformer } from "react-konva"
import { elementToNodeConfigWithSize } from "./Utils"
import { KonvaEventObject } from "konva/lib/Node"
import { ShapeDelegateNew, selectShapeDelegate } from "./shapeDelegates"
import {
  ELEMENT_CHILD_PREFIX,
  MARK_SELECTION_PREFIX,
  TRANSFORMER_PREFIX,
} from "./Keywords"
import { MIN_HEIGHT, MIN_WIDTH, TextShapeProps } from "./Models"
import { height, width } from "./primitiveShapeUtils"

const IDEAL_HEIGHT_TO_WIDTH = 3 / 5

type IsUserNewLine = boolean
type TextLine = [IsUserNewLine, string]

function calculateTextHeight(lines: number, konvaText: Konva.Text) {
  return lines * konvaText.fontSize()
}

function calculateTextLinesMetrics(text: Konva.Text, textArr: TextLine[]) {
  const textHeight = calculateTextHeight(textArr.length, text)
  let maxWidth = text.measureSize(textArr[0][1]).width
  let biggestTextLine = textArr[0]

  text.getTextHeight()

  textArr.forEach(el => {
    const size = text.measureSize(el[1])
    if (size.width > maxWidth) {
      maxWidth = size.width
      biggestTextLine = el
    }
  })

  return {
    width: maxWidth,
    height: textHeight,
    heightToWidth: textHeight / maxWidth,
    biggestLine: biggestTextLine,
  }
}

function makeTextLines(wishedText: string): TextLine[] {
  return wishedText.split("\n").map(line => {
    return [true, line.trimEnd()]
  })
}
const SPACE = " "
const DASH = "-"

type WrapPlaceHypothesis = {
  newHeight: number
  newMaxWidth: number
  newLineArr: TextLine[]
}

function splitLineAroundInHalf(line: string): string[] {
  let low = 0
  let high = line.length
  let mid = (low + high) >>> 1
  let wrappingPlace = low
  function distanceFromMid(place: number) {
    return Math.abs(place - mid)
  }

  function decideIfCanWrapHere(i: number) {
    if (
      line[i] === SPACE &&
      distanceFromMid(i) <= distanceFromMid(wrappingPlace)
    ) {
      wrappingPlace = i
      return true
    } else if (
      line[i] === DASH &&
      distanceFromMid(i + 1) <= distanceFromMid(wrappingPlace)
    ) {
      wrappingPlace = i + 1
      return true
    }
    return false
  }
  for (let i = mid; i < line.length; i++) {
    if (decideIfCanWrapHere(i)) break
  }
  return [
    line.substring(low, wrappingPlace),
    line.substring(wrappingPlace, high),
  ]
}

function makeWrapPlaceHypothesis(
  konvaText: Konva.Text,
  line: TextLine,
): WrapPlaceHypothesis {
  const newLines = splitLineAroundInHalf(line[1])

  return {
    newMaxWidth: Math.max(
      konvaText._getTextWidth(newLines[0]),
      konvaText._getTextWidth(newLines[1]),
    ),
    newLineArr: [
      [line[0], newLines[0]],
      [false, newLines[1]],
    ],
    newHeight: calculateTextHeight(2, konvaText),
  }
}

// lets say it should support new lines
// and add  its new lines to bring the text size closer to ideal

function getSizeBeautifiedText(text: Konva.Text, wishedText: string) {
  let previousTextLines = makeTextLines(wishedText)
  let originalTextLines = previousTextLines
  if (previousTextLines.length == 0) {
    console.log("textlines is empty!", previousTextLines)
    return
  }

  let { heightToWidth: previousIteration, biggestLine } =
    calculateTextLinesMetrics(text, previousTextLines)

  function diffToIdeal(relation: number) {
    return Math.abs(IDEAL_HEIGHT_TO_WIDTH - relation)
  }

  let currentRelation = previousIteration
  let currentTextLines = previousTextLines

  let resultingRelation = currentRelation
  let resultingTextLines = currentTextLines

  while (diffToIdeal(currentRelation) <= diffToIdeal(previousIteration)) {
    const biggestLineIndex = currentTextLines.findIndex(el => {
      return el[1] === biggestLine[1]
    })!

    // reduce size of biggest line
    let newLines = makeWrapPlaceHypothesis(text, biggestLine)
    let tempCurrentTextLines = currentTextLines
    currentTextLines = currentTextLines.toSpliced(
      biggestLineIndex,
      1,
      ...newLines.newLineArr,
    )

    let currentLineMetrics = calculateTextLinesMetrics(text, currentTextLines)
    console.log(
      "currentLineMetrics,",
      currentLineMetrics,
      "text",
      currentTextLines,
    )

    let tempCurrentRelation = currentRelation
    currentRelation = currentLineMetrics.heightToWidth
    biggestLine = currentLineMetrics.biggestLine

    if (diffToIdeal(currentRelation) <= diffToIdeal(previousIteration)) {
      previousIteration = tempCurrentRelation
      previousTextLines = tempCurrentTextLines
      resultingTextLines = currentTextLines
      resultingRelation = currentRelation
    }
  }
  return resultingTextLines
}

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
    if (element.text && element.text.trim().length > 0) {
      let lines = getSizeBeautifiedText(textRef.current!, element.text)
      // let lines = makeTextLines(element.text)

      if (lines) {
        console.log(
          "measurement of ",
          lines,
          "is",
          calculateTextLinesMetrics(textRef.current!, lines),
        )
        textRef.current!.text(lines.map(el => el[1]).join("\n"))
        // textRef.current!.
        console.log(
          "applied size to textref, now",
          textRef.current!.getSize(),
          textRef.current!.text(),
        )
        shapeRef.current!.setSize(textRef.current!.getSize())
      }
    }
  }, [element.text])

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
      },
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
    if (trRef.current) {
      trRef.current.width(width(element))
      trRef.current.height(height(element))
      trRef.current.update()
      // trRef.current.forceUpdate()
      // trRef.current._clearCaches()
      // trRef.current.getLayer()!.batchDraw()
    }
  }, [width(element), height(element)])

  const nodeConfig = useMemo(() => {
    return {
      ...elementToNodeConfigWithSize(element, 10, 10),
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
            width: textRef.current!.getWidth(),
            height: textRef.current!.getHeight(),
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
          // width={undefined}
          // height={undefined}
          align="center"
          verticalAlign="middle"
          padding={0}
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
  setText: (text: string) => void,
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

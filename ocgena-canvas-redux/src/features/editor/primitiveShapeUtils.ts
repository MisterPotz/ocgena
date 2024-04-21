import Konva from "konva"
import {
  CircleShape,
  Element,
  PositionUpdatePayload,
  RectangleShape,
  SelectionWindow,
  SelectionWindowPayload,
  SpecificShape,
} from "./Models"

function isWithinBounds(
  elementLeft: number,
  elementTop: number,
  elementRight: number,
  elementBottom: number,
  windowLeft: number,
  windowTop: number,
  windowRight: number,
  windowBottom: number,
): boolean {
  return (
    elementLeft >= windowLeft &&
    elementRight <= windowRight &&
    elementTop >= windowTop &&
    elementBottom <= windowBottom
  )
}

function rightBound(element: Element) {
  switch (element.shape.type) {
    case "circle":
      return element.x + element.shape.radius * 2
    case "rect":
      return element.x + element.shape.width
  }
}

function bottomBound(element: Element) {
  switch (element.shape.type) {
    case "circle":
      return element.y + element.shape.radius * 2
    case "rect":
      return element.y + element.shape.height
  }
}

function width(element: Element) {
  switch (element.shape.type) {
    case "circle":
      return element.shape.radius * 2
    case "rect":
      return element.shape.width
  }
}

function height(element: Element) {
  switch (element.shape.type) {
    case "circle":
      return element.shape.radius * 2
    case "rect":
      return element.shape.height
  }
}

function getUpdatedShape(
  shape: SpecificShape,
  positionUpdatePayload: PositionUpdatePayload,
): SpecificShape {
  switch (shape.type) {
    case "circle":
      return { ...shape, radius: (positionUpdatePayload.width / 2).closestSize() }
    case "rect":
      return {
        ...shape,
        width: positionUpdatePayload.width.closestSize(),
        height: positionUpdatePayload.height.closestSize(),
      }
  }
}

function elementInSelectionWindow(
  element: Element,
  selectionWindow: SelectionWindowPayload,
): boolean {
  const { x: windowX, y: windowY, width, height } = selectionWindow
  const windowLeft = windowX
  const windowRight = windowX + width
  const windowTop = windowY
  const windowBottom = windowY + height

  let elementLeft, elementRight, elementTop, elementBottom

  switch (element.shape.type) {
    case "rect":
      const rect = element.shape as RectangleShape
      elementLeft = element.x
      elementTop = element.y
      elementRight = element.x + rect.width
      elementBottom = element.y + rect.height
      break

    case "circle":
      const circle = element.shape as CircleShape
      elementLeft = element.x
      elementTop = element.y
      elementRight = element.x + circle.radius * 2
      elementBottom = element.y + circle.radius * 2
      break

    default:
      return false // Unknown shape type
  }

  return isWithinBounds(
    elementLeft,
    elementTop,
    elementRight,
    elementBottom,
    windowLeft,
    windowTop,
    windowRight,
    windowBottom,
  )
}

function getRealWidth(shape: Konva.Shape) {
  return shape.width() * shape.scaleX()
}

function getRealHeight(shape: Konva.Shape) {
  return shape.height() * shape.scaleY()
}

export {
  isWithinBounds,
  elementInSelectionWindow,
  rightBound,
  bottomBound,
  width,
  height,
  getRealHeight,
  getRealWidth,
  getUpdatedShape
}

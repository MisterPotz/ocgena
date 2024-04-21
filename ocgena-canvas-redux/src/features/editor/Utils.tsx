import { RectConfig } from "konva/lib/shapes/Rect"
import { Element } from "./Models"
import { CircleConfig } from "konva/lib/shapes/Circle"
import Konva from "konva"
import {
  ELEMENT_CHILD_PREFIX,
  ELEMENT_CHILD_SHAPE_PREFIX,
  ELEMENT_PREFIX,
} from "./Keywords"
import { SpecificShape } from "./Models"

export interface Coord {
  x: number
  y: number
}

export function elementToNodeConfig(
  element: Element<SpecificShape>,
): CircleConfig | RectConfig {
  let baseConfig = {
    id: element.id,
    stroke: element.stroke,
    fill: element.fill,
    draggable: true,
    key: element.id,
    x: element.x,
    y: element.y,
  }
  switch (element.shape.type) {
    case "rect":
      let rectConfig: RectConfig = {
        ...baseConfig,
        width: element.shape.width,
        height: element.shape.height,
      }
      return rectConfig
    case "circle":
      let circleConfig: CircleConfig = {
        ...baseConfig,
        radius: element.shape.radius,
      }
      return circleConfig
  }
}

export function tryGetElementId(
  shape: Konva.Shape | Konva.Stage,
): string | null {
  let currentEl: any | null = shape
  const layer = shape.getLayer()!
  const stage = shape.getStage()!

  while (currentEl !== layer && currentEl !== stage) {
    if (currentEl.id && currentEl.id().startsWith(ELEMENT_PREFIX)) {
      return currentEl.id() as string
    }
    currentEl = currentEl.getParent()
  }
  return null
}

export function tryGetShapeElementOfGroup(
  group: Konva.Group,
): Konva.Shape | null {
  const result = group.getChildren().find(el => {
    if (el.id().startsWith(ELEMENT_CHILD_SHAPE_PREFIX)) {
      return true
    }
    return false
  })
  return result?.getType() == "Shape" ? (result as Konva.Shape) : null
}

export function isGroup(node : Konva.Node): node is Konva.Group {
  return node.getType() == "Group"
}
export interface Element<Shape extends SpecificShape = SpecificShape> {
  //   type: Tool
  x: number
  y: number
  rawX: number
  rawY: number
  shape: Shape
  fill?: string
  stroke?: string
  id: string
  selectedAtClick?: boolean
  selectedWithWindow?: boolean
  text?: string
}

export type ShapeType = "rect" | "circle"

export type AnyElement = Element<SpecificShape>

interface ElementShape {
  type: ShapeType
}
export interface RectangleShape extends ElementShape {
  type: "rect"
  width: number
  height: number
}
export interface CircleShape extends ElementShape {
  type: "circle"
  radius: number
}

export type SpecificShape = RectangleShape | CircleShape

export type SpecificShapeType = SpecificShape["type"]

export type Elements = Element<SpecificShape>[]

export interface SelectionWindowPayload {
  x: number
  y: number
  width: number
  height: number,
}

export interface SelectionWindow extends SelectionWindowPayload{
  x: number
  y: number
  width: number
  height: number,
  selectedElementIds : string[]
}

export type PositionUpdatePayload = {
  id: string
  x: number
  y: number
  width: number
  height: number
}

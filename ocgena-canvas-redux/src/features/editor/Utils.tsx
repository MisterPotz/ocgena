import { RectConfig } from "konva/lib/shapes/Rect"
import { SpecificShape, Element } from "./editorSlice"
import { CircleConfig } from "konva/lib/shapes/Circle"

export function elementToNodeConfig(
    element: Element<SpecificShape>,
  ): CircleConfig | RectConfig {
    let baseConfig = {
      id: element.id,
      stroke: element.stroke,
      fill: element.fill,
      draggable: true,
      key: element.id,
      // onDragStart={} <--- want to create a common object to not define a callback to each rendered shape
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
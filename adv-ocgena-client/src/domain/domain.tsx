import { simulation } from "ocgena";
import { Observable } from "rxjs";
import { GraphvizPane } from "renderer/allotment-components/graphviz-pane";
import { useObservable } from "observable-hooks";
import { StructureNode } from "./StructureNode";

export type OcDotContent = string
export type SimulationConfig = simulation.config.SimulationConfig

export type SimulationArgument = {
    ocDot: OcDotContent | null,
    simulationConfig: SimulationConfig | null
}

export interface UiError {
    readonly message: string
}


export type ProjectWindowStructure = StructureNode<ProjectWindowId>

export type ProjectWindowId = string

export interface ProjectWindow {
    readonly title: string
    id : ProjectWindowId
    isOpened: boolean
    createReactComponent: (onSizeChangeObservable: Observable<number[]>) => JSX.Element
}


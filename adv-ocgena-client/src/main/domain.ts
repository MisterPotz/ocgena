import { simulation } from "ocgena";
import { Observable } from "rxjs";
import { StructureNode } from "../domain/StructureNode";

export type OcDotContent = string
export type SimulationConfig = simulation.config.SimulationConfig

export type SimulationArgument = {
    ocDot: OcDotContent | undefined | null,
    simulationConfig: SimulationConfig | undefined | null
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
    createReactComponent: (onSizeChangeObservable: Observable<number[]>, visible: boolean) => JSX.Element
}


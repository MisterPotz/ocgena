

export interface StructureNode<T> {
}

export interface StructureWithTabs<T> extends StructureNode<T> {
    tabs: T[];
    currentTabIndex: number;
}

export interface StructureParent<T> extends StructureNode<T> {
    direction: "column" | "row";
    children: StructureNode<T>[];
}

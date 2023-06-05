import { BehaviorSubject, Subject } from 'rxjs';
import { ProjectWindow, ProjectWindowId, ProjectWindowStructure } from './domain';
import { ProjectWindowProvider } from './Project';
import { produce } from 'immer';

export interface StructureNode<T> {
  id: string;
}

export interface StructureWithTabs<T> extends StructureNode<T> {
  tabs: T[];
  currentTabIndex: number;
}

export interface StructureParent<T> extends StructureNode<T> {
  direction: 'column' | 'row';
  children: StructureNode<T>[];
}

export function isAllotedPane<T>(
  structureNode: StructureNode<T>
): structureNode is StructureParent<T> {
  if (typeof structureNode !== 'object') return false;
  return 'direction' in structureNode;
}

export function isTabPane<T>(
  structureNode: StructureNode<T>
): structureNode is StructureWithTabs<T> {
  if (typeof structureNode !== 'object') return false;
  return 'tabs' in structureNode;
}

export class StructureWindowSelector {
  currentProjectWindowStructure: ProjectWindowStructure;

  constructor(initialProjectWindowStructure: ProjectWindowStructure) {
    this.currentProjectWindowStructure = initialProjectWindowStructure;
  }

  updateWithSelectedProjectWindow(selectedProjectWindow: ProjectWindowId): boolean {
    let selected = false;
    let immerized = produce(this.currentProjectWindowStructure, (draft) => {
      selected = this.findAndSelect(draft, selectedProjectWindow);
    })
    if (selected) {
      console.log("immerized: " + JSON.stringify(immerized))
      console.log("immerized shallow equal to previous: " + (immerized===this.currentProjectWindowStructure))
      this.currentProjectWindowStructure = immerized
    }
    return selected
  }

  private findAndSelect(structureNode : StructureNode<ProjectWindowId>, selectedProjectWindow : ProjectWindowId) : boolean /*was changed*/ {
    if (isAllotedPane(structureNode)) {
        for (let node of structureNode.children) {
            let result = this.findAndSelect(node, selectedProjectWindow)
            if (result) {
                return true;
            }
        }
    } else if (isTabPane(structureNode)) {
        let tabs = structureNode.tabs
        let index = tabs.findIndex((projWindow) => projWindow === selectedProjectWindow)
        if (index != -1 && structureNode.currentTabIndex != index) {
            
            structureNode.currentTabIndex = index
            return true
        }
    }
    return false
  } 
}


export type WindowsMap = {  
  [projectWindowId : ProjectWindowId] : ProjectWindow
}


export class ProjectWindowManager {
    structureWindowSelector : StructureWindowSelector

    projectWindowStructure$ : BehaviorSubject<ProjectWindowStructure>
    windowsMap : WindowsMap

    constructor(
      windowStructure: ProjectWindowStructure,
      windowsMap : WindowsMap) {
        this.structureWindowSelector = new StructureWindowSelector(windowStructure);
        this.projectWindowStructure$ = new BehaviorSubject(windowStructure);
        this.windowsMap = windowsMap
    }

    clickTabOfProjectWindow(projectWindow: ProjectWindowId) {
        let changeResult = this.structureWindowSelector.updateWithSelectedProjectWindow(projectWindow)
        if (changeResult) {

            this.projectWindowStructure$.next(this.structureWindowSelector.currentProjectWindowStructure)
        }
    }
}
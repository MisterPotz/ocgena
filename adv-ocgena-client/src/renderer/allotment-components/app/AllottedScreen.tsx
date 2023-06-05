import React, { useEffect, useRef, useState } from "react";

import { ActivityBar } from "../activity-bar";
import { AuxiliaryBar } from "../auxiliary-bar";
import { EditorParent, EditorParentProps } from "../editor";
import { Panel } from "../panel";
import { Sidebar } from "../sidebar";
import { Allotment, LayoutPriority } from "allotment";
import { ActionBar } from "../actions-bar";
import { GraphvizPane } from "../graphviz-pane";
import Graph from "renderer/components/Graph";
import { EditorProps, EditorWrapper } from "renderer/components/Editor";
import { useObservable, useObservableCallback, useObservableState, useSubscription } from "observable-hooks";
import { appService } from "renderer/AppService";
import { ProjectWindow, ProjectWindowStructure } from "domain/domain";
import { StructureNode, StructureParent, StructureWithTabs } from "domain/StructureNode";
import { Observable } from "rxjs";

export interface Document {
  title: string;
  icon: string;
  editorProps: EditorProps
}

export const ACTIVITIES = [
  "Explorer",
  "Search",
  "Source Control",
  "Run and Debug",
  "Extensions",
];

// export function createOcDotEditor(
//   onNewInput: (input: string) => void,
//   ocDot?: string | null,
// ): Document[] {
//   return [
//     {
//       title: "ocdot file", icon: "ts", editorProps: {
//         editorId: "ocdot",
//         onNewInput,
//         ocDot: ocDot || null
//       },
//     },
//     // { title: "yaml", icon: "css" },
//   ];
// }



export type TabProps = {
  title: string
}
function Tab(
  { title
  }: TabProps
) {
  return <div className="ms-0 bg-gray-300 text-black hover:bg-gray-500 h-8 max-w-xs text-ellipsis min-w-fit w-">
    {title}
  </div>
}

type TabPaneProps = {
  onSizeChangeObservable?: Observable<number[]>,
  structureWithTabs: StructureWithTabs<ProjectWindow>,
}

function TabPane({
  structureWithTabs: structureNode,
  onSizeChangeObservable
}: TabPaneProps) {
  let projectWindows = structureNode.tabs
  let visibleIndex = structureNode.currentTabIndex

  let sizeChange$ = onSizeChangeObservable ? onSizeChangeObservable : useObservable(() => new Observable<number[]>())

  return (
    // tabs and the editors
    <div className="container">
      <div className="flex flex-row justify-start h-fit container">
        {projectWindows.map((projectWindow) => <Tab title={projectWindow.title} />)}
      </div>
      <div className={`w-full h-full`}>
        <Allotment>
          {projectWindows.map((projectWindow, index) => {
            let visible = index == visibleIndex
            return <Allotment.Pane visible={visible}>
              {projectWindow.createReactComponent(sizeChange$)}
            </Allotment.Pane>
          })}
        </Allotment>
      </div>
    </div>
  )
}

export type AllottedPane = {
  onSizeChangeObservable?: Observable<number[]>,
  structureParent: StructureParent<ProjectWindow>
}

function isAllotedPane<T>(structureNode: StructureNode<T>): structureNode is StructureParent<T> {
  if (typeof structureNode !== "object") return false
  return "direction" in structureNode
}

function isTabPane<T>(structureNode: StructureNode<T>): structureNode is StructureWithTabs<T> {
  if (typeof structureNode !== "object") return false
  return "tabs" in structureNode
}

function makeStructurePane<T extends ProjectWindow>(
  structureNode: StructureNode<T>,
  onSizeChangeObservable: Observable<number[]> | undefined = undefined,
  ) {
  return isAllotedPane(structureNode)
    ? <AllottedPane structureParent={structureNode} onSizeChangeObservable={onSizeChangeObservable} />
    : isTabPane(structureNode)
      ? <TabPane structureWithTabs={structureNode} onSizeChangeObservable={onSizeChangeObservable}/>
      : <div className="container h-full bg-yellow-300 rounded-md text-black p-4">Unknown structure node encountered</div>
}

function AllottedPane({
  structureParent: paneParent,
  onSizeChangeObservable
}: AllottedPane) {
  let structureNodes = paneParent.children

  let vertical = paneParent.direction == "column"
  let [onChangeSize, sizeChange$] = useObservableCallback((event$) => event$ as Observable<number[]>)
  let [parentSizeChange$] = useState(onSizeChangeObservable)

  if (parentSizeChange$ != null) {
    useSubscription(parentSizeChange$, (event: number[]) => {
      console.log("AllottedPane: useSubscription: " + JSON.stringify(event))
      onChangeSize(event)
    })
  }

  return <Allotment className="w-full h-full" vertical={vertical} onChange={onChangeSize}>
    {
      structureNodes.map((structureNode) => {
        return <Allotment.Pane visible>
          {makeStructurePane(structureNode, onSizeChangeObservable)}
        </Allotment.Pane>
      })
    }
  </Allotment>
}

export type AllottedScreenProps = {
  activity: number;
  activityBar: boolean;
  // editorVisible: boolean;
  // openEditors: Document[];
  panelVisible: boolean;
  primarySideBar: boolean;
  primarySideBarPosition: "left" | "right";
  secondarySideBar: boolean;
  projectWindowStructure: ProjectWindowStructure,
  onClickStart: () => void;
  onOpenNewFile: () => void;
  onClickRefresh: () => void;
  onActivityChanged: (activity: number) => void;
  onPanelVisibleChanged: (visible: boolean) => void;
};

export const AllottedScreen = ({
  activity,
  activityBar,
  panelVisible,
  primarySideBar,
  primarySideBarPosition,
  secondarySideBar,
  projectWindowStructure,
  onOpenNewFile,
  onClickStart,
  onClickRefresh,
  onActivityChanged,
  onPanelVisibleChanged,
}: AllottedScreenProps) => {

  // const auxiliarySidebar = (
  //   <Allotment.Pane
  //     key="auxiliarySidebar"
  //     minSize={170}
  //     priority={LayoutPriority.Low}
  //     preferredSize={300}
  //     visible={secondarySideBar}
  //     snap

  //   >
  //     <GraphvizPane dotSrc="" registerParentSizeUpdate={registerParentSizeUpdate} loading={loadingGraph} />
  //   </Allotment.Pane>
  // );

  // const sidebar = (
  //   <Allotment.Pane
  //     key="sidebar"
  //     minSize={170}
  //     priority={LayoutPriority.Low}
  //     preferredSize={300}
  //     visible={primarySideBar}
  //     snap
  //   >
  //     <Sidebar
  //       title={ACTIVITIES[activity]}
  //       documents={openEditors}
  //       openEditors={openEditors}
  //       onOpenEditorsChange={(openEditor) => {
  //         onOpenEditorsChanged(openEditor);
  //       }}
  //     />
  //   </Allotment.Pane>
  // );

  let structureNode = projectWindowStructure

  return (
    <Allotment proportionalLayout={false} vertical>

      <Allotment.Pane maxSize={48} minSize={48}>
        <ActionBar pauseButtonEnabled
          startButtonMode="start"
          onClickStart={onClickStart}
          onClickRefresh={onClickRefresh}
          onOpenNewFile={onOpenNewFile} />
      </Allotment.Pane>

      <Allotment.Pane>
        <Allotment>
          <Allotment.Pane
            key="activityBar"
            minSize={48}
            maxSize={48}
            visible={activityBar}
          >
            <ActivityBar
              checked={activity}
              items={[
                "files",
                "search",
                "source-control",
                "debug-alt",
                "extensions",
              ]}
              onClick={(index) => {
                onActivityChanged(index);
              }}
            />
          </Allotment.Pane>
          {/* {primarySideBarPosition === "left" ? sidebar : auxiliarySidebar} */}
          <Allotment.Pane
            key="content"
            minSize={300}
            priority={LayoutPriority.High}
          >
            {makeStructurePane(structureNode)}
              {/* <Allotment.Pane
                key="terminal"
                minSize={78}
                preferredSize="40%"
                visible={panelVisible}
              >
                <Panel
                  maximized={false}
                  onClose={() => {
                    onEditorVisibleChanged(true);
                    onPanelVisibleChanged(false);
                  }}
                  onMaximize={() => {
                    onEditorVisibleChanged(false);
                    onPanelVisibleChanged(true);
                  }}
                  onMinimize={() => {
                    onEditorVisibleChanged(true);
                    onPanelVisibleChanged(true);
                  }}
                />
              </Allotment.Pane> */}
          </Allotment.Pane>
          {/* {primarySideBarPosition === "right" ? sidebar : auxiliarySidebar} */}
        </Allotment>
      </Allotment.Pane>
    </Allotment>
  );
};

import React, { useState } from "react";

import { ActivityBar } from "../activity-bar";
import { AuxiliaryBar } from "../auxiliary-bar";
import { EditorParent, EditorParentProps } from "../editor";
import { Panel } from "../panel";
import { Sidebar } from "../sidebar";
import { Allotment, LayoutPriority } from "allotment";
import { ActionBar } from "../actions-bar";
import { GraphvizPane } from "../graphviz-pane";
import Graph from "renderer/components/Graph";
import { OcDotEditorProps } from "renderer/components/Editor";
import { useObservableState } from "observable-hooks";
import { appService } from "renderer/AppService";

export interface Document {
  title: string;
  icon: string;
  editorProps: OcDotEditorProps
}

export const ACTIVITIES = [
  "Explorer",
  "Search",
  "Source Control",
  "Run and Debug",
  "Extensions",
];

export function createOcDotEditor(
  onNewInput: (input: string) => void,
  ocDot?: string | null,
) : Document[]{
  return [
    {
      title: "ocdot file", icon: "ts", editorProps: {
        editorId: "ocdot",
        onNewInput,
        ocDot: ocDot || null
      },
    },
    // { title: "yaml", icon: "css" },
  ];
}

export type AppProps = {
  activity: number;
  activityBar: boolean;
  editorVisible: boolean;
  openEditors: Document[];
  panelVisible: boolean;
  primarySideBar: boolean;
  primarySideBarPosition: "left" | "right";
  secondarySideBar: boolean;
  onClickStart: () => void;
  onOpenNewFile: () => void;
  onClickRefresh: () => void;
  onActivityChanged: (activity: number) => void;
  onEditorVisibleChanged: (visible: boolean) => void;
  onOpenEditorsChanged: (documents: Document[]) => void;
  onPanelVisibleChanged: (visible: boolean) => void;

};

export const AppEditor = ({
  activity,
  activityBar,
  editorVisible,
  openEditors,
  panelVisible,
  primarySideBar,
  primarySideBarPosition,
  secondarySideBar,
  onOpenNewFile,
  onClickStart,
  onClickRefresh,
  onActivityChanged,
  onEditorVisibleChanged,
  onOpenEditorsChanged,
  onPanelVisibleChanged,
}: AppProps) => {

  const [onParentSizeUpdate, setOnParentSizeUpdate] = useState<(() => void) | null>(null);

  const registerParentSizeUpdate: (onParentSizeUpdate: () => void) => void = (onParentSizeUpdate: () => void) => {
    setOnParentSizeUpdate(() => onParentSizeUpdate);
  }

  const loadingGraph = useObservableState(appService.getGraphvizLoading(), false);

  const auxiliarySidebar = (
    <Allotment.Pane
      key="auxiliarySidebar"
      minSize={170}
      priority={LayoutPriority.Low}
      preferredSize={300}
      visible={secondarySideBar}
      snap

    >
      <GraphvizPane dotSrc="" registerParentSizeUpdate={registerParentSizeUpdate} loading={loadingGraph} />
    </Allotment.Pane>
  );

  const sidebar = (
    <Allotment.Pane
      key="sidebar"
      minSize={170}
      priority={LayoutPriority.Low}
      preferredSize={300}
      visible={primarySideBar}
      snap
    >
      <Sidebar
        title={ACTIVITIES[activity]}
        documents={openEditors}
        openEditors={openEditors}
        onOpenEditorsChange={(openEditor) => {
          onOpenEditorsChanged(openEditor);
        }}
      />
    </Allotment.Pane>
  );

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
        <Allotment onChange={() => {
          if (onParentSizeUpdate) {
            console.log("invoking update function");
            onParentSizeUpdate();
          }
        }}>
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
          {primarySideBarPosition === "left" ? sidebar : auxiliarySidebar}
          <Allotment.Pane
            key="content"
            minSize={300}
            priority={LayoutPriority.High}
          >
            <Allotment
              vertical
              snap
              onVisibleChange={(index, value) => {
                if (index === 0) {
                  onEditorVisibleChanged(value);
                } else if (index === 1) {
                  onPanelVisibleChanged(value);
                }
              }}
            >
              <Allotment.Pane key="editor" minSize={70} visible={editorVisible}>
                <EditorParent
                  documents={openEditors}
                  onDocumentsChange={(documents) => {
                    onOpenEditorsChanged(documents);
                  }}
                />
              </Allotment.Pane>
              <Allotment.Pane
                key="terminal"
                minSize={78}
                preferredSize="40%"
                visible={panelVisible}
              >
                <Panel
                  maximized={!editorVisible}
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
              </Allotment.Pane>
            </Allotment>
          </Allotment.Pane>
          {primarySideBarPosition === "right" ? sidebar : auxiliarySidebar}

        </Allotment>
      </Allotment.Pane>
    </Allotment>
  );
};

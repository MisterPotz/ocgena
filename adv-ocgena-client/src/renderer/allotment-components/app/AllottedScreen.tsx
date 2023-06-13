import React, { useEffect, useRef, useState } from 'react';

import { ActivityBar } from '../activity-bar';
import { AuxiliaryBar } from '../auxiliary-bar';
import { EditorParent, EditorParentProps } from '../editor';
import { Panel } from '../panel';
import { Sidebar } from '../sidebar';
import { Allotment, LayoutPriority } from 'allotment';
import { ActionBar } from '../actions-bar';
import { GraphvizPane } from '../graphviz-pane';
import Graph from 'renderer/components/Graph';
import { EditorProps, EditorWrapper } from 'renderer/components/Editor';
import {
  useObservable,
  useObservableCallback,
  useObservableState,
  useSubscription,
} from 'observable-hooks';
import { appService } from 'renderer/AppService';
import {
  ProjectWindow,
  ProjectWindowId,
  ProjectWindowStructure,
} from 'main/domain';
import {
  StructureNode,
  StructureParent,
  StructureWithTabs,
  isAllotedPane,
  isTabPane,
} from 'domain/StructureNode';
import { Observable } from 'rxjs';
import style from './AllottedScreen.module.css';
import { ProjectState } from 'domain/Project';
import { FileType } from 'main/preload';

export interface Document {
  title: string;
  icon: string;
  editorProps: EditorProps;
}

export const ACTIVITIES = [
  'Explorer',
  'Search',
  'Source Control',
  'Run and Debug',
  'Extensions',
];

export type TabProps = {
  title: string;
  active: boolean;
  onClick: () => void;
};
function Tab({ title, active, onClick }: TabProps) {
  return (
    <div
      onClick={onClick}
      className={`
        ${active && 'bg-white'}
        ${!active && 'hover:bg-zinc-200'}
        relative
        flex
        h-9 
        w-16
        min-w-fit
        flex-shrink-0
        flex-row
        items-center
        overflow-hidden
        text-ellipsis
        whitespace-nowrap
        rounded-none
        border-0
        border-r-1 
        border-solid
        border-r-black 
        border-opacity-10
        bg-transparent 
        px-2 
        text-center 
        text-xs
        rounded-tr-lg
         text-black
        text-opacity-75 
        shadow-none transition-colors
        duration-300
        ease-in-out
        `}
    >
      {title}
    </div>
  );
}

type TabPaneProps = {
  onSizeChangeObservable?: Observable<number[]>;
  structureWithTabs: StructureWithTabs<ProjectWindowId>;
};

function TabPane({
  structureWithTabs: structureNode,
  onSizeChangeObservable,
}: TabPaneProps) {
  let projectWindows = structureNode.tabs;
  let visibleIndex = structureNode.currentTabIndex;

  let sizeChange$ = onSizeChangeObservable
    ? onSizeChangeObservable
    : useObservable(() => new Observable<number[]>());

  console.log('doing tab pane for ' + structureNode.id);

  let project = appService.getActiveProject();

  return (
    // tabs and the editors
    <div className="h-full w-full flex flex-col">
      <div className="relative h-10 overflow-hidden bg-white">
        <div
          className={`${style.tabList} absolute inset-0 flex h-fit w-full flex-1 cursor-pointer flex-row flex-nowrap justify-start overflow-x-auto bg-zinc-50`}
        >
          {projectWindows.map((projectWindowId, index) => {
            let active = index == visibleIndex;

            let projectWindow = project.getProjectWindow(projectWindowId)!;

            return (
              <Tab
                key={projectWindow.title}
                title={projectWindow.title}
                active={active}
                onClick={project.clickTab.bind(project, projectWindow.id)}
              />
            );
          })}
        </div>
      </div>
      <div className={`h-full w-full flex-grow`}>
        <Allotment key={structureNode.id} className="h-full w-full">
          {projectWindows.map((projectWindowId, index) => {
            let visible = index == visibleIndex;
            let projectWindow = project.getProjectWindow(projectWindowId)!;
            return (
              <Allotment.Pane
                className="h-full w-full"
                key={projectWindow.title}
                visible={visible}
              >
                {projectWindow.createReactComponent(sizeChange$, visible)}
              </Allotment.Pane>
            );
          })}
        </Allotment>
      </div>
    </div>
  );
}

export type AllottedPane = {
  onSizeChangeObservable?: Observable<number[]>;
  structureParent: StructureParent<ProjectWindow>;
};

function makeStructurePane<T extends ProjectWindowId>(
  structureNode: StructureNode<T>,
  onSizeChangeObservable: Observable<number[]> | undefined = undefined
) {
  return isAllotedPane(structureNode) ? (
    <AllottedPane
      structureParent={structureNode}
      onSizeChangeObservable={onSizeChangeObservable}
    />
  ) : isTabPane(structureNode) ? (
    <TabPane
      structureWithTabs={structureNode}
      onSizeChangeObservable={onSizeChangeObservable}
    />
  ) : (
    <div className="h-full w-full rounded-md bg-yellow-300 p-4 text-black">
      Unknown structure node encountered
    </div>
  );
}

function AllottedPane({
  structureParent: paneParent,
  onSizeChangeObservable,
}: AllottedPane) {
  let structureNodes = paneParent.children;

  let vertical = paneParent.direction == 'column';
  let [onChangeSize, sizeChange$] = useObservableCallback(
    (event$) => event$ as Observable<number[]>
  );
  let [parentSizeChange$] = useState(onSizeChangeObservable);

  // if (parentSizeChange$ != null) {
  //   useSubscription(parentSizeChange$, (event: number[]) => {
  //     console.log('AllottedPane: useSubscription: ' + JSON.stringify(event));
  //     // onChangeSize(event);
  //   });
  // }

  console.log('doing allotted pane for + ' + paneParent.id);
  return (
    <Allotment
      className="h-full w-full"
      vertical={vertical}
      key={paneParent.id}
      onChange={onChangeSize}
    >
      {structureNodes.map((structureNode) => {
        return (
          <Allotment.Pane key={structureNode.id} visible>
            {makeStructurePane(structureNode, sizeChange$)}
          </Allotment.Pane>
        );
      })}
    </Allotment>
  );
}

export type AllottedScreenProps = {
  activity: number;
  activityBar: boolean;
  // editorVisible: boolean;
  // openEditors: Document[];
  panelVisible: boolean;
  primarySideBar: boolean;
  primarySideBarPosition: 'left' | 'right';
  secondarySideBar: boolean;
  projectState?: ProjectState;
  // projectWindowStructure?: ProjectWindowStructure;
  onClickStart: () => void;
  onClickStop : ()=> void;
  onOpenNewFile: (fileType: FileType) => void;
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
  onClickStop,
  projectState,
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

  let projectWindowStructure = projectState?.windowStructure;

  let structureNode = projectWindowStructure;
  console.log('allotting structure : ' + JSON.stringify(structureNode));
  return (
    <Allotment proportionalLayout={false} vertical>
      <Allotment.Pane minSize={36} maxSize={36}>
        <ActionBar
          pauseButtonEnabled
          startButtonMode={
            projectState?.startButtonMode
              ? projectState.startButtonMode
              : 'disabled'
          }
          onClickStop={onClickStop}
          onClickStart={onClickStart}
          onClickRefresh={onClickRefresh}
          onOpenNewFile={onOpenNewFile}
        />
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
                'files',
                'search',
                'source-control',
                'debug-alt',
                'extensions',
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
            {structureNode && makeStructurePane(structureNode)}
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

import { Allotment, LayoutPriority } from "allotment";
import "allotment/dist/style.css";
import { useAppSelector } from "../../app/hooks";
import { executionModeSelector } from "../../app/redux";
import "@vscode/codicons/dist/codicon.css";
import {
  EditorWrapper,
  GraphvizPane,
  exampleModel,
} from "../ocdot/ocdot_editor";
import { OCDotToDOTConverter } from "../ocdot/ocdot_converter";
import { useMemo } from "react";
import { AST } from "ocdot-parser";

export const ACTIVITIES = [
  "Explorer",
  "Search",
  "Source Control",
  "Run and Debug",
  "Extensions",
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
          ${active && "bg-white"}
          ${!active && "hover:bg-zinc-200"}
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

// function TabPane({
//   structureWithTabs: structureNode,
//   onSizeChangeObservable,
// }: TabPaneProps) {
//   let projectWindows = structureNode.tabs;
//   let visibleIndex = structureNode.currentTabIndex;

//   let sizeChange$ = onSizeChangeObservable
//     ? onSizeChangeObservable
//     : useObservable(() => new Observable<number[]>());

//   console.log("doing tab pane for " + structureNode.id);

//   let project = appService.getActiveProject();

//   return (
//     // tabs and the editors
//     <div className="h-full w-full flex flex-col">
//       <div className="relative h-10 overflow-hidden bg-white">
//         <div
//           className={`${style.tabList} absolute inset-0 flex h-fit w-full flex-1 cursor-pointer flex-row flex-nowrap justify-start overflow-x-auto bg-zinc-50`}
//         >
//           {projectWindows.map((projectWindowId, index) => {
//             let active = index == visibleIndex;

//             let projectWindow = project.getProjectWindow(projectWindowId)!;

//             return (
//               <Tab
//                 key={projectWindow.title}
//                 title={projectWindow.title}
//                 active={active}
//                 onClick={project.clickTab.bind(project, projectWindow.id)}
//               />
//             );
//           })}
//         </div>
//       </div>
//       <div className={`h-full w-full flex-grow`}>
//         <Allotment key={structureNode.id} className="h-full w-full">
//           {projectWindows.map((projectWindowId, index) => {
//             let visible = index == visibleIndex;
//             let projectWindow = project.getProjectWindow(projectWindowId)!;
//             return (
//               <Allotment.Pane
//                 className="h-full w-full"
//                 key={projectWindow.title}
//                 visible={visible}
//               >
//                 {projectWindow.createReactComponent(sizeChange$, visible)}
//               </Allotment.Pane>
//             );
//           })}
//         </Allotment>
//       </div>
//     </div>
//   );
// }

export const EditorArea = () => {
  const dotSrc = useMemo(() => {
    const ast = AST.parse(exampleModel, { rule: AST.Types.OcDot });
    const converter = new OCDotToDOTConverter(ast);
    return converter.compileDot()
  }, []);



  return (
    <div className="flex flex-col items-start h-full container">
      <Panes></Panes>
      <div className="flex-grow container">
        <Allotment>
          <Allotment.Pane preferredSize={"50%"} minSize={150}>
            <EditorWrapper />
          </Allotment.Pane>
          <Allotment.Pane preferredSize={"50%"} minSize={150}>
            <GraphvizPane dotSrc={dotSrc} loading={false} registerParentSizeUpdate={(cb) => {
              // cb()
            }} />
          </Allotment.Pane>
        </Allotment>
      </div>
    </div>
  );
};

export const Panes = () => {
  return (
    <div className="container h-7 bg-slate-100 flex flex-row divide-neutral-500">
      <PaneTab active text="first tab" />
      <PaneTab active={false} text="second tab" />
    </div>
  );
};

export const PaneTab = ({
  active,
  text,
}: {
  active: boolean;
  text: string;
}) => {
  const activeStyles = "bg-white";
  const notActiveStyles = "bg-gray-200";

  const resStyles = active ? activeStyles : notActiveStyles;
  return (
    <div
      className={`flex flex-row max-w-80 items-center relative px-2 divide-transparent divide-x-4
      ${resStyles}`}
    >
      <span className="flex-grow overflow-ellipsis overflow-hidden text-base line-clamp-1">
        {text}
      </span>
      <div className="relative scale-90 w-6 h-6">
        <i
          className="align-middle codicon codicon-close relative scale-125 text-xl hover:bg-slate-200
          rounded-sm
          "
          style={{ bottom: "1px" }}
        />
      </div>
    </div>
  );
};

export const LeftArea = () => {
  return (
    <div className="container h-full">
      <Allotment className="h-full container" vertical>
        <Allotment.Pane preferredSize={"50%"}>
          <div className="flex flex-col items-stretch h-full w-full overflow-auto">
            <div className="text-sm w-full min-h-7 bg-slate-200 p-1">Model files</div>

            {/* <div className="flex flex-col"></div> */}
            <div className="flex flex-col items-center justify-center flex-grow mx-2">
              <LeftAreaButton text={"New OC-net model"} />
            </div>
          </div>
        </Allotment.Pane>
        <Allotment.Pane preferredSize={"50%"}>
          <div className="flex flex-col justify-start items-stretch h-full w-full overflow-auto">
            <div className="text-sm min-h-7 w-full bg-slate-200 p-1">
              Simulation configuration files
            </div>

            {/* <div className="flex flex-col"></div> */}
            <div className="flex flex-col items-center justify-center flex-grow mx-2">
              <LeftAreaButton text={"New simulation configuration"} />
            </div>
          </div>
        </Allotment.Pane>
      </Allotment>
    </div>
  );
};

export const BottomArea = () => {
  return <div className="container">Bottom area</div>;
};

export const ActionBar = () => {
  return <div className="container h-9">Action bar</div>;
};

export const Layout = () => {
  return (
    <div className="container flex flex-col h-full">
      <ActionBarDynamic></ActionBarDynamic>

      <Allotment proportionalLayout={false} className="container flex-grow">
        <Allotment.Pane minSize={150}>
          <LeftArea></LeftArea>
        </Allotment.Pane>
        <Allotment.Pane priority={LayoutPriority.High} preferredSize={"70%"}>
          <Allotment proportionalLayout={false} vertical>
            <Allotment.Pane
              /* className="h-5/6" */ preferredSize={"70%"}
              priority={LayoutPriority.High}
            >
              <EditorArea></EditorArea>
            </Allotment.Pane>
            <Allotment.Pane>
              <BottomArea></BottomArea>
            </Allotment.Pane>
          </Allotment>
        </Allotment.Pane>
      </Allotment>
    </div>
  );
};

export interface ActionButtonProps {
  onClick: () => void;
  text: string;
  iconClass: string;
  buttonStyle?: string;
  disabled?: boolean;
}

export function ActionButton(props: ActionButtonProps) {
  return (
    <button
      disabled={props.disabled}
      onClick={props.onClick}
      className={`
            relative
            flex
            flex-row 
            justify-center
            items-center
            rounded-none
            px-2
            shadow-none
            transition-colors
            duration-300
            ease-in-out
            h-9
            ${props.buttonStyle ? props.buttonStyle : ""} `}
    >
      <div
        className={`codicon ${props.iconClass} relative scale-90 text-xs `}
      />
      <div className={`relative pe-1 ps-1 text-sm text-black`}>
        {props.text}
      </div>
    </button>
  );
}

export type StartButtonMode = "executing" | "start" | "disabled";

export type ActionBarProps = {
  startButtonMode: StartButtonMode;
  pauseButtonEnabled: boolean;
  onClickStop: () => void;
  onClickStart: () => void;
  onClickRefresh: () => void;
  // onOpenNewFile: (fileType: FileType) => void;
};

export type LeftAreaButtonProp = {
  text: string;
  // onOpenNewFile: (fileType: FileType) => void;
};

export function LeftAreaButton({ text }: LeftAreaButtonProp) {
  return (
    <button
      // onClick={props.onClick}
      className={`
            relative
            flex
            flex-row 
            justify-center
            items-center
            rounded-sm
            px-2
            border-1
            shadow-md
            transition-colors
            duration-300
            ease-in-out
            min-h-9
            hover:bg-gray-100
            `}
    >
      <i className={`codicon codicon-add relative scale-90 text-xs `} />
      <span className={`relative pe-1 ps-1 text-sm text-black flex-grow`}>
        {text}
      </span>
    </button>
  );
}

export function FileButton({
  onClick,
  text,
}: {
  onClick: () => void;
  text: string;
}) {
  return (
    <ActionButton
      onClick={onClick}
      iconClass="codicon-symbol-file"
      text={text}
      buttonStyle={`text-black bg-transparent text-opacity-75 border-0 hover:bg-zinc-200`}
    />
  );
}

const baseButtonClasses = `bg-transparent text-black border-0`;
const disabledStyle = {
  classes: `${baseButtonClasses} 
  text-opacity-50 bg-zinc-300`,
  text: "Start",
};
const readyStyle = {
  classes: `${baseButtonClasses}
  text-opacity-75 hover:bg-green-300 outline-green-500 outline-2 outline`,
  text: "Start",
};
const runningStyle = {
  classes: `text-opacity-75 bg-green-400 hover:bg-red-300 text-black`,
  text: "Stop",
};

export function RunButton({ onClick }: { onClick: () => void }) {
  const executionMode = useAppSelector(executionModeSelector);
  let style: { classes: string; text: string } | null = null;
  switch (executionMode) {
    case "ready":
      style = readyStyle;
      break;
    case "in_progress":
      style = runningStyle;
      break;
    case "blocked":
      style = disabledStyle;
      break;
  }

  return (
    <ActionButton
      onClick={() => {}}
      iconClass="codicon-debug-start"
      text={style.text}
      buttonStyle={style.classes}
      disabled={executionMode === "blocked"}
    />
  );
}

export function ActionBarDynamic() {
  return (
    <div
      className={`flex flex-row items-center justify-start h-9 container bg-zinc-50`}
    >
      <div
        className="
            rounded-none
            border-0
            border-r-1 border-solid border-r-black border-opacity-10 bg-transparent px-2 text-sm text-black"
      >
        <b>OCGena</b>
      </div>
      <FileButton text={"Open model file"} onClick={() => {}} />
      <FileButton text="Open configuration file" onClick={() => {}} />
      <RunButton onClick={() => {}} />
    </div>
  );
}

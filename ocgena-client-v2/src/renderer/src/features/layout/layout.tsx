import { Allotment, LayoutPriority } from "allotment";
import "allotment/dist/style.css";

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
  return <div className="container">Editor area</div>;
};

export const LeftArea = () => {
  return <div className="container">Left area</div>;
};

export const BottomArea = () => {
  return <div className="container">Bottom area</div>;
};

export const ActionBar = () => {
  return <div className="container h-9">Action bar</div>;
};

export const Layout = () => {
  return (
    <div className="container h-full">
      <ActionBar></ActionBar>

      <Allotment proportionalLayout={false} className="container h-full">
        <Allotment.Pane>
          <LeftArea></LeftArea>
        </Allotment.Pane>
        <Allotment.Pane priority={LayoutPriority.High} preferredSize={"70%"}>
          <Allotment proportionalLayout={false} vertical>
            <Allotment.Pane /* className="h-5/6" */ preferredSize={"70%"} priority={LayoutPriority.High}>
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

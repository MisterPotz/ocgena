import {  useState } from "react";
import { AllottedScreen } from "./allotment-components";
import styles from "./MainScreen.module.css";
import classNames from "classnames";
import { useObservableState } from "observable-hooks";
import { appService } from "./AppService";
import { FileType } from "main/preload";
console.log(styles)

export const DefaultMainScreen = (
) => {
  return MainScreen(
    {
      activityBar: true,
      primarySideBar: false,
      primarySideBarPosition: "left",
      secondarySideBar: true,
    }
  )
}

type MainScreenProps = {
  activityBar: boolean;
  primarySideBar: boolean;
  primarySideBarPosition: "left" | "right";
  secondarySideBar: boolean;
  ocDotSrc?: string,
}
const onNewInput = (newInput : string ) => {
  appService.openConfigurationFile();
}

export const MainScreen = ({
  activityBar,
  primarySideBar,
  primarySideBarPosition,
  secondarySideBar,
}: MainScreenProps) => {
  const [panelVisible, setPanelVisible] = useState(true);
  const [activity, setActivity] = useState(0);
  
  const projectState = useObservableState(appService.getProjectState$(), undefined)
  console.log("MainScreen using new project state: " + JSON.stringify(projectState?.windowStructure))
  return (
    <div className={classNames(styles.editorsContainer, "w-full", "h-full")}>
      <AllottedScreen
        activity={activity}
        activityBar={activityBar}
        projectState={projectState}
        panelVisible={panelVisible}
        primarySideBar={primarySideBar}
        primarySideBarPosition={primarySideBarPosition}
        secondarySideBar={secondarySideBar}
        onActivityChanged={setActivity}
        onPanelVisibleChanged={setPanelVisible}
        onClickStart={() => { 
          appService.onClickStart()
        }}
        onClickRefresh={() => { }}
        onOpenNewFile={(fileType : FileType) => {
          console.log("opening new file of type %s", fileType);
          appService.openFile(fileType);
        }}
      />
    </div>
  );
}
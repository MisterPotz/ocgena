import { useEffect, useState } from "react";
import { AllottedScreen } from "./allotment-components";
import { Document } from "./allotment-components";
import styles from "./VisualStudioCode.module.css";
import classNames from "classnames";
import { useObservable, useObservableState } from "observable-hooks";
import { appService } from "./AppService";
import { map } from "rxjs/operators";
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
  appService.openNewFile();
}

export const MainScreen = ({
  activityBar,
  primarySideBar,
  primarySideBarPosition,
  secondarySideBar,
}: MainScreenProps) => {
  const [panelVisible, setPanelVisible] = useState(true);
  const [activity, setActivity] = useState(0);
  
  const projectState = useObservableState(appService.getProjectState$(), appService.getDefaultProjectState())

  return (
    <div className={classNames(styles.editorsContainer, "w-full", "h-full")}>
      <AllottedScreen
        activity={activity}
        activityBar={activityBar}
        projectWindowStructure={projectState.windowStructure}
        panelVisible={panelVisible}
        primarySideBar={primarySideBar}
        primarySideBarPosition={primarySideBarPosition}
        secondarySideBar={secondarySideBar}
        onActivityChanged={setActivity}
        onPanelVisibleChanged={setPanelVisible}
        onClickStart={() => { }}
        onClickRefresh={() => { }}
        onOpenNewFile={() => {
          console.log("opening new file");
          appService.openNewFile();
        }}
      />
    </div>
  );
}
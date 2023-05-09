import { useState } from "react";
import { AppEditor, DOCUMENTS } from "./allotment-components";
import { Document } from "./allotment-components";
import styles from "./VisualStudioCode.module.css";
import classNames from "classnames";
console.log(styles)

export const DefaultVisualStudioCode = (
) => {
  return VisualStudioCode(
    {
      activityBar: true,
      primarySideBar: true,
      primarySideBarPosition: "left",
      secondarySideBar: true,
    }
  )
}

type VSCodeParams = {
  activityBar: boolean;
  primarySideBar: boolean;
  primarySideBarPosition: "left" | "right";
  secondarySideBar: boolean;
}

export const VisualStudioCode = ({
  activityBar,
  primarySideBar,
  primarySideBarPosition,
  secondarySideBar,
}: VSCodeParams) => {
  const [editorVisible, setEditorVisible] = useState(true);
  const [panelVisible, setPanelVisible] = useState(true);
  const [activity, setActivity] = useState(0);
  const [openEditors, setOpenEditors] = useState<Document[]>(DOCUMENTS);

  return (
    <div className={classNames(styles.editorsContainer, "w-full", "h-full")}>
      <AppEditor
        activity={activity}
        activityBar={activityBar}
        editorVisible={editorVisible}
        panelVisible={panelVisible}
        openEditors={openEditors}
        primarySideBar={primarySideBar}
        primarySideBarPosition={primarySideBarPosition}
        secondarySideBar={secondarySideBar}
        onActivityChanged={setActivity}
        onEditorVisibleChanged={setEditorVisible}
        onOpenEditorsChanged={setOpenEditors}
        onPanelVisibleChanged={setPanelVisible}
        onClickStart={() => { }}
        onClickRefresh={() => { }}
      />
    </div>
  );
}
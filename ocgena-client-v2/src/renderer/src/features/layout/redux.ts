import { createAppSlice } from "../../app/createAppSlice";

enum EditorTab {
  OCDOT_EDITOR,
  GRAPHVIZ,
  GUI_EDITOR,
}

interface PaneWithTabs<Tab> {
  tabs: Tab[];
  currentTab?: number | null;
}

interface EditorPane extends PaneWithTabs<EditorTab> {}

interface EditorAreaProject {
  type: "project";
  panes: EditorPane[]; // in a row
}

interface EditorAreaNoProject {
  type: "no project";
}

type EditorArea = EditorAreaProject | EditorAreaNoProject

// at once can be open any combination of windows: ocdot / graphviz / sim editor
// or: gui editor / sim editor

enum LeftPaneType {
  NO_PROJECT,
  GUI_EDITING,
  DSL_EDITING,
}

interface LeftAreaNoProject {
  type: LeftPaneType.NO_PROJECT;
}

interface LeftAreaGui {
  type: LeftPaneType.GUI_EDITING;
}

interface LeftAreaDSL {
  type: LeftPaneType.DSL_EDITING;
}

type LeftArea = LeftAreaNoProject | LeftAreaDSL | LeftAreaGui;

enum BottomTab {
  ERRORS,
  EXECUTION,
  RUNS,
}

interface BottomArea extends PaneWithTabs<BottomTab> {
    visible: boolean
}

interface LayoutState {
  editorArea: EditorArea;
  leftArea: LeftArea;
  bottomArea: BottomArea;
}

const initialState: LayoutState = {
  editorArea: { type: "no project" },
  bottomArea: {
    tabs: [BottomTab.RUNS, BottomTab.EXECUTION, BottomTab.ERRORS],
    visible: false
  },
  leftArea: {
    type: LeftPaneType.NO_PROJECT
  }
};

export const layoutSlice = createAppSlice({
  name: "layout",
  initialState,
  reducers: (create) => ({

  }),
  selectors: {}
});

export const { } = layoutSlice.actions
export const { } = layoutSlice.selectors
import { EditorWrapper } from 'renderer/components/Editor';
import { isOcDotRegistered, registerOcDot } from 'renderer/ocdot/OcDotMonarch';
import { Observable } from 'rxjs';
import * as monaco from 'monaco-editor/esm/vs/editor/editor.api';
import { EditorDelegate } from './EditorDelegate';
import { ProjectWindow, ProjectWindowId } from '../domain';
import { ProjectWindowManager } from '../StructureNode';
import { trimIndent } from 'ocdot-parser/lib/exts';

export interface ClickHandler {
  clickTab(projectWindowId: ProjectWindowId): void;
}

const exampleModel = trimIndent(`
  ocnet {
    a -> b
    b -> c
    c-> a
    transitions {
      t1 t2
    }
    places { 
      p1 p2 p3
    }

    t3 -> subgraph {
      p3 p4
      subgraph { 
        p5
      }
    }
    t8 -> ad

    p1 => t1 -> p2 [color ="red"] 
    p2 -> t2 
    t2 (3*k + 1)=> p3 [color="orange"]
  }
`)

export type OpenedFile = {
  contents ?: string, 
  filePath ?: string
}

export interface EditorHolder {
  readonly editorKey : 'editor'
  readonly editorDelegate : EditorDelegate
}

export class ModelEditor implements ProjectWindow, EditorHolder {
  readonly title: string = 'OC-net Model';
  static id: ProjectWindowId = 'model';
  isOpened: boolean = false;
  readonly editorDelegate = new EditorDelegate("ocdot", "Model file");
  projectWindowId: string;
  id = ModelEditor.id;
  readonly editorKey = 'editor';

  constructor(projectWindowId: ProjectWindowId) {
    this.projectWindowId = projectWindowId;
  }

  collectFile(): OpenedFile {
    return {
      contents : this.editorDelegate.currentContent,
      filePath : this.editorDelegate.openedFilePath
    }
  }

  getEditorCurrentInput$() {
    return this.editorDelegate.getEditorCurrentInput$();
  }

  updateEditorWithContents(newContents: string, filePath : string) {
    this.editorDelegate.updateEditorWithContentsFromFile(newContents, filePath);
  }

  collectOcDot() {
    return this.editorDelegate.currentContent;
  }

  createReactComponent = (onSizeChangeObservable: Observable<number[]>) => (
    <EditorWrapper
      editorCreator={(htmlElement: HTMLElement) => {
        if (!isOcDotRegistered()) {
          monaco.editor.defineTheme('ocDotTheme', {
            base: 'vs',
            inherit: true,
            rules: [
              { token: 'mult', foreground: '2563eb', fontStyle: 'italic bold' },
            ],
            colors: {},
          });

          registerOcDot();
        }

        let newEditor = monaco.editor.create(htmlElement, {
          value: [exampleModel].join('\n'),
          language: 'ocdot',
          automaticLayout: true,
          theme: 'ocDotTheme',
          fontLigatures: true,
          autoIndent: 'full',
        });
        this.editorDelegate.onNewEditorInput(exampleModel)

        return newEditor;
      }}
      editorId={this.title}
      key={this.title}
      {...this.editorDelegate.createProps()}
    />
  );
}

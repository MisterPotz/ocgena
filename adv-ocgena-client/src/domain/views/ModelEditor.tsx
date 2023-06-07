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

export class ModelEditor implements ProjectWindow {
  readonly title: string = 'model of my OC net';
  static id: ProjectWindowId = 'model';
  isOpened: boolean = false;
  private editorDelegate = new EditorDelegate();
  projectWindowId: string;
  id = ModelEditor.id;

  constructor(projectWindowId: ProjectWindowId) {
    this.projectWindowId = projectWindowId;
  }

  getEditorCurrentInput$() {
    return this.editorDelegate.getEditorCurrentInput$();
  }

  updateEditorWithContents(newContents: string) {
    this.editorDelegate.updateEditorWithContents(newContents);
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

        return newEditor;
      }}
      editorId={this.title}
      key={this.title}
      {...this.editorDelegate.createProps()}
    />
  );
}

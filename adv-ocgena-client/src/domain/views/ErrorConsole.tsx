import { Observable, Subject } from 'rxjs';
import { useObservableState } from 'observable-hooks';
import Graph from 'renderer/components/Graph';
import { ProjectWindow } from '../../main/domain';
import { ProjectWindowManager } from '../StructureNode';
import { Panel } from 'renderer/allotment-components/panel';
import XtermComponent from 'renderer/allotment-components/xterm';
import { Console } from 'renderer/allotment-components/console';
import Anser from 'anser';

export class ErrorConsole implements ProjectWindow {
  readonly title: string = 'Errors';
  isOpened: boolean = false;
  static id: string = 'Errors';
  id = ErrorConsole.id;

  outputLine$ = new Subject<string[] | undefined>();
  clean$ = new Subject<boolean>();
  fitRequest$ = new Subject<boolean>()

  constructor() {}

  writeLine(line: string) {
    this.outputLine$.next([line]);
  }

  fit() {
    this.fitRequest$.next(true)
  }

  writeLines(lines: string[]) {
    this.outputLine$.next(undefined)
    let linesHtml = lines.map((line) => Anser.ansiToHtml(line))
    console.log(linesHtml[0])
    this.outputLine$.next(linesHtml)
  }

  clean() {
    this.outputLine$.next(undefined)
  }

  createReactComponent = (onSizeChangeObservable: Observable<number[]>, visible : boolean) => {
    return (
      // <Panel
      //   sizeChange$={onSizeChangeObservable}
      //   outputLine$={this.outputLine$}
      //   clean$={this.clean$}
      //   maximized={false}
      //   fitRequest$={this.fitRequest$}
      //   onClose={() => {  }}
      //   onMaximize={() => {  }}
      //   onMinimize={() => {  }}
      // />
      <Console
        sizeChange$={onSizeChangeObservable}
        outputLine$={this.outputLine$}
        clean$={this.clean$}
        visible={visible}
        fitRequest$={this.fitRequest$}
    />
    );
  };
}

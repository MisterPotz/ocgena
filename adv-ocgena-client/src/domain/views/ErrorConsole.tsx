import { Observable, Subject } from 'rxjs';
import { useObservableState } from 'observable-hooks';
import Graph from 'renderer/components/Graph';
import { ProjectWindow } from '../domain';
import { ProjectWindowManager } from '../StructureNode';
import { Panel } from 'renderer/allotment-components/panel';

export class ErrorConsole implements ProjectWindow {
  readonly title: string = 'Errors';
  isOpened: boolean = false;
  static id: string = 'Errors';
  id = ErrorConsole.id;

  outputLine$ = new Subject<string[]>();
  clean$ = new Subject<boolean>();

  constructor() {}

  writeLine(line: string) {
    this.outputLine$.next([line]);
  }

  writeLines(lines: string[]) {
    this.outputLine$.next(lines)
  }

  clean() {
    this.clean$.next(true);
  }

  createReactComponent = (onSizeChangeObservable: Observable<number[]>) => {
    return (
      <Panel
        sizeChange$={onSizeChangeObservable}
        outputLine$={this.outputLine$}
        clean$={this.clean$}
        maximized={false}
        onClose={() => {  }}
        onMaximize={() => {  }}
        onMinimize={() => {  }}
      />
    );
  };
}

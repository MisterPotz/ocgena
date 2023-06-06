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

  outputLine$ = new Subject<string>();

  constructor() {}

  writeLine(line: string) {
    this.outputLine$.next(line);
  }

  createReactComponent = (onSizeChangeObservable: Observable<number[]>) => {
    return (
      <Panel
        sizeChange$={onSizeChangeObservable}
        outputLine$={this.outputLine$}
        maximized={false}
        onClose={() => {  }}
        onMaximize={() => {  }}
        onMinimize={() => {  }}
      />
    );
  };
}

import { Observable, Subject } from 'rxjs';
import { useObservableState } from 'observable-hooks';
import Graph from 'renderer/components/Graph';
import { ProjectWindow } from '../domain';
import { ProjectWindowManager } from '../StructureNode';
import { Panel } from 'renderer/allotment-components/panel';
import XtermComponent from 'renderer/allotment-components/xterm';
import { Console } from 'renderer/allotment-components/console';


export class ExecutionConsole implements ProjectWindow {
    readonly title: string = 'Execution';
    isOpened: boolean = false;
    static id: string = 'Execution';
    id = ExecutionConsole.id;
  
    outputLine$ = new Subject<string[] | undefined>();
    clean$ = new Subject<boolean>();
    fitRequest$ = new Subject<boolean>()
  
    constructor() {}

    fit() {
      this.fitRequest$.next(true)
    }
  
    writeLine(line: string) {
      this.outputLine$.next([...line.split("\n")]);
    }
  
    writeLines(lines: string[]) {
      this.outputLine$.next(lines)
    }
  
    clean() {
      this.outputLine$.next(undefined);
    }
  
    createReactComponent = (onSizeChangeObservable: Observable<number[]>, visible: boolean) => {
      return (
        <Console
          sizeChange$={onSizeChangeObservable}
          outputLine$={this.outputLine$}
          clean$={this.clean$}
          fitRequest$={this.fitRequest$}
          visible={visible}
        />
      );
    };
  }
  
import { Observable, Subject } from 'rxjs';
import { useObservableState } from 'observable-hooks';
import Graph from 'renderer/components/Graph';
import { ProjectWindow } from '../../main/domain';
import { ProjectWindowManager } from '../StructureNode';
import { Panel } from 'renderer/allotment-components/panel';
import XtermComponent from 'renderer/allotment-components/xterm';
import { Console } from 'renderer/allotment-components/console';
import Anser from 'anser';
import { red } from '../../main/red';


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
      console.log("execution lines length %d", lines.length)
      if (lines.length >= 999 || !this.countNewLines(lines[0], 1000)) {
        console.log("slicing the output")
        this.outputLine$.next([`<div>${Anser.ansiToHtml(`${red}Output was truncated`)}</div>`, ...lines.slice(0, 999)])
      } else {
        this.outputLine$.next(lines)
      }
    }
  
    countNewLines(str : string, limit: number) {
      let count = 0;
      for(let i = 0; i < str.length && count < limit; i++) {
          if (str[i] === '\n') {
              count++;
          }
      }
      return count < limit;
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
  
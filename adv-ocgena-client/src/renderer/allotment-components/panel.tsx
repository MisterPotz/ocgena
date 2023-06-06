import 'xterm/css/xterm.css';

import classNames from 'classnames';
import { useEffect, useRef, useState } from 'react';
import { Terminal } from 'xterm';
import { FitAddon } from 'xterm-addon-fit';

import styles from './panel.module.css';
import { Observable, Subscription } from 'rxjs';

export type PanelProps = {
  maximized: boolean;
  outputLine$: Observable<string>;
  sizeChange$: Observable<number[]>;
  onClose: () => void;
  onMaximize: () => void;
  onMinimize: () => void;
};

export const Panel = ({
  maximized,
  outputLine$,
  sizeChange$,
  onClose,
  onMaximize,
  onMinimize,
}: PanelProps) => {
  const ref = useRef(null!);
  const [term, setTerminal] = useState<Terminal | undefined>(undefined);
  const [fitAddon, setFitAddon] = useState<FitAddon | undefined>(undefined);

  useEffect(() => {
    let subscription1: Subscription | undefined;
    let subscription2: Subscription | undefined;

    if (term) {
      console.log('terminal subscribed to line output');
      fitAddon?.fit();

      subscription1 = outputLine$.subscribe((line) => {
        term.writeln(line);
      });
      subscription2 = sizeChange$.subscribe((newsize) => {
        fitAddon?.fit();
      });

    }

    return () => {
      subscription1?.unsubscribe();
      subscription2?.unsubscribe();
    };
  }, [outputLine$, term]);

  useEffect(() => {
    const term = new Terminal({
      fontSize: 13,
      theme: {
        background: '#FFFFFF',
        foreground: '#333333',
        cursor: '#333333',
        selectionBackground: '#ADD6FF',
        black: '#000000',
        red: '#CD3131',
        green: '#0DBC79',
        yellow: '#E5E510',
        blue: '#2472C8',
        magenta: '#BC3FBC',
        cyan: '#11A8CD',
        white: '#E5E5E5',
        brightBlack: '#666666',
        brightRed: '#F14C4C',
        brightGreen: '#23D18B',
        brightYellow: '#F5F543',
        brightBlue: '#3B8EEA',
        brightMagenta: '#D670D6',
        brightCyan: '#29B8DB',
        brightWhite: '#E5E5E5'
    }
      // theme: { background: 'rgb(30,30,30)' },
    });

    const fitAddon = new FitAddon();
    setFitAddon(fitAddon);

    setTerminal(term);
    term.loadAddon(fitAddon);

    term.open(ref.current);
    const prompt = () => {
      term.write('\r\n$ ');
    };

    term.writeln('Welcome to allotment');
    term.writeln(
      'This is a local terminal emulation, without a real terminal in the back-end.'
    );
    term.writeln('Type some keys and commands to play around.');
    term.writeln('');
    prompt();
    window.addEventListener('resize', () => fitAddon.fit());

    fitAddon.fit();
    fitAddon.activate(term);

    // term.onKey((e: { key: string; domEvent: KeyboardEvent }) => {
    //   const ev = e.domEvent;
    //   const printable = !ev.altKey && !ev.ctrlKey && !ev.metaKey;

    //   if (ev.keyCode === 13) {
    //     prompt();
    //   } else if (ev.keyCode === 8) {
    //     // Do not delete the prompt
    //     /*         if (term._core.buffer.x > 2) {
    //       term.write("\b \b");
    //     } */
    //   } else if (printable) {
    //     term.write(e.key);
    //   }
    // });
  }, []);

  return (
    <div className={` bg-white border-white h-full w-full flex flex-col`}>
      <div className={`${styles.title} flex h-9 overflow-hidden pl-2 pr-2 justify-between`}>
        <div className={styles.actionBar}>
          <ul className={styles.actionsContainer}>
            <li className={classNames(styles.actionItem, 'checked', "text-opacity-75 text-black")}>
              <a className={classNames(styles.actionLabel, "text-black text-opacity-75")}>
                Terminal
              </a>
              <div className={classNames(styles.activeItemIndicator, "bg-white bg-opacity-75")} />
            </li>
          </ul>
        </div>
        <div>
          <ul className={styles.actionsContainer}>
            <li>
              {maximized ? (
                <a
                  className={classNames(
                    'codicon codicon-chevron-down',
                    styles.actionLabel
                  )}
                  role="button"
                  title="Minimize Panel Size"
                  onClick={onMinimize}
                />
              ) : (
                <a
                  className={classNames(
                    'codicon codicon-chevron-up',
                    styles.actionLabel
                  )}
                  role="button"
                  title="Maximize Panel Size"
                  onClick={onMaximize}
                />
              )}
            </li>
            <li>
              <a
                className={classNames(
                  'codicon codicon-close',
                  styles.actionLabel
                )}
                role="button"
                title="Close Panel"
                onClick={onClose}
              ></a>
            </li>
          </ul>
        </div>
      </div>
      <div className={styles.content}>
        <div ref={ref} className={styles.terminalWrapper}></div>
      </div>
    </div>
  );
};

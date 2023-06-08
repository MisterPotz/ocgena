import React, { useEffect, useRef } from 'react';
import { Observable, Subscription } from 'rxjs';
import { container } from 'webpack';
import { FitAddon } from 'xterm-addon-fit';
import styles from './console.module.css'
export type ConsoleProps = {
    outputLine$: Observable<string[] | undefined>;
    clean$: Observable<boolean>;
    visible: boolean,
    sizeChange$: Observable<number[]>;
    fitRequest$: Observable<boolean>;
};

class Terminal {
    ref: HTMLDivElement | undefined
    buffer : string = ''

    constructor(ref: HTMLDivElement | undefined) {
        this.ref = ref
    }

    private ensureDivWrapped(input: string): string {
        const trimmed = input.trim();
        if (trimmed.startsWith('<div>') && trimmed.endsWith('</div>')) {
          return input;
        } else {
          return `<div>${input}</div>`;
        }
      }
      
    setRef(ref: HTMLDivElement | undefined) {
        if (ref)
            ref.innerHTML = this.buffer
        this.ref = ref
    }

    writeLine(line: string) {
        if (this.ref) {
            this.ref.innerHTML += this.ensureDivWrapped(line)
        }
        this.buffer += this.ensureDivWrapped(line)
        console.log('have ref: ' + this.ref != null + 'writing line %s', this.ensureDivWrapped(line))
    }

    writeLines(lines: string[]) {
        for (let line of lines) {
            this.writeLine(line)
        }
    }

    clean() {

        if (this.ref)
            this.ref.innerHTML = ""
        this.buffer = ""
    }
}

export const Console = ({
    outputLine$,
    sizeChange$,
    visible,
    fitRequest$,
    clean$,
  }: ConsoleProps) => {
  // Create a ref object to refer to the terminal's DOM element
  const containerRef = useRef<HTMLDivElement | null>(null);
  const terminalRef = useRef<Terminal | null>(null); // We keep terminal instance in a ref
  const fitAddonRef = useRef<FitAddon | null>(null); // Also keeping FitAddon instance in a ref

  useEffect(() => {
    let subscriptionWriteLine: Subscription | undefined;
    let subscriptionSizeChange: Subscription | undefined;
    let subscriptionClean: Subscription | undefined;

    if (terminalRef.current) {

      console.log('terminal subscribed to line output');
      // fitAddon?.fit();

      subscriptionWriteLine = outputLine$.subscribe((lines) => {
        let term = terminalRef.current
        if (lines) {
            if (term) { 
                term.writeLines(lines);
            }
        } else {
          term?.clean();
        }
      });
      subscriptionSizeChange = sizeChange$.subscribe((newsize) => {
        // fitAddon?.fit();
      });
      // subscriptionClean = clean$.subscribe((clean) => {
      //   let term = terminalRef.current
      //   term?.clean();
      // });
    }

    return () => {
      subscriptionWriteLine?.unsubscribe();
      subscriptionSizeChange?.unsubscribe();
      subscriptionClean?.unsubscribe();
    };
  }, [terminalRef.current]);
  
  useEffect(() => {
    if (terminalRef.current && containerRef.current) {
        terminalRef.current.setRef(containerRef.current)
    }
  }, [containerRef.current, terminalRef.current])

  useEffect(() => {
    let term = new Terminal(containerRef?.current ? containerRef.current : undefined)
    terminalRef.current = term
    // Clean up the terminal when the component is unmounted
    return () => {
        console.log("disposing of terminal");
    }
  }, []);  // The effect should run only once when the component is mounted

//   useEffect(() => {
//     // Handle window resize
//     const handleResize = () => {
//       // On window resize, refit the terminal
//       if (fitAddonRef.current){ 
//         fitAddonRef.current.fit();
//       }
//     };

//     window.addEventListener('resize', handleResize);
//     return () => {
//       window.removeEventListener('resize', handleResize);
//     };
//   }, []);

  return (
    <div className={`${styles.console} h-full w-full overflow-auto text-sm text-black text-opacity-90`} ref={containerRef}></div>
  );
};

import { AST } from 'ocdot-parser';
import { PeggySyntaxError } from 'ocdot-parser/lib/ocdot.peggy';
import { OCDotToDOTConverter } from 'ocdot/converter';
import { Observable, Subject } from 'rxjs';
import * as rxops from 'rxjs/operators'


export class AppService {

    private subject = new Subject<string>();
    private graphvizDot = new Subject<string>();
    private internalEditorSubject = new Subject<string>();

    private convertRawOcDotToDot(rawOcDot: string) : string | null {
        let result = null
        try {
            const ast = AST.parse(rawOcDot, { rule: AST.Types.OcDot })
            const converter = new OCDotToDOTConverter(ast);

            result = converter.compileDot();
        } catch (e: PeggySyntaxError | any) {
            console.log(e)
        }
        return result
    }

    getGraphvizObservable() : Observable<string> {
        return this.graphvizDot;
    }

    getFileSourceOcDotObservable(): Observable<string> {
        return this.subject;
    }

    initialize() {
        window.electron.ipcRenderer.on('file-opened', (fileName, contents) => {
            this.subject.next(contents as string)
        });

        this.internalEditorSubject.pipe(
            rxops.debounceTime(1000),
            rxops.map(((rawOcDot) => {
                console.log("accepting ocdot value " + rawOcDot);
                return this.convertRawOcDotToDot(rawOcDot);
            })))
            .subscribe((newDot) => {
                console.log("new dot: " + newDot)
                if (newDot) {
                    this.graphvizDot.next(newDot);
                }
            })
    }

    openNewFile() {
        window.electron.ipcRenderer.sendMessage('open', []);
    }

    onNewOcDotEditorValue(newValue: string) {
        this.internalEditorSubject.next(newValue);
    }
}

export const appService = new AppService();
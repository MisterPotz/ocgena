import { OcDotContent, ProjectWindowStructure } from 'domain/domain';
import { ProjectSingleSimulationExecutor } from "domain/ProjectSingleSimulationExecutor";
import { Project, ProjectState } from "domain/Project";
import { AST } from 'ocdot-parser';
import { PeggySyntaxError } from 'ocdot-parser/lib/ocdot.peggy';
import { OCDotToDOTConverter } from 'ocdot/converter';
import { Observable, Subject } from 'rxjs';
import * as rxops from 'rxjs/operators'


export class AppService {
    private openedProject = new Project();


    getActiveProject(): Project {
        return this.openedProject;
    }

    getProjectState$() {
        return this.openedProject.projectState$
    }

    openNewFile() {
        window.electron.ipcRenderer.sendMessage('open', []);
    }

    initialize() {
        window.electron.ipcRenderer.on('file-opened', (fileName, fileContents) => {
            this.openedProject.onFileOpened(fileContents as string)
        });
    }
}

export const appService = new AppService(); 
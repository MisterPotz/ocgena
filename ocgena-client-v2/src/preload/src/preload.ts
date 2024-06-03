// See the Electron documentation for details on how to use preload scripts:
// https://www.electronjs.org/docs/latest/tutorial/process-model#preload-scripts

import { contextBridge, ipcRenderer } from "electron";
import { com } from "../../shared/events";

const toMain = [com("toMain")];
const fromMain = [com("fromMain")];

contextBridge.exposeInMainWorld(com("api"), {
  request: (channel: string, ...data: any[]) => {
    return ipcRenderer.invoke(channel, ...data);
  },
});

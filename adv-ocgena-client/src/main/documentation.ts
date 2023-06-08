import path from "path";
import { resolveDocHtmlPath, resolveHtmlPath } from "./util";
import { app, BrowserWindow, shell, ipcMain, dialog } from 'electron';

export function createDocumentationWindow() {
  const RESOURCES_PATH = app.isPackaged
    ? path.join(process.resourcesPath, 'assets')
    : path.join(__dirname, '../../assets');

  const getAssetPath = (...paths: string[]): string => {
    return path.join(RESOURCES_PATH, ...paths);
  };
  // Create a new browser window
  let newWindow: BrowserWindow | null = new BrowserWindow({
    width: 1200,
    height: 1000,
    webPreferences: {
      preload: app.isPackaged
        ? path.join(__dirname, 'preload.js')
        : path.join(__dirname, '../../.erb/dll/preload.js'),
    },
  });

  // Load the HTML file in the new window
  newWindow?.loadURL(resolveDocHtmlPath('index.html'));

  newWindow?.on('ready-to-show', () => {
    newWindow?.webContents?.closeDevTools();
  })
  // When the window is closed, dereference the window object
  newWindow?.on('closed', () => {
    newWindow = null;
  });
}
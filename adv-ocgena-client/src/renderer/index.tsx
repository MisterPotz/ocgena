import { createRoot } from 'react-dom/client';
import App from './App';
import React from 'react';
import { Editor } from './components/Editor';
import ReactDOM from 'react-dom';
import { appService } from './AppService';


appService.initialize();

const container = document.getElementById('root') as HTMLElement;
const root = createRoot(container);

root.render(<App />);


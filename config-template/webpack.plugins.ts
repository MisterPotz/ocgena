import type IForkTsCheckerWebpackPlugin from 'fork-ts-checker-webpack-plugin';
import { languages } from 'monaco-editor/esm/metadata';

// eslint-disable-next-line @typescript-eslint/no-var-requires
const ForkTsCheckerWebpackPlugin: typeof IForkTsCheckerWebpackPlugin = require('fork-ts-checker-webpack-plugin');

const MonacoWebpackPlugin = require('monaco-editor-webpack-plugin');

export const plugins = [
  new ForkTsCheckerWebpackPlugin({
    logger: 'webpack-infrastructure',
  }),
  new MonacoWebpackPlugin({
    // languages: ['javascript', 'json'],
  })
];

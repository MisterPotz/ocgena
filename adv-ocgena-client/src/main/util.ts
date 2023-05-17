/* eslint import/prefer-default-export: off */
import { URL } from 'url';
import path from 'path';

export function resolveHtmlPath(htmlFileName: string) {
  if (process.env.NODE_ENV === 'development') {
    const port = process.env.PORT || 1212;
    const url = new URL(`http://localhost:${port}`);
    url.pathname = htmlFileName;
    console.log("loading core html path  " + url);
    return url.href;
  }
  console.log("loading at path "+ `file://${path.resolve(__dirname, '../renderer/', htmlFileName)}`)
  return `file://${path.resolve(__dirname, '../renderer/', htmlFileName)}`;
}


export function resolveDocHtmlPath(htmlFileName: string) {
  console.log(`htmlFilename: ${htmlFileName}`)
  if (process.env.NODE_ENV === 'development') {
    const port = process.env.PORT || 1212;
    const url = new URL(`http://localhost:${port}`);
    url.pathname = "docs/" + htmlFileName;
    console.log(`in development environment, ${url}` )
    return url.href;
  }
  console.log("loading at path "+ `file://${path.resolve(__dirname, '../renderer/docs/', htmlFileName)}`)

  return `file://${path.resolve(__dirname, '../renderer/docs/', htmlFileName)}`;
}

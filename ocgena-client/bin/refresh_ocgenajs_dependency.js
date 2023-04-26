#! /usr/bin/env node 
const shell = require('shelljs');
const path = require('path');

shell.exec("npm uninstall ocgena");
const ocgenajs_path = path.resolve(__dirname, "../..", "ocgenajs")
shell.echo(`the path ${ocgenajs_path}`);
shell.exec(`npm install ${ocgenajs_path}`);

const path = require('path');
const loaderUtils = require('loader-utils');

module.exports = function(content) {
  const options = loaderUtils.getOptions(this);
  const url = new URL(options.baseUrl);
  `${url.pathname}`
  
  let regexpa = new RegExp(`(href|src)="(\/docs)([^"]*)"`, 'g')
  let testResult = regexpa.test(content);
  console.log("testing content: " + testResult);

  return content.replace(
    regexpa,
    `$1=".$2$3"`
  );
};

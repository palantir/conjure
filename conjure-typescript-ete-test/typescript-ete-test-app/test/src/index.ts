// Add base tag
const baseTag = document.createElement("base");
baseTag.href = "/typescript-ete-test/";
document.getElementsByTagName("head")[0].appendChild(baseTag);

// Include all test files
const testsContext = require.context(".", true, /Tests\.js$/);

testsContext.keys().forEach(testsContext);

// Include all source files for coverage
const sourceFilesContext = require.context("../../src/", true, /\.js$/);

sourceFilesContext.keys().forEach(sourceFilesContext);

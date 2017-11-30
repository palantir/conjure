#!/usr/bin/env node

/**
 * @license Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 * Lifted from https://github.palantir.build/foundry/spellbook/blob/develop/scripts/pull.js
 */

const child_process = require("child_process");
const fs = require("fs");
const path = require("path");

const argv = process.argv;
const token = argv[2];
const outputDirectory = argv[3];
const GITHUB_API = "https://github.palantir.build/api/v3";
const GITHUB_RAW = "https://raw.github.palantir.build";

if (argv.length <= 3) {
  console.error("usage: download-spellbook-definitions <GH_PULL_TOKEN> <output directory>");
  process.exit(1);
}

const projects = downloadSpellbookProjectsJson();
for (const name of Object.keys(projects)) {
  const project = projects[name];

  execSync(`mkdir -p ${outputDirectory}`)
  downloadFilesToProjectDirectory(outputDirectory, project);
}

function downloadSpellbookProjectsJson() {
  return JSON.parse(
    execSync(`curl -H "Authorization: token ${token}" -k ${GITHUB_RAW}/foundry/spellbook/develop/projects.json`)
  );
}

function downloadFilesToProjectDirectory(outputDirectory, project) {
  const projectFiles = getProjectFiles(project);

  for (const file of projectFiles) {
    const localPath = "." + file.replace(GITHUB_RAW, "");
    console.log("Downloading file: ", localPath);
    execSync(`cd ${outputDirectory} && mkdir -p $(dirname ${localPath}) && curl -H 'Authorization: token ${token}' -k ${file} -o ${localPath} -f || true`);
  }
}

function getProjectFiles(project) {
  return project.hasOwnProperty("repo")
    ? getProjectFilesFromRepo(project.repo)
    : project.urls;
}

function getProjectFilesFromRepo(repo) {
  return convertPartialPathsToFullPaths(
    repo,
    getPartialPathsToConjureFiles(repo)
  );
}

function convertPartialPathsToFullPaths(repo, partial_paths) {
  return partial_paths.map(
    partial_path => `${GITHUB_RAW}/${repo}/develop/${partial_path}`
  );
}

function getPartialPathsToConjureFiles(repo) {
  return searchGithubRepoForConjureFiles(repo).items.map(item => item.path);
}

function searchGithubRepoForConjureFiles(repo) {
  return JSON.parse(
    execSync(`curl -H "Authorization: token ${token}" -k ${GITHUB_API}/search/code?q=src/main/conjure+in:path+extension:yml+repo:${repo}`)
  );
}

function execSync(command) {
  return child_process.execSync(command).toString();
}

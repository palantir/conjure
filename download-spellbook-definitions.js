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
  const projectPath = path.join(outputDirectory, name).replace(/ /g, "\\ ");

  execSync(`mkdir -p ${projectPath}`)
  downloadFilesToProjectDirectory(projectPath, project);
  createProjectManifest(project, name);
}

function downloadSpellbookProjectsJson() {
  return JSON.parse(
    execSync(`curl -H "Authorization: token ${token}" -k ${GITHUB_RAW}/foundry/spellbook/develop/projects.json`)
  );
}

function downloadFilesToProjectDirectory(projectPath, project) {
  const projectFiles = getProjectFiles(project);

  for (const file of projectFiles) {
    console.log("Downloading file: ", file);
    execSync(`cd ${projectPath} && curl -H 'Authorization: token ${token}' -k -O ${file}`);
  }
}

function getProjectFiles(project) {
  return project.hasOwnProperty("repo")
    ? getProjectFilesFromRepo(project.repo)
    : project.urls;
}

function createProjectManifest(project, name) {
  const manifest = {
    description: project.description,
    repo: project.repo,
    urls: project.urls
  };
  const manifestFile = path.join(outputDirectory, name, "manifest.json");
  fs.writeFileSync(manifestFile, JSON.stringify(manifest), "utf8");
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

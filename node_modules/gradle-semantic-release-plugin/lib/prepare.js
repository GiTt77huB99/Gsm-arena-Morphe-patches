"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.updateVersion = updateVersion;
exports.default = prepare;
const fs_1 = require("fs");
const path_1 = require("path");
const promisified_properties_1 = require("promisified-properties");
const gradle_1 = require("./gradle");
async function updateVersion(cwd, version) {
    const path = (0, path_1.join)(cwd, "gradle.properties");
    if ((0, fs_1.existsSync)(path)) {
        const prop = await (0, promisified_properties_1.parseFile)(path);
        const index = prop.findIndex((entry) => "key" in entry && entry.key == "version");
        if (index < 0) {
            prop.push({ key: "version", value: version });
        }
        else {
            prop[index].value = version;
        }
        return (0, promisified_properties_1.write)(prop, path);
    }
    else {
        return (0, promisified_properties_1.write)([{ key: "version", value: version }], path);
    }
}
async function prepare(pluginConfig, context) {
    const { cwd, env, nextRelease } = context;
    await updateVersion(cwd, nextRelease.version);
    const version = await (0, gradle_1.getVersion)(cwd, env);
    if (version !== nextRelease.version) {
        throw new Error(`Failed to update version from ${version} to ${nextRelease.version}. ` +
            "Make sure that you define version not in build.gradle but in gradle.properties.");
    }
}

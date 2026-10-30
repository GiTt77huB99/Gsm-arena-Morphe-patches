"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const gradle_1 = require("./gradle");
module.exports = async function publish(pluginConfig, context) {
    const { cwd, env, logger } = context;
    await (0, gradle_1.publishArtifact)(cwd, env, logger);
};

"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const gradle_1 = require("./gradle");
module.exports = async function verifyConditions(pluginConfig, context) {
    const { cwd, env, logger } = context;
    const command = await (0, gradle_1.getCommand)(cwd);
    if (command !== "./gradlew") {
        throw new Error(`Gradle wrapper not found at ${cwd}`);
    }
    const task = await (0, gradle_1.getTaskToPublish)(cwd, env, logger);
    if (task.length === 0) {
        throw new Error("No task found that can publish artifacts");
    }
    logger.debug("Verified conditions, and found no problem");
};

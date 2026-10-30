import { Config } from "./definitions/config.js";
import type { VerifyConditionsContext, SuccessContext } from "semantic-release";
/**
 * Triggered at the beginning of the release process.
 * Verify plugin configurations.
 * @param pluginConfig
 * @param context
 */
export declare function verifyConditions(pluginConfig: Config, context: VerifyConditionsContext): Promise<void>;
/**
 * Triggered when release succeeded
 * @param pluginConfig
 * @param context
 */
export declare function success(pluginConfig: Partial<Config>, context: SuccessContext): Promise<void>;

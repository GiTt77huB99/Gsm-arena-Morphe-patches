import Git from "./helpers/git.js";
import { Config } from "./definitions/config.js";
import type { SuccessContext } from "semantic-release";
export declare function performBackmerge(git: Git, pluginConfig: Partial<Config>, context: SuccessContext): Promise<void>;

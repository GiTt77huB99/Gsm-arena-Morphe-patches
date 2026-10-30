import lodash from "lodash";
const { defaultTo, castArray, isArray, isNil } = lodash;
import { verify } from "./verify.js";
import { performBackmerge } from "./perform-backmerge.js";
import Git from "./helpers/git.js";
// Package names this plugin may be configured under.
// The first is the current package; the second is the upstream fork it was
// renamed from, kept so existing configurations keep working after migration.
const PLUGIN_NAMES = [
    "@cleyrop-org/semantic-release-backmerge",
    "@saithodev/semantic-release-backmerge",
];
// Every plugin option, resolved as a unit so this never drifts from `Config`.
const CONFIG_KEYS = [
    "backmergeBranches",
    "backmergeStrategy",
    "plugins",
    "forcePush",
    "message",
    "clearWorkspace",
    "restoreWorkspace",
    "allowSameBranchMerge",
    "mergeMode",
    "fastForwardMode",
    "fetchDelay",
];
let verified = false;
/**
 * Triggered at the beginning of the release process.
 * Verify plugin configurations.
 * @param pluginConfig
 * @param context
 */
export async function verifyConditions(pluginConfig, context) {
    // pluginConfig also contains config keys for semantic-release.
    // since we also use a "plugin" setting, we need to get the plugin config ourselves
    // otherwise we may get the "branches" setting of semantic-release itself
    const { options } = context;
    if (!options || !options.plugins) {
        return;
    }
    const ourPlugin = options.plugins.filter((plugin) => {
        return isArray(plugin) && PLUGIN_NAMES.includes(plugin[0]);
    })[0] ?? [];
    const realPluginConfig = (ourPlugin[1] ?? {});
    if (options.prepare) {
        const preparePlugin = (castArray(options.prepare).find((config) => config.path && PLUGIN_NAMES.includes(config.path)) || {});
        // Resolve every option, preferring the plugin entry's value and falling
        // back to the prepare entry's. Iterating CONFIG_KEYS keeps this exhaustive.
        for (const key of CONFIG_KEYS) {
            const value = defaultTo(realPluginConfig[key], preparePlugin[key]);
            if (!isNil(value)) {
                pluginConfig[key] = value;
            }
        }
    }
    verify(pluginConfig);
    verified = true;
}
/**
 * Triggered when release succeeded
 * @param pluginConfig
 * @param context
 */
export async function success(pluginConfig, context) {
    if (!verified) {
        verify(pluginConfig);
        verified = true;
    }
    const { env, cwd } = context;
    const git = new Git({ env, cwd });
    await performBackmerge(git, pluginConfig, context);
}
//# sourceMappingURL=index.js.map
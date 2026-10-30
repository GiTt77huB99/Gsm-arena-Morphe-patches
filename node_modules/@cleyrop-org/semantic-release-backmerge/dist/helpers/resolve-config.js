import lodash from "lodash";
const { isNil } = lodash;
export function resolveConfig(config) {
    const { backmergeBranches, backmergeStrategy, plugins, message, forcePush, allowSameBranchMerge, clearWorkspace, restoreWorkspace, mergeMode, fastForwardMode, fetchDelay, ignorePaths, } = config;
    return {
        backmergeBranches: isNil(backmergeBranches)
            ? ["develop"]
            : backmergeBranches,
        backmergeStrategy: isNil(backmergeStrategy) ? "rebase" : backmergeStrategy,
        plugins: isNil(plugins) ? [] : plugins,
        message: isNil(message)
            ? `chore(release): Preparations for next release [skip ci]`
            : message,
        forcePush: isNil(forcePush) ? false : forcePush,
        allowSameBranchMerge: isNil(allowSameBranchMerge)
            ? false
            : allowSameBranchMerge,
        clearWorkspace: isNil(clearWorkspace) ? false : clearWorkspace,
        restoreWorkspace: isNil(restoreWorkspace) ? false : restoreWorkspace,
        mergeMode: isNil(mergeMode) ? "none" : mergeMode,
        fastForwardMode: isNil(fastForwardMode) ? "none" : fastForwardMode,
        fetchDelay: isNil(fetchDelay) ? 0 : fetchDelay,
        ignorePaths: isNil(ignorePaths) ? [] : ignorePaths,
    };
}
//# sourceMappingURL=resolve-config.js.map
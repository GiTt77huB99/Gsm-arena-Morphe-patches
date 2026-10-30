export type BackmergeStrategy = "rebase" | "merge";
export type MergeMode = "none" | "ours" | "theirs";
export type FastForwardMode = "none" | "ff" | "no-ff" | "ff-only";
export type BranchTypeStruct = {
    from: string;
    to: string;
    /**
     * Glob/path entries to exclude only for this back-merge route. When set,
     * this takes precedence over the top-level `ignorePaths` option.
     */
    ignorePaths?: string[];
};
type BranchType = string | BranchTypeStruct;
export interface Config {
    backmergeBranches: BranchType[];
    backmergeStrategy: BackmergeStrategy;
    plugins: Array<string | string[] | unknown>;
    forcePush: boolean;
    message: string;
    allowSameBranchMerge: boolean;
    clearWorkspace: boolean;
    restoreWorkspace: boolean;
    mergeMode: MergeMode;
    fastForwardMode: FastForwardMode;
    fetchDelay: number;
    /**
     * Glob/path entries to exclude from the back-merge. After the merge/rebase,
     * these paths are restored to the target branch's pre-back-merge state, so
     * changes to them on the release branch are never carried over.
     */
    ignorePaths: string[];
}
export {};

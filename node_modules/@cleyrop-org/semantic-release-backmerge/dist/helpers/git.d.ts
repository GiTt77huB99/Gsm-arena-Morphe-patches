import { FastForwardMode, MergeMode } from "../definitions/config.js";
import { type Result } from "execa";
export default class Git {
    /**
     * @var string Options to pass to `execa`.
     */
    protected execaOpts: object;
    constructor(execaOpts: object);
    protected runGitCommand(args: string[], isLocal?: boolean, options?: object, retry?: number): Promise<Result>;
    /**
     * Add a list of file to the Git index. `.gitignore` will be ignored.
     *
     * @param {Array<String>} files Array of files path to add to the index.
     */
    add(files: string[]): Promise<void>;
    /**
     * Commit to the local repository.
     *
     * @param {String} message Commit message.
     *
     * @throws {Error} if the commit failed.
     */
    commit(message: string): Promise<void>;
    /**
     * Stash unstaged commits
     *
     * @throws {Error} if the commit failed.
     */
    stash(): Promise<void>;
    /**
     * Unstash changes
     *
     * @throws {Error} if the commit failed.
     */
    unstash(): Promise<void>;
    /**
     * Push to the remote repository.
     *
     * @param {String} origin The remote repository URL.
     * @param {String} branch The branch to push.
     * @param {Boolean} forcePush If branch should be force-pushed
     *
     * @throws {Error} if the push failed.
     */
    push(origin: string, branch: string, forcePush?: boolean): Promise<void>;
    /**
     * Fetch commits from the remote repository.
     *
     * @throws {Error} if the fetch failed.
     */
    fetch(url?: string): Promise<void>;
    /**
     * Pull commits from the remote repository.
     *
     * @throws {Error} if the pull failed.
     */
    pull(url?: string): Promise<void>;
    /**
     * Configures Git to fetch all references
     *
     * @throws {Error} if the config failed.
     */
    configFetchAllRemotes(): Promise<void>;
    /**
     * Checks out a branch from the remote repository.
     *
     * @param {String} branch The branch to check out.
     *
     * @throws {Error} if the checkout failed.
     */
    checkout(branch: string): Promise<void>;
    /**
     * Get list of files that are tracked and modified
     * Each entry consists of the status and the file path.
     */
    getModifiedFiles(): Promise<string[]>;
    /**
     * Rebases the currently checked out branch onto another branch.
     *
     * @param {String} branch The branch to rebase onto.
     *
     * @throws {Error} if the rebase failed.
     */
    rebase(branch: string): Promise<void>;
    /**
     * Merges the currently checked out branch onto another branch.
     *
     * @param {String} branch The branch to rebase onto.
     * @param {MergeMode} mergeMode
     * @param {FastForwardMode} fastForwardMode
     *
     * @throws {Error} if the merge failed.
     */
    merge(branch: string, mergeMode?: MergeMode, fastForwardMode?: FastForwardMode): Promise<void>;
    /**
     * Get the current branch name.
     *
     * @returns {String} The current branch name.
     *
     * @throws {Error} if getting the branch failed.
     */
    getCurrentBranch(): Promise<string>;
    /**
     * Resolve a revision to its commit SHA.
     *
     * @param {String} ref The revision to resolve (defaults to `HEAD`).
     *
     * @returns {String} The resolved commit SHA.
     *
     * @throws {Error} if the revision could not be resolved.
     */
    revParse(ref?: string): Promise<string>;
    /**
     * Restore the given path specs to their exact state at a specific revision,
     * staging the result. Used to keep `ignorePaths` out of a back-merge by
     * reverting them to the target branch's pre-merge version.
     *
     * Path specs that match files in `ref` are checked out from it; files that
     * exist now but not in `ref` are removed (so the result matches `ref`
     * exactly, e.g. a release-branch-only file is not carried over). Path specs
     * (including globs) that match nothing are silently skipped rather than
     * aborting the back-merge.
     *
     * @param {String} ref The revision to restore the path specs from.
     * @param {Array<String>} paths The path specs to restore.
     *
     * @throws {Error} if the checkout or remove failed.
     */
    restorePaths(ref: string, paths: string[]): Promise<void>;
    /**
     * Updates a local branch reference to match the remote.
     * This is useful after pushing to ensure the local ref is in sync.
     * If the branch is currently checked out, it will be reset instead.
     *
     * @param {String} branch The branch to update.
     *
     * @throws {Error} if the update failed.
     */
    updateLocalBranch(branch: string): Promise<void>;
}

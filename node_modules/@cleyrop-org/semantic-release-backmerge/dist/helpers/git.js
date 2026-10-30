import { execa } from "execa";
import debugPkg from "debug";
const debug = debugPkg("semantic-release:backmerge");
export default class Git {
    /**
     * @var string Options to pass to `execa`.
     */
    execaOpts = {};
    constructor(execaOpts) {
        this.execaOpts = execaOpts;
    }
    async runGitCommand(args, isLocal = true, options = {}, retry = 0) {
        const maxRetries = isLocal ? 0 : 3; // retry remote Git operations up to 3 times if they fail
        try {
            const result = await execa("git", args, {
                ...this.execaOpts,
                ...options,
            });
            return result;
        }
        catch (error) {
            debug("git command failed", { args, retry, maxRetries, error });
            if (retry >= maxRetries) {
                throw error;
            }
            // Retry
            retry++;
            debug(`Unable to connect to Git. Retrying in 1 second (${retry}/${maxRetries}).`);
            await new Promise((resolve) => setTimeout(resolve, 1000));
            return this.runGitCommand(args, isLocal, options, retry);
        }
    }
    /**
     * Add a list of file to the Git index. `.gitignore` will be ignored.
     *
     * @param {Array<String>} files Array of files path to add to the index.
     */
    async add(files) {
        const shell = await this.runGitCommand(["add", "--force", "--ignore-errors", ...files], true, { reject: false });
        debug("add file to git index", shell);
    }
    /**
     * Commit to the local repository.
     *
     * @param {String} message Commit message.
     *
     * @throws {Error} if the commit failed.
     */
    async commit(message) {
        await this.runGitCommand(["commit", "-m", message]);
    }
    /**
     * Stash unstaged commits
     *
     * @throws {Error} if the commit failed.
     */
    async stash() {
        await this.runGitCommand(["stash"]);
    }
    /**
     * Unstash changes
     *
     * @throws {Error} if the commit failed.
     */
    async unstash() {
        await this.runGitCommand(["stash", "pop"]);
    }
    /**
     * Push to the remote repository.
     *
     * @param {String} origin The remote repository URL.
     * @param {String} branch The branch to push.
     * @param {Boolean} forcePush If branch should be force-pushed
     *
     * @throws {Error} if the push failed.
     */
    async push(origin, branch, forcePush = false) {
        const args = ["push", origin, `HEAD:${branch}`];
        if (forcePush) {
            args.push("-f");
        }
        await this.runGitCommand(args, false);
    }
    /**
     * Fetch commits from the remote repository.
     *
     * @throws {Error} if the fetch failed.
     */
    async fetch(url) {
        const args = ["fetch"];
        if (url) {
            // Use 'origin' remote name instead of URL to ensure refs are updated correctly
            args.push("origin");
        }
        await this.runGitCommand(args, false);
    }
    /**
     * Pull commits from the remote repository.
     *
     * @throws {Error} if the pull failed.
     */
    async pull(url) {
        const args = ["pull", "--ff-only"];
        if (url) {
            args.push(url);
        }
        await this.runGitCommand(args, false);
    }
    /**
     * Configures Git to fetch all references
     *
     * @throws {Error} if the config failed.
     */
    async configFetchAllRemotes() {
        await this.runGitCommand(["config", "remote.origin.fetch", "+refs/heads/*:refs/remotes/origin/*"], false);
    }
    /**
     * Checks out a branch from the remote repository.
     *
     * @param {String} branch The branch to check out.
     *
     * @throws {Error} if the checkout failed.
     */
    async checkout(branch) {
        await this.runGitCommand(["checkout", "-B", branch, "origin/" + branch], false);
    }
    /**
     * Get list of files that are tracked and modified
     * Each entry consists of the status and the file path.
     */
    async getModifiedFiles() {
        const result = await this.runGitCommand(["status", "-s", "-uno"]);
        const stdout = typeof result.stdout === "string" ? result.stdout : "";
        const lines = stdout.split("\n");
        return lines.filter((item) => item.length);
    }
    /**
     * Rebases the currently checked out branch onto another branch.
     *
     * @param {String} branch The branch to rebase onto.
     *
     * @throws {Error} if the rebase failed.
     */
    async rebase(branch) {
        await this.runGitCommand(["rebase", `origin/${branch}`]);
    }
    /**
     * Merges the currently checked out branch onto another branch.
     *
     * @param {String} branch The branch to rebase onto.
     * @param {MergeMode} mergeMode
     * @param {FastForwardMode} fastForwardMode
     *
     * @throws {Error} if the merge failed.
     */
    async merge(branch, mergeMode = "none", fastForwardMode = "none") {
        const args = ["merge"];
        if (mergeMode !== "none") {
            args.push("-X" + mergeMode);
        }
        if (fastForwardMode !== "none") {
            args.push("--" + fastForwardMode);
        }
        args.push("origin/" + branch);
        await this.runGitCommand(args);
    }
    /**
     * Get the current branch name.
     *
     * @returns {String} The current branch name.
     *
     * @throws {Error} if getting the branch failed.
     */
    async getCurrentBranch() {
        const result = await this.runGitCommand([
            "rev-parse",
            "--abbrev-ref",
            "HEAD",
        ]);
        const stdout = typeof result.stdout === "string" ? result.stdout : "";
        return stdout.trim();
    }
    /**
     * Resolve a revision to its commit SHA.
     *
     * @param {String} ref The revision to resolve (defaults to `HEAD`).
     *
     * @returns {String} The resolved commit SHA.
     *
     * @throws {Error} if the revision could not be resolved.
     */
    async revParse(ref = "HEAD") {
        const result = await this.runGitCommand(["rev-parse", ref]);
        const stdout = typeof result.stdout === "string" ? result.stdout : "";
        return stdout.trim();
    }
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
    async restorePaths(ref, paths) {
        const splitLines = (stdout) => (typeof stdout === "string" ? stdout : "").split("\n").filter(Boolean);
        // Files matching the path specs that exist in <ref> and in the current tree.
        const inRef = splitLines((await this.runGitCommand(["ls-tree", "-r", "--name-only", ref, "--", ...paths], true, { reject: false })).stdout);
        const inTree = splitLines((await this.runGitCommand(["ls-files", "--", ...paths], true, {
            reject: false,
        })).stdout);
        // Restore content for files present in <ref>.
        if (inRef.length > 0) {
            await this.runGitCommand(["checkout", ref, "--", ...inRef]);
        }
        // Remove files present now but absent in <ref> so the result matches <ref>.
        const toRemove = inTree.filter((file) => !inRef.includes(file));
        if (toRemove.length > 0) {
            await this.runGitCommand(["rm", "-f", "--", ...toRemove]);
        }
    }
    /**
     * Updates a local branch reference to match the remote.
     * This is useful after pushing to ensure the local ref is in sync.
     * If the branch is currently checked out, it will be reset instead.
     *
     * @param {String} branch The branch to update.
     *
     * @throws {Error} if the update failed.
     */
    async updateLocalBranch(branch) {
        const currentBranch = await this.getCurrentBranch();
        if (currentBranch === branch) {
            // If we're on the branch, reset it to match remote
            await this.runGitCommand(["reset", "--hard", `origin/${branch}`]);
        }
        else {
            // If we're not on the branch, force update the ref
            await this.runGitCommand(["branch", "-f", branch, `origin/${branch}`]);
        }
    }
}
//# sourceMappingURL=git.js.map
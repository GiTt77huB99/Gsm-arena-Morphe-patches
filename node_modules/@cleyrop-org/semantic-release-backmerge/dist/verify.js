import lodash from "lodash";
const { isString, isNil, isArray, isBoolean } = lodash;
import { resolveConfig } from "./helpers/resolve-config.js";
import { getError } from "./helpers/get-error.js";
import AggregateError from "aggregate-error";
const isNonEmptyString = (value) => isString(value) && value.trim();
export function verify(pluginConfig) {
    const options = resolveConfig(pluginConfig);
    const hasValidBranchIgnorePaths = !isArray(options.backmergeBranches) ||
        options.backmergeBranches.every((branchConfig) => typeof branchConfig !== "object" ||
            branchConfig === null ||
            !Object.prototype.hasOwnProperty.call(branchConfig, "ignorePaths") ||
            isArray(branchConfig.ignorePaths));
    const VALIDATORS = {
        backmergeBranches: isArray,
        message: isNonEmptyString,
        plugins: isArray,
        forcePush: isBoolean,
        ignorePaths: (value) => isArray(value) && hasValidBranchIgnorePaths,
    };
    const reducedErrors = Object.entries(options).reduce((errors, [option, value]) => {
        return !isNil(value) &&
            Object.prototype.hasOwnProperty.call(VALIDATORS, option) &&
            !VALIDATORS[option](value)
            ? [
                ...errors,
                getError(`EINVALID${option.toUpperCase()}`, { [option]: value }),
            ]
            : errors;
    }, []);
    if (reducedErrors.length > 0) {
        throw new AggregateError(reducedErrors);
    }
}
//# sourceMappingURL=verify.js.map
import SemanticReleaseError from "@semantic-release/error";
import { ERROR_DEFINITIONS } from "../definitions/errors.js";
export function getError(code, ctx) {
    // @ts-expect-error - ERROR_DEFINITIONS uses dynamic keys
    const { message, details } = ERROR_DEFINITIONS[code](ctx);
    return new SemanticReleaseError(message, code, details);
}
//# sourceMappingURL=get-error.js.map
const linkify = (file) => `https://gitlab.com/cleyrop-org/semantic-release-backmerge/-/blob/main/${file}`;
export const ERROR_DEFINITIONS = {
    // @ts-expect-error - Dynamic context object
    EINVALIDBACKMERGEBRANCHES: ({ backmergeBranches }) => ({
        message: "Invalid `backmergeBranches` option.",
        details: `The [backmergeBranches option](${linkify("README.md#backmergeBranches")}) option must be an \`Array\`. Your configuration for the \`backmergeBranches\` option is \`${backmergeBranches}\`.`,
    }),
    // @ts-expect-error - Dynamic context object
    EINVALIDMESSAGE: ({ message }) => ({
        message: "Invalid `message` option.",
        details: `The [message option](${linkify("README.md#message")}) option, if defined, must be a non empty \`String\`.

Your configuration for the \`message\` option is \`${message}\`.`,
    }),
    EINVALIDPLUGINS: () => ({
        message: "Invalid `plugins` option.",
        details: `The [plugins option](${linkify("README.md#plugins")}) option must be an \`Array\`.`,
    }),
    EINVALIDFORCEPUSH: () => ({
        message: "Invalid `forcePush` option.",
        details: `The [forcePush option](${linkify("README.md#forcePush")}) option must be a \`Boolean\`.`,
    }),
    EINVALIDIGNOREPATHS: () => ({
        message: "Invalid `ignorePaths` option.",
        details: `The [ignorePaths option](${linkify("README.md#ignorePaths")}) option must be an \`Array\`, both at the top level and when configured for a specific back-merge branch pair.`,
    }),
};
//# sourceMappingURL=errors.js.map
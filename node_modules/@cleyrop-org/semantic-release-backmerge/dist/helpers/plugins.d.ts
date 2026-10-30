import type { SuccessContext } from "semantic-release";
interface IPlugin {
    packageName: string;
    pluginConfig: object;
    pluginPackage: {
        success: (pluginConfig: object, context: SuccessContext) => Promise<void>;
    };
}
export declare class PluginGroup {
    protected plugins: Array<IPlugin>;
    constructor(plugins: Array<IPlugin>);
    success(context: SuccessContext): Promise<void>;
}
export declare function loadPlugins(parentPluginConfig: {
    plugins: Array<string | string[] | unknown>;
}, context: SuccessContext): Promise<PluginGroup>;
export {};

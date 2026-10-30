function getPackageData(plugin) {
    if (Array.isArray(plugin)) {
        return { packageName: plugin[0], pluginConfig: plugin[1] };
    }
    if (typeof plugin === "string") {
        return { packageName: plugin, pluginConfig: {} };
    }
    return null;
}
export class PluginGroup {
    plugins = [];
    constructor(plugins) {
        this.plugins = plugins;
    }
    async success(context) {
        for (const pluginPackage of this.plugins) {
            context.logger.log('Executing "success" step of package ' + pluginPackage.packageName);
            await pluginPackage.pluginPackage.success(pluginPackage.pluginConfig, context);
        }
    }
}
export async function loadPlugins(parentPluginConfig, context) {
    const plugins = [];
    for (const plugin of parentPluginConfig.plugins) {
        if (typeof plugin !== "string" && !Array.isArray(plugin)) {
            context.logger.log("Invalid plugin provided. Expected string or array, got " +
                typeof plugin);
            continue;
        }
        const packageData = getPackageData(plugin);
        if (!packageData) {
            context.logger.log("Invalid plugin provided. Expected string or array, got " +
                typeof plugin);
            continue;
        }
        const { packageName, pluginConfig } = packageData;
        const pluginModule = await import(packageName);
        const pluginPackage = pluginModule.default || pluginModule;
        if (!Object.prototype.hasOwnProperty.call(pluginPackage, "success") ||
            typeof pluginPackage.success !== "function") {
            context.logger.log('Method "success" not found in package ' + packageName + ".");
            continue;
        }
        context.logger.log("Plugin " + packageName + " loaded successfully.");
        plugins.push({
            packageName,
            pluginConfig: pluginConfig,
            pluginPackage,
        });
    }
    return new PluginGroup(plugins);
}
//# sourceMappingURL=plugins.js.map
package app.gsmarena.patches.privacy

import app.morphe.patcher.patch.ResourcePatchContext
import org.w3c.dom.Document
import org.w3c.dom.Element

private const val APPLICATION_NODE = "application"
private const val META_DATA_TAG = "meta-data"
private const val ANDROID_NAME_ATTR = "android:name"
private const val ANDROID_VALUE_ATTR = "android:value"

/**
 * Applies a configuration block to the AndroidManifest document.
 */
fun ResourcePatchContext.androidManifest(
    block: Document.() -> Unit,
): Document = document("AndroidManifest.xml").use { document ->
    document.apply(block)
}

private fun Document.applicationElement(): Element =
    documentElement.getElementsByTagName(APPLICATION_NODE).item(0) as Element

/**
 * Removes direct children of `<application>` whose tag matches [tagName] and
 * whose `android:name` attribute fully matches one of the [namePatterns] regexes.
 */
private fun Document.removeApplicationElements(
    tagName: String,
    vararg namePatterns: String,
) {
    val application = applicationElement()
    val regexes = namePatterns.map(String::toRegex)
    val children = mutableListOf<Element>()

    var child = application.firstChild
    while (child != null) {
        if (child is Element && child.tagName == tagName) {
            val name = child.getAttribute(ANDROID_NAME_ATTR)
            if (name != null && regexes.any { it.matches(name) }) {
                children.add(child)
            }
        }
        child = child.nextSibling
    }

    children.forEach { application.removeChild(it) }
}

/**
 * Adds or updates `<meta-data>` elements under `<application>`.
 */
fun Document.metaData(vararg properties: Pair<String, String>) {
    val application = applicationElement()

    properties.forEach { (name, value) ->
        var index = 0
        var existing: Element? = null
        while (index < application.childNodes.length) {
            val node = application.childNodes.item(index)
            if (node is Element && node.tagName == META_DATA_TAG &&
                node.getAttribute(ANDROID_NAME_ATTR) == name
            ) {
                existing = node
                break
            }
            index++
        }

        if (existing != null) {
            existing.setAttribute(ANDROID_VALUE_ATTR, value)
        } else {
            val element = createElement(META_DATA_TAG)
            element.setAttribute(ANDROID_NAME_ATTR, name)
            element.setAttribute(ANDROID_VALUE_ATTR, value)
            application.appendChild(element)
        }
    }
}

/**
 * Removes `<receiver>` elements by `android:name` regex.
 */
fun Document.removeReceiver(vararg receivers: String) =
    removeApplicationElements("receiver", *receivers)

/**
 * Removes `<service>` elements by `android:name` regex.
 */
fun Document.removeService(vararg services: String) =
    removeApplicationElements("service", *services)

/**
 * Removes `<activity>` elements by `android:name` regex.
 */
fun Document.removeActivity(vararg activities: String) =
    removeApplicationElements("activity", *activities)

/**
 * Removes `<uses-permission>` elements at the manifest root level.
 */
fun Document.removeUsesPermission(vararg permissions: String) {
    val regexes = permissions.map(String::toRegex)
    val children = mutableListOf<Element>()

    var child = documentElement.firstChild
    while (child != null) {
        if (child is Element && child.tagName == "uses-permission") {
            val name = child.getAttribute(ANDROID_NAME_ATTR)
            if (name != null && regexes.any { it.matches(name) }) {
                children.add(child)
            }
        }
        child = child.nextSibling
    }

    children.forEach { documentElement.removeChild(it) }
}

/**
 * Removes `<meta-data>` component registrations from the Firebase
 * `ComponentDiscoveryService` service, so the given Firebase SDK components are
 * never instantiated at app start.
 *
 * Example: removeFirebaseComponents("""com\.google\.firebase\.crashlytics\..+""")
 */
fun Document.removeFirebaseComponents(vararg componentPatterns: String) {
    val regexes = componentPatterns.map(String::toRegex)

    val services = documentElement.getElementsByTagName("service")
    for (serviceIndex in 0 until services.length) {
        val service = services.item(serviceIndex) as? Element ?: continue
        if (service.getAttribute(ANDROID_NAME_ATTR) !=
            "com.google.firebase.components.ComponentDiscoveryService"
        ) {
            continue
        }

        val metaDataNodes = service.getElementsByTagName(META_DATA_TAG)
        val toRemove = mutableListOf<Element>()
        for (metaIndex in 0 until metaDataNodes.length) {
            val metaData = metaDataNodes.item(metaIndex) as? Element ?: continue
            val name = metaData.getAttribute(ANDROID_NAME_ATTR)
            if (name != null && regexes.any { it.matches(name) }) {
                toRemove.add(metaData)
            }
        }
        toRemove.forEach { service.removeChild(it) }
    }
}

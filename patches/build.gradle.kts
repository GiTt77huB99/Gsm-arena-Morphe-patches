group = "app.gsmarena"

patches {
    about {
        name = "GSMArena Morphe Patches"
        description = "Removes ads, the ad consent flow, and all Google trackers " +
            "(Firebase Analytics, Crashlytics, Performance, advertising ID) from the " +
            "GSMArena app. Push notifications are left fully functional."
        source = "https://github.com/your-username/gsmarena-morphe-patches"
        author = "your-name"
        contact = "na"
        website = "na"
        license = "GPLv3"
    }
}

val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"
        dependsOn(build)
        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }
    publish {
        dependsOn("generatePatchesList")
    }
}

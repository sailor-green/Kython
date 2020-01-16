plugins {
    id("application")
}

dependencies {
    // == picocli - for CLI parsing == //
    implementation(group = "info.picocli", name = "picocli", version = "4.1.2")

    implementation(project(":kython-interpreter"))
}

application {
    mainClassName = "green.sailor.kython.MakeUp"
}

tasks.startScripts {
    applicationName = "kython"
}

tasks.distZip {
    archiveBaseName.set("kython")
}

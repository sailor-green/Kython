plugins {
    kotlin("jvm")
}

dependencies {
    // === KOTLIN === //
    implementation(kotlin("stdlib-jdk8"))
}

// enforce java 11 class files
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }
}

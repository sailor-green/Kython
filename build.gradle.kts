plugins {
    kotlin("jvm") version "1.3.61"
    id("com.diffplug.gradle.spotless") version "3.26.0"
}

allprojects {
    repositories {
        mavenCentral()
    }
    group = "green.sailor"
    version = "3.9"

    apply(plugin = "com.diffplug.gradle.spotless")

    spotless {
        kotlin {
            targetExclude("build/generated/**")
            ktlint().userData(
                mapOf(
                    "disabled_rules" to "no-wildcard-imports",
                    "max_line_length" to "100"
                )
            )
            @Suppress("INACCESSIBLE_TYPE")  // this works fine?
            licenseHeaderFile("$rootDir/gradle/LICENCE-HEADER")

        }
    }

    tasks.register("spotlessLint") {
        group = "linting"
        description = "Run the spotless linter for Kotlin."
        dependsOn(tasks.spotlessCheck)
        dependsOn(tasks.projects)
    }

    tasks.register("spotlessCorrect") {
        group = "linting"
        description = "Apply a spotless linter correction for Kotlin."
        dependsOn(tasks.spotlessApply)
    }
}

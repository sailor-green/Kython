/*
 * This file is part of kython.
 *
 * kython is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * kython is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with kython.  If not, see <https://www.gnu.org/licenses/>.
 */

plugins {
    kotlin("jvm") version "1.3.61"
    id("com.diffplug.gradle.spotless") version "3.26.0"
    distribution
}

repositories {
    mavenCentral()
    jcenter()
}


subprojects {
    group = "green.sailor.kython"
    version = "3.9-1.0"

    apply(plugin = "kotlin")
    apply(plugin = "com.diffplug.gradle.spotless")

    repositories {
        mavenCentral()
        jcenter()
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation(kotlin("reflect"))
    }

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

    // enforce java 11 class files
    tasks {
        compileKotlin {
            kotlinOptions.jvmTarget = "11"
        }
        compileTestKotlin {
            kotlinOptions.jvmTarget = "11"
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

/* distributions {
    create("release") {
        distributionBaseName.set("kython")
        contents {
            with(subprojects.first { it.name == "kython-core" }) {
                plugins.withType(ApplicationPlugin::class.java) {
                    from(tasks.distZip)
                    into("/")
                }
            }
        }
    }
}*/

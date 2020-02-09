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
        java {
            targetExclude("src/main/com/ochafik/**")
        }
    }

    sourceSets {
        main {
            resources {
                srcDir("src/main/python")
            }
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
}

val cloneCPython = tasks.register("cloneCPython") {
    group = "cpython"
    description = "Clones CPython to a temporary dir."
    doLast {
        val outputDir = project.buildDir.resolve("tmp/cpython")
        if (!outputDir.exists()) {
            outputDir.mkdir()
            logger.lifecycle("Cloning CPython...")
            exec {
                setCommandLine(
                    "git", "clone", "--depth=1",
                    "https://github.com/Python/CPython.git",
                    outputDir.absolutePath
                )
            }
        } else {
            logger.lifecycle("Updating CPython...")
            exec {
                workingDir = outputDir
                setCommandLine("git", "pull", "--depth=1")
            }
        }
    }
}

tasks.register("copyCPythonStdlib") {
    group = "cpython"
    description = "Copies parts of the CPython stdlib."

    dependsOn(cloneCPython)
    doLast {
        val outputDir = project.buildDir.resolve("tmp/cpython")
        logger.lifecycle("Copying stdlib...")
        copy {
            from(outputDir.resolve("Lib/importlib"))
            into(project(":kython-importer-pyimportlib").file("src/main/python/Lib/importlib"))
        }
    }
}


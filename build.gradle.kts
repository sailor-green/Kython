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
 *
 */

// gradle sucks!!!!!

plugins {
    kotlin("jvm") version "1.3.61"
    id("application")
    id("com.diffplug.gradle.spotless") version "3.26.0"
    id("jacoco")
}

group = "green.sailor"
version = "3.9"

repositories {
    mavenCentral()
}


dependencies {
    // === KOTLIN === //
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    // === CORE DEPENDENCIES === //
    // == picocli - for CLI parsing == //
    implementation(group = "info.picocli", name = "picocli", version = "4.1.2")

    // === TEST === //
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
}

spotless {
    kotlin {
        ktlint().userData(
            mapOf(
                "disabled_rules" to "no-wildcard-imports",
                "max_line_length" to "100"
            )
        )
        @Suppress("INACCESSIBLE_TYPE")  // this works fine?
        licenseHeaderFile("gradle/LICENCE-HEADER")
    }
}


application {
    mainClassName = "green.sailor.kython.MakeUp"
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

// apparently this is how you do junit
tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
        html.isEnabled = false
    }
}

tasks.register("spotlessLint"){
    group = "linting"
    description = "Run the spotless linter for Kotlin."
    dependsOn(tasks.spotlessCheck)
}

tasks.register("spotlessCorrect"){
    group = "linting"
    description = "Apply a spotless linter correction for Kotlin."
    dependsOn(tasks.spotlessApply)
}

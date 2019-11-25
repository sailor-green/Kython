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
    kotlin("jvm") version "1.3.60"
    id("application")
    // kotlin("kapt") version "1.3.50"
    id("com.diffplug.gradle.spotless") version "3.26.0"
}

group = "green.sailor"
version = "3.8"

repositories {
    maven {
        setUrl("https://dl.bintray.com/kotlin/kotlin-eap")
    }

    mavenCentral()
}


dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
}

spotless {
    kotlin {
        ktlint().userData(mapOf("disabled_rules" to "no-wildcard-imports"))
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

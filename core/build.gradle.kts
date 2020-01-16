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
    kotlin("jvm")
    kotlin("kapt")
    id("application")
    id("jacoco")
}

kapt {
    correctErrorTypes = true
    annotationProcessor("green.sailor.kython.generation.DictProcessor")
}


dependencies {
    // === CORE DEPENDENCIES === //
    // == picocli - for CLI parsing == //
    implementation(group = "info.picocli", name = "picocli", version = "4.1.2")

    // == Apache Commons Collections - For our custom dict == //
    implementation(group = "org.apache.commons", name = "commons-collections4", version = "4.4")

    // == API Guardian Annotations == //
    api(group = "org.apiguardian", name = "apiguardian-api", version = "1.1.+")

    // === TEST === //
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter", version = "5.5.2")

    // === CODE GENERATION === //
    api(project(":kython-annotations"))
    kapt(project(":kython-generators"))
}


application {
    mainClassName = "green.sailor.kython.MakeUp"
}


tasks.distZip {
    archiveBaseName.set("kython")
}

sourceSets {
    main {
        java {
            srcDir("${buildDir.absolutePath}/generated/source/kaptKotlin/")
        }
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

tasks.compileKotlin {
    kotlinOptions.freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
}

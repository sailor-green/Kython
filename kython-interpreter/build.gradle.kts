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
    id("jacoco")
}

kapt {
    correctErrorTypes = true
    annotationProcessor("green.sailor.kython.generation.DictProcessor")
}


dependencies {
    // === CORE DEPENDENCIES === //

    // == Apache Commons Collections - For our custom dict == //
    implementation(group = "org.apache.commons", name = "commons-collections4", version = "4.4")

    // == API Guardian Annotations == //
    api(group = "org.apiguardian", name = "apiguardian-api", version = "1.1.+")

    // == SUBPROJECTS == //
    api(project(":kython-kyc"))
    api(project(":kython-util"))

    // === TEST === //
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter", version = "5.5.2")

    // === CODE GENERATION === //
    api(project(":kython-codegen-annotations"))
    kapt(project(":kython-codegen"))
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

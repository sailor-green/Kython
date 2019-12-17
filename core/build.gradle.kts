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


plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("application")
    id("jacoco")
}

kapt {
    correctErrorTypes = true
    // FIXME: Who knows what it actually wants here.
    annotationProcessor("green.sailor.kython.generation.builtins.BuiltinMethodProcessor")
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

    // === CODE GENERATION === //
    implementation(project(":annotations"))
    kapt(project(":generators"))

}


application {
    mainClassName = "green.sailor.kython.MakeUp"
}


sourceSets {
    main {
        java {
            srcDir("${buildDir.absolutePath}/generated/source/kaptKotlin/")
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

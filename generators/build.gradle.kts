plugins {
    kotlin("jvm")
    kotlin("kapt")
}


dependencies {
    // === CODE GENERATION === //
    implementation(project(":annotations"))
    compileOnly("com.google.auto.service:auto-service:1.0-rc6")
    kapt("com.google.auto.service:auto-service:1.0-rc6")
    compileOnly("com.squareup:kotlinpoet:1.4.4")
}

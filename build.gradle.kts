plugins {
    application

    alias(libs.plugins.kotlin)
    alias(libs.plugins.serialization)
    alias(libs.plugins.shadow)
}

group = "io.github.xffc.${rootProject.name}"
version = "1.0"

application.mainClass = "$group.MainKt"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.serialization.json)
    implementation(libs.kraft)
    implementation(libs.telegram)
    implementation(libs.kache)
}

sourceSets.main {
    java.srcDir("src")
    kotlin.srcDir("src")
    resources.srcDir("resources")
}

kotlin {
    jvmToolchain(21)
}
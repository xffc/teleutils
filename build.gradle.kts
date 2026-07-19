plugins {
    application
    kotlin("jvm") version "2.4.0"
    kotlin("plugin.serialization") version "2.4.0"
}

group = "io.github.xffc"
version = "1.0"

application.mainClass = "$group.${project.name}.TeleUtilsMainKt"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.xffc.kmc:net:1.1")
    implementation("io.github.xffc.kmc:serverstatus:1.1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")

    implementation("com.github.pengrad:java-telegram-bot-api:10.1.0")

    implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.9.1")
    implementation(kotlin("reflect"))

    implementation("net.kyori:adventure-text-serializer-plain:5.2.0")
    implementation("net.kyori:adventure-text-serializer-gson:5.2.0")

    implementation("org.slf4j:slf4j-api:2.0.18")
    runtimeOnly("ch.qos.logback:logback-classic:1.5.38")
}

sourceSets.main {
    kotlin.srcDir("src")
    resources.srcDir("resources")
}

kotlin.jvmToolchain(25)
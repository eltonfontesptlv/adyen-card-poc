val adyenVersion = project.properties["adyen.api.version"]
val logBackVersion = project.properties["logBack.api.version"]

plugins {
    kotlin("jvm") version "1.7.10"
}

group = "br.com.petlove"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.json:json:20220320")
    implementation("org.bouncycastle:bcprov-jdk15on:1.47")
    implementation("com.adyen:adyen-java-api-library:$adyenVersion")
    runtimeOnly("ch.qos.logback.contrib:logback-json-classic:$logBackVersion")
    runtimeOnly("ch.qos.logback.contrib:logback-jackson:$logBackVersion")
    runtimeOnly("ch.qos.logback:logback-classic")
}

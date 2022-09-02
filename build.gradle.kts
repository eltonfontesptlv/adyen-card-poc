val adyenVersion = project.properties["adyen.api.version"]
val logBackVersion = project.properties["logBack.api.version"]

plugins {
    kotlin("jvm") version "1.7.10"
}

group = "poc-hub-payments.br.com.petlove"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.adyen:adyen-java-api-library:$adyenVersion")
    implementation("org.bouncycastle:bcprov-jdk15on:1.47")
    runtimeOnly("ch.qos.logback.contrib:logback-json-classic:$logBackVersion")
    runtimeOnly("ch.qos.logback.contrib:logback-jackson:$logBackVersion")
    runtimeOnly("ch.qos.logback:logback-classic")
}

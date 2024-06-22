plugins {
    id("klox.kotlin-application-conventions")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-script-runtime:1.4.32")
}

application {
    mainClass.set("com.ericrochester.tools.MainKt")
}
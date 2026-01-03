plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    implementation("com.mysql:mysql-connector-j:9.1.0")
    implementation("com.toedter:jcalendar:1.4")
    implementation("com.github.librepdf:openpdf:1.3.30")
}


tasks.test {
    useJUnitPlatform()
}
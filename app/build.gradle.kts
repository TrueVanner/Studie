plugins {
    alias(libs.plugins.android.application)
//    id("com.android.application") version "8.1.4" apply false
    id("org.sonarqube") version "6.0.1.5171"
    id("jacoco")
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "nl.tue.appdev.studie"
    compileSdk = 35

    defaultConfig {
        applicationId = "nl.tue.appdev.studie"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // auth
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    // firestore
    implementation(libs.firebase.firestore)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.firebase.database)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // qr code
    implementation(libs.zxing.core)
    implementation(libs.zxing.android.embedded)
}


sonar {
    properties {
        property("sonar.host.url", "http://localhost:9000/")
        property("sonar.projectKey", "Studie")
        property("sonar.projectName", "Studi/e")
        property("sonar.token", System.getenv("SONAR_TOKEN"))
        property("sonar.tests", "src/test/java")
        property("sonar.test.inclusions", "**/*Test*/**")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.sources", "src/main/java")
        property("sonar.exclusions", listOf(
            "**/*Test*/**",
            "build/**",
            "*.json",
            "**/*test*/**",
            "**/.gradle/**",
            "**/R.class"
        ).joinToString(", "))
        property("sonar.java.coveragePlugin", "jacoco")
        property("sonar.junit.reportPaths", "**/test-results/**/*.xml")
        property("sonar.coverage.jacoco.xmlReportPaths", "${projectDir}/build/reports/jacoco/jacocoFullReport/jacocoFullReport.xml")
    }
}

// List of modules that don't require Jacoco
val ignoredByJacoco = mutableListOf<String>()
val fileFilter = mutableListOf<String>()
val buildKotlinClasses = "/tmp/kotlin-classes/debug"
val buildJavaClasses = "/intermediates/javac/debug/classes"
val testExecutionFile = "/outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"

// List of files that can be ignored for test coverage
val coverageExclusions = listOf(
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*"
)

// Apply additional build steps to sub-projects
subprojects.forEach { project ->
    if (!ignoredByJacoco.contains(project.name)) {
        project.pluginManager.apply("jacoco")

        project.extensions.configure(org.gradle.testing.jacoco.plugins.JacocoPluginExtension::class.java) {
            toolVersion = "0.8.7"
        }

        project.tasks.register("jacocoReport", JacocoReport::class.java) {
            group = "Reporting"
            description = "Generate Jacoco coverage reports after running tests."
            dependsOn("testDebugUnitTest")

            val buildDebug = listOf("${project.layout.buildDirectory}$buildKotlinClasses", "${project.layout.buildDirectory}$buildJavaClasses")
            val classDirs = project.files(buildDebug.map { classPath ->
                project.fileTree(classPath) {
//                    include(fileFilter)
                    exclude(coverageExclusions)
                }
            })

            val mainSrc = "${project.projectDir}/src/main/java"
            sourceDirectories.from(project.files(mainSrc))
            classDirectories.from(classDirs)
            executionData.from(project.files("${project.layout.buildDirectory}$testExecutionFile"))

            reports {
                xml.required.set(true)
                html.required.set(true)
            }
        }
    }
}

// Root task that generates an aggregated Jacoco test coverage report for all sub-projects
tasks.register("jacocoFullReport", JacocoReport::class.java) {
    group = "Reporting"
    description = "Generates an aggregate report from all subprojects"

    // Initialize an empty ConfigurableFileCollection
    val classDirs = project.files()

    // Filter subprojects to include only those not ignored
    val includedProjects = subprojects.filter { !ignoredByJacoco.contains(it.name) }

    // Collect class directories from included projects
    includedProjects.forEach { prj ->
        val buildDebug = listOf(
            "${prj.layout.buildDirectory}$buildKotlinClasses",
            "${prj.layout.buildDirectory}$buildJavaClasses"
        )

        buildDebug.forEach { classPath ->
            val fileTree = prj.fileTree(classPath) {
//                include(fileFilter)
                exclude(coverageExclusions)
            }
            // Accumulate file trees into the classDirs collection
            classDirs.from(fileTree)
        }
    }

    val dirs = includedProjects.map { "${it.projectDir}/src/main/java" }
    sourceDirectories.setFrom(project.files(dirs))
    classDirectories.setFrom(classDirs)
    executionData.setFrom(includedProjects.flatMap { it.tasks.withType(JacocoReport::class.java).map { task -> task.executionData } })

    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val localProperties = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        localFile.inputStream().use { load(it) }
    }
}

fun stringProperty(name: String, default: String): String =
    (localProperties.getProperty(name) ?: project.findProperty(name) as String?) ?: default

android {
    namespace = "com.a1exymoroz.mergefruit"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.a1exymoroz.mergefruit"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    // Release signing is optional at the Gradle level: if no keystore is
    // configured (e.g. a fresh checkout without local.properties entries),
    // `assembleRelease` still succeeds and just produces an unsigned build
    // instead of failing. See local.properties.template for the properties.
    // releaseKeystorePath is resolved relative to *this* module dir (app/),
    // not the project root.
    val releaseKeystorePath = stringProperty("releaseKeystorePath", "")
    val hasReleaseSigning = releaseKeystorePath.isNotBlank() && file(releaseKeystorePath).exists()

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseKeystorePath)
                storePassword = stringProperty("releaseKeystorePassword", "")
                keyAlias = stringProperty("releaseKeyAlias", "")
                keyPassword = stringProperty("releaseKeyPassword", "")
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField(
                "String",
                "API_BASE_URL",
                "\"${stringProperty("debugApiBaseUrl", stringProperty("DEBUG_API_BASE_URL", "http://10.0.2.2:8080"))}\"",
            )
            buildConfigField(
                "String",
                "TEST_USER_EMAIL",
                "\"${stringProperty("testUserEmail", stringProperty("TEST_USER_EMAIL", ""))}\"",
            )
            buildConfigField(
                "String",
                "TEST_USER_PASSWORD",
                "\"${stringProperty("testUserPassword", stringProperty("TEST_USER_PASSWORD", ""))}\"",
            )
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // LoginScreen references these unconditionally (debug convenience
            // pre-fill); release must never ship real test credentials, so
            // these are always empty here regardless of local.properties.
            buildConfigField("String", "TEST_USER_EMAIL", "\"\"")
            buildConfigField("String", "TEST_USER_PASSWORD", "\"\"")
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            buildConfigField(
                "String",
                "API_BASE_URL",
                "\"${stringProperty("releaseApiBaseUrl", stringProperty("RELEASE_API_BASE_URL", "https://your-backend.example.com"))}\"",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")

    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.navigation:navigation-compose:2.7.7")

    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.11.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    implementation("org.dyn4j:dyn4j:4.2.2")
}

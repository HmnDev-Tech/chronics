import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

/**
 * A stable signing key is supplied through the environment so that every APK,
 * from CI or from a local build, carries the same signature and updates install
 * over the previous build. When the key is absent the build falls back to the
 * default debug signing, which is fine for throwaway builds only.
 *
 * Expected variables:
 *   CHRONICS_KEYSTORE_PATH    path to a PKCS12 keystore
 *   CHRONICS_STORE_PASSWORD   keystore password
 *   CHRONICS_KEY_ALIAS        key alias inside the keystore
 *   CHRONICS_KEY_PASSWORD     key password (defaults to the store password)
 */
val stableKeystoreFile = System.getenv("CHRONICS_KEYSTORE_PATH")
    ?.let { file(it) }
    ?.takeIf { it.isFile }
val stableStorePassword = System.getenv("CHRONICS_STORE_PASSWORD")
val stableKeyAlias = System.getenv("CHRONICS_KEY_ALIAS") ?: "chronics"
val stableKeyPassword = System.getenv("CHRONICS_KEY_PASSWORD") ?: stableStorePassword

android {
    namespace = "com.hmndev.chronika"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.hmndev.chronika"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        stableKeystoreFile?.let { keyStore ->
            create("stable") {
                storeFile = keyStore
                storeType = "PKCS12"
                storePassword = stableStorePassword
                keyAlias = stableKeyAlias
                keyPassword = stableKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            signingConfigs.findByName("stable")?.let { signingConfig = it }
        }
        release {
            signingConfigs.findByName("stable")?.let { signingConfig = it }
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE*"
            excludes += "/META-INF/NOTICE*"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.addAll(
            listOf(
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
                "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
                "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi",
                "-opt-in=androidx.compose.animation.ExperimentalAnimationApi"
            )
        )
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.19.1")
    implementation("androidx.activity:activity-compose:1.12.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")

    implementation("androidx.compose.ui:ui:1.12.1")
    implementation("androidx.compose.ui:ui-graphics:1.12.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.12.1")
    implementation("androidx.compose.foundation:foundation:1.12.1")
    implementation("androidx.compose.material3:material3:1.5.0-beta02")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")

    implementation("dev.rikka.shizuku:api:13.1.5")
    implementation("dev.rikka.shizuku:provider:13.1.5")

    testImplementation("junit:junit:4.13.2")
    debugImplementation("androidx.compose.ui:ui-tooling:1.12.1")
}

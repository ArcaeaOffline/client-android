// This file is modified from "OpenCV-android-sdk/sdk/build.gradle".
// The original "OpenCV-android-sdk/sdk/build.gradle" is a part of OpenCV project.
// It is subject to the license terms in the LICENSE file found in the top-level directory
// of the OpenCV Android SDK distribution (version 4.8.0) and at http://opencv.org/license.html.

//
// Notes about integration OpenCV into existed Android Studio application project are below (application "app" module should exist).
//
// This file is located in <OpenCV-android-sdk>/sdk directory (near "etc", "java", "native" subdirectories)
//
// Add module into Android Studio application project:
//
// - Android Studio way:
//   (will copy almost all OpenCV Android SDK into your project, ~200Mb)
//
//   Import module: Menu -> "File" -> "New" -> "Module" -> "Import Gradle project":
//   Source directory: select this "sdk" directory
//   Module name: ":opencv"
//
// - or attach library module from OpenCV Android SDK
//   (without copying into application project directory, allow to share the same module between projects)
//
//   Edit "settings.gradle" and add these lines:
//
//   def opencvsdk="<path_to_opencv_android_sdk_rootdir>"
//   // You can put declaration above into gradle.properties file instead (including file in HOME directory),
//   // but without "def" and apostrophe symbols ("): opencvsdk=<path_to_opencv_android_sdk_rootdir>
//   include ":opencv"
//   project(":opencv").projectDir = new File(opencvsdk + "/sdk")
//
//
//
// Add dependency into application module:
//
// - Android Studio way:
//   "Open Module Settings" (F4) -> "Dependencies" tab
//
// - or add "project(":opencv")" dependency into app/build.gradle:
//
//   dependencies {
//       implementation fileTree(dir: "libs", include: ["*.jar"])
//       ...
//       implementation project(":opencv")
//   }
//
//
//
// Load OpenCV native library before using:
//
// - avoid using of "OpenCVLoader.initAsync()" approach - it is deprecated
//   It may load library with different version (from OpenCV Android Manager, which is installed separatelly on device)
//
// - use "System.loadLibrary("opencv_java4")" or "OpenCVLoader.initDebug()"
//
//
//
// Native C++ support (necessary to use OpenCV in native code of application only):
//
// - Use find_package() in app/CMakeLists.txt:
//
//   find_package(OpenCV 4.8 REQUIRED java)
//   ...
//   target_link_libraries(native-lib ${OpenCV_LIBRARIES})
//
// - Add "OpenCV_DIR" and enable C++ exceptions/RTTI support via app/build.gradle
//   Documentation about CMake options: https://developer.android.com/ndk/guides/cmake.html
//
//   defaultConfig {
//       ...
//       externalNativeBuild {
//           cmake {
//               cppFlags "-std=c++11 -frtti -fexceptions"
//               arguments "-DOpenCV_DIR=" + opencvsdk + "/sdk/native/jni" // , "-DANDROID_ARM_NEON=TRUE"
//           }
//       }
//   }
//
// - (optional) Limit/filter ABIs to build ("android" scope of "app/build.gradle"):
//   Useful information: https://developer.android.com/studio/build/gradle-tips.html (Configure separate APKs per ABI)
//
//   splits {
//       abi {
//           enable true
//           universalApk false
//           reset()
//           include "armeabi-v7a" // , "x86", "x86_64", "arm64-v8a"
//       }
//   }
//

// this suppresses the android.defaultConfig.externalNativeBuild.cmake unstable api warning
@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

val openCVersionName = "4.8.0"
val openCVersionCode = ((4 * 100 + 8) * 100 + 0) * 10 + 0

println("OpenCV: $openCVersionName($openCVersionCode) ${project.buildscript.sourceFile}")

android {
    namespace = "org.opencv"
    compileSdk = 34
    ndkVersion = "22.1.7171670"

    defaultConfig {
        minSdk = 21
        // targetSdk is deprecated and it is safe to remove directly
        // targetSdk = 34

        externalNativeBuild {
            cmake {
                arguments("-DANDROID_STL=c++_shared")
                targets("opencv_jni_shared")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        abortOnError = false
    }

    buildTypes {
        debug {
            packaging {
                // doNotStrip is deprecated
                // doNotStrip("**/*.so")
                jniLibs.keepDebugSymbols.add("**/*.so")  // controlled by OpenCV CMake scripts
            }
        }
        release {
            packaging {
                // doNotStrip("**/*.so")
                jniLibs.keepDebugSymbols.add("**/*.so")  // controlled by OpenCV CMake scripts
            }
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.txt")
        }
    }

    sourceSets {
        named("main") {
            jniLibs.srcDirs("native/libs")
            java.srcDirs("java/src")
            aidl.srcDirs("java/src")
            res.srcDirs("java/res")
            manifest.srcFile("java/AndroidManifest.xml")
        }
    }

    externalNativeBuild {
        cmake {
            path("${project.projectDir}/libcxx_helper/CMakeLists.txt")
        }
    }

    buildFeatures {
        aidl = true
        buildConfig = true
    }
}

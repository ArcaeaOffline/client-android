# Rules defined in this file affects the upstream (:app) proguard behavior.
#
# The :core **dependency** never builds *independently (as an app, etc.)*, so rules should stay here
# in common situations.

-keep public class org.opencv.core.CvException { *; }
# onnx runtime
# @see https://onnxruntime.ai/docs/build/android.html#note-proguard-rules-for-r8-minimization-android-app-builds-to-work
-keep class ai.onnxruntime.** { *; }

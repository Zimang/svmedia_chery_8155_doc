# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep class com.desaysv.usbbaselib.** { *; }
-keepnames class com.desaysv.usbbaselib.** { *; }
-keep class com.desaysv.moduleusbvideo.businesslogic.** { *; }
-keepnames class com.desaysv.moduleusbvideo.businesslogic.** { *; }
-keep class com.desaysv.moduleusbvideo.bean.** { *; }
-keepnames class com.desaysv.moduleusbvideo.bean.** { *; }


-keep class net.sourceforge.pinyin4j.** { *; }
-keepnames class net.sourceforge.pinyin4j.** { *; }
-keep class com.hp.hpl.sparta.** { *; }
-keepnames class com.hp.hpl.sparta.** { *; }

-keep class com.desaysv.moduleusbvideo.vr.** { *; }
-keepnames class com.desaysv.moduleusbvideo.vr.** { *; }

-keep class com.desaysv.moduleusbvideo.view.** { *; }
-keepnames class com.desaysv.moduleusbvideo.view.** { *; }

-keep class android.animation.** { *; }
-keepnames class android.animation.** { *; }

-keep class com.iwall.cybersdk.** { *; }
-keep class org.bouncycastle.** { *; }
-keep class com.desaysv.moduleusbvideo.util.** { *; }
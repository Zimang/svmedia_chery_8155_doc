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
# Add *one* of the following rules to your Proguard configuration file.
# Alternatively, you can annotate classes and class members with @androidx.annotation.Keep
#忽略警告 不忽略可能打包不成功
-ignorewarnings
# keep the class and specified members from being removed or renamed
-keep class com.desaysv.localmediasdk.bean.MediaInfoBean { *; }

# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class com.desaysv.localmediasdk.bean.MediaInfoBean { *; }

# keep the class and specified members from being renamed only
-keepnames class com.desaysv.localmediasdk.bean.MediaInfoBean { *; }

# keep the specified class members from being renamed only
-keepclassmembernames class com.desaysv.localmediasdk.bean.MediaInfoBean { *; }

#android x
-keep public class * extends androidx.appcompat.app.AppCompatActivity
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends androidx.**

#混淆配置
-keep class com.desaysv.ivi.vdb.** { *; }
-keep class com.desaysv.ivi.platform.** { *; }
-keep class com.desaysv.ivi.extra.** { *; }

-keep class com.desaysv.moduleradio.ui.RadioPlayFragment
-keep class com.desaysv.moduleradio.utils.RadioStatusUtils

# keep everything in this package from being removed or renamed
-keep class com.desaysv.libradio.** { *; }

# keep everything in this package from being renamed only
-keepnames class com.desaysv.libradio.** { *; }

-keep public class com.desaysv.localmediasdk.bean.MediaInfoBean
# keep the class and specified members from being removed or renamed
-keep class com.desaysv.localmediasdk.bean.MediaInfoBean { *; }
# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class com.desaysv.localmediasdk.bean.MediaInfoBean { *; }
# keep the class and specified members from being renamed only
-keepnames class com.desaysv.localmediasdk.bean.MediaInfoBean { *; }
# keep the specified class members from being renamed only
-keepclassmembernames class com.desaysv.localmediasdk.bean.MediaInfoBean { *; }

-keep public class com.desaysv.localmediasdk.bean.MediaInfoBean
# keep the class and specified members from being removed or renamed
-keep class com.desaysv.localmediasdk.bean.MediaInfoBean { *; }
# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class com.desaysv.localmediasdk.bean.MediaInfoBean { *; }
# keep the class and specified members from being renamed only
-keepnames class com.desaysv.localmediasdk.bean.MediaInfoBean { *; }
# keep the specified class members from being renamed only
-keepclassmembernames class com.desaysv.localmediasdk.bean.MediaInfoBean { *; }

-keep public class com.desaysv.localmediasdk.bean.MediaInfoBean
# keep the class and specified members from being removed or renamed
-keep class com.desaysv.localmediasdk.bean.MediaInfoBean { *; }
# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class com.desaysv.localmediasdk.bean.MediaInfoBean { *; }
# keep the class and specified members from being renamed only
-keepnames class com.desaysv.localmediasdk.bean.MediaInfoBean { *; }
# keep the specified class members from being renamed only
-keepclassmembernames class com.desaysv.localmediasdk.bean.MediaInfoBean { *; }

-keep public class com.desaysv.moduleusbmusic.vr.MusicVRActionBean
# keep the class and specified members from being removed or renamed
-keep class com.desaysv.moduleusbmusic.vr.MusicVRActionBean { *; }
# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class com.desaysv.moduleusbmusic.vr.MusicVRActionBean { *; }
# keep the class and specified members from being renamed only
-keepnames class com.desaysv.moduleusbmusic.vr.MusicVRActionBean { *; }
# keep the specified class members from being renamed only
-keepclassmembernames class com.desaysv.moduleusbmusic.vr.MusicVRActionBean { *; }

-keep public class com.desaysv.moduleusbmusic.vr.SemanticBean
# keep the class and specified members from being removed or renamed
-keep class com.desaysv.moduleusbmusic.vr.SemanticBean { *; }
# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class com.desaysv.moduleusbmusic.vr.SemanticBean { *; }
# keep the class and specified members from being renamed only
-keepnames class com.desaysv.moduleusbmusic.vr.SemanticBean { *; }
# keep the specified class members from being renamed only
-keepclassmembernames class com.desaysv.moduleusbmusic.vr.SemanticBean { *; }

#up to IFlytek start
-keep public class com.desaysv.moduleusbmusic.vr.MusicVRUploadBean
# keep the class and specified members from being removed or renamed
-keep class com.desaysv.moduleusbmusic.vr.MusicVRUploadBean { *; }
-keep public class com.desaysv.moduleusbmusic.vr.MusicVRUploadBean$Data
# keep the class and specified members from being removed or renamed
-keep class com.desaysv.moduleusbmusic.vr.MusicVRUploadBean$Data { *; }
# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class com.desaysv.moduleusbmusic.vr.MusicVRUploadBean$Data { *; }
# keep the class and specified members from being renamed only
-keepnames class com.desaysv.moduleusbmusic.vr.MusicVRUploadBean$Data { *; }
# keep the specified class members from being renamed only
-keepclassmembernames class com.desaysv.moduleusbmusic.vr.MusicVRUploadBean$Data { *; }

-keep public class com.desaysv.moduleusbmusic.vr.MusicVRUploadBean$Data$DataInfo
# keep the class and specified members from being removed or renamed
-keep class com.desaysv.moduleusbmusic.vr.MusicVRUploadBean$Data$DataInfo { *; }
# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class com.desaysv.moduleusbmusic.vr.MusicVRUploadBean$Data$DataInfo { *; }
# keep the class and specified members from being renamed only
-keepnames class com.desaysv.moduleusbmusic.vr.MusicVRUploadBean$Data$DataInfo { *; }
# keep the specified class members from being renamed only
-keepclassmembernames class com.desaysv.moduleusbmusic.vr.MusicVRUploadBean$Data$DataInfo { *; }
#up to IFlytek end

-keep public class com.desaysv.moduleusbmusic.dataPoint.UploadData
# keep the class and specified members from being removed or renamed
-keep class com.desaysv.moduleusbmusic.dataPoint.UploadData { *; }
# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class com.desaysv.moduleusbmusic.dataPoint.UploadData { *; }
# keep the class and specified members from being renamed only
-keepnames class com.desaysv.moduleusbmusic.dataPoint.UploadData { *; }
# keep the specified class members from being renamed only
-keepclassmembernames class com.desaysv.moduleusbmusic.dataPoint.UploadData { *; }

-keep public class com.desaysv.moduleusbmusic.dataPoint.ContentData
# keep the class and specified members from being removed or renamed
-keep class com.desaysv.moduleusbmusic.dataPoint.ContentData { *; }
# keep the specified class members from being removed or renamed
# only if the class is preserved
-keepclassmembers class com.desaysv.moduleusbmusic.dataPoint.ContentData { *; }
# keep the class and specified members from being renamed only
-keepnames class com.desaysv.moduleusbmusic.dataPoint.ContentData { *; }
# keep the specified class members from being renamed only
-keepclassmembernames class com.desaysv.moduleusbmusic.dataPoint.ContentData { *; }

###############  BTMusic bean ###########################
-keep class com.desaysv.modulebtmusic.bean.** { *; }
-keep class com.desaysv.modulebtmusic.vr.bean.** { *; }
-keep class com.google.gson.** { *; }
-keep class com.desaysv.moduleradio.vr.** { *; }
-keep class com.desaysv.moduleradio.Trigger.** { *; }

-keep class com.desaysv.usbbaselib.** { *; }
# keep everything in this package from being renamed only
-keepnames class com.desaysv.usbbaselib.** { *; }

#埋点混淆
-keep class com.iwall.cybersdk.** { *; }
-keep class org.bouncycastle.** { *; }

-keep public class android.car.media.** { *; }
-keep public class android.car.** { *; }
-keep class com.desaysv.modulebtmusic.manager.**{*;}
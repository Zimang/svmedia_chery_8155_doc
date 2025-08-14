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
-ignorewarnings
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# 保持泛型不被混淆
-keepattributes Signature
# 保持反射不被混淆
-keepattributes EnclosingMethod
# 保持异常不被混淆
-keepattributes Exceptions
# 保持内部类不被混淆
-keepattributes Exceptions,InnerClasses

#Warning: can't find referenced class 和Warning: can't find superclass or interface
#上述问题，需-injars, otherwise you should specify it with -libraryjars. 还有-outjars
# 保持基本组件不被混淆
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

# 保持 Google 原生服务需要的类不被混淆
-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService

# Support包规则
-dontwarn android.support.**
-keep public class * extends android.support.v4.**
-keep public class * extends android.support.v7.**
-keep public class * extends android.support.annotation.**

# 保留自定义控件(继承自View)不被混淆
-keep public class * extends android.view.View {
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# 保留在Activity中的方法参数是view的方法(避免布局文件里面onClick被影响)
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

# 保持R(资源)下的所有类及其方法不能被混淆
-keep class **.R$* { *; }

# 需要序列化和反序列化的类不能被混淆(注：Java反射用到的类也不能被混淆)
-keepnames class * implements java.io.Serializable

# 保持 Serializable 序列化的类成员不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 保持 BaseAdapter 类不被混淆
-keep public class * extends android.widget.BaseAdapter { *; }
# 保持 CusorAdapter 类不被混淆
-keep public class * extends android.widget.CusorAdapter{ *; }

-keep public class com.example.linkdemo.LinkDemo.R$*{ public static final int *;}

#EventBus
-keep public class com.google.gson.**{*;}
-keep public class com.google.gson.examples.android.model.** { *; }
-keep public class com.hp.hpl.sparta.** { *; }
-keep class com.hp.hpl.sparta.** { *; }
-keep public class net.sourceforge.pinyin4j.** { *; }
-keep class net.sourceforge.pinyin4j.** { *; }
-keep class com.google.gson.** { *; }
-keep public class androidx.core.** { *; }
-keep public class androidx.** { *; }
-keep class com.google.android.material.** {*;}
-keep class androidx.** {*;}
-keep public class * extends androidx.**
-keep interface androidx.** {*;}
-keep public class android.** {*;}
-keep class android.** {*;}
-keep public class android.** {*;}
-keep interface android.** {*;}
-keep public class * extends android.**
-keep public class com.iflytek.autofly.custom.** { *; }
-keep public class org.apache.commons.net.** { *; }
-keep public interface org.apache.commons.net.** { *; }
-keep public class org.apache.commons.net.examples.** { *; }
-keep public interface org.apache.commons.net.examples.** { *; }
-keep public class org.apache.ftpserver.** { *; }
-keep public interface org.apache.ftpserver.** { *; }
-keep public class org.apache.mina.** { *; }
-keep public interface org.apache.mina.** { *; }
-keep public class org.slf4j.** { *; }
-keep public interface org.slf4j.** { *; }
-keep public class org.slf4j.impl.** { *; }
-keep public interface org.slf4j.impl.** { *; }
-keep class com.bumptech.** { *; }
-keep class com.bumptech.glide.** { *; }
-keep class com.desaysv.moduleusbmusic.glidev.** { *; }
#为确保对堆栈轨迹进行轨迹还原时清楚明确
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * {
    public <init>(org.json.JSONObject);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
#EventBus
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

-dontwarn android.os.**
-dontwarn android.util.**
-dontwarn com.android.internal.app.LocalePicker
-dontwarn android.app.*
-dontwarn com.google.android.material.**
-dontwarn androidx.**
-dontwarn android.support.**
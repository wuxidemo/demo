# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/leo/Library/Android/sdk-studio/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#-dontshrink
#-dontoptimize

-keep public class com.dw.merchant.R$*{
    public static final int *;
}

-keep class com.loopj.android.http.** {
        *;
}
-keep class com.github.rahatarmanahmed.cpv.** {
        *;
}
-keep class cz.msebera.** {
        *;
}
-keep class com.dd.** {
        *;
}
-keep class com.rengwuxian.materialedittext.** {
        *;
}
-keep class com.nineoldandroids.** {
        *;
}
-keep class com.viewpagerindicator.** {
        *;
}
#-keep class com.** {
#        *;
#}
-keep class com.google.zxing.** {
        *;
}

#ormlite
-keep class com.j256.ormlite.**{*;}
#-keepclassmembers class com.j256.** { *; }
#-keep enum com.j256.**
#-keepclassmembers enum com.j256.** { *; }
#-keep interface com.j256.**
#-keepclassmembers interface com.j256.** { *; }

-keep class com.dw.merchant.db.**{*;}


#-keep class com.alimama.mobile.** {
#        *;
#}

#-dontwarn com.umeng.update.**
#-keep class com.umeng.update.** {
#        *;
#}

-dontwarn android.util.**
#-keep class android.util.** { *; }


# For enumeration classes, See
# http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

#-keepclassmembers class * {
#    public <init>(org.json.JSONObject);
#}

#-keepclassmembers class **.R$* {
#    public static <fields>;
#}

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**




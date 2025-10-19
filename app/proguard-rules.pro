# Stripe Terminal SDK (your existing lines - keep these!)
-keep class com.stripe.stripeterminal.** { *; }
-keep class com.stripe.stripeterminal.models.** { *; }

# ADD THESE BELOW:

# Keep OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# Keep Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Keep Jackson (used by Stripe)
-keep class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.**
-dontwarn java.beans.**

# Keep SLF4J (suppress warnings)
-dontwarn org.slf4j.**
-keep class org.slf4j.** { *; }

# Keep data classes
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Android components
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
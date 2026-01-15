# Revisit this when datastore > 1.1.6 is released
# Repro: ./gradlew :app:installGithubRelease (should crash on startup)
# https://issuetracker.google.com/issues/413078297
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

# JEBML uses reflection
-keepclassmembers class * extends org.ebml.Element {
    <init>(...);
}

# Keep logs even with optimization
-keep class android.util.Log {
    public static *** d(...);
    public static *** e(...);
    public static *** i(...);
    public static *** v(...);
    public static *** w(...);
    public static *** wtf(...);
}

-dontwarn android.util.Log

# Google API Client rules
-keep class com.google.api.client.** { *; }
-keep interface com.google.api.client.** { *; }
-keep class com.google.api.services.drive.** { *; }
-keep interface com.google.api.services.drive.** { *; }
-keep class com.google.api.client.json.GenericJson { *; }
-keep class com.google.api.services.drive.model.** { *; }

# Google Http Client
-keep class com.google.api.client.http.HttpMethods { *; }
-keep class com.google.api.client.util.Data { *; }
-keep class com.google.api.client.util.FieldInfo { *; }
-keep class com.google.api.client.util.DateTime { *; }
-keep class com.google.api.client.util.NanoClock { *; }
-keep class com.google.api.client.util.Sleeper { *; }
-keep class com.google.api.client.util.Clock { *; }
-keep class com.google.api.client.util.ArrayValueMap { *; }
-keep class com.google.api.client.util.ArrayValueMap$ArrayValue { *; }
-keep class com.google.api.client.util.Types { *; }

# Needed for JSON parsing
-keepclassmembers class * {
  @com.google.api.client.util.Key <fields>;
}

-dontwarn com.google.api.client.**
-dontwarn com.google.j2objc.annotations.**
-dontwarn javax.annotation.**
-dontwarn org.checkerframework.**
-dontwarn javax.naming.**
-dontwarn org.apache.http.**


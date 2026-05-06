# Keep data model classes used with kotlinx.serialization
-keep class com.songlink.share.model.** { *; }

# Keep Retrofit interfaces
-keep interface com.songlink.share.api.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class **$$serializer { *; }

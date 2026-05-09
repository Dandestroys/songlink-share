# Keep data model and history classes used with kotlinx.serialization
-keep class com.songlink.share.model.** { *; }
-keep class com.songlink.share.HistoryRepository { *; }

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class **$$serializer { *; }

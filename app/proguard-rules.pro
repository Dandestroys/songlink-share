-keep class com.songlink.share.model.** { *; }
-allowaccessmodification

# Strip all Android logging in release
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# Strip Kotlin null-safety assertions (R8 proves types at compile time)
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkNotNull(java.lang.Object, java.lang.String);
    static void checkNotNullExpressionValue(java.lang.Object, java.lang.String);
    static void checkNotNullParameter(java.lang.Object, java.lang.String);
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
    static void checkExpressionValueIsNotNull(java.lang.Object, java.lang.String);
    static void throwNpe(java.lang.String);
    static void throwUninitializedPropertyAccessException(java.lang.String);
    static void throwIllegalArgument(java.lang.String);
    static void throwIllegalState(java.lang.String);
}

-dontwarn kotlin.reflect.**

# ==============================================================================
# VaultX Enterprise ProGuard / R8 Configuration (Full Mode Optimized)
# ==============================================================================
# Enabled alongside `isMinifyEnabled = true` and `isShrinkResources = true`
# in `app/build.gradle.kts`.

# ------------------------------------------------------------------------------
# 1. Source File & Line Numbers (Crashlytics / Production Stacktrace Deobfuscation)
# ------------------------------------------------------------------------------
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod

# ------------------------------------------------------------------------------
# 2. Kotlin Metadata & Coroutines
# ------------------------------------------------------------------------------
-keep class kotlin.Metadata { *; }

# Allow R8 to optimize Continuation and CoroutineDispatcher while preserving
# ServiceLoader-backed MainDispatcherFactory and CoroutineExceptionHandler
-dontwarn kotlinx.coroutines.flow.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Preserve volatile fields updated via AtomicFieldUpdater in kotlinx.coroutines
-keepclassychangemembers class kotlinx.** {
    volatile <fields>;
}

# ------------------------------------------------------------------------------
# 3. Kotlinx Serialization (and Moshi / Reflective Fallback Protection)
# ------------------------------------------------------------------------------
# Keep @Serializable companion objects and generated $serializer classes
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep serializer() lookup on all @Serializable classes across VaultX modules
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepnames class <2>$<3>
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1>$* {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1>$*$serializer {
    <init>();
    *** INSTANCE;
    kotlinx.serialization.KSerializer[] typeParametersSerializers();
}

# Explicitly protect network DTOs & domain enums
-keep,allowobfuscation,allowshrinking class com.vaultx.core.network.dto.** { *; }
-keepclassmembers enum com.vaultx.core.model.CollectibleCategory {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Optional Moshi protection (if Moshi adapters or @JsonClass are introduced)
-dontwarn com.squareup.moshi.**
-keepclassmembers @com.squareup.moshi.JsonClass class * extends java.lang.Enum {
    <fields>;
    **[] values();
}
-if @com.squareup.moshi.JsonClass class *
-keepnames class <1>
-if @com.squareup.moshi.JsonClass class *
-keep class <1>JsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}

# ------------------------------------------------------------------------------
# 4. Room Database Schemas, Entities, DAOs & TypeConverters
# ------------------------------------------------------------------------------
# Keep Room database implementations generated via KSP (*_Impl)
-keep class * extends androidx.room.RoomDatabase {
    void <init>();
}
-keep @androidx.room.Database class * { *; }

# Keep @Entity data classes, constructors, and column getters/setters
-keep @androidx.room.Entity class * { *; }
-keep class com.vaultx.core.database.entity.** {
    <fields>;
    <init>(...);
    *** get*();
    void set*(...);
}

# Keep Room TypeConverters referenced via @TypeConverters
-keep class com.vaultx.core.database.converter.** { *; }

# ------------------------------------------------------------------------------
# 5. Retrofit2 & OkHttp3
# ------------------------------------------------------------------------------
# Retrofit inspects generic return types (e.g., List<MarketPriceUpdateDto>),
# HTTP method annotations (@GET, @Path, @Query), and Kotlin suspend continuations
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Preserve VaultX Retrofit service interface contract
-keep interface com.vaultx.core.network.api.** { *; }

# Suppress warnings from OkHttp platform-specific TLS providers (Conscrypt/BouncyCastle)
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn retrofit2.**

# ------------------------------------------------------------------------------
# 6. Dagger-Hilt & ViewModel Injection
# ------------------------------------------------------------------------------
-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Consumer ProGuard rules for :core:network
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class com.vaultx.core.network.dto.** {
    kotlinx.serialization.KSerializer serializer(...);
}

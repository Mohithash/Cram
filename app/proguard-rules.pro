# kotlinx.serialization: keep the generated serializers for our @Serializable models.
-keepclassmembers class com.mohithash.cram.**.** {
    *** Companion;
    *** serializer(...);
}
-keepclasseswithmembers class com.mohithash.cram.**.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.mohithash.cram.**.**$$serializer { *; }
-dontwarn org.slf4j.**

# Add project specific ProGuard rules here.
# You can use the set property applyDefaultProguardFile
# to include the default files in sdk root directory.
# For more information, see
#   http://developer.android.com/studio/write/proguard-rules.pro

# Moshi reflection adapters need the API models and their constructor parameter names.
-keep class com.hyderabad.booking.common.model.** { *; }
-keep class com.hyderabad.booking.common.dto.** { *; }
-keepclassmembers class kotlin.Metadata { *; }

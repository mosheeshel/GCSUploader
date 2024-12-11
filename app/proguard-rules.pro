# proguard-rules.pro
# Add any specific ProGuard rules if needed
-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault
-keepnames class com.google.api.client.** { *; }
-dontwarn org.brotli.**
-dontwarn org.conscrypt.**

-dontwarn nl.adaptivity.xmlutil.**

-dontwarn com.ibm.icu.**

-keep class org.slf4j.**

-keep class io.ktor.client.engine.apache5.** { *; }
-keep class io.ktor.client.engine.** { *; }
-keep class io.ktor.client.** { *; }

-keep class org.sqlite.** { *; }
-keep interface java.sql.Driver

-keep class androidx.navigation3.** { *; }
-keep class androidx.navigationevent.** { *; }
-keep class androidx.compose.runtime.** { *; }

-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

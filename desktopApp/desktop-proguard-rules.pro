-dontwarn org.brotli.**
-dontwarn org.conscrypt.**

-dontwarn io.ktor.**
-dontwarn nl.adaptivity.xmlutil.**

-dontwarn com.ibm.icu.**

-keep class org.slf4j.**

-keep class org.sqlite.** { *; }
-keep interface java.sql.Driver

-keep class androidx.navigation3.** { *; }
-keep class androidx.navigationevent.** { *; }
-keep class androidx.compose.runtime.** { *; }

-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

-dontwarn org.brotli.**
-dontwarn org.conscrypt.**

-dontwarn io.ktor.**
-dontwarn nl.adaptivity.xmlutil.**

-dontwarn com.ibm.icu.**

-dontwarn org.slf4j.**

# SQLite JDBC Driver
-keep class org.sqlite.** { *; }
-keep interface java.sql.Driver

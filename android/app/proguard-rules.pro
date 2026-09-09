-dontwarn com.facebook.**
-dontwarn androidx.work.impl.**
-dontwarn androidx.room.**
-dontwarn androidx.sqlite.db.**

-keep class androidx.work.impl.** { *; }
-keep class **.ListenableWorker { *; }
-keep class **.WorkerParameters { *; }

#noinspection ShrinkerUnresolvedReference
-keep class * extends androidx.room.RoomDatabase
-keep class androidx.room.** { *; }

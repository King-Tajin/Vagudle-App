-dontwarn com.facebook.**
-dontwarn androidx.work.impl.**
-dontwarn androidx.room.**
-dontwarn androidx.sqlite.db.**

-keep class androidx.work.impl.** { *; }
#noinspection ShrinkerUnresolvedReference
-keep class * extends androidx.work.ListenableWorker { *; }
-keepclassmembers class * extends androidx.work.ListenableWorker {
    public <init>(...);
}
-keep class **.WorkerParameters { *; }

#noinspection ShrinkerUnresolvedReference
-keep class * extends androidx.room.RoomDatabase
-keep class androidx.room.** { *; }

-dontwarn com.facebook.**
-dontwarn androidx.work.impl.**
-dontwarn androidx.room.**
-dontwarn androidx.sqlite.db.**

#noinspection ShrinkerUnresolvedReference
-keep class * extends androidx.work.ListenableWorker
-keepclassmembers class * extends androidx.work.ListenableWorker {
    #noinspection ShrinkerUnresolvedReference
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.InputMerger
-keepclassmembers class * extends androidx.work.InputMerger {
    public <init>();
}

-keep class * extends androidx.room.RoomDatabase
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public <init>();
}

-keep class * extends androidx.glance.appwidget.GlanceAppWidget
-keepclassmembers class * extends androidx.glance.appwidget.GlanceAppWidget {
    public <init>();
}
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver
-keepclassmembers class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver {
    public <init>();
}

-keepattributes InnerClasses,EnclosingMethod,Signature,*Annotation*
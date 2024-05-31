package pt.ulisboa.tecnico.cmov.pharmacist;

import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import com.google.firebase.database.FirebaseDatabase;

import pt.ulisboa.tecnico.cmov.pharmacist.services.AlarmReceiver;

public class PharmacistApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Enable Firebase persistence only once here
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        createNotificationChannel();
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE);

        long intervalMillis = 60000;
        long triggerTime = SystemClock.elapsedRealtime() + intervalMillis;

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, intervalMillis, pendingIntent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Pharmacist";
            String description = "Pharmacist Notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Pharmacist", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}

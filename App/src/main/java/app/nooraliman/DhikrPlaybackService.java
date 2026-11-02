package app.nooraliman;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

public class DhikrPlaybackService extends Service {
    static final String ACTION_PLAY = "app.nooraliman.action.PLAY_DHIKR";
    static final String ACTION_STOP = "app.nooraliman.action.STOP_DHIKR";
    private static final String CHANNEL = "dhikr_playback";
    private static final int NOTIFICATION_ID = 72;
    private MediaPlayer player;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }
        String type = intent == null ? "subhan"
                : intent.getStringExtra(DhikrAlarmScheduler.EXTRA_TYPE);
        float volume = intent == null ? 0.6f
                : intent.getFloatExtra(DhikrAlarmScheduler.EXTRA_VOLUME, 0.6f);
        createChannel();
        startForeground(NOTIFICATION_ID, notification(type));
        play(type, volume);
        return START_NOT_STICKY;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationChannel channel = new NotificationChannel(CHANNEL,
                "التسبيح الدوري", NotificationManager.IMPORTANCE_LOW);
        channel.setSound(null, null);
        channel.enableVibration(false);
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(channel);
    }

    private Notification notification(String type) {
        Intent stop = new Intent(this, DhikrPlaybackService.class).setAction(ACTION_STOP);
        PendingIntent stopPending = PendingIntent.getService(this, 73, stop,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, CHANNEL)
                : new Notification.Builder(this).setPriority(Notification.PRIORITY_LOW);
        return builder.setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("التسبيح الدوري")
                .setContentText(dhikrName(type))
                .setCategory(Notification.CATEGORY_SERVICE)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_pause, "إيقاف", stopPending)
                .build();
    }

    private void play(String type, float volume) {
        releasePlayer();
        try {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            Uri audio = Uri.parse("android.resource://" + getPackageName() + "/" + audioResource(type));
            player = new MediaPlayer();
            player.setAudioAttributes(attributes);
            player.setDataSource(this, audio);
            player.setWakeMode(this, android.os.PowerManager.PARTIAL_WAKE_LOCK);
            float level = Math.max(0f, Math.min(1f, volume));
            player.setVolume(level, level);
            player.setOnCompletionListener(mp -> stopSelf());
            player.setOnErrorListener((mp, what, extra) -> {
                stopSelf();
                return true;
            });
            player.prepare();
            player.start();
        } catch (Exception error) {
            stopSelf();
        }
    }

    private int audioResource(String type) {
        switch (type == null ? "subhan" : type) {
            case "salawat": return R.raw.salawat;
            case "hamd": return R.raw.dhikr_hamd;
            case "akbar": return R.raw.dhikr_akbar;
            default: return R.raw.dhikr_subhan;
        }
    }

    private String dhikrName(String type) {
        switch (type == null ? "subhan" : type) {
            case "hamd": return "الحمد لله";
            case "akbar": return "الله أكبر";
            case "salawat": return "صلوا على رسول الله";
            default: return "سبحان الله";
        }
    }

    private void releasePlayer() {
        if (player == null) return;
        try { player.stop(); } catch (Exception ignored) {}
        player.release();
        player = null;
    }

    @Override
    public void onDestroy() {
        releasePlayer();
        if (Build.VERSION.SDK_INT >= 24) stopForeground(STOP_FOREGROUND_REMOVE);
        else stopForeground(true);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}

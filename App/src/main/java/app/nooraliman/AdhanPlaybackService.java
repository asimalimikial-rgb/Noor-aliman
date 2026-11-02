package app.nooraliman;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;

public class AdhanPlaybackService extends Service {
    static final String ACTION_PLAY = "app.nooraliman.action.PLAY_ADHAN";
    static final String ACTION_STOP = "app.nooraliman.action.STOP_ADHAN";
    static final String ACTION_STOP_PREVIEW = "app.nooraliman.action.STOP_PREVIEW";
    static final String ACTION_UPDATE_NOTIFICATION = "app.nooraliman.action.UPDATE_ADHAN_NOTIFICATION";
    private static final String CHANNEL = "adhan_playback_controls_v2";
    private static final int NOTIFICATION_ID = 71;
    private static volatile boolean playing;
    private MediaPlayer player;
    private boolean preview;
    private String prayerName = "";
    private String currentSound = "classic";

    static boolean isPlaying() { return playing; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }
        if (intent != null && ACTION_STOP_PREVIEW.equals(intent.getAction())) {
            if (preview) stopSelf();
            else if (player == null) stopSelf(startId);
            return START_NOT_STICKY;
        }
        if (intent != null && ACTION_UPDATE_NOTIFICATION.equals(intent.getAction())) {
            if (player != null) {
                startForeground(NOTIFICATION_ID, buildNotification(prayerName, currentSound));
            } else stopSelf(startId);
            return START_NOT_STICKY;
        }

        createChannel();
        prayerName = intent == null ? "" : intent.getStringExtra(PrayerAlarmScheduler.EXTRA_NAME);
        currentSound = intent == null ? "classic"
                : intent.getStringExtra(PrayerAlarmScheduler.EXTRA_SOUND);
        preview = intent != null && intent.getBooleanExtra(PrayerAlarmScheduler.EXTRA_PREVIEW, false);
        if (currentSound == null) currentSound = "classic";
        startForeground(NOTIFICATION_ID, buildNotification(prayerName, currentSound));
        playAdhan(currentSound);
        return START_NOT_STICKY;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationChannel channel = new NotificationChannel(CHANNEL,
                "تشغيل الأذان", NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("يظهر أثناء تشغيل الأذان الكامل");
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        channel.setSound(null, null);
        channel.enableVibration(false);
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                .createNotificationChannel(channel);
    }

    private Notification buildNotification(String prayer, String sound) {
        boolean showControls = getSharedPreferences(PrayerAlarmScheduler.PREFS, MODE_PRIVATE)
                .getBoolean("showAdhanNotification", true);
        Intent stop = new Intent(this, AdhanPlaybackService.class).setAction(ACTION_STOP);
        PendingIntent stopPending = PendingIntent.getService(this, 72, stop,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Intent open = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openPending = PendingIntent.getActivity(this, 73, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, CHANNEL)
                : new Notification.Builder(this).setPriority(Notification.PRIORITY_LOW);
        builder.setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(preview ? "تجربة صوت الأذان" : showControls
                        ? "حان الآن موعد أذان " + (prayer == null ? "" : prayer)
                        : "الأذان قيد التشغيل")
                .setContentText(showControls ? "يُشغّل " + soundName(sound) : "")
                .setCategory(Notification.CATEGORY_ALARM)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setContentIntent(openPending);
        if (showControls) builder.addAction(android.R.drawable.ic_media_pause, "إيقاف الأذان", stopPending);
        return builder.build();
    }

    private void playAdhan(String sound) {
        releasePlayer();
        try (AssetFileDescriptor audio = getAssets().openFd(soundAsset(sound))) {
            player = new MediaPlayer();
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build());
            player.setWakeMode(this, android.os.PowerManager.PARTIAL_WAKE_LOCK);
            player.setDataSource(audio.getFileDescriptor(), audio.getStartOffset(), audio.getLength());
            player.setOnCompletionListener(mp -> stopSelf());
            player.setOnErrorListener((mp, what, extra) -> {
                stopSelf();
                return true;
            });
            player.prepare();
            player.start();
            playing = true;
        } catch (Exception error) {
            stopSelf();
        }
    }

    private String soundAsset(String sound) {
        if ("aqib".equals(sound)) return "adhan-aqib.mp3";
        if ("short".equals(sound)) return "adhan-short.ogg";
        if ("azemmour".equals(sound)) return "adhan-azemmour.mp3";
        if ("quseir".equals(sound)) return "adhan-quseir.mp3";
        return "adhan.ogg";
    }

    private String soundName(String sound) {
        if ("aqib".equals(sound)) return "أذان عقيب عزيز";
        if ("short".equals(sound)) return "الأذان المختصر";
        if ("azemmour".equals(sound)) return "أذان أزمور، المغرب";
        if ("quseir".equals(sound)) return "أذان القصير، البحر الأحمر";
        return "الأذان الكامل";
    }

    private void releasePlayer() {
        if (player == null) {
            playing = false;
            return;
        }
        try {
            player.stop();
        } catch (Exception ignored) {
        }
        player.release();
        player = null;
        playing = false;
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

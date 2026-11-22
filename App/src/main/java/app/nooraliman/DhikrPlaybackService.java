package app.nooraliman;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

public class DhikrPlaybackService extends Service {
    static final String ACTION_PLAY = "app.nooraliman.action.PLAY_DHIKR";
    static final String ACTION_STOP = "app.nooraliman.action.STOP_DHIKR";
    private static final String CHANNEL = "dhikr_playback";
    private static final int NOTIFICATION_ID = 72;
    private static final long PREPARE_TIMEOUT_MS = 20_000L;
    private static final String TAG = "NoorDhikrPlayback";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private MediaPlayer player;
    private MediaPlayer preparingPlayer;
    private long preparationStartedAt;
    private final Runnable preparationTimeout = () -> {
        MediaPlayer pending = preparingPlayer;
        if (pending == null || player != pending
                || !isPreparationTimedOut(preparationStartedAt, SystemClock.uptimeMillis())) return;
        Log.e(TAG, "MediaPlayer preparation timed out after " + PREPARE_TIMEOUT_MS + "ms");
        releasePlayer();
        stopSelf();
    };
    static boolean isPreparationTimedOut(long startedAt, long now) {
        return startedAt > 0L && now >= startedAt && now - startedAt >= PREPARE_TIMEOUT_MS;
    }
    private AudioManager audioManager;
    private AudioFocusRequest focusRequest;
    private boolean hasAudioFocus;
    private final AudioManager.OnAudioFocusChangeListener focusListener = change -> {
        MediaPlayer active = player;
        if (active == null) return;
        if (change == AudioManager.AUDIOFOCUS_LOSS) stopSelf();
        else if (change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            try { if (active.isPlaying()) active.pause(); } catch (IllegalStateException ignored) { }
        } else if (change == AudioManager.AUDIOFOCUS_GAIN) {
            try { if (!active.isPlaying()) active.start(); } catch (IllegalStateException ignored) { }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
    }

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
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) manager.createNotificationChannel(channel);
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
        if (!requestAudioFocus()) { stopSelf(); return; }
        try {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build();
            Uri audio = Uri.parse("android.resource://" + getPackageName() + "/" + audioResource(type));
            MediaPlayer created = new MediaPlayer();
            player = created;
            created.setAudioAttributes(attributes);
            created.setDataSource(this, audio);
            created.setWakeMode(this, android.os.PowerManager.PARTIAL_WAKE_LOCK);
            float level = Math.max(0f, Math.min(1f, volume));
            created.setVolume(level, level);
            created.setOnPreparedListener(mp -> {
                handler.removeCallbacks(preparationTimeout);
                if (preparingPlayer == mp) preparingPlayer = null;
                preparationStartedAt = 0L;
                if (player != mp) return;
                try { mp.start(); } catch (IllegalStateException error) { stopSelf(); }
            });
            created.setOnCompletionListener(mp -> {
                handler.removeCallbacks(preparationTimeout);
                if (preparingPlayer == mp) preparingPlayer = null;
                preparationStartedAt = 0L;
                stopSelf();
            });
            created.setOnErrorListener((mp, what, extra) -> {
                handler.removeCallbacks(preparationTimeout);
                if (preparingPlayer == mp) preparingPlayer = null;
                preparationStartedAt = 0L;
                Log.e(TAG, "MediaPlayer error what=" + what + ", extra=" + extra);
                stopSelf();
                return true;
            });
            preparingPlayer = created;
            preparationStartedAt = SystemClock.uptimeMillis();
            handler.removeCallbacks(preparationTimeout);
            handler.postDelayed(preparationTimeout, PREPARE_TIMEOUT_MS);
            created.prepareAsync();
        } catch (Exception error) {
            handler.removeCallbacks(preparationTimeout);
            preparingPlayer = null;
            preparationStartedAt = 0L;
            Log.e(TAG, "Could not prepare dhikr audio", error);
            stopSelf();
        }
    }

    private boolean requestAudioFocus() {
        if (audioManager == null) return false;
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build();
        int result;
        if (Build.VERSION.SDK_INT >= 26) {
            focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(attributes).setOnAudioFocusChangeListener(focusListener).build();
            result = audioManager.requestAudioFocus(focusRequest);
        } else {
            result = audioManager.requestAudioFocus(focusListener, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }
        hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        return hasAudioFocus;
    }

    private void abandonAudioFocus() {
        if (!hasAudioFocus || audioManager == null) return;
        if (Build.VERSION.SDK_INT >= 26 && focusRequest != null) audioManager.abandonAudioFocusRequest(focusRequest);
        else audioManager.abandonAudioFocus(focusListener);
        hasAudioFocus = false;
        focusRequest = null;
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
        handler.removeCallbacks(preparationTimeout);
        preparingPlayer = null;
        preparationStartedAt = 0L;
        if (player != null) {
            try { player.stop(); } catch (Exception ignored) {}
            player.release();
            player = null;
        }
        abandonAudioFocus();
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

package app.nooraliman;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public class AdhanPlaybackService extends Service implements SensorEventListener {
    static final String ACTION_PLAY = "app.nooraliman.action.PLAY_ADHAN";
    static final String ACTION_STOP = "app.nooraliman.action.STOP_ADHAN";
    static final String ACTION_STOP_PREVIEW = "app.nooraliman.action.STOP_PREVIEW";
    static final String ACTION_UPDATE_NOTIFICATION = "app.nooraliman.action.UPDATE_ADHAN_NOTIFICATION";
    static final String ACTION_UPDATE_SENSOR_SETTINGS = "app.nooraliman.action.UPDATE_SENSOR_SETTINGS";
    static final String ACTION_FINISHED = "app.nooraliman.action.ADHAN_FINISHED";
    private static final String CHANNEL = "adhan_playback_controls_v3";
    private static final String TAG = "NoorAdhanPlayback";
    private static final int NOTIFICATION_ID = 71;
    private static volatile boolean playing;
    private static volatile boolean playbackActive;
    static boolean isPlaybackActive() { return playbackActive; }
    private MediaPlayer player;
    private AssetFileDescriptor activeAudioDescriptor;
    private boolean preview;
    private String prayerName = "";
    private String currentSound = "classic";
    private String currentPrayerKey = "";
    private AudioManager audioManager;
    private AudioFocusRequest focusRequest;
    private boolean hasAudioFocus;
    private SensorManager sensorManager;
    private Sensor faceDownSensor;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean faceDownStopPending;
    private boolean screenOffReceiverRegistered;
    private final BroadcastReceiver screenOffReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) stopSelf();
        }
    };
    private final Runnable stopIfStillFaceDown = () -> {
        faceDownStopPending = false;
        stopSelf();
    };
    private final AudioManager.OnAudioFocusChangeListener focusListener = change -> {
        MediaPlayer active = player;
        if (active == null) return;
        if (change == AudioManager.AUDIOFOCUS_LOSS) {
            stopSelf();
        } else if (change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            try { if (active.isPlaying()) active.pause(); } catch (IllegalStateException ignored) { }
        } else if (change == AudioManager.AUDIOFOCUS_GAIN) {
            try { if (!active.isPlaying()) active.start(); } catch (IllegalStateException ignored) { }
        }
    };

    static boolean isPlaying() { return playing; }

    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        faceDownSensor = sensorManager == null ? null
                : sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if (faceDownSensor == null && sensorManager != null) {
            faceDownSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

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
        if (intent != null && ACTION_UPDATE_SENSOR_SETTINGS.equals(intent.getAction())) {
            if (player != null) startFaceDownSensorIfEnabled();
            else stopSelf(startId);
            return START_NOT_STICKY;
        }
        if (intent != null && ACTION_UPDATE_NOTIFICATION.equals(intent.getAction())) {
            if (player != null) {
                configureScreenOffStopReceiver();
                startForeground(NOTIFICATION_ID, buildNotification(prayerName, currentSound));
            } else stopSelf(startId);
            return START_NOT_STICKY;
        }

        playbackActive = true;
        createChannel();
        prayerName = intent == null ? "" : intent.getStringExtra(PrayerAlarmScheduler.EXTRA_NAME);
        currentPrayerKey = intent == null ? "" : intent.getStringExtra(PrayerAlarmScheduler.EXTRA_KEY);
        currentSound = intent == null ? "classic"
                : intent.getStringExtra(PrayerAlarmScheduler.EXTRA_SOUND);
        preview = intent != null && intent.getBooleanExtra(PrayerAlarmScheduler.EXTRA_PREVIEW, false);
        if (preview && (currentPrayerKey == null || currentPrayerKey.isEmpty())) currentPrayerKey = "fajr";
        if (!isAllowedSound(currentSound)) currentSound = "classic";
        if (!preview) recordPlaybackState("service-started", "");
        startForeground(NOTIFICATION_ID, buildNotification(prayerName, currentSound));
        configureScreenOffStopReceiver();
        playAdhan(currentSound, currentPrayerKey);
        return START_NOT_STICKY;
    }

    private void configureScreenOffStopReceiver() {
        boolean enabled = getSharedPreferences(PrayerAlarmScheduler.PREFS, MODE_PRIVATE)
                .getBoolean("stopAdhanScreenOff", false);
        if (enabled && !screenOffReceiverRegistered) {
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
            if (Build.VERSION.SDK_INT >= 33) {
                registerReceiver(screenOffReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(screenOffReceiver, filter);
            }
            screenOffReceiverRegistered = true;
        } else if (!enabled && screenOffReceiverRegistered) {
            try { unregisterReceiver(screenOffReceiver); } catch (IllegalArgumentException ignored) { }
            screenOffReceiverRegistered = false;
        }
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationChannel channel = new NotificationChannel(CHANNEL,
                "تشغيل الأذان", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("إشعار الأذان وأدوات إيقافه");
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        channel.setSound(null, null);
        channel.enableVibration(false);
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(channel);
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
                : new Notification.Builder(this).setPriority(Notification.PRIORITY_MAX);
        builder.setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(preview ? "تجربة صوت الأذان" : "حان الآن موعد أذان " + (prayer == null ? "" : prayer))
                .setContentText(showControls ? "يُشغّل " + soundName(sound) : "الأذان قيد التشغيل")
                .setCategory(Notification.CATEGORY_ALARM)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setContentIntent(openPending);
        if (showControls) builder.addAction(android.R.drawable.ic_media_pause, "إيقاف الأذان", stopPending);

        // Android 14+ may deny full-screen intents unless the user grants the special access.
        // In that case the ordinary high-priority notification remains available.
        PendingIntent fullScreen = PendingIntent.getActivity(this, 74,
                new Intent(this, AdhanAlertActivity.class)
                        .putExtra(PrayerAlarmScheduler.EXTRA_NAME, prayer)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        boolean canUseFullScreen = Build.VERSION.SDK_INT < 34
                || ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).canUseFullScreenIntent();
        if (!preview && canUseFullScreen) builder.setFullScreenIntent(fullScreen, true);
        return builder.build();
    }

    private boolean requestAudioFocus() {
        if (audioManager == null) {
            Log.e(TAG, "AudioManager unavailable");
            return false;
        }
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
        int result;
        if (Build.VERSION.SDK_INT >= 26) {
            focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(attributes)
                    .setOnAudioFocusChangeListener(focusListener, handler)
                    .setWillPauseWhenDucked(true).build();
            result = audioManager.requestAudioFocus(focusRequest);
        } else {
            result = audioManager.requestAudioFocus(focusListener, AudioManager.STREAM_ALARM,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }
        hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        if (!hasAudioFocus) Log.w(TAG, "Audio focus request denied: " + result);
        return hasAudioFocus;
    }

    private void abandonAudioFocus() {
        if (!hasAudioFocus || audioManager == null) return;
        if (Build.VERSION.SDK_INT >= 26 && focusRequest != null) audioManager.abandonAudioFocusRequest(focusRequest);
        else audioManager.abandonAudioFocus(focusListener);
        hasAudioFocus = false;
        focusRequest = null;
    }

    private void playAdhan(String sound, String prayerKey) {
        releasePlayer();
        if (!requestAudioFocus()) {
            String detail = "Audio focus was not granted";
            recordPlaybackState("audio-focus-denied", detail);
            notifyPlaybackFailure("audio-focus-denied", "تعذّر الحصول على أولوية الصوت لتشغيل الأذان.");
            stopSelf();
            return;
        }
        try {
            AssetFileDescriptor audio = openAdhanAsset(sound, prayerKey);
            activeAudioDescriptor = audio;
            MediaPlayer created = new MediaPlayer();
            player = created;
            created.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
            created.setWakeMode(this, android.os.PowerManager.PARTIAL_WAKE_LOCK);
            created.setDataSource(audio.getFileDescriptor(), audio.getStartOffset(), audio.getLength());
            created.setOnPreparedListener(mp -> {
                closeAudioDescriptor();
                if (player != mp) return;
                try {
                    mp.start();
                    playing = true;
                    recordPlaybackState("playing", "");
                    Log.i(TAG, "Adhan playback started: " + sound + "/" + prayerKey);
                    startFaceDownSensorIfEnabled();
                } catch (IllegalStateException error) {
                    recordPlaybackState("playback-start-error", error.toString());
                    Log.e(TAG, "MediaPlayer could not start", error);
                    notifyPlaybackFailure("playback-start-error", "تعذّر بدء تشغيل ملف الأذان.");
                    stopSelf();
                }
            });
            created.setOnCompletionListener(mp -> {
                recordPlaybackState("completed", "");
                stopSelf();
            });
            created.setOnErrorListener((mp, what, extra) -> {
                recordPlaybackState("media-player-error", what + ":" + extra);
                Log.e(TAG, "MediaPlayer error what=" + what + ", extra=" + extra);
                notifyPlaybackFailure("media-player-error", "حدث خطأ أثناء تشغيل ملف الأذان.");
                stopSelf();
                return true;
            });
            created.prepareAsync();
        } catch (Exception error) {
            closeAudioDescriptor();
            recordPlaybackState("asset-or-prepare-error", error.toString());
            Log.e(TAG, "Could not prepare adhan asset: " + sound + "/" + prayerKey, error);
            notifyPlaybackFailure("asset-or-prepare-error", "تعذّر فتح أو تجهيز ملف الأذان المحدد.");
            stopSelf();
        }
    }

    private void notifyPlaybackFailure(String state, String message) {
        if (preview) return;
        PrayerAlarmReceiver.showAdhanFailure(this, state, prayerName,
                currentPrayerKey, message);
    }

    private void closeAudioDescriptor() {
        AssetFileDescriptor descriptor = activeAudioDescriptor;
        activeAudioDescriptor = null;
        if (descriptor != null) {
            try { descriptor.close(); }
            catch (Exception error) { Log.w(TAG, "Could not close adhan asset descriptor", error); }
        }
    }

    private void startFaceDownSensorIfEnabled() {
        if (sensorManager == null) return;
        sensorManager.unregisterListener(this);
        handler.removeCallbacks(stopIfStillFaceDown);
        faceDownStopPending = false;
        boolean enabled = getSharedPreferences(PrayerAlarmScheduler.PREFS, MODE_PRIVATE)
                .getBoolean("stopAdhanFaceDown", false);
        if (enabled && faceDownSensor != null) {
            sensorManager.registerListener(this, faceDownSensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    static boolean isFaceDown(float x, float y, float z) {
        double magnitude = Math.sqrt(x * x + y * y + z * z);
        return magnitude >= 6.5 && magnitude <= 12.5 && z <= -6.0f;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values.length < 3) return;
        if (isFaceDown(event.values[0], event.values[1], event.values[2])) {
            if (!faceDownStopPending) {
                faceDownStopPending = true;
                handler.postDelayed(stopIfStillFaceDown, 800L);
            }
        } else {
            handler.removeCallbacks(stopIfStillFaceDown);
            faceDownStopPending = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    private boolean isAllowedSound(String sound) {
        return "classic".equals(sound) || "aqib".equals(sound) || "short".equals(sound)
                || "azemmour".equals(sound) || "makkah".equals(sound) || "doha".equals(sound);
    }

    private AssetFileDescriptor openAdhanAsset(String sound, String prayerKey) throws Exception {
        String selectedAsset = soundAsset(sound, prayerKey);
        try {
            return getAssets().openFd(selectedAsset);
        } catch (Exception missingSelectedRecording) {
            // Keep playback compatible with older or incomplete packages that do not
            // contain the selected prayer-specific Doha recording.
            String fajrFallback = "Adhan_Doha_Qatar_01_Fajr_Adhan.mp3";
            if (!"doha".equals(sound) || fajrFallback.equals(selectedAsset)) {
                throw missingSelectedRecording;
            }
            return getAssets().openFd(fajrFallback);
        }
    }

    private String soundAsset(String sound, String prayerKey) {
        if ("doha".equals(sound)) {
            if ("fajr".equals(prayerKey)) return "Adhan_Doha_Qatar_01_Fajr_Adhan.mp3";
            if ("dhuhr".equals(prayerKey)) return "Adhan_Doha_Qatar_02_Dhuhr_Adhan.mp3";
            if ("asr".equals(prayerKey)) return "Adhan_Doha_Qatar_03_Asr_Adhan.mp3";
            if ("maghrib".equals(prayerKey)) return "Adhan_Doha_Qatar_04_Maghrib_Adhan.mp3";
            if ("isha".equals(prayerKey)) return "Adhan_Doha_Qatar_05_Isha_Adhan.mp3";
            return "Adhan_Doha_Qatar_01_Fajr_Adhan.mp3";
        }
        if ("aqib".equals(sound)) return "adhan-aqib.mp3";
        if ("short".equals(sound)) return "adhan-short.ogg";
        if ("azemmour".equals(sound)) return "adhan-azemmour.mp3";
        if ("makkah".equals(sound)) return "adhan-makkah.mp3";
        return "adhan.ogg";
    }

    private String soundName(String sound) {
        if ("aqib".equals(sound)) return "أذان عقيب عزيز";
        if ("short".equals(sound)) return "الأذان المختصر";
        if ("azemmour".equals(sound)) return "أذان أزمور، المغرب";
        if ("makkah".equals(sound)) return "أذان مكة";
        if ("doha".equals(sound)) return "أذان الدوحة، قطر";
        return "الأذان الكامل";
    }

    private void recordPlaybackState(String state, String detail) {
        if (preview) return;
        getSharedPreferences(PrayerAlarmScheduler.PREFS, MODE_PRIVATE).edit()
                .putLong("lastAdhanPlaybackEventAt", System.currentTimeMillis())
                .putString("lastAdhanPlaybackState", state == null ? "" : state)
                .putString("lastAdhanPlaybackDetail", detail == null ? "" : detail)
                .putString("lastAdhanPlaybackPrayer", currentPrayerKey == null ? "" : currentPrayerKey)
                .putString("lastAdhanPlaybackSound", currentSound == null ? "" : currentSound)
                .apply();
    }

    private void releasePlayer() {
        if (sensorManager != null) sensorManager.unregisterListener(this);
        handler.removeCallbacks(stopIfStillFaceDown);
        faceDownStopPending = false;
        if (player != null) {
            try { player.setOnPreparedListener(null); player.setOnCompletionListener(null); player.setOnErrorListener(null); } catch (Exception ignored) { }
            try { player.release(); } catch (Exception ignored) { }
            player = null;
        }
        closeAudioDescriptor();
        playing = false;
        abandonAudioFocus();
    }

    @Override
    public void onDestroy() {
        if (screenOffReceiverRegistered) {
            try { unregisterReceiver(screenOffReceiver); } catch (IllegalArgumentException ignored) { }
            screenOffReceiverRegistered = false;
        }
        releasePlayer();
        playbackActive = false;
        sendBroadcast(new Intent(ACTION_FINISHED).setPackage(getPackageName()));
        if (Build.VERSION.SDK_INT >= 24) stopForeground(STOP_FOREGROUND_REMOVE);
        else stopForeground(true);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}

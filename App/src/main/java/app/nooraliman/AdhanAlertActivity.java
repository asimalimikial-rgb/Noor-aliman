package app.nooraliman;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AdhanAlertActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long startedAt;
    private boolean playbackObserved;
    private final Runnable playbackMonitor = new Runnable() {
        @Override public void run() {
            if (isFinishing()) return;
            if (AdhanPlaybackService.isPlaying()) playbackObserved = true;
            if (playbackObserved && !AdhanPlaybackService.isPlaying()) {
                finish();
                return;
            }
            if (!playbackObserved && SystemClock.elapsedRealtime() - startedAt > 7000L) {
                finish();
                return;
            }
            handler.postDelayed(this, 650L);
        }
    };

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        getWindow().setStatusBarColor(Color.rgb(16, 44, 38));
        getWindow().setNavigationBarColor(Color.rgb(16, 44, 38));
        String prayer = getIntent().getStringExtra(PrayerAlarmScheduler.EXTRA_NAME);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(dp(28), dp(28), dp(28), dp(28));
        layout.setBackgroundColor(Color.rgb(16, 44, 38));

        TextView title = new TextView(this);
        title.setText("حان الآن موعد أذان " + (prayer == null ? "الصلاة" : prayer));
        title.setTextColor(Color.WHITE);
        title.setTextSize(25);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, dp(28));
        layout.addView(title, new LinearLayout.LayoutParams(-1, -2));

        Button stop = new Button(this);
        stop.setText("إيقاف الأذان");
        stop.setOnClickListener(v -> {
            startService(new Intent(this, AdhanPlaybackService.class).setAction(AdhanPlaybackService.ACTION_STOP));
            finish();
        });
        layout.addView(stop, new LinearLayout.LayoutParams(-1, -2));

        Button open = new Button(this);
        open.setText("فتح تطبيق نور الإيمان");
        open.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            finish();
        });
        LinearLayout.LayoutParams openParams = new LinearLayout.LayoutParams(-1, -2);
        openParams.topMargin = dp(12);
        layout.addView(open, openParams);
        setContentView(layout);
        startedAt = SystemClock.elapsedRealtime();
        handler.postDelayed(playbackMonitor, 1200L);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(playbackMonitor);
        super.onDestroy();
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}

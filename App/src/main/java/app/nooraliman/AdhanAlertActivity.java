package app.nooraliman;

import android.app.Activity;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.graphics.drawable.GradientDrawable;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AdhanAlertActivity extends Activity {
    private boolean receiverRegistered;
    private final BroadcastReceiver playbackFinishedReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (AdhanPlaybackService.ACTION_FINISHED.equals(intent.getAction())) finish();
        }
    };
    private static final int GREEN = Color.rgb(16, 44, 38);
    private static final int GOLD = Color.rgb(202, 167, 102);
    private static final int CREAM = Color.rgb(247, 245, 239);

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        IntentFilter finishedFilter = new IntentFilter(AdhanPlaybackService.ACTION_FINISHED);
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(playbackFinishedReceiver, finishedFilter, Context.RECEIVER_NOT_EXPORTED);
        else registerReceiver(playbackFinishedReceiver, finishedFilter);
        receiverRegistered = true;
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setStatusBarColor(GREEN);
        getWindow().setNavigationBarColor(GREEN);
        getWindow().setBackgroundDrawable(new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.rgb(13, 37, 32), GREEN, Color.rgb(8, 27, 24)}));
        hideSystemBars();

        String prayer = getIntent().getStringExtra(PrayerAlarmScheduler.EXTRA_NAME);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(26), dp(26), dp(26), dp(28));
        root.setBackgroundColor(Color.TRANSPARENT);

        TextView brand = new TextView(this);
        brand.setText("نــور الإيــمــان");
        brand.setTextColor(GOLD);
        brand.setTextSize(17);
        brand.setGravity(Gravity.CENTER);
        brand.setLetterSpacing(0.12f);
        root.addView(brand, new LinearLayout.LayoutParams(-1, dp(42)));

        MosqueArtwork artwork = new MosqueArtwork(this);
        LinearLayout.LayoutParams artParams = new LinearLayout.LayoutParams(-1, 0, 1f);
        artParams.topMargin = dp(12);
        artParams.bottomMargin = dp(10);
        root.addView(artwork, artParams);

        TextView eyebrow = new TextView(this);
        eyebrow.setText("حَانَ الآنَ وَقْتُ الصَّلَاة");
        eyebrow.setTextColor(GOLD);
        eyebrow.setTextSize(13);
        eyebrow.setGravity(Gravity.CENTER);
        root.addView(eyebrow, new LinearLayout.LayoutParams(-1, dp(34)));

        TextView title = new TextView(this);
        title.setText("أذان " + (prayer == null || prayer.trim().isEmpty() ? "الصلاة" : prayer));
        title.setTextColor(CREAM);
        title.setTextSize(31);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(-1, -2);
        titleParams.bottomMargin = dp(26);
        root.addView(title, titleParams);

        Button stop = new Button(this);
        stop.setText("إيقاف الأذان");
        stop.setTextColor(GREEN);
        stop.setTextSize(17);
        stop.setAllCaps(false);
        stop.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        GradientDrawable stopBackground = new GradientDrawable();
        stopBackground.setColor(GOLD);
        stopBackground.setCornerRadius(dp(16));
        stop.setBackground(stopBackground);
        stop.setMinHeight(dp(58));
        stop.setOnClickListener(v -> stopAdhanAndClose());
        root.addView(stop, new LinearLayout.LayoutParams(-1, dp(60)));

        TextView hint = new TextView(this);
        hint.setText("يمكنك إيقاف الأذان من الزر أو من إشعار التشغيل");
        hint.setTextColor(Color.rgb(210, 218, 210));
        hint.setTextSize(12);
        hint.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(-1, dp(42));
        hintParams.topMargin = dp(7);
        root.addView(hint, hintParams);
        setContentView(root);
    }

    private void stopAdhanAndClose() {
        startService(new Intent(this, AdhanPlaybackService.class)
                .setAction(AdhanPlaybackService.ACTION_STOP));
        finish();
    }

    @Override
    protected void onDestroy() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (receiverRegistered) {
            try { unregisterReceiver(playbackFinishedReceiver); } catch (IllegalArgumentException ignored) { }
            receiverRegistered = false;
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)
                && getSharedPreferences(PrayerAlarmScheduler.PREFS, MODE_PRIVATE)
                .getBoolean("stopAdhanVolumeKeys", false)) {
            stopAdhanAndClose();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemBars();
    }

    private void hideSystemBars() {
        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static final class MosqueArtwork extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        MosqueArtwork(Activity activity) { super(activity); setLayerType(View.LAYER_TYPE_SOFTWARE, null); }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth(), h = getHeight();
            float cx = w / 2f, base = h * 0.78f;
            paint.setColor(0x22CAA766);
            canvas.drawCircle(cx, h * 0.38f, Math.min(w, h) * 0.34f, paint);
            paint.setColor(GOLD);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            canvas.drawCircle(cx, h * 0.38f, Math.min(w, h) * 0.31f, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFFF7F5EF);
            canvas.drawRoundRect(new RectF(w * .19f, base - h * .23f, w * .81f, base),
                    dp(10), dp(10), paint);
            paint.setColor(GOLD);
            Path dome = new Path();
            dome.moveTo(w * .31f, base - h * .22f);
            dome.cubicTo(w * .32f, base - h * .52f, w * .68f, base - h * .52f, w * .69f, base - h * .22f);
            dome.close();
            canvas.drawPath(dome, paint);
            canvas.drawRect(w * .29f, base - h * .22f, w * .71f, base - h * .18f, paint);
            paint.setColor(GREEN);
            canvas.drawRoundRect(new RectF(w * .46f, base - h * .17f, w * .54f, base),
                    dp(20), dp(20), paint);
            paint.setColor(CREAM);
            canvas.drawRoundRect(new RectF(w * .24f, base - h * .14f, w * .33f, base),
                    dp(14), dp(14), paint);
            canvas.drawRoundRect(new RectF(w * .67f, base - h * .14f, w * .76f, base),
                    dp(14), dp(14), paint);
            drawMinaret(canvas, w * .16f, base - h * .04f, h);
            drawMinaret(canvas, w * .84f, base - h * .04f, h);
            paint.setColor(GOLD);
            canvas.drawCircle(cx, base - h * .53f, dp(4), paint);
            paint.setStrokeWidth(dp(2));
            canvas.drawLine(cx, base - h * .57f, cx, base - h * .62f, paint);
            canvas.drawLine(cx - dp(7), base - h * .60f, cx + dp(7), base - h * .60f, paint);
            paint.setColor(0x55CAA766);
            canvas.drawRect(w * .12f, base, w * .88f, base + dp(2), paint);
        }

        private void drawMinaret(Canvas canvas, float x, float base, float h) {
            paint.setColor(CREAM);
            canvas.drawRoundRect(new RectF(x - dp(8), base - h * .42f, x + dp(8), base),
                    dp(5), dp(5), paint);
            paint.setColor(GOLD);
            canvas.drawRect(x - dp(12), base - h * .42f, x + dp(12), base - h * .39f, paint);
            Path roof = new Path();
            roof.moveTo(x - dp(9), base - h * .42f);
            roof.lineTo(x, base - h * .49f);
            roof.lineTo(x + dp(9), base - h * .42f);
            roof.close();
            canvas.drawPath(roof, paint);
            canvas.drawCircle(x, base - h * .51f, dp(2), paint);
        }

        private int dp(int value) {
            return Math.round(value * getResources().getDisplayMetrics().density);
        }
    }
}

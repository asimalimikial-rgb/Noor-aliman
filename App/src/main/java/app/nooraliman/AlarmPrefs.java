package app.nooraliman;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.UserManager;

import java.util.Map;
import java.util.Set;

/**
 * Keeps alarm configuration in device-protected storage so boot receivers can
 * read it before the first user unlock. On API 23 the normal preferences remain
 * in use because Direct Boot storage is not available.
 */
final class AlarmPrefs {
    private static final Object MIGRATION_LOCK = new Object();
    private static volatile boolean migrationChecked;

    private AlarmPrefs() {}

    static SharedPreferences get(Context context) {
        if (Build.VERSION.SDK_INT < 24) {
            return context.getSharedPreferences(PrayerAlarmScheduler.PREFS, Context.MODE_PRIVATE);
        }

        Context appContext = context.getApplicationContext();
        if (appContext == null) appContext = context;
        Context deviceContext = appContext.createDeviceProtectedStorageContext();
        migrateAfterUnlock(appContext, deviceContext);
        return deviceContext.getSharedPreferences(
                PrayerAlarmScheduler.PREFS, Context.MODE_PRIVATE);
    }

    private static void migrateAfterUnlock(Context context, Context deviceContext) {
        if (migrationChecked) return;
        UserManager userManager =
                (UserManager) context.getSystemService(Context.USER_SERVICE);
        if (userManager == null || !userManager.isUserUnlocked()) return;

        synchronized (MIGRATION_LOCK) {
            if (migrationChecked) return;
            Context credentialContext = context.createCredentialProtectedStorageContext();
            SharedPreferences device = deviceContext.getSharedPreferences(
                    PrayerAlarmScheduler.PREFS, Context.MODE_PRIVATE);
            Map<String, ?> deviceValues = device.getAll();

            // Use the native move only when it cannot overwrite any Direct Boot values.
            if (deviceValues.isEmpty()) {
                try {
                    if (deviceContext.moveSharedPreferencesFrom(
                            credentialContext, PrayerAlarmScheduler.PREFS)) {
                        migrationChecked = true;
                        return;
                    }
                } catch (RuntimeException ignored) {
                    // Fall through to a key-preserving merge.
                }
            }

            SharedPreferences legacy = credentialContext.getSharedPreferences(
                    PrayerAlarmScheduler.PREFS, Context.MODE_PRIVATE);
            Map<String, ?> legacyValues = legacy.getAll();
            if (legacyValues.isEmpty()) {
                migrationChecked = true;
                return;
            }

            deviceValues = device.getAll();
            SharedPreferences.Editor editor = device.edit();
            boolean complete = true;

            // Keep values already written in device-protected storage (for example,
            // diagnostics recorded during Direct Boot); copy every missing legacy key.
            for (Map.Entry<String, ?> entry : legacyValues.entrySet()) {
                if (deviceValues.containsKey(entry.getKey())) continue;
                if (!putValue(editor, entry.getKey(), entry.getValue())) {
                    complete = false;
                    break;
                }
            }

            if (complete && editor.commit()) {
                credentialContext.deleteSharedPreferences(PrayerAlarmScheduler.PREFS);
                migrationChecked = true;
            }
        }
    }

    private static boolean putValue(SharedPreferences.Editor editor,
                                    String key, Object value) {
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Set) {
            @SuppressWarnings("unchecked")
            Set<String> strings = (Set<String>) value;
            editor.putStringSet(key, strings);
        } else {
            return false;
        }
        return true;
    }
}

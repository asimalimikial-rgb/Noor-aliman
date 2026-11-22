package app.nooraliman;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.UserManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35)
public class AlarmPrefsTest {
    @Test
    public void getUsesDeviceProtectedPreferencesWithoutUnlocking() {
        Context context = RuntimeEnvironment.getApplication();
        UserManager userManager =
                (UserManager) context.getSystemService(Context.USER_SERVICE);
        Shadows.shadowOf(userManager).setUserUnlocked(false);
        assertFalse(userManager.isUserUnlocked());

        SharedPreferences actual = AlarmPrefs.get(context);
        Context deviceContext = context.createDeviceProtectedStorageContext();
        SharedPreferences expected = deviceContext.getSharedPreferences(
                PrayerAlarmScheduler.PREFS, Context.MODE_PRIVATE);

        assertTrue(deviceContext.isDeviceProtectedStorage());
        expected.edit().putBoolean("directBootTestValue", true).commit();
        assertTrue(actual.getBoolean("directBootTestValue", false));
    }


    @Test
    @Config(sdk = 23)
    public void api23KeepsCredentialProtectedPreferences() {
        Context context = RuntimeEnvironment.getApplication();
        SharedPreferences expected = context.getSharedPreferences(
                PrayerAlarmScheduler.PREFS, Context.MODE_PRIVATE);
        expected.edit().putBoolean("api23Value", true).commit();

        SharedPreferences actual = AlarmPrefs.get(context);
        assertTrue(actual.getBoolean("api23Value", false));
    }

    @Test
    public void getMigratesLegacyPreferencesAfterUnlock() {
        Context context = RuntimeEnvironment.getApplication();
        UserManager userManager =
                (UserManager) context.getSystemService(Context.USER_SERVICE);
        Shadows.shadowOf(userManager).setUserUnlocked(false);

        Context credentialContext = context;
        credentialContext.getSharedPreferences(
                PrayerAlarmScheduler.PREFS, Context.MODE_PRIVATE)
                .edit().putBoolean("adhan", true).putInt("dhikrEvery", 30)
                .putBoolean("sharedKey", true).commit();
        Context deviceContext = context.createDeviceProtectedStorageContext();
        deviceContext.getSharedPreferences(
                PrayerAlarmScheduler.PREFS, Context.MODE_PRIVATE)
                .edit().putBoolean("directBootValue", true)
                .putBoolean("sharedKey", false).commit();

        AlarmPrefs.get(context);
        assertTrue(credentialContext.getSharedPreferences(
                PrayerAlarmScheduler.PREFS, Context.MODE_PRIVATE).contains("adhan"));

        Shadows.shadowOf(userManager).setUserUnlocked(true);
        SharedPreferences migrated = AlarmPrefs.get(context);
        assertTrue(migrated.getBoolean("adhan", false));
        assertEquals(30, migrated.getInt("dhikrEvery", 0));
        assertTrue(migrated.getBoolean("directBootValue", false));
        assertFalse(migrated.getBoolean("sharedKey", true));
    }
}

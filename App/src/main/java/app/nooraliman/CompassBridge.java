package app.nooraliman;

import android.app.Activity;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.webkit.JavascriptInterface;

public final class CompassBridge implements SensorEventListener {
    private final Activity activity;
    private final SensorManager sensorManager;
    private final HandlerThread sensorThread;
    private final Handler sensorHandler;
    private final float[] rotation = new float[9];
    private final float[] remapped = new float[9];
    private final float[] orientation = new float[3];
    private final float[] gravity = new float[3];
    private final float[] magnetic = new float[3];
    private volatile double heading = -1;
    private volatile double declination;
    private double latitude = Double.NaN;
    private double longitude = Double.NaN;
    private volatile boolean running;
    private volatile boolean rotationVectorMode;
    private volatile boolean hasGravity;
    private volatile boolean hasMagnetic;

    CompassBridge(Activity activity) {
        this.activity = activity;
        sensorManager = (SensorManager) activity.getSystemService(Activity.SENSOR_SERVICE);
        sensorThread = new HandlerThread("NoorCompassSensors");
        sensorThread.start();
        sensorHandler = new Handler(sensorThread.getLooper());
    }

    @JavascriptInterface
    public synchronized boolean start(double lat, double lon) {
        if (Double.isNaN(lat) || Double.isInfinite(lat)
                || Double.isNaN(lon) || Double.isInfinite(lon)
                || lat < -90 || lat > 90 || lon < -180 || lon > 180) return false;
        if (running && Math.abs(lat - latitude) < 0.000001
                && Math.abs(lon - longitude) < 0.000001) return true;

        if (sensorManager == null) {
            heading = -1;
            running = false;
            return false;
        }
        stopListening();
        latitude = lat;
        longitude = lon;
        heading = -1;
        declination = new GeomagneticField((float) lat, (float) lon, 0,
                System.currentTimeMillis()).getDeclination();

        Sensor rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (rotationVector != null
                && sensorManager.registerListener(this, rotationVector,
                SensorManager.SENSOR_DELAY_UI, sensorHandler)) {
            rotationVectorMode = true;
            running = true;
            return true;
        }

        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (accelerometer == null || magnetometer == null) return false;
        boolean accelRegistered = sensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_UI, sensorHandler);
        boolean magneticRegistered = sensorManager.registerListener(this, magnetometer,
                SensorManager.SENSOR_DELAY_UI, sensorHandler);
        if (!accelRegistered || !magneticRegistered) {
            stopListening();
            return false;
        }
        rotationVectorMode = false;
        running = true;
        return true;
    }

    @JavascriptInterface
    public synchronized void stop() {
        stopListening();
        heading = -1;
    }

    @JavascriptInterface
    public double headingDegrees() {
        return heading;
    }

    synchronized void shutdown() {
        stopListening();
        sensorThread.quitSafely();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!running) return;
        if (rotationVectorMode && event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotation, event.values);
            updateHeading();
        } else if (!rotationVectorMode) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(event.values, 0, gravity, 0, 3);
                hasGravity = true;
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, magnetic, 0, 3);
                hasMagnetic = true;
            }
            if (hasGravity && hasMagnetic
                    && SensorManager.getRotationMatrix(rotation, null, gravity, magnetic)) {
                updateHeading();
            }
        }
    }

    private void updateHeading() {
        int axisX = SensorManager.AXIS_X;
        int axisY = SensorManager.AXIS_Y;
        switch (activity.getWindowManager().getDefaultDisplay().getRotation()) {
            case android.view.Surface.ROTATION_90:
                axisX = SensorManager.AXIS_Y;
                axisY = SensorManager.AXIS_MINUS_X;
                break;
            case android.view.Surface.ROTATION_180:
                axisX = SensorManager.AXIS_MINUS_X;
                axisY = SensorManager.AXIS_MINUS_Y;
                break;
            case android.view.Surface.ROTATION_270:
                axisX = SensorManager.AXIS_MINUS_Y;
                axisY = SensorManager.AXIS_X;
                break;
            default:
                break;
        }
        if (!SensorManager.remapCoordinateSystem(rotation, axisX, axisY, remapped)) return;
        SensorManager.getOrientation(remapped, orientation);
        double degrees = Math.toDegrees(orientation[0]) + declination;
        heading = (degrees % 360 + 360) % 360;
    }

    private void stopListening() {
        if (sensorManager != null) sensorManager.unregisterListener(this);
        running = false;
        rotationVectorMode = false;
        hasGravity = false;
        hasMagnetic = false;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}

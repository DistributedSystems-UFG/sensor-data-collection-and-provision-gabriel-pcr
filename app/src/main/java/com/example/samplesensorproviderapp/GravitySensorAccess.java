package com.example.samplesensorproviderapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.EditText;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;

import java.util.UUID;

public class GravitySensorAccess implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor mGravity;
    private EditText sensor_field;
    private Mqtt5BlockingClient client;
    public static final String GRAVITY_IN_TOPIC = "gravity_in";
    public static final String GRAVITY_OUT_TOPIC = "gravity_out";

    private long lastUpdate = 0;
    private static final long UPDATE_INTERVAL = 3000; // 3 seconds

    public GravitySensorAccess(SensorManager sm, EditText et){
        sensorManager = sm;
        sensor_field = et;
        mGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_NORMAL);

        client = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(MainActivity.brokerURI)
                .buildBlocking();
        client.connect();
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastUpdate) > UPDATE_INTERVAL) {
            lastUpdate = currentTime;

            float temp = event.values[1];
            sensor_field.setText(String.valueOf(temp));

            client.publishWith()
                    .topic(GRAVITY_IN_TOPIC)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .payload(String.valueOf(temp).getBytes())
                    .send();
        }
    }

    @Override
    protected void finalize() {
        sensorManager.unregisterListener(this);
        if (client != null) {
            client.disconnect();
        }
    }
}

package com.example.samplesensorproviderapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.EditText;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;

import java.util.UUID;

public class LightSensorAccess implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor mLight;
    private EditText sensor_field;
    private Mqtt5BlockingClient client;
    public static final String LUMINOSITY_IN_TOPIC = "luminosity_in";
    public static final String LUMINOSITY_OUT_TOPIC = "luminosity_out";

    private long lastUpdate = 0;
    private static final long UPDATE_INTERVAL = 3000; // 3 seconds

    public LightSensorAccess(SensorManager sm, EditText et){
        sensorManager = sm;
        sensor_field = et;
        mLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);

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

            float lux = event.values[0];
            sensor_field.setText(String.valueOf(lux));

            client.publishWith()
                    .topic(LUMINOSITY_IN_TOPIC)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .payload(String.valueOf(lux).getBytes())
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

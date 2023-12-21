package com.example.samplesensorproviderapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.EditText;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public static final String brokerURI = "54.243.87.127";
    Activity thisActivity;

    private LightSensorAccess lightSensorAccess;
    private GravitySensorAccess gravitySensorAccess;
    EditText luminosityEditText;
    EditText gravityEditText;
    private Mqtt5BlockingClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        thisActivity = this;
        luminosityEditText = findViewById(R.id.editTextLuminosityTopic);
        gravityEditText = findViewById(R.id.editTextGravityTopic);

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightSensorAccess = new LightSensorAccess(sensorManager, luminosityEditText);
        gravitySensorAccess = new GravitySensorAccess(sensorManager, gravityEditText);

        subscribeToLuminosityTopic();
        subscribeToTemperatureTopic();
    }

    public void subscribeToLuminosityTopic() {
        Mqtt5BlockingClient client = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(brokerURI)
                .buildBlocking();

        client.connect();

        // Use a callback lambda function to show the message on the screen
        client.toAsync().subscribeWith()
                .topicFilter(LightSensorAccess.LUMINOSITY_OUT_TOPIC)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(msg -> {
                    thisActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            luminosityEditText.setText(new String(msg.getPayloadAsBytes(), StandardCharsets.UTF_8));
                        }
                    });
                })
                .send();
    }

    public void subscribeToTemperatureTopic() {
        Mqtt5BlockingClient client = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(brokerURI)
                .buildBlocking();

        client.connect();

        // Use a callback lambda function to show the message on the screen
        client.toAsync().subscribeWith()
                .topicFilter(GravitySensorAccess.GRAVITY_OUT_TOPIC)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(msg -> {
                    thisActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            gravityEditText.setText(new String(msg.getPayloadAsBytes(), StandardCharsets.UTF_8));
                        }
                    });
                })
                .send();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (client != null) {
            client.disconnect();
        }
    }
}

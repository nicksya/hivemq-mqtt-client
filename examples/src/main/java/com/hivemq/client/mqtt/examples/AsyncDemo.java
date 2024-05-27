/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hivemq.client.mqtt.examples;

import com.hivemq.client.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client.internal.mqtt.message.auth.MqttSimpleAuth;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttWebSocketConfig;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Small completely asynchronous example.
 *
 * @author Silvio Giebl
 */
public class AsyncDemo {

    private static final String HOST = "";
    private static final String SECRET = "";
    private static final String CLIENT_ID = "";
    private static final int MESSAGE_COUNT = 10;
    public static void main(final String[] args) throws InterruptedException {

        MqttWebSocketConfig config = MqttWebSocketConfig.builder()
                .serverPath("mqtt")
                .build();

        MqttClientSslConfig sslConfig = MqttClientSslConfig.builder()
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                })
                .build();

        MqttSimpleAuth auth = new MqttSimpleAuth(MqttUtf8StringImpl.of(CLIENT_ID), ByteBuffer.wrap(SECRET.getBytes()));

        final Mqtt5AsyncClient client = Mqtt5Client.builder()
                .serverHost(HOST)
                .simpleAuth(auth)
                .serverPort(443)
                .identifier(CLIENT_ID)
                .webSocketConfig(config)
                .useSsl(sslConfig)
                .buildAsync();

        CountDownLatch latch = new CountDownLatch(MESSAGE_COUNT);

        client.subscribeWith()
                .topicFilter("dt/#")
                .callback(publish -> {
                    System.out.println(publish);
                    // Process the received message
                    latch.countDown();
                })
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        System.out.println("Error subscribing to the topic !");
                        System.out.println(throwable);
                        // Handle failure to subscribe
                    } else {
                        System.out.println("Subscribed to the topic !");
                        System.out.println(subAck);
                        // Handle successful subscription, e.g. logging or incrementing a metric
                    }
                });
        client.connect();

        System.out.println("Waiting for all messages to get received...");
        latch.await(60, TimeUnit.SECONDS);
        System.out.println("Done.");

        client.disconnect();
    }
}

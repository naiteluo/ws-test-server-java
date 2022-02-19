package com.example;

import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.example.protos.MyProtobufData;
import com.google.gson.JsonObject;
import com.google.protobuf.ByteString;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        assertTrue(true);
    }

    @Test
    public void shouldStartServerSuccess() throws InterruptedException {

        final Lock isServerStartLock = new ReentrantLock();
        final Lock isWaitingDataLock = new ReentrantLock();
        final List<JsonObject> dataList = new ArrayList();
        Thread serverThread = new Thread(new Runnable() {
            public void run() {
                final MyServer server = new MyServer(new InetSocketAddress("localhost", 8777), isServerStartLock);
                server.start();

                new Timer().scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        server.broadcast("Hi of String!");
                        try {
                            JsonObject payloadJSON = new JsonObject();
                            payloadJSON.addProperty("name", "mob");
                            payloadJSON.addProperty("age", 16);
                            payloadJSON.addProperty("bio", "muda muda muda");
                            ByteString payloadJSONBytesString = ByteString.copyFrom(payloadJSON.toString(), "utf-8");
                            MyProtobufData.MessageData data = MyProtobufData.MessageData.newBuilder()
                                    .setText("Hi of Protobuf!")
                                    .setSeed(123)
                                    .setPayload(payloadJSONBytesString)
                                    .build();
                            server.broadcast(data.toByteArray());
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }, 1000, 5000);
            }
        });

        Thread clientThread = new Thread(new Runnable() {
            public void run() {
                synchronized (isServerStartLock) {
                    try {
                        isServerStartLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    MyClient client = new MyClient(new URI("ws://localhost:8777"),
                            "Client A", dataList, isWaitingDataLock);
                    client.connect();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });

        serverThread.start();
        clientThread.start();

        int failCount = 0;
        while (failCount < 5) {
            synchronized (isWaitingDataLock) {
                isWaitingDataLock.wait();
            }
            System.out.println(dataList.size());
            failCount++;
            isWaitingDataLock.lock();
        }

        System.out.println("end");
        assertTrue(true);
    }
}

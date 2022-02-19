package com.example;

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

import org.java_websocket.enums.ReadyState;
import org.java_websocket.server.WebSocketServer;

/**
 * Hello world!
 *
 */

public class App {
    public static void main(String[] args) throws URISyntaxException, InterruptedException {
        final Lock isServerStartLock = new ReentrantLock();
        final Lock isWaitingDataLock = new ReentrantLock();
        final List<JsonObject> dataList = new ArrayList();
        Thread serverThread = new Thread(new Runnable() {
            public void run() {
                final MyServer server = new MyServer(new InetSocketAddress("localhost", 8777), isServerStartLock);
                server.start();

                new Timer().scheduleAtFixedRate(new TimerTask() {
                    int count = 0;

                    @Override
                    public void run() {
                        server.broadcast("Hi of String!");
                        try {
                            int age = 15 + count;
                            count++;
                            JsonObject payloadJSON = new JsonObject();
                            payloadJSON.addProperty("name", "mob");
                            payloadJSON.addProperty("age", age);
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
                int index = dataList.size() - 1;
                JsonObject j = dataList.get(index);
                System.out.println(j.get("age").getAsInt());
                if (j.get("age").getAsInt() == 17) {
                    System.out.println("Test pass!");
                    break;
                }
                failCount++;
                isWaitingDataLock.lock();
            }
        }

        System.out.println("end");
    }
}

// public class App {

// static int port = 8777;
// static Lock lock = new ReentrantLock();

// static WebSocketServer server;

// public static void main(String[] args) throws URISyntaxException,
// InterruptedException {
// Thread serverThread = new Thread(new App().new RunnableServerImpl());
// Thread clientThread1 = new Thread(new App().new RunnableClientImpl("Client
// A"));
// Thread clientThread2 = new Thread(new App().new RunnableClientImpl("Client
// B"));

// serverThread.start();
// clientThread1.start();
// clientThread2.start();
// }

// /**
// * server thread
// */
// private class RunnableServerImpl implements Runnable {
// public void run() {
// System.out.println(Thread.currentThread().getName()
// + ", executing run() method!");
// String host = "localhost";
// server = new MyServer(new InetSocketAddress(host, port), lock);
// server.start();
// // broadcast text message every 5 seconds
// new Timer().scheduleAtFixedRate(new TimerTask() {
// @Override
// public void run() {
// server.broadcast("Hi of String!");
// try {
// JsonObject payloadJSON = new JsonObject();
// payloadJSON.addProperty("name", "mob");
// payloadJSON.addProperty("age", 16);
// payloadJSON.addProperty("bio", "muda muda muda");
// ByteString payloadJSONBytesString =
// ByteString.copyFrom(payloadJSON.toString(), "utf-8");
// MyProtobufData.MessageData data = MyProtobufData.MessageData.newBuilder()
// .setText("Hi of Protobuf!")
// .setSeed(123)
// .setPayload(payloadJSONBytesString)
// .build();
// server.broadcast(data.toByteArray());
// } catch (UnsupportedEncodingException e) {
// e.printStackTrace();
// }
// }
// }, 1000, 5000);
// }
// }

// /**
// * client thread
// */
// private class RunnableClientImpl implements Runnable {
// private String clientName;

// public RunnableClientImpl(String clientName) {
// this.clientName = clientName;
// }

// public void run() {
// System.out.println(Thread.currentThread().getName()
// + ", executing run() method!");
// // Wait until server finishing start
// synchronized (lock) {
// try {
// lock.wait();
// } catch (InterruptedException e) {
// e.printStackTrace();
// }
// }
// try {
// System.out.println("Create client and connect.");
// MyClient client = new MyClient(new URI("ws://localhost:" + port),
// clientName);
// client.connect();
// while (!client.getReadyState().equals(ReadyState.OPEN)) {
// System.out.println(clientName + " connecting... " + client.getReadyState());
// Thread.sleep(1000);
// }
// } catch (URISyntaxException | InterruptedException e) {
// e.printStackTrace();
// }
// }
// }
// }

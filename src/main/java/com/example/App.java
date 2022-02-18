package com.example;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.example.protos.MyProtobufData;

import org.java_websocket.server.WebSocketServer;

/**
 * Hello world!
 *
 */
public class App {

    static int port = 8777;
    static Lock lock = new ReentrantLock();

    static WebSocketServer server;

    public static void main(String[] args) throws URISyntaxException, InterruptedException {
        Thread serverThread = new Thread(new App().new RunnableServerImpl());
        Thread clientThread1 = new Thread(new App().new RunnableClientImpl("Client A"));
        Thread clientThread2 = new Thread(new App().new RunnableClientImpl("Client B"));

        serverThread.start();
        clientThread1.start();
        clientThread2.start();
    }

    /**
     * server thread
     */
    private class RunnableServerImpl implements Runnable {
        public void run() {
            System.out.println(Thread.currentThread().getName()
                    + ", executing run() method!");
            String host = "localhost";
            server = new MyServer(new InetSocketAddress(host, port), lock);
            server.start();
            // broadcast text message every 5 seconds
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    server.broadcast("Hi of String!");
                    MyProtobufData.MessageData data = MyProtobufData.MessageData.newBuilder().setText("Hi of Protobuf!")
                            .setSeed(123).build();
                    server.broadcast(data.toByteArray());
                }
            }, 1000, 5000);
        }
    }

    /**
     * client thread
     */
    private class RunnableClientImpl implements Runnable {
        private String clientName;

        public RunnableClientImpl(String clientName) {
            this.clientName = clientName;
        }

        public void run() {
            System.out.println(Thread.currentThread().getName()
                    + ", executing run() method!");
            // Wait until server finishing start
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                System.out.println("Create client and connect.");
                MyClient client = new MyClient(new URI("ws://localhost:" + port), clientName);
                client.connect();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }
}

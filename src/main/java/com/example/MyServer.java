package com.example;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Lock;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class MyServer extends WebSocketServer {

    private Lock lock;

    public MyServer(InetSocketAddress address, Lock lock) {
        super(address);
        this.lock = lock;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conn.send("Welcome to the server!"); // This method sends a message to the new client
        broadcast("new connection: " + handshake.getResourceDescriptor()); // This method sends a message to all clients
                                                                           // connected
        System.out.println("[Server] new connection to " + conn.getRemoteSocketAddress());

    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println(
                "closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("[Server] received message from " + conn.getRemoteSocketAddress() + ": " + message);
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        System.out.println("[Server] received ByteBuffer from " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("[Server] an error occurred on connection " + conn.getRemoteSocketAddress() + ":" + ex);
    }

    @Override
    public void onStart() {
        System.out.println("[Server] server started successfully");
        synchronized (lock) {
            lock.notifyAll();
        }
    }

}

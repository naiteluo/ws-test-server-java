package com.example;

import java.net.URI;
import java.nio.ByteBuffer;

import com.example.protos.MyProtobufData;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.InvalidProtocolBufferException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class MyClient extends WebSocketClient {

    private String clientName;

    public MyClient(URI serverURI) {
        super(serverURI);
    }

    public MyClient(URI serverUri, String clientName) {
        super(serverUri);
        this.clientName = clientName;
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        // The close codes are documented in class org.java_websocket.framing.CloseFrame
        System.out.println(
                "Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: "
                        + reason);
    }

    @Override
    public void onError(Exception exception) {
        exception.printStackTrace();
        // if the error is fatal then onClose will be called additionally
    }

    @Override
    public void onMessage(String message) {
        System.out.println("[Client " + clientName + "] received string: " + message);
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        System.out.println("[Client " + clientName + "] received bytes: " + bytes);
        try {
            MyProtobufData.MessageData data = MyProtobufData.MessageData.parseFrom(bytes);

            System.out.println("text = " + data.getText());
            System.out.println("seed = " + data.getSeed());
            System.out.println("payload = " + data.getPayload());

            JsonObject j = JsonParser.parseString(data.getPayload().toStringUtf8()).getAsJsonObject();

            System.out.println("payload(parsed) = " + j);

            System.out.println("[Client " + clientName + "] received bytes and parsed: " + data.getText());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOpen(ServerHandshake arg0) {
        System.out.println("Connection opened.");
    }
}

package com.life.lifelink.util;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketService {
    private static final String WS_URL = "ws://your-server/ws/user";
    private WebSocket webSocket;
    private final String userId;
    private final WebSocketCallback callback;

    public interface WebSocketCallback {
        void onBookingAccepted(String driverId, String bookingId);
        void onStatusUpdate(String bookingId, String status);
        void onDriverLocation(String driverId, double lat, double lng);
        void onError(String message);
    }

    public WebSocketService(String userId, WebSocketCallback callback) {
        this.userId = userId;
        this.callback = callback;
        connect();
    }

    private void connect() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(WS_URL + "?userId=" + userId)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                handleMessage(text);
            }
        });
    }

    private void handleMessage(String message) {
        try {
            JSONObject json = new JSONObject(message);
            String type = json.getString("type");

            switch (type) {
                case "booking_accepted":
                    callback.onBookingAccepted(
                            json.getString("driverId"),
                            json.getString("bookingId")
                    );
                    break;
                case "booking_status_update":
                    callback.onStatusUpdate(
                            json.getString("bookingId"),
                            json.getString("status")
                    );
                    break;
                case "driver_location":
                    callback.onDriverLocation(
                            json.getString("driverId"),
                            json.getDouble("latitude"),
                            json.getDouble("longitude")
                    );
                    break;
                case "error":
                    callback.onError(json.getString("message"));
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "User disconnected");
        }
    }
}

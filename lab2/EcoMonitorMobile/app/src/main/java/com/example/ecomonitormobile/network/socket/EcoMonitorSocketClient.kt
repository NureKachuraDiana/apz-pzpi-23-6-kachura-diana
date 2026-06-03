package com.example.ecomonitormobile.network.socket

import android.util.Log
import com.example.ecomonitormobile.models.Alert.ThresholdAlertPayload
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URI

class EcoMonitorSocketClient(
    private val serverBaseUrl: String,
    private val onThresholdAlert: (ThresholdAlertPayload) -> Unit,
    private val onConnectionChange: (Boolean) -> Unit = {}
) {
    private var socket: Socket? = null
    private val gson = Gson()

    fun connect() {
        if (socket?.connected() == true) return

        try {
            val options = IO.Options().apply {
                forceNew = true
                reconnection = true
                reconnectionAttempts = Int.MAX_VALUE
                reconnectionDelay = 2000
                transports = arrayOf("websocket", "polling")
            }

            val uri = URI.create("$serverBaseUrl/notifications")
            socket = IO.socket(uri, options).apply {
                on(Socket.EVENT_CONNECT) {
                    Log.d(TAG, "Socket connected")
                    onConnectionChange(true)
                }
                on(Socket.EVENT_DISCONNECT) {
                    Log.d(TAG, "Socket disconnected")
                    onConnectionChange(false)
                }
                on(Socket.EVENT_CONNECT_ERROR) { args ->
                    Log.e(TAG, "Socket connect error: ${args.joinToString()}")
                    onConnectionChange(false)
                }
                on(EVENT_THRESHOLD_ALERT) { args ->
                    val json = args.getOrNull(0) as? JSONObject ?: return@on
                    runCatching {
                        gson.fromJson(json.toString(), ThresholdAlertPayload::class.java)
                    }.onSuccess { payload ->
                        onThresholdAlert(payload)
                    }.onFailure { e ->
                        Log.e(TAG, "Failed to parse threshold_alert", e)
                    }
                }
                connect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Socket setup failed", e)
            onConnectionChange(false)
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        onConnectionChange(false)
    }

    fun isConnected(): Boolean = socket?.connected() == true

    companion object {
        private const val TAG = "EcoMonitorSocket"
        const val EVENT_THRESHOLD_ALERT = "threshold_alert"
    }
}

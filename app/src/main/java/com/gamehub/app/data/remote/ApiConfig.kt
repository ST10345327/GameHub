package com.gamehub.app.data.remote

/** The one place that says where the backend lives. */
object ApiConfig {
    /**
     * Must end with "/".
     *  - Android emulator talking to the backend on your PC:  http://10.0.2.2:3000/
     *  - Real phone on the same Wi-Fi as your PC:              http://192.168.x.x:3000/
     *  - Hosted backend (final demo):                          https://your-api.onrender.com/
     */
    /**
     * Recommended for local development (Emulator or USB-connected phone):
     * 1. Set this to http://127.0.0.1:3000/
     * 2. Run: adb reverse tcp:3000 tcp:3000
     * This bypasses Windows Firewall and works without knowing your PC's IP address.
     */
    const val BASE_URL = "http://127.0.0.1:3000/"
}
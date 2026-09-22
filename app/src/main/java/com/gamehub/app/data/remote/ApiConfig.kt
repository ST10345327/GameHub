package com.gamehub.app.data.remote

/** The one place that says where the backend lives. */
object ApiConfig {
    /**
     * Must end with "/".
     *  - Android emulator talking to the backend on your PC:  http://10.0.2.2:3000/
     *  - Real phone on the same Wi-Fi as your PC:              http://192.168.x.x:3000/
     *  - Hosted backend (final demo):                          https://your-api.onrender.com/
     */
    const val BASE_URL = "http://10.0.2.2:3000/"
}
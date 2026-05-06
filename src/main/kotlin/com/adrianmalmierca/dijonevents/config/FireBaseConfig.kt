package com.adrianmalmierca.dijonevents.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream
import jakarta.annotation.PostConstruct

@Configuration
class FirebaseConfig { //to config and initialize Firebase in the application, allowing us to use its services like authentication and database access.

    @PostConstruct
    fun initFirebase() {//it executes automatically after the bean is created,
        if (FirebaseApp.getApps().isNotEmpty()) return // ensuring that Firebase is initialized before any other component tries to use it.

        val serviceAccountJson = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON")

        if (serviceAccountJson != null) {
            val stream = serviceAccountJson.toByteArray().inputStream() //transform into JSON
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(stream))  //use the credentials from the json
                .build()
            FirebaseApp.initializeApp(options) //initialize firebase with that config
            return
        }

        //if its local
        val serviceAccountFile = java.io.File("firebase-service-account.json")
        if (!serviceAccountFile.exists()) {
            println("WARNING: firebase-service-account.json not found. Push notifications disabled.")
            return
        }

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccountFile.inputStream()))
            .build()
        FirebaseApp.initializeApp(options)
    }
}
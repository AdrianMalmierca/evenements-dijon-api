package com.adrianmalmierca.dijonevents.service

import com.adrianmalmierca.dijonevents.repository.UserRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.springframework.stereotype.Service

@Service
class NotificationService( //to handle the notifications to the users, using Firebase Cloud Messaging (FCM)
    private val userRepository: UserRepository
) {

    fun updateFcmToken(email: String, token: String) { //to save the token
        val user = userRepository.findByEmail(email).orElse(null) ?: return
        user.fcmToken = token
        userRepository.save(user) //save the token in the db
    }

    fun sendNotificationToUser(email: String, title: String, body: String) {
        if (FirebaseApp.getApps().isEmpty()) return
        val user = userRepository.findByEmail(email).orElse(null) ?: return
        val fcmToken = user.fcmToken ?: return

        val message = Message.builder()
            .setNotification(
                Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build()
            )
            .setToken(fcmToken) //to determinate to which device we send the notification
            .build()

        try {
            FirebaseMessaging.getInstance().send(message) //send the notification
        } catch (e: Exception) {
            println("Error sending notification: ${e.message}")
        }
    }

    //we dont use it yet, but it can be useful to send a notification to all users,
    // for example, when we add a new event
    fun sendNotificationToAll(title: String, body: String) {
        val users = userRepository.findAll()
        users.forEach { user ->
            user.fcmToken?.let { token ->
                val message = Message.builder()
                    .setNotification(
                        Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build()
                    )
                    .setToken(token)
                    .build()
                try {
                    FirebaseMessaging.getInstance().send(message)
                } catch (e: Exception) {
                    println("Error sending to ${user.email}: ${e.message}")
                }
            }
        }
    }
}
package com.finalyear.fieldtrack.utils

import android.content.Context
import android.telephony.SmsManager

object SmsHelper {

    /**
     * Sends a plain text SMS. Caller must already hold SEND_SMS permission
     * (requested at runtime) before calling this.
     */
    fun sendVisitNotification(context: Context, phoneNumber: String, siteName: String) {
        try {
            val smsManager = context.getSystemService(SmsManager::class.java)
                ?: SmsManager.getDefault()
            val message = "FieldTrack: Visit logged for site '$siteName'."
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

package com.textcount
 
import android.content.Context
import android.content.SharedPreferences
import android.media.RingtoneManager
 
class TextCountPreferences(context: Context) {
 
    private val prefs: SharedPreferences =
        context.getSharedPreferences("text_count_prefs", Context.MODE_PRIVATE)
 
    var smsCount: Int
        get() = prefs.getInt(KEY_SMS_COUNT, 0)
        set(value) = prefs.edit().putInt(KEY_SMS_COUNT, value).apply()
 
    var targetCount: Int
        get() = prefs.getInt(KEY_TARGET_COUNT, DEFAULT_TARGET)
        set(value) = prefs.edit().putInt(KEY_TARGET_COUNT, value).apply()
 
    var ringtoneUri: String
        get() {
            val default = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE).toString()
            return prefs.getString(KEY_RINGTONE_URI, default) ?: default
        }
        set(value) = prefs.edit().putString(KEY_RINGTONE_URI, value).apply()
 
    fun incrementCount(): Int {
        val newCount = smsCount + 1
        smsCount = newCount
        return newCount
    }
 
    fun resetCount() {
        smsCount = 0
    }
 
    companion object {
        private const val KEY_SMS_COUNT = "sms_count"
        private const val KEY_TARGET_COUNT = "target_count"
        private const val KEY_RINGTONE_URI = "ringtone_uri"
        private const val DEFAULT_TARGET = 100
    }
}

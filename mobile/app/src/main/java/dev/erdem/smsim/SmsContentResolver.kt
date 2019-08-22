package dev.erdem.smsim

import android.content.ContentResolver
import android.provider.ContactsContract
import android.provider.Telephony

data class SmsConversationInfo(val thread_id: String, val msg_count: String, val snippet: String, val person: String)

data class SmsMessage(val address: String, val date: String, val body: String, val type: String)

class SmsContentResolver(private val contentResolver: ContentResolver) {
    fun getConversations(count: Int = 10): List<SmsConversationInfo> {
        val conversations = mutableListOf<SmsConversationInfo>()
        var counter = 0
        val conversationsCursor = contentResolver.query(Telephony.Sms.Conversations.CONTENT_URI, null, null, null, null)
        while (conversationsCursor!!.moveToNext() && counter < count) {
            val threadId = conversationsCursor.getString(conversationsCursor.getColumnIndex("thread_id"))
            val msgCount = conversationsCursor.getString(conversationsCursor.getColumnIndex("msg_count"))
            val snippet = conversationsCursor.getString(conversationsCursor.getColumnIndex("snippet"))

            val person = getPerson(threadId)
            conversations.add(SmsConversationInfo(threadId, msgCount, snippet, person))
            counter += 1
        }
        return conversations
    }

    fun getMessagesByThreadId(threadId: String, count: Int = 10): List<SmsMessage> {
        val messages = mutableListOf<SmsMessage>()
        var counter = 0

        val messagesCursor = contentResolver.query(Telephony.Sms.Conversations.CONTENT_URI.buildUpon().appendPath(threadId).build(), null, null, null, null)

        while (messagesCursor!!.moveToNext() && counter < count) {
            val address = messagesCursor.getString(messagesCursor.getColumnIndex("address"))
            val date = messagesCursor.getString(messagesCursor.getColumnIndex("date"))
            val body = messagesCursor.getString(messagesCursor.getColumnIndex("body"))
            val type = messagesCursor.getString(messagesCursor.getColumnIndex("type"))

            messages.add(SmsMessage(address, date, body, type))
            counter += 1
        }

        return messages
    }

    private fun getPerson(threadId: String): String {
        val message = getMessagesByThreadId(threadId, 1).first()
        val contactCursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_FILTER_URI.buildUpon().appendPath(message.address).build(),
            null,
            null,
            null,
            null
        )

        if (contactCursor!!.moveToFirst()) {
            return contactCursor.getString(contactCursor.getColumnIndex("display_name"))
        }
        return message.address
    }
}

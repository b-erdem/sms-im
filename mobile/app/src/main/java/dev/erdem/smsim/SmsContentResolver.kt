package dev.erdem.smsim

import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony

data class SmsConversationInfo(val thread_id: String, val msg_count: String, val snippet: String, val date: String, val read: String, val person: String)

data class SmsMessage(val address: String, val date: String, val body: String, val type: String)

class SmsContentResolver(private val contentResolver: ContentResolver) {
    private val PROJECTION = arrayOf("_id", "date", "message_count", "recipient_ids", "snippet", "read")

    fun getConversations(position: Int = 0, count: Int = 10): List<SmsConversationInfo> {
        val conversations = mutableListOf<SmsConversationInfo>()
        val cCursor = contentResolver.query(Uri.parse("content://mms-sms/conversations?simple=true"), PROJECTION, null, null,
            "date DESC limit $count offset $position"
        )
        while (cCursor!!.moveToNext()) {
            val threadId = cCursor.getString(cCursor.getColumnIndex("_id"))
            val msgCount = cCursor.getString(cCursor.getColumnIndex("message_count"))
            val snippet = cCursor.getString(cCursor.getColumnIndex("snippet"))
            val date = cCursor.getString(cCursor.getColumnIndex("date"))
            val read = cCursor.getString(cCursor.getColumnIndex("read"))

            val recipients = cCursor.getString(cCursor.getColumnIndex("recipient_ids"))

            val person = getPerson(threadId)
            conversations.add(SmsConversationInfo(threadId, msgCount, snippet, date, read, person))
        }
        return conversations
    }

    fun getMessagesByThreadId(threadId: String, position: Int = 0, count: Int = 10): List<SmsMessage> {
        val messages = mutableListOf<SmsMessage>()

        val mCursor = contentResolver.query(Telephony.Sms.Conversations.CONTENT_URI.buildUpon().appendPath(threadId).build(), null, null, null, "date DESC limit $count OFFSET $position")

        while (mCursor!!.moveToNext()) {
            val address = mCursor.getString(mCursor.getColumnIndex("address"))
            val date = mCursor.getString(mCursor.getColumnIndex("date"))
            val body = mCursor.getString(mCursor.getColumnIndex("body"))
            val type = mCursor.getString(mCursor.getColumnIndex("type"))

            messages.add(SmsMessage(address, date, body, type))
        }

        return messages
    }

    private fun getPerson(threadId: String): String {
        val message = getMessagesByThreadId(threadId, count = 1).first()
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

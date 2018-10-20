package nu.tunedal.bdmem

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.widget.RemoteViews
import android.provider.ContactsContract.CommonDataKinds.Event
import java.util.Calendar
import android.app.PendingIntent
import android.content.Intent
import android.util.Log

class CursorIterator(val cursor: Cursor): Iterator<Cursor> {
    override fun next() = cursor.apply { moveToNext() }
    override fun hasNext() = !cursor.isLast
}

operator fun Cursor.iterator() = CursorIterator(this)

data class Birthday(val name: String, val date: String) {
    val birthday get() = date.substring(5)
}

fun <T> MutableList<T>.rotate(offset: Int) {
    java.util.Collections.rotate(this, offset)
}

fun <T> Collection<T>.rotated(offset: Int): List<T> =
        toMutableList().apply { rotate(offset) }

val Calendar.month get() = get(Calendar.MONTH) + 1
val Calendar.dayOfMonth get() = get(Calendar.DAY_OF_MONTH)

object log {
    private const val tag = "BDMem"
    fun verbose(msg: String) = Log.v(tag, msg)
    fun debug(msg: String) = Log.d(tag, msg)
    fun info(msg: String) = Log.i(tag, msg)
    fun warning(msg: String) = Log.w(tag, msg)
    fun error(msg: String) = Log.e(tag, msg)
}

class BDMemWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context,
                          appWidgetManager: AppWidgetManager,
                          appWidgetIds: IntArray) {
        log.debug("Sweet zombie Jesus!")
        for (widgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName,
                    R.layout.bdmem_appwidget)
            views.removeAllViews(R.id.container)

            val updateIntent = Intent().apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                         intArrayOf(widgetId))
            }
            val pendingIntent = PendingIntent.getBroadcast(
                    context, 0, updateIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
            views.setOnClickPendingIntent(R.id.container, pendingIntent)

            for ((namn, datum) in getBirthdays(context).take(5)) {
                val row = RemoteViews(context.packageName,
                        R.layout.widget_row)
                row.setTextViewText(R.id.datum, datum)
                row.setTextViewText(R.id.namn, namn)
                views.addView(R.id.container, row)
                log.verbose("getBirthdays: ${datum}, ${namn}")
            }
            // TODO: Uppdatera med Service istället.
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }

    fun getBirthdays(context: Context): List<Birthday> {
        val projection = arrayOf(
                ContactsContract.Data._ID,
                Event.CONTACT_ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                Event.START_DATE,
                Event.TYPE,
                ContactsContract.Data.MIMETYPE)
        val resolver = context.contentResolver
        val where = ContactsContract.Data.MIMETYPE +
                " = ? AND " +
                ContactsContract.Data.DATA2 +
                " = ?"
        val cursor = resolver.query(ContactsContract.Data.CONTENT_URI,
                projection,
                where,
                arrayOf(Event.CONTENT_ITEM_TYPE, "" + Event.TYPE_BIRTHDAY),
                null)
        val now = Calendar.getInstance().let {
            "%02d-%02d".format(it.month, it.dayOfMonth)
        }
        val birthdays = cursor.iterator().asSequence().map {
            Birthday(it.getString(2), it.getString(3))
        }.sortedBy { it.birthday }.toList()
        val nextIndex = birthdays.indexOfFirst { it.birthday >= now }
                .let { if (it == -1) 0 else it }  // ta första om ingen hittas
        log.debug("Next birthday: index $nextIndex, ${birthdays[nextIndex]}")
        return birthdays.rotated(1 - nextIndex)
    }
}

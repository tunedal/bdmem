package nu.tunedal.bdmem

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.util.Pair
import android.widget.RemoteViews
import android.widget.TextView
import android.widget.LinearLayout
import java.util.ArrayList
import java.util.HashMap
import android.text.TextUtils
import android.provider.ContactsContract.CommonDataKinds.Event

class BDMemWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context,
                          appWidgetManager: AppWidgetManager,
                          appWidgetIds: IntArray) {
        println("Sweet zombie Jesus!")
        for (i in appWidgetIds.indices) {
            val views = RemoteViews(context.packageName,
                    R.layout.bdmem_appwidget)
            views.removeAllViews(R.id.container)
            for (p in getBirthdays(context)) {
                val row = RemoteViews(context.packageName,
                        R.layout.widget_row)
                row.setTextViewText(R.id.datum, p.first)
                row.setTextViewText(R.id.namn, p.second)
                views.addView(R.id.container, row)
                println("getBirthdays: " + p.first
                        + ", " + p.second)
            }
            // TODO: Uppdatera med Service istället, eftersom det kan
            // ta en stund att köra queryn.
            /*
            System.out.println("Sleeping...");
            try {
                Thread.sleep(30000);
            }
            catch (InterruptedException ex) {
            }
            System.out.println("Awake!");
            */
            appWidgetManager.updateAppWidget(appWidgetIds[i], views)
        }
    }

    fun getBirthdays(context: Context): List<Pair<String, String>> {
        val projection = arrayOf(
                ContactsContract.Data._ID,
                Event.CONTACT_ID,
                Event.START_DATE,
                Event.TYPE,
                ContactsContract.Data.MIMETYPE)

        val resolver = context.contentResolver
        val where = ContactsContract.Data.MIMETYPE +
                " = ? AND " +
                ContactsContract.Data.DATA2 +
                " = ?"
        val cur = resolver.query(ContactsContract.Data.CONTENT_URI,
                projection,
                where,
                arrayOf(Event.CONTENT_ITEM_TYPE, "" + Event.TYPE_BIRTHDAY),
                ContactsContract.Data._ID + " ASC")
        val list = ArrayList<Pair<String, String>>()
        list.add(Pair.create("Rader:", "" + cur!!.count))
        var j = 1
        val ignorables = arrayOf("name", "phone_v2", "email_v2", "photo")
        val birthdays = HashMap<String, String>()
        val nicknames = HashMap<String, String>()
        while (cur.moveToNext() && j++ < 5) {
            birthdays[cur.getString(1)] = cur.getString(2)
            var ignore = false
            for (s in ignorables) {
                if (cur.getString(projection.size - 1).endsWith("/$s")) {
                    ignore = true
                    break
                }
            }
            if (ignore) continue
            val sb = StringBuilder()
            for (i in projection.indices) {
                sb.append(", ")
                sb.append(cur.getString(i))
            }
            println(sb.toString())
        }
        println(TextUtils.join(", ", birthdays.keys))
        // TODO: query mot kontakttabellen för att översätta id:n till namn.
        for ((key, value) in birthdays) {
            list.add(Pair.create(key, value))
        }
        return list
    }
}

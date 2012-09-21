package nu.tunedal.bdmem;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Pair;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.text.TextUtils;
import android.provider.ContactsContract.CommonDataKinds.Event;

public class BDMemWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        System.out.println("Sweet zombie Jesus!");
        for (int i=0; i < appWidgetIds.length; i++) {
            RemoteViews views = new RemoteViews(context.getPackageName(),
                                                R.layout.bdmem_appwidget);
            int j = 0;
            for (Pair<String,String> p : getBirthdays(context)) {
                RemoteViews row = new RemoteViews(context.getPackageName(),
                                                  R.layout.widget_row);
                row.setTextViewText(R.id.datum, p.first);
                row.setTextViewText(R.id.namn, p.second);
                views.addView(R.id.container, row);
                System.out.println("getBirthdays: " + p.first + ", "
                                   + p.second);
                if (++j == 5)
                    break;
            }
            System.out.println("Sleeping...");
            try {
                //Thread.sleep(30000);
                Thread.sleep(100);
            }
            catch (InterruptedException ex) {
            }
            System.out.println("Awake!");
            appWidgetManager.updateAppWidget(appWidgetIds[i], views);
        }
    }

    /*
    private void addLine(RemoteViews views, String text1, String text2) {
        LinearLayout layout = new LinearLayout();
        TextView a = new TextView();
        a.setText(text1);
        a.setMaxLines(1);
        a.setTextSize(11);
        a.setTextColor(0xEEEEEE);
        layout.addView(a);
        TextView b = new TextView();
        b.setText(text2);
        b.setMaxLines(1);
        b.setTextSize(11);
        b.setTextColor(0xEEEEEE);
        layout.addView(b);
        views.addView(R.id.container, layout);
    }
    */

    public List<Pair<String,String>> getBirthdays(Context context) {
        String[] projection = {
            ContactsContract.Data._ID,
            Event.CONTACT_ID,
            Event.START_DATE,
            Event.TYPE,
            ContactsContract.Data.MIMETYPE
        };

        ContentResolver resolver = context.getContentResolver();
        String where = (ContactsContract.Data.MIMETYPE +
                        " = ? AND " +
                        ContactsContract.Data.DATA2 +
                        " = ?");
        Cursor cur = resolver.query(ContactsContract.Data.CONTENT_URI,
                                    projection,
                                    // where,
                                    // new String[] {
                                    //     Event.CONTENT_ITEM_TYPE,
                                    //     "" + Event.TYPE_BIRTHDAY
                                    // },
                                    null,
                                    null,
                                    ContactsContract.Data._ID + " ASC");
        List<Pair<String,String>> list = new ArrayList<Pair<String,String>>();
        list.add(Pair.create("Rader:", "" + cur.getCount()));
        //String[] ignorables = {"name", "phone_v2", "email_v2", "photo"};
        String[] ignorables = {"name", "email_v2", "photo"};
        Map<String,String> birthdays = new HashMap<String,String>();
        Map<String,String> nicknames = new HashMap<String,String>();
        while (cur.moveToNext()) {
            birthdays.put(cur.getString(1), cur.getString(2));
            boolean ignore = false;
            for (String s : ignorables) {
                if (cur.getString(projection.length - 1).endsWith("/" + s)) {
                    ignore = true;
                    break;
                }
            }
            if (ignore) continue;
            StringBuilder sb = new StringBuilder();
            for (int i=0; i < projection.length; i++) {
                sb.append(", ");
                sb.append(cur.getString(i));
            }
            System.out.println(sb.toString());
        }
        System.out.println(TextUtils.join(", ", birthdays.keySet()));
        // TODO: query mot kontakttabellen för att översätta id:n till namn.
        // ...och lägg till views dynamiskt:
        // http://stackoverflow.com/questions/4078039
        for (Map.Entry<String,String> entry : birthdays.entrySet()) {
            list.add(Pair.create(entry.getKey(), entry.getValue()));
        }
        return list;
    }
}

package nu.tunedal.bdmem;

import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.widget.RemoteViews;

public class BDMemWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        System.out.println("Sweet zombie Jesus!");
        for (int i=0; i < appWidgetIds.length; i++) {
            RemoteViews views = new RemoteViews(context.getPackageName(),
                                                R.layout.bdmem_appwidget);
            views.setTextViewText(R.id.datum1, "Tjosan!");
            appWidgetManager.updateAppWidget(appWidgetIds[i], views);
        }
    }
}

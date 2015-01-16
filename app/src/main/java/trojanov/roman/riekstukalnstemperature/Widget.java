package trojanov.roman.riekstukalnstemperature;

/**
 * Created by Roman on 16.01.2015.
 */

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Widget extends AppWidgetProvider {

    RemoteViews remoteViews;
    Context cntxt;


    public static String ACTION_WIDGET_RECEIVER = "ActionReceiverWidget";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        //Создаем новый RemoteViews
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);

        //Подготавливаем Intent для Broadcast
        Intent active = new Intent(context, Widget.class);
        active.setAction(ACTION_WIDGET_RECEIVER);

        //создаем наше событие
        PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);

        //регистрируем наше событие
        remoteViews.setOnClickPendingIntent(R.id.widget_bg, actionPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.temperature_text, actionPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.refresh_time, actionPendingIntent);

        //обновляем виджет
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        //Ловим наш Broadcast, проверяем и выводим сообщение
        final String action = intent.getAction();
        if (ACTION_WIDGET_RECEIVER.equals(action)) {

            getTemperature();
        }
        super.onReceive(context, intent);
    }

    private void updateTemperature(Response response){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(cntxt);
        ComponentName AppWidget = new ComponentName(cntxt.getPackageName(), Widget.class.getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(AppWidget);

        remoteViews = new RemoteViews(AppWidget.getPackageName(), R.layout.main);
        remoteViews.setTextViewText(R.id.temperature_text, response.getTemperature());
        remoteViews.setTextViewText(R.id.refresh_time, response.getTime());

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }

    private void getTemperature(){
        LongAndComplicatedTask longTask = new LongAndComplicatedTask(); // Создаем экземпляр
        longTask.execute(); // запускаем
    }

    private Response getTemperatureFromRequest(){
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://www.riekstukalns.lv/ajax/get-temp");

        try {
            // Выполним запрос
            HttpResponse response = httpclient.execute(httppost);
            String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
            Document doc = Jsoup.parse(responseString);

            Element spanElement = doc.getElementsByTag("span").get(0);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date refreshTime = sdf.parse(spanElement.attr("title"));
            Format formatter = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");

            return new Response(spanElement.text(), formatter.format(refreshTime));

        } catch (IOException e) {
            Log.e("Error", "sendRequest", e);
        } catch (ParseException e) {
            Log.e("Error", "date parse error", e);
        }
        return null;
    }

    class LongAndComplicatedTask extends AsyncTask<Void, Void, Response> {

        @Override
        protected Response doInBackground(Void... noargs) {

            return getTemperatureFromRequest();
        }

        @Override
        protected void onPostExecute(Response response) {

            updateTemperature(response);
        }
    }

}

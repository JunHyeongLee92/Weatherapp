package com.bignerdranch.android.weatherapp;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextView;

/**
 * Implementation of App Widget functionality.
 */
public class WeatherWidget extends AppWidgetProvider {
    private static final String TAG = "GpsInfo";
    SharedPreferences widgetWeatherData;
    static String weather = "";
    static boolean gpsEnable = false;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        CharSequence widgetText =  weather;
        //context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        Intent intent=new Intent(context, MainActivity.class);
        PendingIntent pe=PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.imageButton_my, pe);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them

        Log.d(TAG, "onUpdate");

        widgetWeatherData = context.getSharedPreferences("widgetWeatherData", 0);
        weather = "";
        weather = weather + widgetWeatherData.getString("weatherDay", null) + "\r\n"
                + widgetWeatherData.getString("weatherName", null) + "\r\n"
                + widgetWeatherData.getString("cloudsSort", null)
                + " /Cloud amount: " + widgetWeatherData.getString("cloudsValue", null) + "%" + "\r\n"
                + widgetWeatherData.getString("windName", null)
                + " /WindSpeed: " + widgetWeatherData.getString("windSpeed", null) + " mps" + "\r\n"
                + "Max: " + widgetWeatherData.getString("tempMax", null) + "℃"
                + " /Min: " + widgetWeatherData.getString("tempMin", null) + "℃" + "\r\n"
                + "Humidity: " + widgetWeatherData.getString("humidity", null) + "%";

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}


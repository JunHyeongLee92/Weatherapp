package com.bignerdranch.android.weatherapp;

import android.Manifest;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Message;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.os.Handler;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "GpsInfo";
    public static final int THREAD_HANDLER_SUCCESS_INFO = 1;
    TextView tv_WeatherInfo;
    TextView GpsTextView;

    boolean ResumeFlag = false;

    private GpsInfo gps;

    ForeCastManager mForeCast;

    String lon; // 좌표 설정
    String lat;  // 좌표 설정
    String juso;  // 주소 설정

    MainActivity mThis;
    ArrayList<ContentValues> mWeatherData;
    ArrayList<WeatherInfo> mWeatherInfomation;

    SharedPreferences widgetWeatherData; //preferences 정의
    SharedPreferences.Editor editor; //preferences 에디터


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreat()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GpsTextView = (TextView) findViewById(R.id.GpsTextView);

        GetGps();
        Initialize();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();

       // GetGps();
        //Initialize();
        Log.d(TAG, "onResume() if문 밖");
        ResumeFlag = true;
        Log.d(TAG, "onResume() 정상 종료");
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.d(TAG, "onPause()");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    public void GetGps() {
        gps = new GpsInfo(MainActivity.this, mThis);
        Log.d(TAG, "gps = new GetGps()");
        // GPS 사용유무 가져오기

        if (gps.isGetLocation()) {
            Log.d(TAG, "isGetLocation is true");
            lat = String.valueOf(gps.getLatitude());
            lon = String.valueOf(gps.getLongitude());
            juso = gps.getAddress();

            String address = "위도 : " + lat + "\n경도 : " + lon + "\n주소 : " + juso;
            GpsTextView.setText(address);


        } else {
            // GPS 를 사용할수 없으므로
            Log.d(TAG, "isGetLocation is false");
            gps.stopUsingGPS(MainActivity.this);
            //Intent gpsOptionsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            //startActivity(gpsOptionsIntent);
            showSettingsAlert();
        }
    }

    public void Initialize() {
        tv_WeatherInfo = (TextView) findViewById(R.id.tv_WeatherInfo);
        mWeatherInfomation = new ArrayList<>();
        mThis = this;
        mForeCast = new ForeCastManager(lon, lat, mThis);
        mForeCast.run();
    }

    //읽어온 날씨 값을 출력해주는 메써드
    public String PrintValue() {
        String mData = "";
        for (int i = 0; i < mWeatherInfomation.size(); i++) {
            mData = mData + mWeatherInfomation.get(i).getWeather_Day() + "\r\n"
                    + mWeatherInfomation.get(i).getWeather_Name() + "\r\n"
                    + mWeatherInfomation.get(i).getClouds_Sort()
                    + " /Cloud amount: " + mWeatherInfomation.get(i).getClouds_Value()
                    + mWeatherInfomation.get(i).getClouds_Per() + "\r\n"
                    + mWeatherInfomation.get(i).getWind_Name()
                    + " /WindSpeed: " + mWeatherInfomation.get(i).getWind_Speed() + " mps" + "\r\n"
                    + "Max: " + mWeatherInfomation.get(i).getTemp_Max() + "℃"
                    + " /Min: " + mWeatherInfomation.get(i).getTemp_Min() + "℃" + "\r\n"
                    + "Humidity: " + mWeatherInfomation.get(i).getHumidity() + "%";

            mData = mData + "\r\n" + "----------------------------------------------" + "\r\n";
        }
        widgetWeatherData = getSharedPreferences("widgetWeatherData", 0);
        editor= widgetWeatherData.edit();
        editor.putString("weatherDay", mWeatherInfomation.get(0).getWeather_Day());
        editor.putString("weatherName", mWeatherInfomation.get(0).getWeather_Name());
        editor.putString("cloudsSort", mWeatherInfomation.get(0).getClouds_Sort());
        editor.putString("cloudsValue", mWeatherInfomation.get(0).getClouds_Value());
        editor.putString("windName", mWeatherInfomation.get(0).getWind_Name());
        editor.putString("windSpeed", mWeatherInfomation.get(0).getWind_Speed());
        editor.putString("tempMax", mWeatherInfomation.get(0).getTemp_Max());
        editor.putString("tempMin", mWeatherInfomation.get(0).getTemp_Min());
        editor.putString("humidity", mWeatherInfomation.get(0).getHumidity()) ;
        editor.commit();

        //정보가 바뀌면 위젯에 값을 업데이트 시킴
        Intent intent = new Intent(MainActivity.this, WeatherWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        MainActivity.this.sendBroadcast(intent);

        return mData;
    }

    public void DataChangedToHangeul() {
        for (int i = 0; i < mWeatherInfomation.size(); i++) {
            WeatherToHangeul mHangeul = new WeatherToHangeul(mWeatherInfomation.get(i));
            mWeatherInfomation.set(i, mHangeul.getHangeulWeather());
        }
    }


    public void DataToInformation() {
        for (int i = 0; i < mWeatherData.size(); i++) {
            mWeatherInfomation.add(new WeatherInfo(
                    String.valueOf(mWeatherData.get(i).get("weather_Name")),
                    String.valueOf(mWeatherData.get(i).get("weather_Number")),
                    String.valueOf(mWeatherData.get(i).get("weather_Much")),
                    String.valueOf(mWeatherData.get(i).get("weather_Type")),
                    String.valueOf(mWeatherData.get(i).get("wind_Direction")),
                    String.valueOf(mWeatherData.get(i).get("wind_SortNumber")),
                    String.valueOf(mWeatherData.get(i).get("wind_SortCode")),
                    String.valueOf(mWeatherData.get(i).get("wind_Speed")),
                    String.valueOf(mWeatherData.get(i).get("wind_Name")),
                    String.valueOf(mWeatherData.get(i).get("temp_Min")),
                    String.valueOf(mWeatherData.get(i).get("temp_Max")),
                    String.valueOf(mWeatherData.get(i).get("humidity")),
                    String.valueOf(mWeatherData.get(i).get("Clouds_Value")),
                    String.valueOf(mWeatherData.get(i).get("Clouds_Sort")),
                    String.valueOf(mWeatherData.get(i).get("Clouds_Per")),
                    String.valueOf(mWeatherData.get(i).get("day"))
            ));

        }

    }

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case THREAD_HANDLER_SUCCESS_INFO:
                    mForeCast.getmWeather();
                    mWeatherData = mForeCast.getmWeather();
                    if (mWeatherData.size() == 0)
                        tv_WeatherInfo.setText("데이터가 없습니다");

                    DataToInformation(); // 자료 클래스로 저장,

                    String data = "";
                    DataChangedToHangeul();
                    data = PrintValue();

                    tv_WeatherInfo.setText(data);
                    break;
                default:
                    break;
            }
        }
    };

    public void showSettingsAlert() {
        Log.d(TAG, "Dialog start");

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

        alertDialog.setTitle("GPS 사용유무셋팅");
        alertDialog.setMessage("GPS 사용이 설정되지 않았습니다..\n 설정창으로 가시겠습니까?");
        Log.d(TAG, "Dialog 창 설정");
        // Cancle 하면 종료 합니다.
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        moveTaskToBack(true);
                        finish();
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                });

        // OK 를 누르게 되면 설정창으로 이동합니다.
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Dialog settings");
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                        dialog.cancel();
                    }
                });
        Log.d(TAG, "Dialog 창 설정 완료");
        alertDialog.show();
        Log.d(TAG, "Dialog 창 띄움");
    };
}

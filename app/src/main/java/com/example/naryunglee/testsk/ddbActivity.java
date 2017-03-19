package com.example.naryunglee.testsk;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.skp.Tmap.TMapData;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.ParserConfigurationException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ddbActivity extends Activity {

    ImageButton emergency, cameraBtn;
    private NetworkService api;
    SharedPreferences login_email;
    private Context context;
    TMapData tmapdata = new TMapData();

    private static final int PERMISSIONS_REQUEST_SEND_SMS = 1;
    private static final int PERMISSIONS_REQUEST_LOCATION = 2;
    private Activity mainActivity = this;

    double lat, lon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // 움직이는 발자국
        ImageView iv = (ImageView)findViewById(R.id.iv); //이미지라이브러리: 움직이는 발자국

        GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(iv);
        Glide.with(this).load(R.raw.moving).into(iv);

        ApplicationController application = ApplicationController.getInstance();
        application.buildNetworkService("203.249.127.60", 65017);
        api = ApplicationController.getInstance().getNetworkService();

        // 사용자 이메일
        login_email = getSharedPreferences("login_email", MODE_PRIVATE);
        final String loginCheck = login_email.getString("loginEmail", null);

        String send = "sms";
        final PendingIntent sentIntent;
        sentIntent = PendingIntent.getBroadcast(this, 0, new Intent(send), 0);    // 메세지 전송을 위한 pendingintent 설정

        // LocationManager 객체 생성
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        // 메뉴로 가는 버튼 정의
        ImageButton menubtn = (ImageButton) findViewById(R.id.imageButton2);

        // 카메라 켜지는 버튼 정의
        cameraBtn = (ImageButton) findViewById(R.id.imageButton3);

        // 긴급 연락 버튼 정의
        emergency = (ImageButton) findViewById(R.id.imageButton4);

        // 지도로 가는 버튼 정의
        ImageButton mapbtn = (ImageButton) findViewById(R.id.imageButton5);


        // 클릭시 메뉴로 이동
        menubtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent menuintent = new Intent(getApplicationContext(), menuActivity.class);
                startActivity(menuintent);
            }
        });


        // 클릭시 카메라 켜짐
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraOn();
            }
        });


        // 클릭시 안심지도 켜짐
        mapbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent mapintent = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(mapintent);
            }
        });

        emergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("test");
                int permissionCheck1 = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.SEND_SMS);
                int permissionCheck2 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
                if(permissionCheck1 == PackageManager.PERMISSION_GRANTED && permissionCheck2 == PackageManager.PERMISSION_GRANTED){   // 권한이 모두 허가 되었으면
                    Toast.makeText(getApplicationContext(), "메세지 전송이 가능합니다.", Toast.LENGTH_SHORT).show();
                    System.out.println("메세지 보내기 시작");
                    String send = "sms";

                    Call<List<SMS>> data = api.getAll(loginCheck);

                    try {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, locationListener);  // 현재 위치 잡기
                        data.enqueue(new Callback<List<SMS>>() {
                            @Override
                            public void onResponse(Call<List<SMS>> call, Response<List<SMS>> response) {

                                SmsManager smsManager = SmsManager.getDefault();
                                String message;
                                if (response.isSuccessful()) {
                                    List<SMS> msgs = response.body();

                                    if (msgs != null) {
                                        for (SMS msg : msgs) {
                                            if (msg.getPHONE().isEmpty() == false && msg.getPHONE2().isEmpty() == false && msg.getPHONE3().isEmpty() == false && msg.getPHONE4().isEmpty() == false && msg.getPHONE5().isEmpty() == false) {
                                                smsManager.sendTextMessage(msg.getPHONE(), null, msg.getSOS_Message() + '\n' + "위도/경도: " + lat + "/" + lon, sentIntent, null);
                                                smsManager.sendTextMessage(msg.getPHONE2(), null, msg.getSOS_Message() + '\n' + "위도/경도: " + lat + "/" + lon, sentIntent, null);
                                                smsManager.sendTextMessage(msg.getPHONE3(), null, msg.getSOS_Message() + '\n' + "위도/경도: " + lat + "/" + lon, sentIntent, null);
                                                smsManager.sendTextMessage(msg.getPHONE4(), null, msg.getSOS_Message() + '\n' + "위도/경도: " + lat + "/" + lon, sentIntent, null);
                                                smsManager.sendTextMessage(msg.getPHONE5(), null, msg.getSOS_Message() + '\n' + "위도/경도: " + lat + "/" + lon, sentIntent, null);
                                                Toast.makeText(getApplicationContext(), "전송을 완료 했습니다.", Toast.LENGTH_SHORT).show();
                                            } else if (msg.getPHONE().isEmpty() == false && msg.getPHONE2().isEmpty() == false && msg.getPHONE3().isEmpty() == false && msg.getPHONE4().isEmpty() == false && msg.getPHONE5().isEmpty() == true) {
                                                smsManager.sendTextMessage(msg.getPHONE(), null, msg.getSOS_Message() + '\n' + "위도/경도: " + lat + "/" + lon, sentIntent, null);
                                                smsManager.sendTextMessage(msg.getPHONE2(), null, msg.getSOS_Message() + '\n' + "위도/경도: " + lat + "/" + lon, sentIntent, null);
                                                smsManager.sendTextMessage(msg.getPHONE3(), null, msg.getSOS_Message() + '\n' + "위도/경도: " + lat + "/" + lon, sentIntent, null);
                                                smsManager.sendTextMessage(msg.getPHONE4(), null, msg.getSOS_Message() + '\n' + "위도/경도: " + lat + "/" + lon, sentIntent, null);
                                                Toast.makeText(getApplicationContext(), "전송을 완료 했습니다.", Toast.LENGTH_SHORT).show();
                                            } else if (msg.getPHONE().isEmpty() == false && msg.getPHONE2().isEmpty() == false && msg.getPHONE3().isEmpty() == false && msg.getPHONE4().isEmpty() == true && msg.getPHONE5().isEmpty() == true) {
                                                smsManager.sendTextMessage(msg.getPHONE(), null, msg.getSOS_Message() + '\n' + "위도/경도: " + lat + "/" + lon, sentIntent, null);
                                                smsManager.sendTextMessage(msg.getPHONE2(), null, msg.getSOS_Message() + '\n' + "위도/경도: " + lat + "/" + lon, sentIntent, null);
                                                smsManager.sendTextMessage(msg.getPHONE3(), null, msg.getSOS_Message() + '\n' + "위도/경도: " + lat + "/" + lon, sentIntent, null);
                                                Toast.makeText(getApplicationContext(), "전송을 완료 했습니다.", Toast.LENGTH_SHORT).show();
                                            } else if (msg.getPHONE().isEmpty() == false && msg.getPHONE2().isEmpty() == false && msg.getPHONE3().isEmpty() == true && msg.getPHONE4().isEmpty() == true && msg.getPHONE5().isEmpty() == true) {
                                                smsManager.sendTextMessage(msg.getPHONE(), null, msg.getSOS_Message() + '\n' + "위도/경도: " + lat + "/" + lon, sentIntent, null);
                                                smsManager.sendTextMessage(msg.getPHONE2(), null, msg.getSOS_Message() + '\n' + "위도/경도: " + lat + "/" + lon, sentIntent, null);
                                                Toast.makeText(getApplicationContext(), "전송을 완료 했습니다.", Toast.LENGTH_SHORT).show();
                                            } else if (msg.getPHONE().isEmpty() == false && msg.getPHONE2().isEmpty() == true && msg.getPHONE3().isEmpty() == true && msg.getPHONE4().isEmpty() == true && msg.getPHONE5().isEmpty() == true) {
                                                smsManager.sendTextMessage(msg.getPHONE(), null, msg.getSOS_Message() + '\n' + "위도/경도: " + lat + "/" + lon, sentIntent, null);
                                                Toast.makeText(getApplicationContext(), "전송을 완료 했습니다.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getApplicationContext(), "긴급 연락처를 설정해주세요.", Toast.LENGTH_SHORT).show();
                                            }
                                        }   // for문 끝
                                    } else {
                                        Toast.makeText(getApplicationContext(), "메세지 내용을 설정해주세요.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<List<SMS>> call, Throwable t) {
                                Log.i("MyTag", "서버 failure 내용: " + t.getMessage());
                                Toast.makeText(getApplicationContext(), "오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                else{   // access location 권한이 허가 안됐을 시에

                    System.out.println("test2");
                    ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_SEND_SMS);
                }
            }
        });
    }

    private void cameraOn() {
        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivity(intentCamera);
    }

    // 여기서 위치값 갱신되면 이벤트가 발생
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("test", "locationChanged");
            lon = location.getLongitude();
            lat = location.getLatitude();
            String provider = location.getProvider();   // 위치 제공자

        /*
                System.out.println("********주소**********");
            try {
                address = tmapdata.convertGpsToAddress(lat, lon);
                System.out.println("주소:" + address);
            } catch (IOException e) {
                System.out.println("예외처리");
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                System.out.println("예외처리");
                e.printStackTrace();
            } catch (SAXException e) {
                System.out.println("예외처리");
                e.printStackTrace();
            }
            */
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };
}
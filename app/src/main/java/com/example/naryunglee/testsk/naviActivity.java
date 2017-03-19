package com.example.naryunglee.testsk;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.os.Looper;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.net.ConnectivityManagerCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.support.v7.widget.MenuItemHoverListener;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.skp.Tmap.TMapCircle;
import com.skp.Tmap.TMapData;
import com.skp.Tmap.TMapData.FindAllPOIListenerCallback;
import com.skp.Tmap.TMapData.FindPathDataListenerCallback;
import com.skp.Tmap.TMapData.TMapPathType;
import com.skp.Tmap.TMapGpsManager;
import com.skp.Tmap.TMapGpsManager.onLocationChangedCallback;
import com.skp.Tmap.TMapMarkerItem;
import com.skp.Tmap.TMapPOIItem;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapPolyLine;
import com.skp.Tmap.TMapView;

import org.w3c.dom.Document;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class naviActivity extends Activity implements  onLocationChangedCallback, LocationListener, TMapView.OnCalloutRightButtonClickCallback, NavigationView.OnNavigationItemSelectedListener {

    private Coordinate coordinate;
    private NetworkService api;
    private List<CCTV> cctvs;   // cctv 가져온 정보 저장할 List
    private LocationManager locationManager;
    private Context mContext;
    private Toolbar toolbar;

    ImageView backimg;

    ImageView compass_button;
    ImageView current_button;      //현재위치 버튼

    Button cctv_button;       //cctv 보기 버튼
    Button jwalk_button;        //보행자 사고 보기 버튼

    Button navi_button;         //길찾기 버튼

    ImageButton start_button;
    ImageButton end_button;

    EditText start_edit;
    EditText end_edit;

    //현재위치 위도,경도 저장할 변수
    double current_lat = 0;
    double current_lon = 0;

    //출발지 이름, 위도, 경도 저장할 변수
    String start_name = null;
    double start_lat = 0;
    double start_lon = 0;

    //도착지 이름, 위도, 경도 저장할 변수
    String end_name = null;
    double end_lat = 0;
    double end_lon = 0;

    boolean compass_count = false;  //나침반 버튼을 스위치처럼 사용하기 위한 변수

    boolean current_count = false;  //현재위치 버튼을 스위치처럼 사용하기 위한 변수

    boolean navi_count = false;     //길찾기 버튼을 스위치처럼 사용하기 위한 변수

    boolean cctv_count = false;     //CCTV 버튼을 스위치처럼 사용하기 위한 변수

    boolean jwalk_count = false;    //보행자 버튼을 스위치처럼 사용하기 위한 변수

    Timer mTimer = new Timer();       //현재위치 잡기 위한 타이머


    private static final int PERMISSION_REQUEST_CODE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_LOCATION = 2;

    //현재위치 변할 때마다 잡아줌
    @Override
    public void onLocationChange(Location location) {
        current_lat = location.getLatitude();
        current_lon = location.getLongitude();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.navi_activity_main);
        setTitle("뚜벅뚜벅");

        backimg  = (ImageView) findViewById(R.id.backimg);

        backimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent menu_intent = new Intent(getApplicationContext(), ddbActivity.class);
                startActivity(menu_intent);
            }
        });

        // IP, port 연결, restcontroller 연결
        ApplicationController application = ApplicationController.getInstance();
        application.buildNetworkService("203.249.127.60", 65017);
        api = ApplicationController.getInstance().getNetworkService();

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());

        compass_button = (ImageView) findViewById(R.id.compass_button);
        current_button = (ImageView) findViewById(R.id.current_button);
        cctv_button = (Button) findViewById(R.id.cctv_button);
        jwalk_button = (Button) findViewById(R.id.jwalk_button);

        navi_button = (Button) findViewById(R.id.navi_button);

        start_button = (ImageButton) findViewById(R.id.start_button);
        end_button = (ImageButton) findViewById(R.id.end_button);
        start_edit = (EditText) findViewById(R.id.start_edit);
        end_edit = (EditText) findViewById(R.id.end_edit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_navi);
        //setSupportActionBar(toolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_navi);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_navi);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar , R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(drawer.isDrawerOpen(Gravity.RIGHT)){
                    drawer.closeDrawer(Gravity.RIGHT);
                }
                else{
                    drawer.openDrawer(Gravity.RIGHT);
                }
            }
        });

        //navigationView.setNavigationItemSelectedListener(naviActivity.this);

        //위치 액세스 권한 넣어주기
        int permissionCheck1 = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION);

        //승인되면 보여줌
        if(permissionCheck1 == PackageManager.PERMISSION_GRANTED) {

            ConnectivityManager con_manager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mobile = con_manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifi = con_manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            //인터넷 연결 안되어있으면 토스트창으로 띄워주기
            if(wifi.isConnected() || mobile.isConnected()){
                System.out.println("인터넷 연결 된 상태임");
            }
            else{
                Toast.makeText(this, "인터넷을 연결해주세요", Toast.LENGTH_SHORT).show();
                System.out.println("인터넷 연결 안된 상태임");
            }

            FrameLayout framelayout = (FrameLayout) findViewById(R.id.map_view);

            final TMapView tmapview = new TMapView(this);

            framelayout.addView(tmapview);

            tmapview.setSKPMapApiKey("6b12e66e-e4f5-38d4-a67a-7dc5f3929111");
            tmapview.setLanguage(TMapView.LANGUAGE_KOREAN);
            tmapview.setIconVisibility(false);
            tmapview.setZoomLevel(14);
            tmapview.setMapType(TMapView.MAPTYPE_STANDARD);

            tmapview.setTrackingMode(false);    //트래킹 모드 없음
            tmapview.setCompassMode(false);   //나침반 모드 해제

            //현재위치 탐색 시작
            TMapGpsManager gps = new TMapGpsManager(naviActivity.this);
            gps.setMinTime(1000);       //위치변경 인식 최소시간을 설정
            gps.setMinDistance(5);      //위치변경 인식 최소거리를 설정
            gps.setProvider(gps.GPS_PROVIDER);  //위치탐색 타입을 설정(GPS_PROVIDER는 위성기반의 위치탐색)
            gps.setLocationCallback();  //현재 위치상태 변경시 호출되는 콜백인터페이스 설정하고 성공여부를 반환
            gps.OpenGps();

            if(current_lon!=0 && current_lat!=0){
                tmapview.setLocationPoint(current_lon, current_lat);
            }

            //출발지 검색
            start_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    search_start(tmapview);
                }
            });

            //도착지 검색
            end_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    search_end(tmapview);
                }
            });

            start_edit.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    start_edit.setTextColor(Color.BLACK);
                    start_name=null;
                }
            });

            end_edit.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    end_edit.setTextColor(Color.BLACK);
                    end_name=null;
                }
            });

            //나침반 모드 설정 및 해제
            compass_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {

                    System.out.println(current_lat + " / " + current_lon);

                    //compass가 false로 되어있는 경우(= 안켜져있는 경우)
                    if(compass_count == false) {
                        tmapview.setCompassMode(true);
                        compass_count = true;
                    }

                    //compass가 true로 되어있는 경우
                    else{
                        tmapview.setCompassMode(false);
                        compass_count = false;
                    }
                }
            });

            //현재위치 잡기
            current_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {

                    //current_count가 false로 되어있는 경우(= 안켜져있는 경우)
                    if(current_count == false) {

                        Toast.makeText(naviActivity.this, "현재위치 잡는중", Toast.LENGTH_LONG).show();

                        long start_time = System.currentTimeMillis();
                        long wait_time = 5000;
                        long end_time = start_time + wait_time;

                        while (System.currentTimeMillis() < end_time) {
                            if (current_lat != 0 && current_lon != 0) {
                                tmapview.setCenterPoint(current_lon, current_lat, true);   //화면 중심으로 이동시킴
                                current_count = true;
                                break;
                            }
                        }

                        //5초동안 돌렸는데 아직 현재위치 못잡은 경우에 토스트 함수로 띄워줌
                        if (current_lat != 0 || current_lon != 0) {

                            TimerTask task  = new TimerTask() {
                                @Override
                                public void run() {
                                    try{
                                        current(tmapview);
                                    } catch (Exception e){
                                        Toast.makeText(naviActivity.this, "오류가 발생하였습니다!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            };

                            mTimer = new Timer();

                            mTimer.schedule(task, 1000, 1000);
                        }

                        else{
                            Toast.makeText(naviActivity.this, "아직 현재위치가 잡히지 않았습니다.\n다시 시도해주세요!", Toast.LENGTH_LONG).show();
                        }
                    }//if(current_count==false)

                    //현재위치 버튼 켜져있는거 끄려고 할 때
                    else{
                        mTimer.cancel();    //타이머 취소
                        mTimer = null;     //타이머 초기화

                        Toast.makeText(naviActivity.this, "현재위치 모드를 종료합니다!", Toast.LENGTH_LONG).show();

                        tmapview.removeMarkerItem("current_point"); //마커 삭제
                        tmapview.setTrackingMode(false);    //트래킹 없애기
                        tmapview.setSightVisible(false);     //시야 삭제

                        current_count = false;              //버튼 꺼져있는 걸로 설정하기
                    }
                }
            });

            //지도 위에 서클 그리기
            cctv_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    if(cctv_count==false && navi_count == true && start_name != null && end_name != null) {
                        try {
                            Call<List<CCTV>> datas = api.getCCTV(start_lat, start_lon, end_lat, end_lon);
                            datas.enqueue(new Callback<List<CCTV>>() {

                                @Override
                                public void onResponse(Call<List<CCTV>> call, Response<List<CCTV>> response) {
                                    if (response.isSuccessful()) {
                                        List<CCTV> cctvs = response.body();

                                        if (cctvs.isEmpty() == true) {
                                            System.out.println("넘어온 데이터 없음");
                                            Toast.makeText(getApplicationContext(), "근방에 cctv가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            for (CCTV cctv : cctvs) {
                                                System.out.println("주소: " + cctv.getLatitude() + ", " + cctv.getLongitude());
                                                String tel = cctv.getTel();
                                                double latitude = Double.parseDouble(cctv.getLatitude());
                                                double longtitude = Double.parseDouble(cctv.getLongitude());
                                                String address = cctv.getAddress1();
                                                String management = cctv.getManagement();
                                                cctvMarker(tmapview, address,management, tel, latitude, longtitude);
                                                drawCircle(tmapview, latitude, longtitude, address);
                                            }
                                        }
                                    }
                                    else {
                                        int statusCode = response.code();
                                        Log.i("MyTag", "응답코드: " + statusCode);
                                    }
                                }

                                @Override
                                public void onFailure(Call<List<CCTV>> call, Throwable t) {
                                    Log.i("MyTag", "서버 failure 내용: " + t.getMessage());
                                }
                            });

                            cctv_count = true;

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    else if(cctv_count){

                        tmapview.removeAllTMapCircle();
                        tmapview.removeAllMarkerItem();

                        showMarker1(tmapview, start_name, start_lat, start_lon);
                        showMarker2(tmapview, end_name, end_lat, end_lon);
                        cctv_count = false;

                    }
                    else{
                        System.out.println("좌표: " + start_name + start_lat + ":" + end_name + end_lat);
                        Toast.makeText(getApplicationContext(), "길찾기를 먼저 하세요", Toast.LENGTH_SHORT).show();
                    }
                }

            });

            // 보행자 무단횡단 데이터
            jwalk_button.setOnClickListener(new View.OnClickListener() {
                                                public void onClick(View arg0) {
                                                    if (jwalk_count==false && navi_count == true && start_name != null && end_name != null) {
                                                        try {
                                                            Call<List<Jaywalk>> datas = api.getJaywalk(start_lat, start_lon, end_lat, end_lon);
                                                            datas.enqueue(new Callback<List<Jaywalk>>() {

                                                                @Override
                                                                public void onResponse(Call<List<Jaywalk>> call, Response<List<Jaywalk>> response) {

                                                                    if (response.isSuccessful()) {

                                                                        List<Jaywalk> jaywalks = response.body();

                                                                        if (jaywalks.isEmpty() == true) {
                                                                            System.out.println("넘어온 데이터 없음");
                                                                            Toast.makeText(getApplicationContext(), "근방에 관련 데이터가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                        else {
                                                                            System.out.println(jaywalks);

                                                                            for (Jaywalk jaywalk : jaywalks) {
                                                                                int i = 0;
                                                                                double latitude = Double.parseDouble(jaywalk.getLat());
                                                                                double longtitude = Double.parseDouble(jaywalk.getLon());
                                                                                showMarker4(tmapview, "jaywalk"+i, latitude, longtitude);
                                                                                i++;
                                                                            }
                                                                        }
                                                                    }
                                                                    else {
                                                                        int statusCode = response.code();
                                                                        Log.i("MyTag", "응답코드: " + statusCode);
                                                                    }
                                                                }

                                                                @Override
                                                                public void onFailure(Call<List<Jaywalk>> call, Throwable t) {
                                                                    Log.i("MyTag", "서버 failure 내용: " + t.getMessage());
                                                                }
                                                            });

                                                            jwalk_count = true;

                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }

                                                    else if(jwalk_count){

                                                        //마커들 지우기
                                                        tmapview.removeAllMarkerItem();
                                                        tmapview.removeAllTMapCircle();

                                                        showMarker1(tmapview, start_name, start_lat, start_lon);
                                                        showMarker2(tmapview, end_name, end_lat, end_lon);

                                                        jwalk_count = false;
                                                    }

                                                    else{
                                                        System.out.println("좌표: " + start_name + start_lat + ":" + end_name + end_lat);
                                                        Toast.makeText(getApplicationContext(), "길찾기를 먼저 하세요", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
            });


                                                      //길찾기
            navi_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {

                    String start = null;
                    String end = null;
                    start = start_edit.getText().toString();
                    end = end_edit.getText().toString();

                    System.out.println(start_edit.getHighlightColor());

                    if(navi_count == false && start!=null && end != null) {

                        navigation(tmapview);
                        navi_button.setText("길찾기 종료");
                        navi_count = true;
                        cctv_button.setText("CCTV 끄기");
                    }

                    else {
                        tmapview.removeAllTMapCircle();     //원 삭제
                        tmapview.removeAllMarkerItem();     //CCTV 마커 삭제
                        tmapview.removeTMapPath();          //길찾기 경로 삭제
                        //navi_time.setVisibility(View.GONE);    //걸리는 거리, 시간 안보이게 처리
                        showMarker1(tmapview, start_name, start_lat, start_lon);
                        showMarker2(tmapview, end_name, end_lat, end_lon);
                        navi_button.setText("길찾기");
                        navi_count = false;
                        cctv_count = false;
                        cctv_button.setText("CCTV 보기");
                    }
                }
            });

            //롱클릭 메소드
            tmapview.setOnLongClickListenerCallback(new TMapView.OnLongClickListenerCallback(){
                @Override
                public void onLongPressEvent(ArrayList<TMapMarkerItem> markerItems, ArrayList<TMapPOIItem> poilist, TMapPoint point){

                    System.out.println("롱 클릭 이벤트 발생");

                    final TMapPoint long_point = point;

                    final TMapData tmapdata = new TMapData();

                    try {
                        String point_address = tmapdata.convertGpsToAddress(long_point.getLatitude(), long_point.getLongitude());   //위도, 경도 값을 주소로 반환
                        System.out.println("주소 : "+point_address+ " / 포인트 값 : " +point);

                        showMarker3(tmapview , point_address , point.getLatitude(), point.getLongitude());
                    }
                    catch(Exception e){
                        System.out.println("오류발생 : " + e);
                        Toast.makeText(naviActivity.this, "오류가 발생하였습니다!", Toast.LENGTH_LONG).show();
                    }

                }
            });
        }

        //권한 거부했을때
        else{
            //거부 다음 동작 정의해야
            ActivityCompat.requestPermissions(naviActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
        }
    }//onCreate 끝

    //현재위치 함수
    public void current(TMapView tmapView) {

        //마커 만들기 시작
        TMapPoint cpoint = new TMapPoint(current_lat, current_lon);      //현재위치로 표시될 좌표의 위도와 경도를 설정
        TMapMarkerItem cItem = new TMapMarkerItem();                        //마커아이템 만들기

        cItem.setTMapPoint(cpoint);                                          //마커에 포인트 설정
        cItem.setName("현재 위치");                                         //이름 셋
        cItem.setVisible(TMapMarkerItem.VISIBLE);                           //보이게하기

        Bitmap current_flag = BitmapFactory.decodeResource(getResources(), R.drawable.current_point);    //마크에 쓸 이미지 만들기
        cItem.setIcon(current_flag);                                  //마크에 비트맵이미지 추가

        cItem.setPosition(0.5f, 0.5f);                                //위치 어디쯤에 마크를 둘지 설정

        tmapView.addMarkerItem("current_point", cItem);             //(최종)뷰에 마크 추가

        tmapView.setSightVisible(true);                              //시야 보이게

        tmapView.setLocationPoint(current_lon, current_lat);       //현재위치로 설정
    }//current

    //위도, 경도로 마커찍기
    public void showMarker1(final TMapView tmapView, final String name,  final double lat, final double lon){

        TMapPoint tpoint = new TMapPoint(lat, lon);

        TMapMarkerItem tItem = new TMapMarkerItem();

        tItem.setTMapPoint(tpoint);
        tItem.setName(name);
        tItem.setVisible(TMapMarkerItem.VISIBLE);

        tItem.setCanShowCallout(true);
        tItem.setCalloutTitle(name);
        //tItem.setCalloutSubTitle("♬");

        Bitmap right_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.yello_arrow);
        tItem.setCalloutRightButtonImage(right_bitmap);

        //tItem.setEnableClustering(true);    //마커에 대한 클러스터링 유무
        //tItem.setAutoCalloutVisible(true);  //풍선뷰 자동으로 활성화

        Bitmap current_flag = BitmapFactory.decodeResource(getResources(), R.drawable.blue_flag2);
        tItem.setIcon(current_flag);


        tItem.setPosition(0.5f, 0.5f);

        tmapView.addMarkerItem(name, tItem);


        tmapView.setOnCalloutRightButtonClickListener(new TMapView.OnCalloutRightButtonClickCallback(){
            @Override
            public void onCalloutRightButton(TMapMarkerItem markerItem){

                final String getName = markerItem.getName();
                final double getlat = markerItem.getTMapPoint().getLatitude();
                final double getlon = markerItem.getTMapPoint().getLongitude();

                System.out.println(getName + getlat + getlon);

                start_name = getName;
                start_lat = getlat;
                start_lon = getlon;

                System.out.println("말풍선 터치성공");

                Toast.makeText(naviActivity.this, "출발지로 설정됨", Toast.LENGTH_LONG).show();

                start_edit.setText(getName);
                start_edit.setTextColor(Color.BLUE);
            }
        });

    }//showMarker

    //위도, 경도로 마커찍기
    public void showMarker2(final TMapView tmapView, final String name,  final double lat, final double lon){

        TMapPoint tpoint = new TMapPoint(lat, lon);

        TMapMarkerItem tItem = new TMapMarkerItem();

        tItem.setTMapPoint(tpoint);
        tItem.setName(name);
        tItem.setVisible(TMapMarkerItem.VISIBLE);

        tItem.setCanShowCallout(true);
        tItem.setCalloutTitle(name);
        //tItem.setCalloutSubTitle("♬");

        Bitmap right_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.yello_arrow);
        tItem.setCalloutRightButtonImage(right_bitmap);

        //tItem.setEnableClustering(true);    //마커에 대한 클러스터링 유무
        //tItem.setAutoCalloutVisible(true);  //풍선뷰 자동으로 활성화

        Bitmap current_flag = BitmapFactory.decodeResource(getResources(), R.drawable.red_flag2);
        tItem.setIcon(current_flag);

        tItem.setPosition(0.5f, 0.5f);

        tmapView.addMarkerItem(name, tItem);


        tmapView.setOnCalloutRightButtonClickListener(new TMapView.OnCalloutRightButtonClickCallback(){
            @Override
            public void onCalloutRightButton(TMapMarkerItem markerItem){

                final String getName = markerItem.getName();
                final double getlat = markerItem.getTMapPoint().getLatitude();
                final double getlon = markerItem.getTMapPoint().getLongitude();

                System.out.println(getName + getlat + getlon);

                System.out.println("말풍선 터치성공");

                end_name = getName;
                end_lat = getlat;
                end_lon = getlon;

                Toast.makeText(naviActivity.this, "도착지로 설정됨", Toast.LENGTH_LONG).show();

                end_edit.setText(getName);
                end_edit.setTextColor(Color.BLUE);
            }
        });

    }//showMarker2

    //위도, 경도로 마커찍기
    public void showMarker3(final TMapView tmapView, final String name,  final double lat, final double lon){

        TMapPoint tpoint = new TMapPoint(lat, lon);

        TMapMarkerItem tItem = new TMapMarkerItem();

        tItem.setTMapPoint(tpoint);
        tItem.setName(name);
        tItem.setVisible(TMapMarkerItem.VISIBLE);

        tItem.setCanShowCallout(true);
        tItem.setCalloutTitle(name);
        //tItem.setCalloutSubTitle("♬");

        Bitmap right_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.yello_arrow);
        tItem.setCalloutRightButtonImage(right_bitmap);

        //tItem.setEnableClustering(true);    //마커에 대한 클러스터링 유무
        //tItem.setAutoCalloutVisible(true);  //풍선뷰 자동으로 활성화

        Bitmap current_flag = BitmapFactory.decodeResource(getResources(), R.drawable.red_marker);
        tItem.setIcon(current_flag);

        tItem.setPosition(0.5f, 0.5f);

        tmapView.addMarkerItem(name, tItem);


        tmapView.setOnCalloutRightButtonClickListener(new TMapView.OnCalloutRightButtonClickCallback(){
            @Override
            public void onCalloutRightButton(TMapMarkerItem markerItem){

                final String getName = markerItem.getName();
                final double getlat = markerItem.getTMapPoint().getLatitude();
                final double getlon = markerItem.getTMapPoint().getLongitude();

                System.out.println(getName + getlat + getlon);

                System.out.println("말풍선 터치성공");

                final CharSequence[] items = {"출발지로 설정", "도착지로 설정", "즐겨찾기 등록"};

                AlertDialog.Builder builder = new AlertDialog.Builder(naviActivity.this);

                builder.setTitle(getName)
                        .setItems(items, new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which){

                                if(which == 0){
                                    System.out.println("확인 0 : 출발지 설정 중");

                                    //기존 출발지를 설정하였다면 기존 마크 삭제
                                    if(start_name != null){
                                        System.out.println("기존 마크 삭제 성공 : " + start_name);
                                        tmapView.removeMarkerItem2(start_name);                       //도착 마크 지우기
                                        showMarker3(tmapView, start_name, start_lat, start_lon);      //기존 마크(3)로 변경
                                    }

                                    start_name = getName;          //시작지점 이름 초기화
                                    start_lat = getlat;            //시작지점 위도 초기화
                                    start_lon = getlon;            //시작지점 경도 초기화

                                    tmapView.removeMarkerItem2(getName);           //기존 마커 제거
                                    showMarker1(tmapView, getName, getlat, getlon);    //파랑(출발) 깃발 마커 추가

                                    start_edit.setText(start_name);
                                    start_edit.setTextColor(Color.BLUE);

                                    Toast.makeText(naviActivity.this, "'" + getName + "' 출발지로 설정 됨", Toast.LENGTH_LONG).show();
                                }

                                else if(which == 1){
                                    System.out.println("확인 1 : 도착지 설정 중");

                                    //기존 출발지를 설정하였다면 기존 마크 삭제
                                    if(end_name != null){
                                        System.out.println("기존 마크 삭제 성공 : " + end_name);
                                        tmapView.removeMarkerItem2(end_name);                       //도착 마크 지우기
                                        showMarker3(tmapView, end_name, end_lat, end_lon);      //기존 마크(3)로 변경
                                    }

                                    end_name = getName;            //도착지점 이름 초기화
                                    end_lat = getlat;              //도착지점 위도 초기화
                                    end_lon = getlon;             //도착지점 경도 초기화

                                    tmapView.removeMarkerItem2(getName);           //기존 마커 제거
                                    showMarker2(tmapView, getName, getlat, getlon);    //빨강(도착) 깃발 마커 추가

                                    end_edit.setText(end_name);
                                    end_edit.setTextColor(Color.BLUE);

                                    Toast.makeText(naviActivity.this, "'" + getName + "' 도착지 설정 됨", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

    }//showMarker3

    //보행자 사고 마커 찍기
    public void showMarker4(final TMapView tmapView, final String name,  final double lat, final double lon){

        TMapPoint tpoint = new TMapPoint(lat, lon);

        TMapMarkerItem tItem = new TMapMarkerItem();

        tItem.setTMapPoint(tpoint);
        tItem.setName(name);
        tItem.setVisible(TMapMarkerItem.VISIBLE);

        tItem.setCanShowCallout(true);
        tItem.setCalloutTitle(name);

        Bitmap current_flag = BitmapFactory.decodeResource(getResources(), R.drawable.jwalk);
        tItem.setIcon(current_flag);

        tItem.setPosition(0.5f, 0.5f);

        tmapView.addMarkerItem(name, tItem);

    }//showMarker3


    // cctv 마커
    public void cctvMarker(TMapView tmapView, final String address, final String management, final String tel, double lat, double lon){
        String name = address;

        TMapPoint tpoint = new TMapPoint(lat, lon);

        TMapMarkerItem tItem = new TMapMarkerItem();

        tItem.setTMapPoint(tpoint);
        tItem.setName(name);
        tItem.setVisible(TMapMarkerItem.VISIBLE);

        tItem.setCanShowCallout(true);
        tItem.setCalloutTitle(name);
        tItem.setCalloutSubTitle("관리기관 전화번호: " + tel);

        Bitmap right_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.yello_arrow);
        tItem.setCalloutRightButtonImage(right_bitmap);

        //tItem.setEnableClustering(true);    // 마커에 대한 클러스터링 유무
        tItem.setAutoCalloutVisible(true);  // 풍선뷰 자동으로 활성화

        tmapView.setOnCalloutRightButtonClickListener(new TMapView.OnCalloutRightButtonClickCallback(){
            @Override
            public void onCalloutRightButton(TMapMarkerItem markerItem){
                System.out.println("말풍선 터치성공");

                AlertDialog.Builder alert = new AlertDialog.Builder(naviActivity.this);

                alert.setTitle("CCTV");

                alert.setMessage("주소 : " + address +"\n관리기관명 : " + management + "\n관리기관 전화번호 : " + tel);

                alert.setPositiveButton("뒤로가기", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int whichButton){

                    }
                });

                alert.show();
            }
        });

        Bitmap current_flag = BitmapFactory.decodeResource(getResources(), R.drawable.cctv_marker);
        tItem.setIcon(current_flag);

        tItem.setPosition(0.5f, 0.5f);
        tmapView.addMarkerItem(name, tItem);
        System.out.println("마커 찍힘");
    }


    //길찾기 (얘는 됨)
    public void navigation(final TMapView tMapView){

        if(start_name!=null && end_name!=null) {
            Toast.makeText(this, "길찾기 시작", Toast.LENGTH_LONG).show();

            tMapView.removeAllMarkerItem();     //이전에 있던 모든 마크 지우기
            tMapView.removeAllTMapPolygon();    //이전에 있던 경로 지우기

            tMapView.setCenterPoint(start_lon, start_lat, true);

            //출발지 도착지 잘 설정되어있는지 확인 차 해놓음
            System.out.println("출발지이름 : " + start_name);
            System.out.println("출발지위도 : " + start_lat);
            System.out.println("출발지경도 : " + start_lon);
            System.out.println("도착지이름 : " + end_name);
            System.out.println("도착지위도 : " + end_lat);
            System.out.println("도착지경도 : " + end_lon);

            //try - catch 문으로 오류 대비
            try {
                TMapData tmapdata = new TMapData();     //통합검색 POI 데이터를 요청

                TMapPoint startpoint = new TMapPoint(start_lat, start_lon);
                TMapPoint endpoint = new TMapPoint(end_lat, end_lon);

                showMarker1(tMapView, start_name, start_lat, start_lon);
                showMarker2(tMapView, end_name, end_lat, end_lon);

                tmapdata.findPathDataWithType(
                        TMapPathType.PEDESTRIAN_PATH,
                        startpoint, endpoint,
                        new FindPathDataListenerCallback() {
                            @Override
                            public void onFindPathData(TMapPolyLine polyLine) {
                                polyLine.setLineColor(Color.RED);
                                tMapView.addTMapPath(polyLine);

                                double distance = polyLine.getDistance();
                                double time  = distance / 4.0;

                                System.out.println("거리 : " + distance);
                                System.out.println("시간 : " + time);

                            }
                        });

            } catch (Exception e) {
                Toast.makeText(this, "오류가 발생하였습니다!", Toast.LENGTH_LONG).show();
                System.out.println(e);
            }
        }

        else {
            Toast.makeText(this, "출발지 또는 목적지를 설정해주세요!", Toast.LENGTH_LONG).show();
        }

    }//navigation

    //원 그리기
    //double
    public void drawCircle(TMapView tmapView, double lat, double lon, String address){

        //포인트 설정하기
        TMapPoint tpoint = new TMapPoint(lat, lon);

        TMapCircle tcircle = new TMapCircle();

        tcircle.setCenterPoint(tpoint);  //포인트 변수가 들어가야 함 (TMapPoint)
        tcircle.setRadius(50);
        tcircle.setAreaColor(Color.BLUE);
        tcircle.setLineColor(Color.BLUE);
        tcircle.setCircleWidth(2);
        tcircle.setAreaAlpha(50);
        tcircle.setLineAlpha(0);
        tcircle.setRadiusVisible(true);

        tmapView.addTMapCircle(address, tcircle);
    }//drawCircle

    //검색기능
    public void search_start(final TMapView tmapView){

        tmapView.removeAllMarkerItem();            //이전에 있던 마커들 모두 지우기

        //출발지를 설정했다면 다시 그려주기
        if(start_name != null){
            showMarker1(tmapView, start_name, start_lat, start_lon);
        }
        //도착지를 설정했다면 다시 그려주기
        if(end_name != null){
            showMarker2(tmapView, end_name, end_lat, end_lon);
        }

        String search_name = null;

        System.out.println(start_edit);

        //검색어가 있을 경우 text 가져오기
        if(start_edit.getText().length() != 0){
            search_name = start_edit.getText().toString();
        }

        System.out.println("검색어 : " + start_edit.getText());

        //검색어 없을 경우
        if(search_name == null){
            Toast.makeText(this, "검색어를 입력해주세요!", Toast.LENGTH_LONG).show();
            System.out.println("검색어 없음");
        }

        //검색어 있을 경우
        else {
            Toast.makeText(this, "검색 중...", Toast.LENGTH_LONG).show();

            //try - catch 문으로 오류 대비
            try {
                TMapData tmapdata = new TMapData();     //통합검색 POI 데이터를 요청
                final ArrayList<TMapPOIItem> poiItems;

                tmapView.setZoomLevel(16);

                //통합검색 POI데이터를 검색개수만큼 요청!
                tmapdata.findAllPOI(search_name, 20, new FindAllPOIListenerCallback() {

                    @Override
                    public void onFindAllPOI(ArrayList<TMapPOIItem> poiItem) {

                        Looper.prepare();

                        if (poiItem.size() != 0) {
                            System.out.println("첫 번째 아이템 : " + poiItem.get(0));

                            for (int i = 0; i < poiItem.size(); i++) {

                                TMapPOIItem item = poiItem.get(i);

                                String name = item.getPOIName();
                                double lat = item.getPOIPoint().getLatitude();
                                double lon = item.getPOIPoint().getLongitude();

                                System.out.println("POI Name : " + name + "," +
                                        "POI Address : " + item.getPOIAddress().replace("null", "") + "," +
                                        "POI Point : " + lat + ", " + lon);

                                showMarker1(tmapView, name, lat, lon);        //마커 띄워주기
                            }   //for문

                            //제일 먼저 검색되는 결과 중심점으로 설정
                            tmapView.setCenterPoint(poiItem.get(0).getPOIPoint().getLongitude(), poiItem.get(0).getPOIPoint().getLatitude(), true);
                            Toast.makeText(naviActivity.this, "검색 완료", Toast.LENGTH_LONG).show();
                        }   //if문

                        else {
                            Toast.makeText(naviActivity.this, "검색 결과 없음", Toast.LENGTH_LONG).show();
                        }
                        Looper.loop();
                    }   //findAllPOI
                });

            } catch (Exception e) {
                Toast.makeText(this, "오류가 발생하였습니다!", Toast.LENGTH_LONG).show();
                System.out.println(e);
            }
        }//else
    }//search_start

    //검색기능
    public void search_end(final TMapView tmapView){

        tmapView.removeAllMarkerItem();            //이전에 있던 마커들 모두 지우기

        //출발지를 설정했다면 다시 그려주기
        if(start_name != null){
            showMarker1(tmapView, start_name, start_lat, start_lon);
        }
        //도착지를 설정했다면 다시 그려주기
        if(end_name != null){
            showMarker2(tmapView, end_name, end_lat, end_lon);
        }

        String search_name = null;

        //검색어가 있을 경우 text 가져오기
        if(end_edit.getText().length() != 0){
            search_name = end_edit.getText().toString();
        }

        System.out.println("검색어 : " + search_name);

        //검색어 없을 경우
        if(search_name == null){
            Toast.makeText(this, "검색어를 입력해주세요!", Toast.LENGTH_LONG).show();
            System.out.println("검색어 없음");
        }

        //검색어 있을 경우
        else {
            Toast.makeText(this, "검색 중...", Toast.LENGTH_LONG).show();

            //try - catch 문으로 오류 대비
            try {
                TMapData tmapdata = new TMapData();     //통합검색 POI 데이터를 요청
                final ArrayList<TMapPOIItem> poiItems;

                tmapView.setZoomLevel(16);

                //통합검색 POI데이터를 검색개수만큼 요청!
                tmapdata.findAllPOI(search_name, 20, new FindAllPOIListenerCallback() {

                    @Override
                    public void onFindAllPOI(ArrayList<TMapPOIItem> poiItem) {

                        Looper.prepare();

                        if (poiItem.size() != 0) {
                            System.out.println("첫 번째 아이템 : " + poiItem.get(0));

                            for (int i = 0; i < poiItem.size(); i++) {

                                TMapPOIItem item = poiItem.get(i);

                                String name = item.getPOIName();
                                double lat = item.getPOIPoint().getLatitude();
                                double lon = item.getPOIPoint().getLongitude();

                                System.out.println("POI Name : " + name + "," +
                                        "POI Address : " + item.getPOIAddress().replace("null", "") + "," +
                                        "POI Point : " + lat + ", " + lon);

                                showMarker2(tmapView, name, lat, lon);        //마커 띄워주기
                            }   //for문

                            //제일 먼저 검색되는 결과 중심점으로 설정
                            tmapView.setCenterPoint(poiItem.get(0).getPOIPoint().getLongitude(), poiItem.get(0).getPOIPoint().getLatitude(), true);
                            Toast.makeText(naviActivity.this, "검색 완료", Toast.LENGTH_LONG).show();
                        }   //if문

                        else {
                            Toast.makeText(naviActivity.this, "검색 결과 없음", Toast.LENGTH_LONG).show();
                        }
                        Looper.loop();
                    }   //findAllPOI
                });

            } catch (Exception e) {
                Toast.makeText(this, "오류가 발생하였습니다!", Toast.LENGTH_LONG).show();
                System.out.println(e);
            }
        }//else
    }//search_end

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onCalloutRightButton(TMapMarkerItem markerItem){

    }

    @Override
    public void onBackPressed(){
        Toast.makeText(this, "뒤로가기 버튼을 누르셨습니다.", Toast.LENGTH_LONG).show();
        mTimer.cancel();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy(){
        mTimer.cancel();
        super.onDestroy();
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item){
        //Handle navigation view item clicks here.

        int id = item.getItemId();

        if(id == R.id.search){
            System.out.println("testSEARCH");
            Intent searchIntent = new Intent(getApplicationContext(), MapActivity.class);
            startActivity(searchIntent);
        }

        else if(id==R.id.navi){
            System.out.println("testNAVI");
            Intent naviIntent = new Intent(getApplicationContext(), naviActivity.class);
            startActivity(naviIntent);
        }

        //얘네 어떻게 해야될지 고민중....ㅠㅠ
        else if(id==R.id.police_station){
            //police_station();
        }

        else if(id==R.id.fire_station){
            //fire_station();
        }


        return true;
    }

}
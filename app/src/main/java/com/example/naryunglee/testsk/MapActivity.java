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
import android.support.v4.view.GravityCompat;
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
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

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

public class MapActivity extends Activity implements  onLocationChangedCallback, LocationListener, TMapView.OnCalloutRightButtonClickCallback, NavigationView.OnNavigationItemSelectedListener {

    private Coordinate coordinate;
    private NetworkService api;
    private List<CCTV> cctvs;   // cctv 가져온 정보 저장할 List
    private LocationManager locationManager;
    private Context mContext;
    private Toolbar toolbar;

    ImageView menu_button;
    ImageView backimg;
    EditText search_p;          //검색어 텍스트
    Button search_button;       //검색 버튼
    ImageView compass_button;
    ImageView current_button;      //현재위치 버튼
    Button cctv;       //cctv 보기 버튼
    Button navi_button;         //길찾기 버튼
    LinearLayout navi_time;          //길찾기 시간 레이아웃
    TextView time_info;

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
        setContentView(R.layout.map_activity_main);
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

        search_p = (EditText) findViewById(R.id.search_p);
        search_button = (Button) findViewById(R.id.search_button);
        compass_button = (ImageView) findViewById(R.id.compass_button);
        current_button = (ImageView) findViewById(R.id.current_button);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

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
                Toast.makeText(this, "인터넷을 연결해주세요", Toast.LENGTH_LONG).show();
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
            TMapGpsManager gps = new TMapGpsManager(MapActivity.this);
            gps.setMinTime(1000);       //위치변경 인식 최소시간을 설정
            gps.setMinDistance(5);      //위치변경 인식 최소거리를 설정
            gps.setProvider(gps.GPS_PROVIDER);  //위치탐색 타입을 설정(GPS_PROVIDER는 위성기반의 위치탐색)
            gps.setLocationCallback();  //현재 위치상태 변경시 호출되는 콜백인터페이스 설정하고 성공여부를 반환
            gps.OpenGps();

            if(current_lon!=0 && current_lat!=0){
                tmapview.setLocationPoint(current_lon, current_lat);
            }

            //검색
            search_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    search(tmapview);
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

                        Toast.makeText(MapActivity.this, "현재위치 잡는중", Toast.LENGTH_LONG).show();

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
                                        Toast.makeText(MapActivity.this, "오류가 발생하였습니다!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            };

                            mTimer = new Timer();

                            mTimer.schedule(task, 1000, 1000);
                        }

                        else{
                            Toast.makeText(MapActivity.this, "아직 현재위치가 잡히지 않았습니다.\n다시 시도해주세요!", Toast.LENGTH_LONG).show();
                        }
                    }//if(current_count==false)

                    //현재위치 버튼 켜져있는거 끄려고 할 때
                    else{
                        mTimer.cancel();    //타이머 취소
                        mTimer = null;     //타이머 초기화

                        Toast.makeText(MapActivity.this, "현재위치 모드를 종료합니다!", Toast.LENGTH_LONG).show();

                        tmapview.removeMarkerItem("current_point"); //마커 삭제
                        tmapview.setTrackingMode(false);    //트래킹 없애기
                        tmapview.setSightVisible(false);     //시야 삭제

                        current_count = false;              //버튼 꺼져있는 걸로 설정하기
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

                        showMarker(tmapview , point_address , point.getLatitude(), point.getLongitude(), 3);
                    }
                    catch(Exception e){
                        System.out.println("오류발생 : " + e);
                        Toast.makeText(MapActivity.this, "오류가 발생하였습니다!", Toast.LENGTH_LONG).show();
                    }

                }
            });
        }

        //권한 거부했을때
        else{
            //거부 다음 동작 정의해야
            ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
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
    public void showMarker(final TMapView tmapView, final String name,  final double lat, final double lon, int flag){

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

        //회색 깃발 마크
        if(flag==3){
            Bitmap current_flag = BitmapFactory.decodeResource(getResources(), R.drawable.red_marker);
            tItem.setIcon(current_flag);
        }

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

                final CharSequence[] items = {"즐겨찾기 등록"};

                AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);

                builder.setTitle(getName)
                        .setItems(items, new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which){

                                if(which == 0){
                                    System.out.println("확인 2 : 즐겨찾기 설정함");
                                }
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

    }//showMarker

    //검색기능
    public void search(final TMapView tmapView){

        tmapView.removeAllMarkerItem();            //이전에 있던 마커들 모두 지우기

        String search_name = null;

        //검색어가 있을 경우 text 가져오기
        if(search_p.getText().length() != 0){
            search_name = search_p.getText().toString();
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

                                showMarker(tmapView, name, lat, lon, 3);        //마커 띄워주기
                            }   //for문

                            //제일 먼저 검색되는 결과 중심점으로 설정
                            tmapView.setCenterPoint(poiItem.get(0).getPOIPoint().getLongitude(), poiItem.get(0).getPOIPoint().getLatitude(), true);
                            Toast.makeText(MapActivity.this, "검색 완료", Toast.LENGTH_LONG).show();
                        }   //if문

                        else {
                            Toast.makeText(MapActivity.this, "검색 결과 없음", Toast.LENGTH_LONG).show();
                        }
                        Looper.loop();
                    }   //findAllPOI
                });

            } catch (Exception e) {
                Toast.makeText(this, "오류가 발생하였습니다!", Toast.LENGTH_LONG).show();
                System.out.println(e);
            }
        }//else
    }//search

    /*
    //경찰서
    public void police_station(){
        Toast.makeText(this, "근처 경찰서 검색 중..", Toast.LENGTH_LONG).show();

        //try - catch 문으로 오류 대비
        try {
            TMapData tmapdata = new TMapData();     //통합검색 POI 데이터를 요청
            final ArrayList<TMapPOIItem> poiItems;

            tmapview.setZoomLevel(16);

            TMapPoint current_point = new TMapPoint(current_lat, current_lon);

            //통합검색 POI데이터를 검색개수만큼 요청!
            tmapdata.findAroundNamePOI(current_point, "경찰서", 3, 100,
                    new TMapData.FindAroundNamePOIListenerCallback() {

                @Override
                public void onFindAroundNamePOI(ArrayList<TMapPOIItem> poiItem) {

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

                            showMarker(tmapview, name, lat, lon, 3);        //마커 띄워주기
                        }   //for문

                        //tmapView.setLocationPoint(poiItem.get(0).getPOIPoint().getLatitude(), poiItem.get(0).getPOIPoint().getLongitude());   //제일 먼저 검색되는 결과 중심점으로 설정
                        tmapview.setCenterPoint(poiItem.get(0).getPOIPoint().getLongitude(), poiItem.get(0).getPOIPoint().getLatitude(), true);
                        Toast.makeText(MapActivity.this, "검색 완료", Toast.LENGTH_LONG).show();
                    }   //if문

                    else {
                        Toast.makeText(MapActivity.this, "검색 결과 없음", Toast.LENGTH_LONG).show();
                    }
                    Looper.loop();
                }   //findAllPOI
            });

        } catch (Exception e) {
            Toast.makeText(this, "오류가 발생하였습니다!", Toast.LENGTH_LONG).show();
            System.out.println(e);
        }
    }

    //소방서
    //경찰서
    public void fire_station(){
        Toast.makeText(this, "근처 소방서 검색 중...", Toast.LENGTH_LONG).show();

        //try - catch 문으로 오류 대비
        try {
            TMapData tmapdata = new TMapData();     //통합검색 POI 데이터를 요청
            final ArrayList<TMapPOIItem> poiItems;

            tmapview.setZoomLevel(16);

            TMapPoint current_point = new TMapPoint(current_lat, current_lon);

            //통합검색 POI데이터를 검색개수만큼 요청!
            tmapdata.findAroundNamePOI(current_point, "소방서", 3, 100,
                    new TMapData.FindAroundNamePOIListenerCallback() {

                        @Override
                        public void onFindAroundNamePOI(ArrayList<TMapPOIItem> poiItem) {

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

                                    showMarker(tmapview, name, lat, lon, 3);        //마커 띄워주기
                                }   //for문

                                //tmapView.setLocationPoint(poiItem.get(0).getPOIPoint().getLatitude(), poiItem.get(0).getPOIPoint().getLongitude());   //제일 먼저 검색되는 결과 중심점으로 설정
                                tmapview.setCenterPoint(poiItem.get(0).getPOIPoint().getLongitude(), poiItem.get(0).getPOIPoint().getLatitude(), true);
                                Toast.makeText(MapActivity.this, "검색 완료", Toast.LENGTH_LONG).show();
                            }   //if문

                            else {
                                Toast.makeText(MapActivity.this, "검색 결과 없음", Toast.LENGTH_LONG).show();
                            }
                            Looper.loop();
                        }   //findAllPOI
                    });

        } catch (Exception e) {
            Toast.makeText(this, "오류가 발생하였습니다!", Toast.LENGTH_LONG).show();
            System.out.println(e);
        }
    }
    */

    // 미래의 특정시간에 경로탐색 결과를 가져오는 함수인데 아직 활용 못함...
    public void pathInfo(){

        TMapData tmapdata = new TMapData();

        HashMap<String ,String> pathInfo = new HashMap<String, String>();
        pathInfo.put("rStName", start_name);
        pathInfo.put("rStlat", Double.toString(start_lat));
        pathInfo.put("rStlon", Double.toString(start_lon));
        pathInfo.put("rGoName", end_name);
        pathInfo.put("rGolat", Double.toString(end_lat));
        pathInfo.put("rGolon", Double.toString(end_lon));
        pathInfo.put("type", "arrival");        //경로 서비스 구분(departure, arrival)
        Date currentTime = new Date();             //길 안내의 기준이 되는 출발 혹은 도착시간

        tmapdata.findTimeMachineCarPath(pathInfo, currentTime, null);   //null에는 원래 경유지 위도경도 좌표 넣어야 함

        new TMapData.FindTimeMachineCarPathListenerCallback(){

            @Override
            public void onFindTimeMachineCarPath(Document doc){

            }
        };
    }

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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer.isDrawerOpen(Gravity.RIGHT)) {
            drawer.closeDrawer(Gravity.RIGHT);
        }
        else {
            Toast.makeText(this, "안심지도를 종료합니다", Toast.LENGTH_LONG).show();
            mTimer.cancel();
            super.onBackPressed();
        }
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

        System.out.println("Selected Item : " + id);

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
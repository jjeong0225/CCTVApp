package com.example.naryunglee.testsk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static String OAUTH_CLIENT_ID = "lnbAD6zoiDsp9qMHth3t";  // 1)에서 받아온 값들을 넣어좁니다
    private static String OAUTH_CLIENT_SECRET = "ZkLr7VB2xj";
    private static String OAUTH_CLIENT_NAME = "Ddbddb";
    protected static OAuthLogin mOAuthLoginInstance;
    protected static Context mContext;
    SharedPreferences login_email;
    SharedPreferences.Editor editor;

    private NetworkService networkService;

    String email = "test";
    String nickname = "";
    String enc_id = "";
    String profile_image = "";
    String age = "";
    String gender = "";
    String id = "";
    String name = "";
    String birthday = "";

    String accessToken = "";
    String tokenType;

    static final String BASE = "http://203.249.127.60:65017";
    static String show_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();

        setContentView(R.layout.activity_main);
        mOAuthLoginInstance = OAuthLogin.getInstance();
        mOAuthLoginInstance.init(mContext, OAUTH_CLIENT_ID,OAUTH_CLIENT_SECRET, OAUTH_CLIENT_NAME);
        initSetting();

        /*
        // ip, port 연결
        ApplicationController application = ApplicationController.getInstance();
        //ApplicationController application = new ApplicationController();
        application.buildNetworkService("192.168.20.128", 65010);
        networkService = ApplicationController.getInstance().getNetworkService();
        */

        // 뚜벅뚜벅 앱 로그인 여부
        //SharedPreferences login_email = getSharedPreferences("login_email", MODE_PRIVATE);
        login_email = getSharedPreferences("login_email", MODE_PRIVATE);
        String loginCheck = login_email.getString("loginEmail", null);


        if( loginCheck != null) {
            Log.i("로그인 여부 아이디:", loginCheck);
            final Intent goMain_intent = new Intent(getApplicationContext(), ddbActivity.class);
            finish();
            startActivity(goMain_intent);
        }
    }

    private void initSetting() {
        OAuthLoginButton btn_naver = (OAuthLoginButton) findViewById(R.id.buttonOAuthLoginImg);
        btn_naver.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mOAuthLoginInstance.startOauthLoginActivity(MainActivity.this,
                        mOAuthLoginHandler);
            }
        });


        Button ddb_btn = (Button) findViewById(R.id.button4);
        ddb_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent goMain_intent = new Intent(getApplicationContext(), ddbActivity.class);
                startActivity(goMain_intent);
            }
        });
    }

    private OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
        @Override
        public void run(boolean success) {
            if (success) {
                accessToken = mOAuthLoginInstance.getAccessToken(mContext);
                String refreshToken = mOAuthLoginInstance
                        .getRefreshToken(mContext);
                long expiresAt = mOAuthLoginInstance.getExpiresAt(mContext);
                tokenType = mOAuthLoginInstance.getTokenType(mContext);

                Log.d("myLog", "accessToken  " + accessToken);
                Log.d("myLog", "refreshToken  " + refreshToken);
                Log.d("myLog",
                        "String.valueOf(expiresAt)  "
                                + String.valueOf(expiresAt));
                Log.d("myLog", "tokenType  " + tokenType);
                Log.d("myLog",
                        "mOAuthLoginInstance.getState(mContext).toString()  "
                                + mOAuthLoginInstance.getState(mContext)
                                .toString());

                new RequestApiTask().execute(); //로그인이 성공하면  네이버에 계정값들을 가져온다.

                /*
                //서버에 보낼 객체 생성 및 값 저장
                final Membership membership = new Membership();
                membership.setMname(name);
                membership.setMemail(email);
                membership.setMnickname(nickname);

                String memberInfo = membership.getMemail() + ',' + membership.getMname() + ',' + membership.getMnickname();

                Log.d("myLog", "회원정보:" + memberInfo);

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BASE)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                NetworkService service = retrofit.create(NetworkService.class);
                Call<List<Membership>> call = service.insert_member(memberInfo);
                //call.enqueue(memberships);


                retrofit2.Callback<List<Membership>> memberships = new retrofit2.Callback<List<Membership>>(){

                    @Override
                    public void onResponse(Call<List<Membership>> call, Response<List<Membership>> response) {
                        if (response.isSuccessful()) {
                            List<Membership> memberships = response.body();
                            //Log.i("서버로부터받음1: ", String.valueOf(memberships));
                            //StringBuilder builder = new StringBuilder();
                            //for (Membership membership: memberships) {
                            //    builder.append(membership.toString()+"\n");
                            //}
                            //Log.i("서버로부터받음2: ", builder.toString());

                            String show_txt = "";
                            for(Membership membership : memberships) {
                                String member_email = membership.getMemail();
                                show_txt = membership.getCode();
                                show_email = membership.getMemail();
                            }
                            Log.i("서버가 보낸 코드: ", show_txt);

                            mOAuthLoginInstance.logout(mContext);

                            //Toast.makeText(getBaseContext(), "등록되었습니다", Toast.LENGTH_SHORT).show();

                            if( show_email == "" ) {
                                Toast.makeText(getBaseContext(), "다시 로그인", Toast.LENGTH_SHORT).show();
                                final Intent remain_intent = new Intent(getApplicationContext(), MainActivity.class);
                                finish();
                                startActivity(remain_intent);
                            } else {
                                if( "join".equals(show_txt) ) {
                                    Log.i("조인테스트!!!!!", "!!");
                                    final Intent goNonmember_intent = new Intent(getApplicationContext(), nonmemberActivity.class);
                                    startActivity(goNonmember_intent);
                                } else if ( "login".equals(show_txt) ) {

                                    // 뚜벅뚜벅 앱 로그인
                                    // SharedPreferences login_email = getSharedPreferences("login_email", MODE_PRIVATE);
                                    login_email = getSharedPreferences("login_email", MODE_PRIVATE);
                                    // SharedPreferences.Editor editor = login_email.edit();
                                    editor = login_email.edit();
                                    editor.putString("loginEmail", show_email); // loginEmail라는 key값으로 사용자 email 데이터를 저장
                                    editor.commit(); // 완료

                                    final Intent goMain_intent = new Intent(getApplicationContext(), ddbActivity.class);
                                    startActivity(goMain_intent);
                                }
                            }

                        } else
                        {
                            int statusCode = response.code();
                            Log.i("MyTag", "응답코드 : " + statusCode);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Membership>> call, Throwable t) {
                        Log.i("MyTag", "서버 onFailure 에러내용 : " + t.getMessage());
                    }
                };

                call.enqueue(memberships);

                //final Intent goMain_intent = new Intent(getApplicationContext(), ddbActivity.class);
                //startActivity(goMain_intent);
                */

            } else {
                String errorCode = mOAuthLoginInstance.getLastErrorCode(
                        mContext).getCode();
                String errorDesc = mOAuthLoginInstance
                        .getLastErrorDesc(mContext);
                // Toast.makeText(mContext, "errorCode:" + errorCode +
                // ", errorDesc:" + errorDesc, Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, "로그인이 취소/실패 하였습니다.!",
                        Toast.LENGTH_SHORT).show();
            }
        };
    };

    private class RequestApiTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(Void... params) {
            String url = "https://openapi.naver.com/v1/nid/getUserProfile.xml";
            String at = mOAuthLoginInstance.getAccessToken(mContext);
            Pasingversiondata(mOAuthLoginInstance.requestApi(mContext, at, url));

            return null;
        }

        protected void onPostExecute(Void content) {
            //Log.d("myLog", "email " + email);
            //Log.d("myLog", "name " + name);
            //Log.d("myLog", "nickname " + nickname);

            if (email == null) {
                Toast.makeText(MainActivity.this,"로그인 실패하였습니다. 잠시후 다시 시도해 주세요!!",Toast.LENGTH_SHORT).show();
            } else {

            }
        }

        private void Pasingversiondata(String data) { // xml 파싱
            String f_array[] = new String[9];

            try {
                XmlPullParserFactory parserCreator = XmlPullParserFactory
                        .newInstance();
                XmlPullParser parser = parserCreator.newPullParser();
                InputStream input = new ByteArrayInputStream(
                        data.getBytes("UTF-8"));
                parser.setInput(input, "UTF-8");

                int parserEvent = parser.getEventType();
                String tag;
                boolean inText = false;
                boolean lastMatTag = false;
                int colIdx = 0;

                while (parserEvent != XmlPullParser.END_DOCUMENT) {
                    switch (parserEvent) {
                        case XmlPullParser.START_TAG:
                            tag = parser.getName();
                            if (tag.compareTo("xml") == 0) {
                                inText = false;
                            } else if (tag.compareTo("data") == 0) {
                                inText = false;
                            } else if (tag.compareTo("result") == 0) {
                                inText = false;
                            } else if (tag.compareTo("resultcode") == 0) {
                                inText = false;
                            } else if (tag.compareTo("message") == 0) {
                                inText = false;
                            } else if (tag.compareTo("response") == 0) {
                                inText = false;
                            } else {
                                inText = true;
                            }
                            break;
                        case XmlPullParser.TEXT:
                            tag = parser.getName();
                            if (inText) {
                                if (parser.getText() == null) {
                                    f_array[colIdx] = "";
                                } else {
                                    f_array[colIdx] = parser.getText().trim();
                                }
                                colIdx++;
                            }
                            inText = false;
                            break;
                        case XmlPullParser.END_TAG:
                            tag = parser.getName();
                            inText = false;
                            break;
                    }
                    parserEvent = parser.next();
                }
            } catch (Exception e) {
                Log.e("dd", "Error in network call", e);
            }

            email = f_array[0];
            nickname = f_array[1];
            enc_id = f_array[2];
            profile_image = f_array[3];
            age = f_array[4];
            gender = f_array[5];
            id = f_array[6];
            name = f_array[7];
            birthday = f_array[8];




            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //서버에 보낼 객체 생성 및 값 저장
            final Membership membership = new Membership();
            membership.setMname(name);
            membership.setMemail(email);
            membership.setMnickname(nickname);

            String memberInfo = membership.getMemail() + ',' + membership.getMname() + ',' + membership.getMnickname();

            Log.d("myLog", "회원정보:" + memberInfo);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            NetworkService service = retrofit.create(NetworkService.class);
            Call<List<Membership>> call = service.insert_member(memberInfo);
            //call.enqueue(memberships);


            retrofit2.Callback<List<Membership>> memberships = new retrofit2.Callback<List<Membership>>(){

                @Override
                public void onResponse(Call<List<Membership>> call, Response<List<Membership>> response) {
                    if (response.isSuccessful()) {
                        List<Membership> memberships = response.body();
                        //Log.i("서버로부터받음1: ", String.valueOf(memberships));
                        //StringBuilder builder = new StringBuilder();
                        //for (Membership membership: memberships) {
                        //    builder.append(membership.toString()+"\n");
                        //}
                        //Log.i("서버로부터받음2: ", builder.toString());

                        String show_txt = "";
                        for(Membership membership : memberships) {
                            String member_email = membership.getMemail();
                            show_txt = membership.getCode();
                            show_email = membership.getMemail();
                        }
                        Log.i("서버가 보낸 코드: ", show_txt);

                        mOAuthLoginInstance.logout(mContext);

                        //Toast.makeText(getBaseContext(), "등록되었습니다", Toast.LENGTH_SHORT).show();

                        if( show_email == "" ) {
                            Toast.makeText(getBaseContext(), "다시 로그인", Toast.LENGTH_SHORT).show();
                            final Intent remain_intent = new Intent(getApplicationContext(), MainActivity.class);
                            finish();
                            startActivity(remain_intent);
                        } else {
                            if( "join".equals(show_txt) ) {
                                Log.i("조인테스트!!!!!", "!!");
                                final Intent goNonmember_intent = new Intent(getApplicationContext(), nonmemberActivity.class);
                                startActivity(goNonmember_intent);
                            } else if ( "login".equals(show_txt) ) {

                                // 뚜벅뚜벅 앱 로그인
                                // SharedPreferences login_email = getSharedPreferences("login_email", MODE_PRIVATE);
                                login_email = getSharedPreferences("login_email", MODE_PRIVATE);
                                // SharedPreferences.Editor editor = login_email.edit();
                                editor = login_email.edit();
                                editor.putString("loginEmail", show_email); // loginEmail라는 key값으로 사용자 email 데이터를 저장
                                editor.commit(); // 완료

                                final Intent goMain_intent = new Intent(getApplicationContext(), ddbActivity.class);
                                startActivity(goMain_intent);
                            }
                        }

                    } else
                    {
                        int statusCode = response.code();
                        Log.i("MyTag", "응답코드 : " + statusCode);
                    }
                }

                @Override
                public void onFailure(Call<List<Membership>> call, Throwable t) {
                    Log.i("MyTag", "서버 onFailure 에러내용 : " + t.getMessage());
                }
            };

            call.enqueue(memberships);

            //final Intent goMain_intent = new Intent(getApplicationContext(), ddbActivity.class);
            //startActivity(goMain_intent);

        }
    }
}
package com.example.naryunglee.testsk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by leejiyoung on 2016-11-03.
 */
public class menuActivity extends Activity {
    protected static Context mContext;
    private static final int LEAVE_YES_NO_MESSAGE = 1;
    static final String BASE = "http://203.249.127.60:65017";
    SharedPreferences login_email;

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case LEAVE_YES_NO_MESSAGE:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("회원 탈퇴")
                        .setMessage("정말 탈퇴하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("아니요",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.cancel();
                                    }
                                })
                        .setNegativeButton("네",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {

                                        // 사용자 정보 삭제하는 것을 넣어야 됨.
                                        Retrofit retrofit = new Retrofit.Builder()
                                                .baseUrl(BASE)
                                                .addConverterFactory(GsonConverterFactory.create())
                                                .build();

                                        login_email = getSharedPreferences("login_email", MODE_PRIVATE);
                                        String loginCheck = login_email.getString("loginEmail", null);

                                        NetworkService service = retrofit.create(NetworkService.class);
                                        Call<List<Membership>> call = service.withdraw(loginCheck);

                                        retrofit2.Callback<List<Membership>> memberships = new retrofit2.Callback<List<Membership>>(){

                                            @Override
                                            public void onResponse(Call<List<Membership>> call, Response<List<Membership>> response) {
                                                if (response.isSuccessful()) {

                                                    Toast.makeText(getBaseContext(), "탈퇴되었습니다", Toast.LENGTH_SHORT).show();

                                                    // 뚜벅뚜벅 앱 로그아웃
                                                    SharedPreferences login_email = getSharedPreferences("login_email", MODE_PRIVATE);
                                                    SharedPreferences.Editor editor = login_email.edit();
                                                    editor.remove("loginEmail");
                                                    editor.commit();

                                                    final Intent leave_intent = new Intent(getApplicationContext(), MainActivity.class);
                                                    startActivity(leave_intent);

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

                                    }
                                });
                AlertDialog alert = builder.create();
                return alert;
        }
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        mContext = getApplicationContext();


        // 메인으로 돌아가기
        ImageButton main_btn = (ImageButton) findViewById(R.id.imageButton6);

        main_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //final Intent menu_intent = new Intent(getApplicationContext(), ddbActivity.class);
                finish();
                //startActivity(menu_intent);
            }
        });


        /*
        Button button1 = (Button) findViewById(R.id.button1);

        button1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                final Intent add_member = new Intent(getApplicationContext(), AddMember.class);
                startActivity(add_member);
            }
        });
        */


        // 긴급 연락처 수정
        Button egphone_btn = (Button) findViewById(R.id.button1);

        egphone_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent egphone_intent = new Intent(getApplicationContext(), emergencyphoneActivity.class);
                startActivity(egphone_intent);
            }
        });



        // 로그아웃
        Button logout_btn = (Button) findViewById(R.id.logoutbutton2);

        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent logout_intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(logout_intent);

                // 뚜벅뚜벅 앱 로그아웃
                SharedPreferences login_email = getSharedPreferences("login_email", MODE_PRIVATE);
                SharedPreferences.Editor editor = login_email.edit();
                editor.remove("loginEmail");
                editor.commit();

                Toast logouttoast = Toast.makeText(mContext, "로그아웃 되었습니다", Toast.LENGTH_SHORT);
                logouttoast.show();
            }
        });


        // 서비스 탈퇴
        Button leave_btn = (Button) findViewById(R.id.button3);
        leave_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(LEAVE_YES_NO_MESSAGE);
            }
        });

        //SOS 문자 설정
        Button msg_btn = (Button) findViewById(R.id.button4);
        msg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("test");

                final Intent msg_intent = new Intent(getApplicationContext(), ModifyMsg.class);
                startActivity(msg_intent);
            }
        });

        /*
        // 이용 안내
        Button guide_btn = (Button) findViewById(R.id.button5);

        guide_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent guide_intent = new Intent(getApplicationContext(), guideActivity.class);
                startActivity(guide_intent);
            }
        });


        // 버전 정보
        Button version_btn = (Button) findViewById(R.id.button6);

        version_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent version_intent = new Intent(getApplicationContext(), versionActivity.class);
                startActivity(version_intent);
            }
        });
        */


        // 문의하기
        Button inquiry_btn = (Button) findViewById(R.id.button7);

        inquiry_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent inquiry_intent = new Intent(getApplicationContext(), inquiryActivity.class);
                startActivity(inquiry_intent);
            }
        });

    }
}
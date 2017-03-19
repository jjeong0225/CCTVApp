package com.example.naryunglee.testsk;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by leejiyoung on 2017-01-21.
 */

public class emergencyphoneActivity extends Activity {
    static String show_number, show_number2, show_number3, show_number4, show_number5;
    static final String BASE = "http://203.249.127.60:65017";
    SharedPreferences login_email;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emergencyphone);

        final EditText emergency_number = (EditText) findViewById(R.id.emergencyNumber);
        final EditText emergency_number2 = (EditText) findViewById(R.id.emergencyNumber2);
        final EditText emergency_number3 = (EditText) findViewById(R.id.emergencyNumber3);
        final EditText emergency_number4 = (EditText) findViewById(R.id.emergencyNumber4);
        final EditText emergency_number5 = (EditText) findViewById(R.id.emergencyNumber5);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        login_email = getSharedPreferences("login_email", MODE_PRIVATE);
        String loginCheck = login_email.getString("loginEmail", null);
        Log.i("emergency 요청 아이디:", loginCheck);

        NetworkService service = retrofit.create(NetworkService.class);
        Call<List<Membership>> call = service.find_number(loginCheck);

        retrofit2.Callback<List<Membership>> memberships = new retrofit2.Callback<List<Membership>>(){

            @Override
            public void onResponse(Call<List<Membership>> call, Response<List<Membership>> response) {
                if (response.isSuccessful()) {
                    List<Membership> memberships = response.body();

                    for(Membership membership : memberships) {
                        show_number = membership.getPHONE();
                        show_number2 = membership.getPHONE2();
                        show_number3 = membership.getPHONE3();
                        show_number4 = membership.getPHONE4();
                        show_number5 = membership.getPHONE5();
                    }
                    Log.i("사용자의 긴급연락처: ", show_number);
                    Log.i("사용자의 긴급연락처: ", show_number2);
                    Log.i("사용자의 긴급연락처: ", show_number3);
                    Log.i("사용자의 긴급연락처: ", show_number4);
                    Log.i("사용자의 긴급연락처: ", show_number5);


                    emergency_number.setText(show_number);
                    emergency_number2.setText(show_number2);
                    emergency_number3.setText(show_number3);
                    emergency_number4.setText(show_number4);
                    emergency_number5.setText(show_number5);

                } else
                {
                    int statusCode = response.code();
                    Log.i("find_number", "응답코드 : " + statusCode);
                }
            }

            @Override
            public void onFailure(Call<List<Membership>> call, Throwable t) {
                Log.i("MyTag", "서버 onFailure 에러내용 : " + t.getMessage());
            }


        };

        call.enqueue(memberships);

        //emergency_number.setHint(show_number);


        // 수정 완료 버튼 정의
        Button modifybtn = (Button) findViewById(R.id.modifyButton);

        // 클릭시 메인으로 이동
        modifybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                login_email = getSharedPreferences("login_email", MODE_PRIVATE);
                String loginCheck = login_email.getString("loginEmail", null);

                String ep_number = loginCheck + ',' + emergency_number.getText().toString() + ',' + emergency_number2.getText().toString() + ',' + emergency_number3.getText().toString() + ',' + emergency_number4.getText().toString() + ',' + emergency_number5.getText().toString();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BASE)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                //Log.i("http연결주소:", BASE);

                NetworkService service = retrofit.create(NetworkService.class);
                Call<List<Membership>> call = service.modify_number(ep_number);

                retrofit2.Callback<List<Membership>> memberships = new retrofit2.Callback<List<Membership>>(){

                    @Override
                    public void onResponse(Call<List<Membership>> call, Response<List<Membership>> response) {
                        if (response.isSuccessful()) {

                            Toast.makeText(getBaseContext(), "수정되었습니다", Toast.LENGTH_SHORT).show();

                            final Intent egphone_intent = new Intent(getApplicationContext(), emergencyphoneActivity.class);
                            finish();
                            startActivity(egphone_intent);

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

    }


}

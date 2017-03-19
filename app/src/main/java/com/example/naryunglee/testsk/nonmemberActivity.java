package com.example.naryunglee.testsk;

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
 * Created by leejiyoung on 2017-01-19.
 */

public class nonmemberActivity extends MainActivity {
    private EditText edit_number, edit_number2, edit_number3, edit_number4, edit_number5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nonmember);

        // 긴급 연락처 저장하고 메인으로 가는 버튼 정의
        Button mainbtn = (Button) findViewById(R.id.mainButton);

        // 클릭시 메인으로 이동
        mainbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                edit_number = (EditText) findViewById(R.id.personNumber);
                edit_number2 = (EditText) findViewById(R.id.personNumber2);
                edit_number3 = (EditText) findViewById(R.id.personNumber3);
                edit_number4 = (EditText) findViewById(R.id.personNumber4);
                edit_number5 = (EditText) findViewById(R.id.personNumber5);
                String number = edit_number.getText().toString();
                String number2 = edit_number2.getText().toString();
                String number3 = edit_number3.getText().toString();
                String number4 = edit_number4.getText().toString();
                String number5 = edit_number5.getText().toString();


                String save_number = show_email + "," + number + "," + number2 + "," + number3 + "," + number4 + "," + number5;



                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BASE)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                //Log.i("http연결주소:", BASE);

                NetworkService service = retrofit.create(NetworkService.class);
                Call<List<Membership>> call = service.insert_phonenumber(save_number);

                retrofit2.Callback<List<Membership>> memberships = new retrofit2.Callback<List<Membership>>(){

                    @Override
                    public void onResponse(Call<List<Membership>> call, Response<List<Membership>> response) {
                        if (response.isSuccessful()) {

                            Toast.makeText(getBaseContext(), "등록되었습니다", Toast.LENGTH_SHORT).show();

                            // 뚜벅뚜벅 앱 로그인
                            SharedPreferences login_email = getSharedPreferences("login_email", MODE_PRIVATE);
                            SharedPreferences.Editor editor = login_email.edit();
                            editor.putString("loginEmail", show_email); // loginEmail라는 key값으로 사용자 email 데이터를 저장
                            editor.commit(); // 완료

                            final Intent goMain_intent = new Intent(getApplicationContext(), ddbActivity.class);
                            finish();
                            startActivity(goMain_intent);

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

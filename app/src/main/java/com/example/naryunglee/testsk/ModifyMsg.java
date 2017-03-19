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
import retrofit2.Callback;
import retrofit2.Response;

/*
 * Created by user on 2017-02-09.
 * SOS 메세지 저장 클래스
 */

public class ModifyMsg extends Activity {

    EditText content;
    Button save;
    private NetworkService api;
    SharedPreferences login_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message);

        ApplicationController application = ApplicationController.getInstance();
        application.buildNetworkService("203.249.127.60", 65017);
        api = ApplicationController.getInstance().getNetworkService();

        System.out.println("수정 화면 들어옴");

        // 로그인한 이메일 정보 가져오기
        login_email = getSharedPreferences("login_email", MODE_PRIVATE);

        content = (EditText) findViewById(R.id.content);
        save = (Button) findViewById(R.id.save);

        Intent intent = getIntent();
        String text = intent.getStringExtra("content");  // 서버에서 가져온 메세지 데이터

        content.setText(text);

        // 메세지 저장 이벤트
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(content.getText() != null){
                    String loginCheck = login_email.getString("loginEmail", null);   // 로그인한 이메일 저장

                    System.out.println("저장 완료");

                    Call<List<Message>> msg = api.saveMessage(loginCheck, content.getText().toString());

                    msg.enqueue(new Callback<List<Message>>() {
                        @Override
                        public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                            if(response.isSuccessful()){
                                Toast.makeText(getApplicationContext(), "저장 되었습니다.", Toast.LENGTH_SHORT).show();
                                final Intent back = new Intent(getApplicationContext(), menuActivity.class);
                                startActivity(back);
                            }
                            else{
                                int statusCode = response.code();
                                Log.i("MyTag", "응답코드 : " + statusCode);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Message>> call, Throwable t) {
                            Log.i("MyTag", "서버 failure 내용: " + t.getMessage());
                            Toast.makeText(getApplicationContext(), "오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else{
                    Toast.makeText(getApplicationContext(), "메세지 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}

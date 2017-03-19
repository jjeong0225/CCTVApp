package com.example.naryunglee.testsk;

import android.app.Activity;
import android.content.Intent;
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

import static com.example.naryunglee.testsk.MainActivity.show_email;

/**
 * Created by leejiyoung on 2016-11-09.
 */
public class inquiryActivity extends Activity {
    private EditText inquiryText;
    static final String BASE = "http://203.249.127.60:65017";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inquiry);

        // 문의하기 버튼 정의
        Button inquirybtn = (Button) findViewById(R.id.inquiryButton);

        inquirybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                inquiryText = (EditText) findViewById(R.id.inquiryText);
                String inquirytext = inquiryText.getText().toString();


                String save_inquirytext = show_email + "," + inquirytext;


                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BASE)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                Log.i("http연결주소:", BASE);

                NetworkService service = retrofit.create(NetworkService.class);
                Call<List<Membership>> call = service.inquiry(save_inquirytext);

                retrofit2.Callback<List<Membership>> memberships = new retrofit2.Callback<List<Membership>>(){

                    @Override
                    public void onResponse(Call<List<Membership>> call, Response<List<Membership>> response) {
                        if (response.isSuccessful()) {

                            Toast.makeText(getBaseContext(), "전달되었습니다", Toast.LENGTH_SHORT).show();

                            final Intent goInquiry_intent = new Intent(getApplicationContext(), inquiryActivity.class);
                            finish();
                            startActivity(goInquiry_intent);

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
```java
package com.example.implicitintent2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button map,google,message,photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void InitializeView(){
        map = (Button) findViewById(R.id.mapBtn);
        google = (Button) findViewById(R.id.googleBtn);
        message = (Button) findViewById(R.id.messageBtn);
        photo = (Button) findViewById(R.id.photoBtn);
    }

    public void clickBtn(View view) {
        // 클릭한 버튼의 id값을 case문과 비교
        switch (view.getId()) {
            case R.id.mapBtn:
//                서울디지텍고등학교의 위치를 지도에 띄어줌
                Uri location = Uri.parse("geo:0,0?q=서울디지텍고등학교");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW,location);
                startActivity(mapIntent);
                break;
            case R.id.googleBtn:
                Intent googleIntent = new Intent(Intent.ACTION_WEB_SEARCH);
                googleIntent.putExtra(SearchManager.QUERY,"서울디지텍고등학교");
                try {
                    startActivity(googleIntent);
                }catch (ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "앱 불러오기에 실패했습니다. 404 ERROR", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.messageBtn:
                Intent msgIntent = new Intent(Intent.ACTION_SENDTO);
                msgIntent.putExtra("sms_body","안녕하세요");
                msgIntent.setData(Uri.parse("smsto:"+Uri.encode("010-2931-2157")));
                startActivity(msgIntent);
                break;


        }
    }

}
```

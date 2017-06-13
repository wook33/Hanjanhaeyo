package wooks.hanjanhaeyo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

public class IntroActivity extends AppCompatActivity
{
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
    
    
        final Handler h = new Handler() {
            @Override
            public void handleMessage(Message msg)
            {
                Intent intent = new Intent(IntroActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        };
        h.sendEmptyMessageDelayed(0, 1000);
    }
}

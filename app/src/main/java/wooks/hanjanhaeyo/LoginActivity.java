package wooks.hanjanhaeyo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity
{
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        
    }
    
    
    public void OnClick_Login(View v)
    {
        EditText etNickname = (EditText)findViewById(R.id.et_nickname);
        String sNickname = etNickname.getText().toString();
        
        // TODO : assert(sNickname)
        // TODO : 금지문자(@, #, %) 사용못하게
        if( sNickname.contains("@")  ||  sNickname.contains("#")  ||  sNickname.contains("%") ) {
            Toast.makeText(this, "잘못된 닉네임!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Runtime.SetNickname(sNickname);
    
    
        Intent intent = new Intent(LoginActivity.this, LobbyActivity.class);
        startActivity(intent);
        finish();
        
    }
}

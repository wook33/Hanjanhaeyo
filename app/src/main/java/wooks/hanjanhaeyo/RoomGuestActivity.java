package wooks.hanjanhaeyo;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

public class RoomGuestActivity extends AppCompatActivity
{
    private SessionEventHandler m_sessionEventHandler;
    
    private TextView m_tvNickname;
    private TextView m_tvPlayerList;
    private Button m_btnGameSelect1;
    private Button m_btnGameSelect2;
    private Button m_btnGameSelect3;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_guest);
    
    
        m_tvNickname = (TextView)findViewById(R.id.tv_nickname);
        m_tvNickname.setText(Runtime.GetNickname());
    
        m_tvPlayerList = (TextView)findViewById(R.id.tv_playerList);
        m_tvPlayerList.setText("접속자 : -");
    
        m_btnGameSelect1 = (Button)findViewById(R.id.btn_gameSelect1);
        m_btnGameSelect2 = (Button)findViewById(R.id.btn_gameSelect2);
        m_btnGameSelect3 = (Button)findViewById(R.id.btn_gameSelect3);
        
        
        
        m_sessionEventHandler = new SessionEventHandler(Looper.getMainLooper());
        GuestWorks.Register_RoomGuestActivityEventHandler(m_sessionEventHandler);
        
        GuestWorks.RequestPlayerList();
        GuestWorks.RequestSelectedGame();
    }
    
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    
        GuestWorks.Register_RoomGuestActivityEventHandler(null);
    }
    
    @Override
    public void onBackPressed()
    {
        GuestWorks.QuitRoom();
        
        Intent intent = new Intent(RoomGuestActivity.this, LobbyActivity.class);
        startActivity(intent);
        finish();
    }
    
    
    
    private void GameSelected(int nGame)
    {
        m_btnGameSelect1.setText("게임1");
        m_btnGameSelect2.setText("게임2");
        m_btnGameSelect3.setText("게임3");
        
        switch(nGame) {
        case 1:
            m_btnGameSelect1.setText("게임1 *");
            break;
        case 2:
            m_btnGameSelect2.setText("게임2 *");
            break;
        case 3:
            m_btnGameSelect3.setText("게임3 *");
            break;
        }
    }
    
    private void GameStarted(int nGame)
    {
        /// 게임 시작
        Intent intent = null;
        switch(nGame) {
        case 1 :
            intent = new Intent(RoomGuestActivity.this, Game1Activity.class);
            break;
        case 2 :
            intent = new Intent(RoomGuestActivity.this, Game2Activity.class);
            break;
        case 3 :
            intent = new Intent(RoomGuestActivity.this, Game3Activity.class);
            break;
        }
        Log.d("QQQQQ", "게임 시작 : " + nGame);
        startActivity(intent);
        finish();
    }
    
    
    
    
    
    class SessionEventHandler extends Handler
    {
        public static final int WHAT_UPDATE_PLAYER_LIST = 0;
        public static final int WHAT_UPDATE_SELECTED_GAME = 1;
        public static final int WHAT_GAMESTART = 2;
        public static final int WHAT_HOST_DISCONNECTED = 3;
        
        
        public SessionEventHandler(Looper looper)
        {
            super(looper);
        }
        
        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what) {
            
            case WHAT_UPDATE_PLAYER_LIST: {
                /// 플레이어 목록 갱신
                String[] sPlayerList = (String[])msg.obj;
                // TODO : 플레이어 리스트뷰 갱신
                String[] playerList = (String[])msg.obj;
                String tmp = "접속자 : ";
                for(int i=0; i < playerList.length; i++)
                    tmp += playerList[i] + ", ";
                if( tmp.endsWith(", ") )
                    tmp = tmp.substring(0, tmp.length()-2);
                m_tvPlayerList.setText(tmp);
                
                Log.d("QQQQQ", "플레이어 리스트 갱신");
            } break;
            
            case WHAT_UPDATE_SELECTED_GAME: {
                /// 선택된 게임 갱신
                int nGame = GuestWorks.GetSelectedGame();
                GameSelected(nGame);
                // TODO : 선택된 게임 표시부 갱신
                Log.d("QQQQQ", "선택된 게임 갱신 : " + nGame);
            } break;
            
            case WHAT_GAMESTART: {
                /// 게임 시작
                int nGame = GuestWorks.GetSelectedGame();
                GameStarted(nGame);
            } break;
            
            case WHAT_HOST_DISCONNECTED: {
                Intent intent = new Intent(RoomGuestActivity.this, LobbyActivity.class);
                startActivity(intent);
                finish();
            } break;
                
            }
        }
    }
    
}

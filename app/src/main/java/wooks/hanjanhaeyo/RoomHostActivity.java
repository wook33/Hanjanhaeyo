package wooks.hanjanhaeyo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class RoomHostActivity extends AppCompatActivity
{
    private SessionEventHandler m_sessionEventHandler;
    
    private boolean m_bKeepBroadcasting;
    
    private TextView m_tvNickname;
    private TextView m_tvPlayerList;
    private Button m_btnGameSelect1;
    private Button m_btnGameSelect2;
    private Button m_btnGameSelect3;
    private Button m_btnGameStart;
    
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_host);
        
        
        
        m_tvNickname = (TextView)findViewById(R.id.tv_nickname);
        m_tvNickname.setText(Runtime.GetNickname());
        
        m_tvPlayerList = (TextView)findViewById(R.id.tv_playerList);
        
        m_btnGameSelect1 = (Button)findViewById(R.id.btn_gameSelect1);
        m_btnGameSelect2 = (Button)findViewById(R.id.btn_gameSelect2);
        m_btnGameSelect3 = (Button)findViewById(R.id.btn_gameSelect3);
        m_btnGameStart = (Button)findViewById(R.id.btn_gameStart);
        
    
    
    
        m_sessionEventHandler = new SessionEventHandler(Looper.getMainLooper());
        HostWorks.Register_RoomHostActivityEventHandler(m_sessionEventHandler);
        
        if( HostWorks.IsOpened() == false ) {
            m_tvPlayerList.setText("접속자 : " + Runtime.GetNickname());
            HostWorks.Open();
        }
        else {
            HostWorks.UpdatePlayerList();
        }
        
        Broadcast();
    }
    
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    
        HostWorks.Register_RoomHostActivityEventHandler(null);
    }
    
    @Override
    public void onBackPressed()
    {
        BroadcastStop();
        
        HostWorks.Close();
    
        Intent intent = new Intent(RoomHostActivity.this, LobbyActivity.class);
        startActivity(intent);
        finish();
    }
    
    
    private void Broadcast()
    {
        m_bKeepBroadcasting = true;
    
        /// 브로드캐스트
        Thread t = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    DatagramSocket udpSocket = new DatagramSocket();
                    InetAddress addr = InetAddress.getByName("192.168.0.255");
                    
                    byte[] buffer = new byte[1024];
                    buffer[0] = 0x11;
                    buffer[1] = 0x22;
                    buffer[2] = 0x33;
                    buffer[3] = 0x44;
                    
                    String sNickname = Runtime.GetNickname();
                    byte[] nicknameBytes = sNickname.getBytes("UTF-8");
                    for(int i=0; i < nicknameBytes.length; i++)
                        buffer[4+i] = nicknameBytes[i];
                    int nMessageLength = 4 + nicknameBytes.length;
                    // TODO : boundary testing
                    
                    DatagramPacket packet = new DatagramPacket(buffer, nMessageLength, addr, Constants.BROADCAST_PORT);
                
                    while(m_bKeepBroadcasting) {
                        try {
                            Log.d("QQQQQ", "브로드캐스팅...");
                            udpSocket.send(packet);
                            Thread.sleep(2000);
                        }
                        catch(Exception ex) {
                            // NOP
                        }
                    }
                }
                catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        t.start();
    }
    
    private void BroadcastStop()
    {
        m_bKeepBroadcasting = false;
    }
    
    
    private void GameSelect(int nGame)
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
        
        HostWorks.GameSelect(nGame);
    }
    
    public void OnClick_GameSelect1(View v)
    {
        GameSelect(1);
    }
    
    public void OnClick_GameSelect2(View v)
    {
        GameSelect(2);
    }
    
    public void OnClick_GameSelect3(View v)
    {
        GameSelect(3);
    }
    
    public void OnClick_GameStart(View v)
    {
        int nSelectedGame = HostWorks.GetSelectedGame();
        int nPlayerCount = HostWorks.GetPlayerCount();
        Intent intent = null;
        
        switch(nSelectedGame) {
        case 0: {
            // TODO : 게임 선택 안했음 팝업
            Toast.makeText(RoomHostActivity.this, "게임이 선택되지 않음!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        case 1: {
            if( nPlayerCount < 2) {
                Toast.makeText(RoomHostActivity.this, "게임을 하기 위한 인원이 부족합니다 (최소 2명 필요)", Toast.LENGTH_SHORT).show();
                return;
            }
            intent = new Intent(RoomHostActivity.this, Game1Activity.class);
        } break;
        
        case 2: {
            if( nPlayerCount < 3) {
                Toast.makeText(RoomHostActivity.this, "게임을 하기 위한 인원이 부족합니다 (최소 3명 필요)", Toast.LENGTH_SHORT).show();
                return;
            }
            intent = new Intent(RoomHostActivity.this, Game2Activity.class);
        } break;
        
        case 3: {
            if( nPlayerCount < 2) {
                Toast.makeText(RoomHostActivity.this, "게임을 하기 위한 인원이 부족합니다 (최소 2명 필요)", Toast.LENGTH_SHORT).show();
                return;
            }
            intent = new Intent(RoomHostActivity.this, Game3Activity.class);
        } break;
        
        }
    
        BroadcastStop();
        HostWorks.GameStart();
        
        Log.d("QQQQQ", "게임 화면으로 이동 : " + nSelectedGame);
        startActivity(intent);
        finish();
    }
    
    
    
    
    
    class SessionEventHandler extends Handler
    {
        public static final int WHAT_UPDATE_PLAYER_LIST = 0;
        
        
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

            }
        }
    }
    
}

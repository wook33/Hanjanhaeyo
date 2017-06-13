package wooks.hanjanhaeyo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.Enumeration;

public class LobbyActivity extends AppCompatActivity
{
    private SessionEventHandler m_sessionEventHandler;
    
    private ListView m_lvHostList;
    private ArrayAdapter<HostInfo> m_lvAdapter;
    
    private boolean m_bKeepSearching;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        
    
        m_lvHostList = (ListView)findViewById(R.id.lv_hostList);
        m_lvHostList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // TODO : 해당 호스트로 접속
                HostInfo item = (HostInfo)m_lvHostList.getItemAtPosition(position);
                Log.d("QQQQQ", "선택한 호스트 : " + item.GetHostname() + " / " + item.GetNickname());
                JoinRoom(item.GetHostname());
            }
        });
        
    
        SearchHost();
    }
    
    private void SearchHost()
    {
        m_bKeepSearching = true;
        m_lvAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        m_lvHostList.setAdapter(m_lvAdapter);
    
        /// 서버의 브로드캐스트 감지
        Thread t = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    DatagramSocket udpSocket = new DatagramSocket(Constants.BROADCAST_PORT);
                    udpSocket.setSoTimeout(2000);
                
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                
                    while(m_bKeepSearching) {
                        try {
                            Log.d("QQQQQ", "검색중...");
                            udpSocket.receive(packet);
                            byte[] message = packet.getData();
                            if(packet.getLength() >= 4 && message[0] == 0x11 && message[1] == 0x22 && message[2] == 0x33 && message[3] == 0x44) {
                                InetSocketAddress hostAddress = (InetSocketAddress) packet.getSocketAddress();
                                byte[] nicknameBytes = new byte[packet.getLength() - 4];
                                for(int i=0; i < packet.getLength()-4; i++)
                                    nicknameBytes[i] = message[4+i];
                                
                                final String sHostname = hostAddress.getHostName();
                                final String sNickname = new String(nicknameBytes, "UTF-8");
                            
                                Log.d("QQQQQ", "검색한 호스트 : " + sHostname + " / " + sNickname);
                                //
                                
                                //
                            
                                /// 리스트 내에 존재하지 않으면 추가
                                boolean bAlreadyContained = false;
                                for(int i=0; i < m_lvAdapter.getCount(); i++) {
                                    HostInfo t = m_lvAdapter.getItem(i);
                                    if( t.GetHostname().equals(sHostname) ) {
                                        bAlreadyContained = true;
                                        break;
                                    }
                                }
                                if( bAlreadyContained == false  &&  m_bKeepSearching ) {
                                    runOnUiThread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            HostInfo hi = new HostInfo(sHostname, sNickname);
                                            m_lvAdapter.add(hi);
                                        }
                                    });
                                }
                                //
                            }
                        }
                        catch(SocketTimeoutException ste) {
                            // NOP
                        }
                        catch(Exception ex) {
                            // NOP
                            ex.printStackTrace();
                            Log.d("QQQQQ", "방 검색 오류");
                        }
                    }
                    udpSocket.close();
                }
                catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        t.start();
    }
    
    
    public void OnClick_CreateRoom(View v)
    {
        m_bKeepSearching = false;
        Runtime.SetHost(true);
    
        m_sessionEventHandler = null;
        GuestWorks.Register_LobbyActivityEventHandler(null);
    
        Intent intent = new Intent(LobbyActivity.this, RoomHostActivity.class);
        startActivity(intent);
        finish();
    }
    
    
    public void JoinRoom(String sHostname)
    {
        m_bKeepSearching = false;
        Runtime.SetHost(false);
    
        m_sessionEventHandler = new SessionEventHandler(Looper.getMainLooper());
        GuestWorks.Register_LobbyActivityEventHandler(m_sessionEventHandler);
        
        GuestWorks.JoinRoom(sHostname);
    }
    
    
    
    class SessionEventHandler extends Handler
    {
        public static final int WHAT_HOST_CONNECT_FAIL = 0;
        public static final int WHAT_JOIN_ROOM_OK = 1;
        public static final int WHAT_JOIN_ROOM_NO = 2;
        public static final int WHAT_HOST_DISCONNECTED = 3;
        
        
        
        public SessionEventHandler(Looper looper)
        {
            super(looper);
        }
        
        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what) {
            
            case WHAT_HOST_CONNECT_FAIL: {
                Log.d("QQQQQ", "룸 입장 실패");
                
                // TODO : 실패 팝업
                Toast.makeText(LobbyActivity.this, "입장 실패!", Toast.LENGTH_SHORT).show();
            } break;
            
            case WHAT_JOIN_ROOM_OK: {
                /// 룸 입장 허가
                // TODO : 룸 화면 이동
                Log.d("QQQQQ", "룸 입장 허가");
    
                Intent intent = new Intent(LobbyActivity.this, RoomGuestActivity.class);
                startActivity(intent);
                finish();
                
            } break;
            
            case WHAT_JOIN_ROOM_NO: {
                /// 룸 입장 금지
                // TODO : 룸 입장 금지
                Log.d("QQQQQ", "룸 입장 금지");
    
                Toast.makeText(LobbyActivity.this, "실패!!!!", Toast.LENGTH_SHORT).show();
            } break;
                
            case WHAT_HOST_DISCONNECTED: {
                Log.d("QQQQQ", "호스트 연결 끊김");
            } break;
            }
        }
    }
}

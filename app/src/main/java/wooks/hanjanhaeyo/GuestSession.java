package wooks.hanjanhaeyo;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by in2etv on 2017-06-09.
 */

public class GuestSession
{
    private IRemoteClosedEventHandler m_evhRemoteClosed;
    private IMessageReceivedEventHandler m_evhMessageReceived;
    
    private int m_nId;
    private Socket m_socket;
    private String m_sNickname;
    private boolean m_bIsPlayer;
    private Object m_csSend = new Object();
    
    private Thread m_receiveThread;
    private boolean m_bKeepReceiving;
    
    
    /**
     * 게스트 측에서 사용하는 경우
     */
    public GuestSession()
    {
        m_nId = 0;
        m_socket = null;
    }
    
    /**
     * 호스트 측에서 accept 시 생성하는 경우
     * @param nId
     * @param socket
     */
    public GuestSession(int nId, Socket socket)
    {
        m_nId = nId;
        m_socket = socket;
    }
    

    public void Register_OnRemoteClosed(IRemoteClosedEventHandler evh)
    {
        m_evhRemoteClosed = evh;
    }
    public void Register_OnMessageReceived(IMessageReceivedEventHandler evh)
    {
        m_evhMessageReceived = evh;
    }
    
    public void SetKeepReceiving(boolean bKeepReceiving)
    {
        m_bKeepReceiving = bKeepReceiving;
    }
    
    public int GetId()
    {
        return m_nId;
    }
    public Socket GetSocket()
    {
        return m_socket;
    }
    public String GetNickname()
    {
        return m_sNickname;
    }
    public void SetNickname(String sNickname)
    {
        m_sNickname = sNickname;
    }
    public boolean IsPlayer()
    {
        return m_bIsPlayer;
    }
    public void SetPlayer(boolean bIsPlayer)
    {
        m_bIsPlayer = bIsPlayer;
    }
    
    
    public boolean Connect(String sHostname, int nPort)
    {
        try {
            m_socket = new Socket(sHostname, nPort);
            
            ReceiveStart();
            
            return true;
        }
        catch(Exception ex) {
            return false;
        }
    }
    
    
    public void Send(String sMessage)
    {
        synchronized(m_csSend) {
            try {
                // TODO : 송신 메시지 큐잉 & 큐에 남은 순으로 꺼내서 송신
    
                Log.d("QQQQQ", "세션 " + m_nId + " | 송신 : " + sMessage);
                
                /// 스트림에 쓰기
                byte[] messageBytes = sMessage.getBytes("UTF-8");
                
                DataOutputStream os = new DataOutputStream(m_socket.getOutputStream());
                os.writeInt(messageBytes.length);
                os.write(messageBytes);
                os.flush();
            }
            catch(Exception ex) {
                ex.printStackTrace();
                Close();
                if( m_evhRemoteClosed != null ) {
                    m_evhRemoteClosed.OnRemoteClosed(GuestSession.this);
                }
            }
        }
    }
    
    
    public void ReceiveStart()
    {
        m_bKeepReceiving = true;
        
        m_receiveThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    DataInputStream is = new DataInputStream(m_socket.getInputStream());
    
                    while(m_bKeepReceiving) {
                        int nLength = is.readInt();
                        
                        byte[] buffer = new byte[nLength];
                        int nReadBytes = is.read(buffer, 0, nLength);
                        if( nReadBytes != nLength ) {
                            // TODO : 부족한만큼 더 읽고 합치기
                            Log.d("QQQQQ", "체크포인트1");
                        }
                        
                        String sMessage = new String(buffer, "UTF-8");
                        Log.d("QQQQQ", "세션 " + m_nId +" | 읽은 메시지 : " + sMessage);
                        if( m_evhMessageReceived != null ) {
                            m_evhMessageReceived.OnMessageReceived(GuestSession.this, sMessage);
                        }
                    }
                }
                catch(EOFException eofe) {
                    Close();
                    if( m_evhRemoteClosed != null ) {
                        m_evhRemoteClosed.OnRemoteClosed(GuestSession.this);
                    }
                }
                /// 호스트 측에서 연결 끊는 경우, 이미 Close가 동작되므로
                catch(NullPointerException npe) {
                    // NOP;
                }
                catch(SocketException se) {
                    Close();
                }
                catch(Exception ex) {
                    ex.printStackTrace();
                    Close();
                    if( m_evhRemoteClosed != null ) {
                        m_evhRemoteClosed.OnRemoteClosed(GuestSession.this);
                    }
                }
            }
        });
        m_receiveThread.start();
    }
    
    public void Close()
    {
        /// 세션 종료
        if( m_socket != null ) {
            if( m_bKeepReceiving )
                m_bKeepReceiving = false;
            try {
                m_socket.close();
            }
            catch(Exception ex) {
                // NOP
            }
            m_socket = null;
        }
    }
}

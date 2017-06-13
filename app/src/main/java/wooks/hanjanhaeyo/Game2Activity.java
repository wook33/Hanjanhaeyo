package wooks.hanjanhaeyo;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Game2Activity extends AppCompatActivity
{
    private SessionEventHandler m_sessionEventHandler;
    
    
    private Button m_btnSelect1;
    private Button m_btnSelect2;
    private TextView m_tvCountdown;
    private TextView m_tvPlayerList;
    private TextView m_tvResult;
    private Button m_btnRetry;
    private Button m_btnReturnToRoom;
    
    
    private boolean m_bSeletable;
    private int m_nSelection = -1;
    private int m_nCountDownSeconds;
    private int m_nShape;
    private String[] m_nicknameList;
    private int m_nMyIdx;
    private int[] m_resultSelect1PlayerList;
    private int[] m_resultSelect2PlayerList;
    private int[] m_resultTimeoutPlayerList;
    private int[] m_resultLoserPlayerList;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game2);
    
        m_btnSelect1 = (Button)findViewById(R.id.btn_select1);
        m_btnSelect2 = (Button)findViewById(R.id.btn_select2);
        m_tvCountdown = (TextView)findViewById(R.id.tv_countdown);
        m_tvPlayerList = (TextView)findViewById(R.id.tv_playerList);
        m_tvResult = (TextView)findViewById(R.id.tv_result);
        m_btnRetry = (Button)findViewById(R.id.btn_retry);
        m_btnReturnToRoom = (Button)findViewById(R.id.btn_returnToRoom);
        
        
    
        m_sessionEventHandler = new SessionEventHandler(Looper.getMainLooper());
        if( Runtime.IsHost() ) {
            HostWorks.Register_Game2ActivityEventHandler(m_sessionEventHandler);
        }
        else {
            GuestWorks.Register_Game2ActivityEventHandler(m_sessionEventHandler);
            GuestWorks.Game2_GuestReady();
        }
    }
    
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    
        if( Runtime.IsHost() ) {
            HostWorks.Register_Game2ActivityEventHandler(null);
        }
        else {
            GuestWorks.Register_Game2ActivityEventHandler(null);
        }
    }
    
    @Override
    public void onBackPressed()
    {
        if( Runtime.IsHost() ) {
            HostWorks.Close();
        }
        else {
            GuestWorks.QuitRoom();
        }
    
        Intent intent = new Intent(Game2Activity.this, LobbyActivity.class);
        startActivity(intent);
        finish();
    }
    
    public void OnClick_Select1(View v)
    {
        if( m_bSeletable ) {
            m_bSeletable = false;
            m_nSelection = 0;
            OnSelected(0);
        }
    }
    public void OnClick_Select2(View v)
    {
        if( m_bSeletable ) {
            m_bSeletable = false;
            m_nSelection = 1;
            OnSelected(1);
        }
    }
    
    public void OnClick_Retry(View v)
    {
        HostWorks.Game2_Retry();
    }
    public void OnClick_ReturnToRoom(View v)
    {
        HostWorks.ReturnToRoom();
    }
    
    
    
    
    
    /**
     * @brief 전체 플레이어 목록을 표시한다
     * @param playerNicknameList 플레이어 닉네임 목록
     */
    private void DisplayPlayerList(String[] playerNicknameList)
    {
        /// TODO : 전체 플레이어의 목록을 표시한다.
        String sPlayerList = "플레이어 목록 : ";
        for(int i=0; i < playerNicknameList.length; i++) {
            sPlayerList += playerNicknameList[i] + (i<(playerNicknameList.length-1)?", ":"");
        }
        m_tvPlayerList.setText(sPlayerList);
        //
    }
    
    /**
     * @brief 두 버튼을 정해진 모양으로 표시한다.
     * @param nShape 버튼의 모양을 나타내는 정수 (0~9)
     */
    private void DisplayShape(int nShape)
    {
        /// nShape 값에 따라 버튼의 모양을 변경한다.
        String[] shapes1 = { "☆", "★", "○", "●", "◇", "◆", "♡", "♥", "♧", "♣" };
        String[] shapes2 = { "★", "☆", "●", "○", "◆", "◇", "♥", "♡", "♣", "♧" };
        m_btnSelect1.setText(shapes1[nShape]);
        m_btnSelect2.setText(shapes2[nShape]);
        //
    }
    
    /**
     * @brief 선택의 시간이 왔도다
     */
    private void OnSelectionTime()
    {
        m_bSeletable = true;
        
        // TODO : 버튼 활성화 등
        /// 카운트다운 시작
        m_nCountDownSeconds = Constants.COUNTDOWN_GAME2;
        m_sessionEventHandler.sendEmptyMessage(SessionEventHandler.WHAT_PLAYER_INPUT_COUNTDOWN);
    }
    
    /**
     * @brief 선택됨
     * @param nSelection 선택값. (0 or 1, -1은 타임아웃)
     */
    private void OnSelected(int nSelection)
    {
        if( nSelection == 0 )
            m_btnSelect1.setText("선택");
        else if( nSelection == 1 )
            m_btnSelect2.setText("선택");
        
        
        m_sessionEventHandler.removeMessages(SessionEventHandler.WHAT_PLAYER_INPUT_COUNTDOWN);
        if( Runtime.IsHost() ) {
            HostWorks.Game2_PlayerInput(m_nMyIdx, nSelection);
        }
        else {
            GuestWorks.Game2_PlayerSelection(m_nMyIdx, nSelection);
        }
    }
    
    /**
     * @brief 카운트다운 남은 시간을 화면에 갱신
     * @param nCountDownSeconds 남은 시간 (초)
     */
    private void UpdateCountDown(int nCountDownSeconds)
    {
        /// TODO : 화면에 남은 시간 표시
        m_tvCountdown.setText("카운트다운 : " + nCountDownSeconds);
        //
        Log.d("QQQQQ", "카운트다운 : " + nCountDownSeconds);
    }
    
    /**
     * @brief 카운트다운의 시간제한이 지났을 때 호출되는 메서드
     */
    private void CountDownTimeout()
    {
        Log.d("QQQQQ", "카운트다운 끝");
        m_bSeletable = false;
        OnSelected(-1);
    }
    
    /**
     * @brief 게임 결과를 화면에 표시
     * @param select1PlayerList 1번을 선택한 플레이어 목록
     * @param select2PlayerList 2번을 선택한 플레이어 목록
     * @param timeoutPlayerList 제한시간 내에 선택하지 않은 플레이어 목록
     * @param loserPlayerList 결정된 패배자 목록
     */
    private void DisplayGameResult(int[] select1PlayerList, int[] select2PlayerList, int[] timeoutPlayerList, int[] loserPlayerList)
    {
        // TODO : 팝업 등의 방법으로 화면에 표시
        String sResult = "";
        
        sResult += "선택1 : ";
        for(int i=0; i < select1PlayerList.length; i++) {
            sResult += m_nicknameList[select1PlayerList[i]] + ((i<(select1PlayerList.length-1))?", ":"");
        }
        sResult += "\n선택2 : ";
        for(int i=0; i < select2PlayerList.length; i++) {
            sResult += m_nicknameList[select2PlayerList[i]] + ((i<(select2PlayerList.length-1))?", ":"");
        }
        sResult += "\n시간초과 : ";
        for(int i=0; i < timeoutPlayerList.length; i++) {
            sResult += m_nicknameList[timeoutPlayerList[i]] + ((i<(timeoutPlayerList.length-1))?", ":"");
        }
        sResult += "\n\n패배 : ";
        for(int i=0; i < loserPlayerList.length; i++) {
            sResult += m_nicknameList[loserPlayerList[i]] + ((i<(loserPlayerList.length-1))?", ":"");
        }
        
        m_tvResult.setText(sResult);
    
        if( Runtime.IsHost() ) {
            m_btnRetry.setVisibility(View.VISIBLE);
            m_btnReturnToRoom.setVisibility(View.VISIBLE);
        }
    }
    
    
    
    
    
    class SessionEventHandler extends Handler
    {
        public static final int WHAT_HOST_DISCONNECTED = 0;
        public static final int WHAT_GAME_RETRY = 1;
        public static final int WHAT_RETURN_TO_ROOM = 2;
        public static final int WHAT_RECEIVED_GAME_DATA = 3;
        public static final int WHAT_ON_SELECTION_TIME = 4;
        public static final int WHAT_PLAYER_INPUT_COUNTDOWN = 5;
        public static final int WHAT_GAME_FINISH = 6;
        
        
        public SessionEventHandler(Looper looper)
        {
            super(looper);
        }
        
        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what) {
            case WHAT_HOST_DISCONNECTED: {
                // TODO : 서버로부터의 연결 끊김 처리
            } break;

            case WHAT_GAME_RETRY: {
                Intent intent = new Intent(Game2Activity.this, Game2Activity.class);
                startActivity(intent);
                finish();
            } break;

            case WHAT_RETURN_TO_ROOM: {
                Intent intent = null;
                if( Runtime.IsHost() )
                    intent = new Intent(Game2Activity.this, RoomHostActivity.class);
                else
                    intent = new Intent(Game2Activity.this, RoomGuestActivity.class);
                startActivity(intent);
                finish();
            } break;

            case WHAT_RECEIVED_GAME_DATA: {
                /// 게임 정보를 받은 후 화면에 표시
                m_nicknameList = (String[])msg.obj;
                m_nMyIdx = msg.arg1;
                m_nShape = msg.arg2;
                DisplayPlayerList(m_nicknameList);
                DisplayShape(m_nShape);
            } break;
            
            case WHAT_ON_SELECTION_TIME: {
                OnSelectionTime();
            } break;

            case WHAT_PLAYER_INPUT_COUNTDOWN: {
                m_nCountDownSeconds--;
                UpdateCountDown(m_nCountDownSeconds);
                if( m_nCountDownSeconds <= 0 ) {
                    CountDownTimeout();
                }
                else {
                    sendEmptyMessageDelayed(WHAT_PLAYER_INPUT_COUNTDOWN, 1000);
                }
            } break;
            
            case WHAT_GAME_FINISH: {
                String sResultInfo = (String)msg.obj;
                String[] paramList = sResultInfo.split("%");
                String[] select1ListTmp = Util.SplitWithoutEmptyStrings(paramList[0], "#");
                String[] select2ListTmp = Util.SplitWithoutEmptyStrings(paramList[1], "#");
                String[] timeoutListTmp = Util.SplitWithoutEmptyStrings(paramList[2], "#");
                String[] LoserListTmp = Util.SplitWithoutEmptyStrings(paramList[3], "#");
                
                m_resultSelect1PlayerList = new int[select1ListTmp.length];
                for(int i=0; i < select1ListTmp.length; i++)
                    m_resultSelect1PlayerList[i] = Integer.parseInt(select1ListTmp[i]);
                m_resultSelect2PlayerList = new int[select2ListTmp.length];
                for(int i=0; i < select2ListTmp.length; i++)
                    m_resultSelect2PlayerList[i] = Integer.parseInt(select2ListTmp[i]);
                m_resultTimeoutPlayerList = new int[timeoutListTmp.length];
                for(int i=0; i < timeoutListTmp.length; i++)
                    m_resultTimeoutPlayerList[i] = Integer.parseInt(timeoutListTmp[i]);
                m_resultLoserPlayerList = new int[LoserListTmp.length];
                for(int i=0; i < LoserListTmp.length; i++)
                    m_resultLoserPlayerList[i] = Integer.parseInt(LoserListTmp[i]);
                
                DisplayGameResult(m_resultSelect1PlayerList, m_resultSelect2PlayerList, m_resultTimeoutPlayerList, m_resultLoserPlayerList);
            } break;
            
            
            }
        }
    }
}

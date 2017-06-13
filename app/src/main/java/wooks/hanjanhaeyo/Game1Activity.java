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
import android.widget.Toast;

public class Game1Activity extends AppCompatActivity
{
    private SessionEventHandler m_sessionEventHandler;
    
    private Button[] m_toothButtons;
    private TextView m_tvPlayerList;
    private TextView m_tvCurrentTurn;
    private TextView m_tvCountdown;
    private TextView m_tvResult;
    private Button m_btnRetry;
    private Button m_btnReturnToRoom;
    
    private String[] m_playerTurnNicknameList;
    private int m_nMyTurn;
    private boolean m_bMyTurnInputted;
    private int m_nBomb;
    private int m_nTurnCount;
    private int m_nCountDownSeconds;
    
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game1);
    
        m_toothButtons = new Button[10];
        m_toothButtons[0] = (Button)findViewById(R.id.btn_tooth1);
        m_toothButtons[1] = (Button)findViewById(R.id.btn_tooth2);
        m_toothButtons[2] = (Button)findViewById(R.id.btn_tooth3);
        m_toothButtons[3] = (Button)findViewById(R.id.btn_tooth4);
        m_toothButtons[4] = (Button)findViewById(R.id.btn_tooth5);
        m_toothButtons[5] = (Button)findViewById(R.id.btn_tooth6);
        m_toothButtons[6] = (Button)findViewById(R.id.btn_tooth7);
        m_toothButtons[7] = (Button)findViewById(R.id.btn_tooth8);
        m_toothButtons[8] = (Button)findViewById(R.id.btn_tooth9);
        m_toothButtons[9] = (Button)findViewById(R.id.btn_tooth10);
        m_tvPlayerList = (TextView)findViewById(R.id.tv_playerList);
        m_tvCurrentTurn = (TextView)findViewById(R.id.tv_currentTurn);
        m_tvCountdown = (TextView)findViewById(R.id.tv_countdown);
        m_tvResult = (TextView)findViewById(R.id.tv_result);
        m_btnRetry = (Button)findViewById(R.id.btn_retry);
        m_btnReturnToRoom = (Button)findViewById(R.id.btn_returnToRoom);
        
    
        m_sessionEventHandler = new SessionEventHandler(Looper.getMainLooper());
        if( Runtime.IsHost() ) {
            HostWorks.Register_Game1ActivityEventHandler(m_sessionEventHandler);
        }
        else {
            GuestWorks.Register_Game1ActivityEventHandler(m_sessionEventHandler);
            GuestWorks.Game1_GuestReady();
        }
    }
    
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    
        if( Runtime.IsHost() ) {
            HostWorks.Register_Game1ActivityEventHandler(null);
        }
        else {
            GuestWorks.Register_Game1ActivityEventHandler(null);
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
        
        Intent intent = new Intent(Game1Activity.this, LobbyActivity.class);
        startActivity(intent);
        finish();
    }
    
    
    public void OnClick_Tooth(View v)
    {
        for(int i=0; i < m_toothButtons.length; i++) {
            Button b = m_toothButtons[i];
            if( b.getId() == v.getId() )
                InputToothButton(i);
        }
    }
    
    public void OnClick_Retry(View v)
    {
        HostWorks.Game1_Retry();
    }
    public void OnClick_ReturnToRoom(View v)
    {
        HostWorks.ReturnToRoom();
    }
    
    
    
    /**
     * @brief 전체 플레이어 목록을 표시한다 (턴 순서)
     * @param playerNicknameList 턴 순서로 리스트화된 플레이어 닉네임 목록
     */
    private void DisplayPlayerList(String[] playerNicknameList)
    {
        /// TODO : 전체 플레이어의 목록을 턴 순서로 표시한다.
        String sPlayerList = "순서 : ";
        for(int i=0; i < playerNicknameList.length; i++) {
            sPlayerList += playerNicknameList[i] + (i<(playerNicknameList.length-1)?" - ":"");
        }
        m_tvPlayerList.setText(sPlayerList);
        //
        Log.d("QQQQQ", "[전체 플레이어 차례 순서 표시]");
    }
    
    /**
     * @brief 턴 갱신
     * @param playerNicknameList 차례순 플레이어 닉네임 목록
     * @param nTurn 현재 턴 카운트
     * @param bIsMyTurn 자신의 턴 여부
     */
    private void UpdatePlayerTurn(String[] playerNicknameList, int nTurn, boolean bIsMyTurn)
    {
        /// TODO : 현재 차례의 플레이어가 누구인지 화면에 표시한다.
        m_tvCurrentTurn.setText("현재 턴 : " + playerNicknameList[nTurn] + (bIsMyTurn?"*":""));
        //
        Log.d("QQQQQ", "[현재 차례의 플레이어 표시] 현재 턴 : " + nTurn + " / 자신의 턴 여부 : " + bIsMyTurn);
        
        /// 자신의 턴이라면
        if( bIsMyTurn ) {
            /// 5초의 카운트다운을 표시하며, 플레이어는 버튼을 눌러야한다.
            Log.d("QQQQQ", "[버튼 입력 활성화 &   5초 카운트다운 시작]");
            
            /// 카운트다운 시작
            m_nCountDownSeconds = Constants.COUNTDOWN_GAME1;
            m_sessionEventHandler.sendEmptyMessage(SessionEventHandler.WHAT_PLAYER_INPUT_COUNTDOWN);
        }
    }
    
    /**
     * @brief 플레이어의 버튼 입력 정보를 업데이트
     * @param nTurn 턴
     * @param nTooth 이빨 버튼
     */
    private void UpdatePlayerInput(int nTurn, int nTooth)
    {
        /// 플레이어가 누른 버튼이 폭탄인 경우
        if( nTooth == m_nBomb ) {
            // TODO : 폭탄 누른 효과
            m_toothButtons[nTooth].setText("***");
            m_toothButtons[nTooth].setEnabled(false);
        }
        /// 타임오버인 경우
        else if( nTooth == -1 ) {
            // TODO : 타임오버 효과
        }
        /// 정상 버튼인 경우
        else {
            /// 버튼 눌림 효과
            SetPressedToothButton(nTooth);
        }
    }
    
    /**
     * @brief 게임 결과를 화면에 표시
     * @param nLosePlayerTurn 패배한 플레이어의 턴 번호. -1인 경우 패배자가 없음
     */
    private void DisplayGameResult(int nLosePlayerTurn)
    {
        // TODO : 게임 종료 표시
        if( nLosePlayerTurn == -1 ) {
            Toast.makeText(Game1Activity.this, "게임종료 - 패배자 없음", Toast.LENGTH_SHORT).show();
            m_tvResult.setText("결과 : 패배자 없음");
            Log.d("QQQQQ", "게임 종료. 패배자 없음");
        }
        else {
            Toast.makeText(Game1Activity.this, "게임종료 - 패배자: " + m_playerTurnNicknameList[nLosePlayerTurn], Toast.LENGTH_SHORT).show();
            m_tvResult.setText("결과 : " + m_playerTurnNicknameList[nLosePlayerTurn] + "가 패배");
            Log.d("QQQQQ", "게임 종료. 패배자 : " + nLosePlayerTurn + " / " + m_playerTurnNicknameList[nLosePlayerTurn]);
        }
        
        
        if( Runtime.IsHost() ) {
            m_btnRetry.setVisibility(View.VISIBLE);
            m_btnReturnToRoom.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * @brief 악어 이빨 버튼 누름
     * @param nTooth 이빨 버튼
     */
    private void InputToothButton(int nTooth)
    {
        if( m_nTurnCount == m_nMyTurn  &&  m_bMyTurnInputted == false ) {
            m_bMyTurnInputted = true;
            m_sessionEventHandler.removeMessages(SessionEventHandler.WHAT_PLAYER_INPUT_COUNTDOWN);
            if( Runtime.IsHost() ) {
                HostWorks.Game1_PlayerInput(m_nMyTurn, nTooth);
            }
            else {
                GuestWorks.Game1_InputToothButton(m_nMyTurn, nTooth);
            }
        }
    }
    
    /**
     * @brief 지정한 버튼을 눌림 상태로 하여, UI상에서 더 이상 누를 수 없도록 한다.
     * @param nTooth 이빨 버튼
     */
    private void SetPressedToothButton(int nTooth)
    {
        m_toothButtons[nTooth].setText("-");
        m_toothButtons[nTooth].setEnabled(false);
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
        m_bMyTurnInputted = true;
        if( Runtime.IsHost() ) {
            HostWorks.Game1_PlayerInput(m_nMyTurn, -1);
        }
        else {
            GuestWorks.Game1_InputToothButton(m_nMyTurn, -1);
        }
    }
    
    
    
    
    class SessionEventHandler extends Handler
    {
        public static final int WHAT_HOST_DISCONNECTED = 0;
        public static final int WHAT_GAME_RETRY = 1;
        public static final int WHAT_RETURN_TO_ROOM = 2;
        public static final int WHAT_RECEIVED_GAME_DATA = 3;
        public static final int WHAT_TURN_CHANGED = 4;
        public static final int WHAT_PLAYER_INPUT_COUNTDOWN = 5;
        public static final int WHAT_PLAYER_INPUT_UPDATED = 6;
        public static final int WHAT_GAME_FINISH = 7;
        
        
        
        
        
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
                Intent intent = new Intent(Game1Activity.this, Game1Activity.class);
                startActivity(intent);
                finish();
            } break;

            case WHAT_RETURN_TO_ROOM: {
                Intent intent = null;
                if( Runtime.IsHost() )
                    intent = new Intent(Game1Activity.this, RoomHostActivity.class);
                else
                    intent = new Intent(Game1Activity.this, RoomGuestActivity.class);
    
                startActivity(intent);
                finish();
            } break;
            
            case WHAT_RECEIVED_GAME_DATA: {
                m_playerTurnNicknameList = (String[])msg.obj;
                m_nMyTurn = msg.arg1;
                m_nBomb = msg.arg2;
                Log.d("QQQQQ", "정보받음... " + m_playerTurnNicknameList.length+"명 / " + m_nMyTurn + " / " + m_nBomb);
                DisplayPlayerList(m_playerTurnNicknameList);
            } break;

            case WHAT_TURN_CHANGED: {
                m_nTurnCount = msg.arg1;
                boolean bIsMyTurn = m_nMyTurn == m_nTurnCount;
                UpdatePlayerTurn(m_playerTurnNicknameList, m_nTurnCount, bIsMyTurn);
            } break;

            case WHAT_PLAYER_INPUT_COUNTDOWN: {
                if( m_nMyTurn != m_nTurnCount ) {
                    m_sessionEventHandler.removeMessages(SessionEventHandler.WHAT_PLAYER_INPUT_COUNTDOWN);
                    return;
                }
                m_nCountDownSeconds--;
                UpdateCountDown(m_nCountDownSeconds);
                if( m_nCountDownSeconds <= 0 ) {
                    CountDownTimeout();
                }
                else {
                    sendEmptyMessageDelayed(WHAT_PLAYER_INPUT_COUNTDOWN, 1000);
                }
            } break;
            
            case WHAT_PLAYER_INPUT_UPDATED: {
                int nTurn = msg.arg1;
                int nTooth = msg.arg2;
                UpdatePlayerInput(nTurn, nTooth);
            } break;
            
            case WHAT_GAME_FINISH: {
                int nLosePlayerTurn = msg.arg1;
                DisplayGameResult(nLosePlayerTurn);
            } break;
            
            
                
            
            }
        }
    }
    
}

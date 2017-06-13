package wooks.hanjanhaeyo;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Game3Activity extends AppCompatActivity
{
    private SessionEventHandler m_sessionEventHandler;
    
    /////////////////////////////////
    Button btn[];
    int[] randomArr = new int[25];
    static int count = 1;
    TextView textView;
    Chronometer timer;
    FrameLayout frame;
    ////////////////////////////////
    
    private TextView m_tvNotice;
    private Button m_btnRetry;
    private Button m_btnReturnToRoom;
    
    private String[] m_nicknameList;
    private int m_nMyIdx;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game3);
    
        
        m_tvNotice = (TextView)findViewById(R.id.tv_notice);
        m_btnRetry = (Button)findViewById(R.id.btn_retry);
        m_btnReturnToRoom = (Button)findViewById(R.id.btn_returnToRoom);
        
        /////////////////////////////////
        timer = (Chronometer) findViewById(R.id.chronometer1);
        timer.setTextSize(getLcdSIzeHeight() / 32);
        timer.setHeight(getLcdSIzeHeight() / 8);
        frame = (FrameLayout) findViewById(R.id.frame);
        // LinearLayout linear = (LinearLayout)findViewById(R.id.linear);
        LinearLayout linear1 = (LinearLayout) findViewById(R.id.linear1);
        LinearLayout linear2 = (LinearLayout) findViewById(R.id.linear2);
        LinearLayout linear3 = (LinearLayout) findViewById(R.id.linear3);
        LinearLayout linear4 = (LinearLayout) findViewById(R.id.linear4);
        LinearLayout linear5 = (LinearLayout) findViewById(R.id.linear5);
    
        LinearLayout.LayoutParams parambtn = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT);
        parambtn.weight = 1.0f;
    
        
        View.OnClickListener btnListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag.equals(count + "")) {
            
                    Animation anim = AnimationUtils.loadAnimation(
                            getApplicationContext(), R.anim.scale);
                    v.startAnimation(anim);
            
                    count++;
                    if (count == 26) {
                        timer.stop();
                        count = 1;
                        
                        /// 끝까지 버튼을 눌렀다. 완주했음을 호스트에게 알려야한다.
                        Finish_1to25();
                    }
                }
            }
        };
        
        btn = new Button[25];
    
        for (int i = 0; i < 25; i++) {
            btn[i] = new Button(this);
            btn[i].setText(" ");
            btn[i].setTextSize(getLcdSIzeHeight() / 32);
            btn[i].setId(i);
            btn[i].setHeight(getLcdSIzeHeight() / 8);
            btn[i].setOnClickListener(btnListener);
            btn[i].setEnabled(false);
            if (i < 5)
                linear1.addView(btn[i], parambtn);
            else if (i < 10)
                linear2.addView(btn[i], parambtn);
            else if (i < 15)
                linear3.addView(btn[i], parambtn);
            else if (i < 20)
                linear4.addView(btn[i], parambtn);
            else if (i < 25)
                linear5.addView(btn[i], parambtn);
        }
        
        /////////////////////////////////
        
        
        
    
        m_sessionEventHandler = new SessionEventHandler(Looper.getMainLooper());
        if( Runtime.IsHost() ) {
            HostWorks.Register_Game3ActivityEventHandler(m_sessionEventHandler);
        }
        else {
            GuestWorks.Register_Game3ActivityEventHandler(m_sessionEventHandler);
            GuestWorks.Game3_GuestReady();
        }
    }
    
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        
        if( Runtime.IsHost() ) {
            HostWorks.Register_Game3ActivityEventHandler(null);
        }
        else {
            GuestWorks.Register_Game3ActivityEventHandler(null);
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
        
        Intent intent = new Intent(Game3Activity.this, LobbyActivity.class);
        startActivity(intent);
        finish();
    }
    
    
    /////////////////////////////////
    public int[] generate() {
        int[] result = new int[25];
        int count = 0;
    
        while (count != 25) {
            boolean test = true;
            int r = (int) (Math.random() * 25 + 1);
            for (int i = 0; i < result.length; i++) {
                if (result[i] == r) {
                    test = false;
                    break;
                }
            }
            if (test) {
                result[count++] = r;
            }
        }
        return result;
    }
    
    
    /**
     * @brief 자신의 화면에서 1 to 25 게임을 시작한다.
     */
    private void Start()
    {
        randomArr = generate();
    
        for (int i = 0; i < btn.length; i++) {
            btn[i].setEnabled(true);
            btn[i].setText("" + randomArr[i]);
            btn[i].setTag(btn[i].getText());
            btn[i].setAnimation(null);
        }
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();
    
        Animation anim = AnimationUtils.loadAnimation(
                getApplicationContext(), R.anim.translate);
        frame.setAnimation(anim);
    }
    
    public int getLcdSIzeHeight() {
        return ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getHeight();
    }
    
    public int getLcdSIzeWidth() {
        return ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getWidth();
    }
    ////////////////////////////////
    
    
    public void OnClick_Retry(View v)
    {
        HostWorks.Game3_Retry();
    }
    public void OnClick_ReturnToRoom(View v)
    {
        HostWorks.ReturnToRoom();
    }
    
    
    /**
     * @brief 1 to 25 시작하기전 카운트다운
     * @param nReadyCountdown
     */
    private void ReadyCountdown(int nReadyCountdown)
    {
        m_tvNotice.setText("준비하세요... 카운트다운 " + nReadyCountdown);
    }
    
    /**
     * @brief 1 to 25 시작
     */
    private void Start_1to25()
    {
        m_tvNotice.setText("남들보다 먼저 1 to 25를 클리어하세요!");
        Start();
    }
    
    /**
     * @brief 1 to 25를 완주했음
     */
    private void Finish_1to25()
    {
        if( Runtime.IsHost() ) {
            HostWorks.Game3_PlayerFinish(m_nMyIdx);
        }
        else {
            GuestWorks.Game3_PlayerFinish(m_nMyIdx);
        }
    }
    
    /**
     * @brief 남은 플레이어 수를 알림
     * @param nRemainPlayers 남은 플레이어 수
     */
    private void UpdatePlayerInput(int nRemainPlayers)
    {
        m_tvNotice.setText("아직 성공하지 못한 플레이어 수 : " + nRemainPlayers + "명");
    }
    
    /**
     * @brief 게임 결과 표시
     * @param nLoserIdx 패배자 idx
     */
    private void DisplayGameResult(int nLoserIdx)
    {
        if( timer.isEnabled() ) {
            timer.stop();
        }
        for (int i = 0; i < 25; i++) {
            btn[i].setEnabled(false);
        }
        
        
        
        String sLoserNickname = m_nicknameList[nLoserIdx];
        m_tvNotice.setText("패배자 : " + sLoserNickname);
    
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
        public static final int WHAT_1_TO_25_READY = 4;
        public static final int WHAT_1_TO_25_START = 5;
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
                Intent intent = new Intent(Game3Activity.this, Game3Activity.class);
                startActivity(intent);
                finish();
            } break;

            case WHAT_RETURN_TO_ROOM: {
                Intent intent = null;
                if( Runtime.IsHost() )
                    intent = new Intent(Game3Activity.this, RoomHostActivity.class);
                else
                    intent = new Intent(Game3Activity.this, RoomGuestActivity.class);
                startActivity(intent);
                finish();
            } break;
            
            case WHAT_RECEIVED_GAME_DATA: {
                m_nicknameList = (String[])msg.obj;
                m_nMyIdx = msg.arg1;
            } break;
            
            case WHAT_1_TO_25_READY: {
                int nReadyCountdown = msg.arg1; // 3.. 2.. 1..
                ReadyCountdown(nReadyCountdown);
            } break;
            
            case WHAT_1_TO_25_START: {
                Start_1to25();
            } break;
            
            case WHAT_PLAYER_INPUT_UPDATED: {
                int nRemainPlayers = msg.arg1;
                UpdatePlayerInput(nRemainPlayers);
            } break;
            
            case WHAT_GAME_FINISH: {
                int nLoserIdx = msg.arg1;
                DisplayGameResult(nLoserIdx);
            } break;
            
            }
        }
    }
}

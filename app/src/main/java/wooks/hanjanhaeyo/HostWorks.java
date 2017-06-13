package wooks.hanjanhaeyo;

import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by in2etv on 2017-06-09.
 */

public class HostWorks
{
    private static RoomHostActivity.SessionEventHandler g_evhRoomHostActivity;
    private static Game1Activity.SessionEventHandler g_evhGame1Activity;
    private static Game2Activity.SessionEventHandler g_evhGame2Activity;
    private static Game3Activity.SessionEventHandler g_evhGame3Activity;
    
    private static IRemoteClosedEventHandler g_evhRemoteClosedCallback = new IRemoteClosedEventHandler()
    {
        @Override
        public void OnRemoteClosed(GuestSession session)
        {
            /// 게스트 측의 접속 종료 처리
            if( g_sessionList != null  &&  g_sessionList.contains(session) ) {
                g_sessionList.remove(session);
                session.SetKeepReceiving(false);
                session.SetPlayer(false);
            }
            UpdatePlayerList();
        }
    };
    private static IMessageReceivedEventHandler g_evhMessageReceivedCallback = new IMessageReceivedEventHandler()
    {
        @Override
        public void OnMessageReceived(GuestSession session, String sMessage)
        {
            String[] sp = sMessage.split("@");
            if( sp.length >= 1 ) {
                switch(sp[0].toUpperCase()) {
                /// 게스트의 방 입장 요청
                case Constants.PROTOCOL_ROOM_JOIN_REQUEST: {
                    /// 현재 방의 상태가 입장가능한 상태인지 여부에 따라 [ROOM_JOIN_RESPONSE]@[OK/NO] 메시지
                    if( m_state == EHostState.ROOM_WAITING ) {
                        session.SetPlayer(true);
                        session.SetNickname(sp[1]);
                        session.Send(Constants.PROTOCOL_ROOM_JOIN_RESPONSE + "@OK");
                        UpdatePlayerList();
                    }
                    else {
                        session.SetPlayer(false);
                        session.Send(Constants.PROTOCOL_ROOM_JOIN_RESPONSE + "@NO");
                    }
                } break;

                /// 게스트의 플레이어 리스트 요청
                case Constants.PROTOCOL_ROOM_PLAYERLIST_REQUEST: {
                    UpdatePlayerList(session);
                } break;
                
                /// 현재 선택된 게임 요청
                case Constants.PROTOCOL_ROOM_SELECTED_GAME_REQUEST: {
                    UpdateSelectedGame(session);
                } break;
                
                /// 플레이어가 게임1 데이터를 수신할 준비가 되었음
                case Constants.PROTOCOL_GAME1_GUEST_READY: {
                    Game1_GuestReady();
                } break;
                
                /// 플레이어의 버튼 입력 혹은 시간초과
                case Constants.PROTOCOL_GAME1_PLAYER_INPUT_REQUEST: {
                    int nTurn = Integer.parseInt(sp[1]);
                    int nTooth = Integer.parseInt(sp[2]);
                    Game1_PlayerInput(nTurn, nTooth);
                } break;


                /// 플레이어가 게임2 데이터를 수신할 준비가 되었음
                case Constants.PROTOCOL_GAME2_GUEST_READY: {
                    Game2_GuestReady();
                } break;
                
                /// 플레이어가 버튼 입력 혹은 시간초과
                case Constants.PROTOCOL_GAME2_PLAYER_INPUT: {
                    int nPlayerIdx = Integer.parseInt(sp[1]);
                    int nSelection = Integer.parseInt(sp[2]);
                    Game2_PlayerInput(nPlayerIdx, nSelection);
                } break;


                /// 플레이어가 게임3 데이터를 수신할 준비가 되었음
                case Constants.PROTOCOL_GAME3_GUEST_READY: {
                    Game3_GuestReady();
                } break;

                /// 플레이어의 1 to 25 완주
                case Constants.PROTOCOL_GAME3_PLAYER_INPUT_REQUEST: {
                    int nPlayerIdx = Integer.parseInt(sp[1]);
                    Game3_PlayerFinish(nPlayerIdx);
                } break;
                
                }
            }
        }
    };
    
    private static Object g_csSessionList = new Object();
    private static ArrayList<GuestSession> g_sessionList;
    private static int g_nNextId;
    
    private static EHostState m_state;
    private static boolean g_bKeepListening;
    private static int g_nSelectedGame;
    
    private static int g_nGame1GuestReadyCount;
    private static ArrayList<Integer> g_game1PlayerTurnList;
    private static int g_nGame1CurrentTurn;
    private static int g_nGame1Bomb;
    
    private static int g_nGame2GuestReadyCount;
    private static int g_nGame2InputCount;
    private static ArrayList<Integer> g_game2InputSelect1List;
    private static ArrayList<Integer> g_game2InputSelect2List;
    private static ArrayList<Integer> g_game2InputTimeoutList;
    
    private static int g_nGame3GuestReadyCount;
    private static ArrayList<Integer> g_game3PlayerInputList;
    
    
    
    private HostWorks() {}
    
    
    public static void Register_RoomHostActivityEventHandler(RoomHostActivity.SessionEventHandler evh)
    {
        g_evhRoomHostActivity = evh;
    }
    public static void Register_Game1ActivityEventHandler(Game1Activity.SessionEventHandler evh)
    {
        g_evhGame1Activity = evh;
    }
    public static void Register_Game2ActivityEventHandler(Game2Activity.SessionEventHandler evh)
    {
        g_evhGame2Activity = evh;
    }
    public static void Register_Game3ActivityEventHandler(Game3Activity.SessionEventHandler evh)
    {
        g_evhGame3Activity = evh;
    }
    
    
    public static boolean IsOpened()
    {
        return m_state == EHostState.ROOM_WAITING  ||  m_state == EHostState.GAME_PLAYING;
    }
    
    public static int GetSelectedGame()
    {
        return g_nSelectedGame;
    }
    
    
    public static int GetPlayerCount()
    {
        int nPlayerCount = 0;
        synchronized(g_csSessionList) {
            if(g_sessionList != null) {
                for(GuestSession s : g_sessionList) {
                    if(s.IsPlayer()) {
                        nPlayerCount++;
                    }
                }
            }
        }
        return nPlayerCount + 1;
    }
    
    
    public static void Open()
    {
        new Thread(new Runnable(){
            @Override
            public void run()
            {
                try {
                    g_nNextId = 0;
                    g_sessionList = new ArrayList<>();
                    
                    ServerSocket listener = new ServerSocket(Constants.ROOM_HOST_PORT);
                    listener.setSoTimeout(1000);
                    m_state = EHostState.ROOM_WAITING;
    
                    g_bKeepListening = true;
                    while(g_bKeepListening) {
                        try {
                            Socket socket = listener.accept();
                            GuestSession session = new GuestSession(g_nNextId++, socket);
                            session.SetNickname("undefined");
    
                            session.Register_OnRemoteClosed(g_evhRemoteClosedCallback);
                            session.Register_OnMessageReceived(g_evhMessageReceivedCallback);
                            session.ReceiveStart();
                            
                            synchronized(g_csSessionList) {
                                g_sessionList.add(session);
                            }
                        }
                        catch(SocketTimeoutException ste) {
                            // NOP
                        }
                    }
                    listener.close();
                }
                catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }
    
    public static void Close()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                m_state = EHostState.CLOSED;
                g_bKeepListening = false;
                
                synchronized(g_csSessionList) {
                    if(g_sessionList != null) {
                        for(GuestSession s : g_sessionList) {
                            s.Close();
                        }
                        g_sessionList.clear();
                        g_sessionList = null;
                    }
                }
            }
        }).start();
    }
    
    public static void SendAllPlayer(final String sMessage)
    {
        if( Looper.myLooper() == Looper.getMainLooper() ) {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    SendAllPlayer(sMessage);
                }
            }).start();
            return;
        }
        
        synchronized(g_csSessionList) {
            for(GuestSession s : g_sessionList) {
                if(s.IsPlayer()) {
                    s.Send(sMessage);
                }
            }
        }
    }
    
    
    public static void UpdatePlayerList()
    {
        String sParam = BuildMessageParam_PlayerList();
        String sMessage = Constants.PROTOCOL_ROOM_PLAYERLIST_UPDATE + "@" + sParam;
        
        SendAllPlayer(sMessage);
        
        /// 호스트 자신의 화면도 갱신
        if( g_evhRoomHostActivity != null ) {
            String[] sPlayerList = sParam.split("#");
    
            Message msg = g_evhRoomHostActivity.obtainMessage();
            msg.what = RoomHostActivity.SessionEventHandler.WHAT_UPDATE_PLAYER_LIST;
            msg.obj = sPlayerList;
            g_evhRoomHostActivity.sendMessage(msg);
        }
    }
    public static void UpdatePlayerList(GuestSession session)
    {
        String sParam = BuildMessageParam_PlayerList();
        String sMessage = Constants.PROTOCOL_ROOM_PLAYERLIST_UPDATE + "@" + sParam;
    
        if( session.IsPlayer() ) {
            session.Send(sMessage);
        }
    }
    private static String BuildMessageParam_PlayerList()
    {
        String sParam = "";
        sParam += Runtime.GetNickname() + "#";
        for(GuestSession s : g_sessionList) {
            if( s.IsPlayer() ) {
                sParam += s.GetNickname() + "#";
            }
        }
        if( sParam.endsWith("#") ) {
            sParam = sParam.substring(0, sParam.length()-1);
        }
    
        return sParam;
    }
    
    
    public static void GameSelect(int nGame)
    {
        g_nSelectedGame = nGame;
    
        SendAllPlayer(Constants.PROTOCOL_ROOM_SELECTED_GAME_UPDATE+"@"+nGame);
    }
    public static void UpdateSelectedGame(GuestSession session)
    {
        if( session.IsPlayer() ) {
            session.Send(Constants.PROTOCOL_ROOM_SELECTED_GAME_UPDATE+"@"+g_nSelectedGame);
        }
    }
    
    
    public static void GameStart()
    {
        if( g_nSelectedGame <= 0 )
            return;
        
        switch(g_nSelectedGame) {
        case 1:
            Game1_Init();
            break;
        case 2:
            Game2_Init();
            break;
        case 3:
            Game3_Init();
            break;
        }
        
        
        m_state = EHostState.GAME_PLAYING;
    
        SendAllPlayer(Constants.PROTOCOL_ROOM_GAME_START +"@"+g_nSelectedGame);
    }
    
    public static void ReturnToRoom()
    {
        m_state = EHostState.ROOM_WAITING;
        
        SendAllPlayer(Constants.PROTOCOL_GAME_RETURN_TO_ROOM);
    
        if( g_evhGame1Activity != null ) {
            g_evhGame1Activity.sendEmptyMessage(Game1Activity.SessionEventHandler.WHAT_RETURN_TO_ROOM);
        }
        if( g_evhGame2Activity != null ) {
            g_evhGame2Activity.sendEmptyMessage(Game2Activity.SessionEventHandler.WHAT_RETURN_TO_ROOM);
        }
        if( g_evhGame3Activity != null ) {
            g_evhGame3Activity.sendEmptyMessage(Game3Activity.SessionEventHandler.WHAT_RETURN_TO_ROOM);
        }
    }
    
    
    public static void Game1_Init()
    {
        g_nGame1GuestReadyCount = 0;
        g_game1PlayerTurnList = null;
        g_nGame1CurrentTurn = 0;
        g_nGame1Bomb = 0;
    }
    
    public static void Game1_GuestReady()
    {
        g_nGame1GuestReadyCount++;
        int nGuestPlayerCount = GetPlayerCount() - 1;
        
        /// 모든 플레이어가 데이터를 수신할 준비가 되면
        if( g_nGame1GuestReadyCount >= nGuestPlayerCount ) {
            Game1_InitGameData();
        }
    }
    
    public static void Game1_InitGameData()
    {
        synchronized(g_csSessionList) {
            if(g_sessionList != null) {
                ArrayList<Integer> playerTurnList = new ArrayList<>();
    
                for(GuestSession s : g_sessionList) {
                    if(s.IsPlayer()) {
                        playerTurnList.add(s.GetId());
                    }
                }
                playerTurnList.add(-1); // 호스트 자신
        
                Collections.shuffle(playerTurnList);
                g_game1PlayerTurnList = playerTurnList;
        
                ArrayList<String> playerTurnNicknameList = new ArrayList<>();
                for(int i = 0; i < playerTurnList.size(); i++) {
                    int nPlayerId = playerTurnList.get(i);
                    if(nPlayerId == -1) {
                        playerTurnNicknameList.add(Runtime.GetNickname());
                        continue;
                    }
                    for(GuestSession s : g_sessionList) {
                        if(s.GetId() == nPlayerId) {
                            playerTurnNicknameList.add(s.GetNickname());
                            break;
                        }
                    }
                }
        
                String sTurnInfo = "";
                for(int i = 0; i < playerTurnNicknameList.size(); i++) {
                    sTurnInfo += playerTurnNicknameList.get(i) + "#";
                }
                if(sTurnInfo.endsWith("#")) {
                    sTurnInfo = sTurnInfo.substring(0, sTurnInfo.length() - 1);
                }
    
                g_nGame1Bomb = new Random().nextInt(10);
        
                for(GuestSession s : g_sessionList) {
                    if(s.IsPlayer()) {
                        int nTurn = playerTurnList.indexOf(s.GetId());
                        String sMessage = Constants.PROTOCOL_GAME1_INIT_GAME_DATA + "@" + sTurnInfo + "@" + nTurn + "@" + g_nGame1Bomb;
                        s.Send(sMessage);
                    }
                }
    
                /// 호스트 자신의 액티비티에도 반영하도록 핸들러 호출
                if(g_evhGame1Activity != null) {
                    Message msg = g_evhGame1Activity.obtainMessage();
                    msg.what = Game1Activity.SessionEventHandler.WHAT_RECEIVED_GAME_DATA;
                    msg.obj = playerTurnNicknameList.toArray(new String[playerTurnNicknameList.size()]);
                    msg.arg1 = playerTurnList.indexOf(-1);
                    msg.arg2 = g_nGame1Bomb;
                    g_evhGame1Activity.sendMessage(msg);
                }
        
                g_nGame1CurrentTurn = -1;
                Game1_NextTurn();
            }
        }
    }
    
    public static void Game1_NextTurn()
    {
        g_nGame1CurrentTurn++;
        
        /// 모든 플레이어가 눌렀지만, 걸린 놈이 나오지 않은 경우
        if( g_nGame1CurrentTurn >= g_game1PlayerTurnList.size() ) {
            SendAllPlayer(Constants.PROTOCOL_GAME1_GAME_FINISH + "@-1");
            /// 호스트 자신의 게임1 액티비티에 메시지 전달
            if( g_evhGame1Activity != null ) {
                Message msg = g_evhGame1Activity.obtainMessage();
                msg.what = Game1Activity.SessionEventHandler.WHAT_GAME_FINISH;
                msg.arg1 = -1;
                g_evhGame1Activity.sendMessage(msg);
            }
        }
        /// 다음 플레이어의 턴 시작을 알림
        else {
            SendAllPlayer(Constants.PROTOCOL_GAME1_TURN_CHANGED + "@" + g_nGame1CurrentTurn);
            /// 호스트 자신의 게임1 액티비티에 메시지 전달
            if( g_evhGame1Activity != null ) {
                Message msg = g_evhGame1Activity.obtainMessage();
                msg.what = Game1Activity.SessionEventHandler.WHAT_TURN_CHANGED;
                msg.arg1 = g_nGame1CurrentTurn;
                g_evhGame1Activity.sendMessage(msg);
            }
        }
    }
    
    public static void Game1_PlayerInput(final int nTurn, final int nTooth)
    {
        /// 플레이어가 누른 이빨이 무엇인지 모든 플레이어에게 알린다.
        SendAllPlayer(Constants.PROTOCOL_GAME1_PLAYER_INPUT_UPDATE + "@" + nTurn + "@" + nTooth);
        /// 호스트 자신의 게임1 액티비티에도 알린다.
        if( g_evhGame1Activity != null ) {
            Message msg = g_evhGame1Activity.obtainMessage();
            msg.what = Game1Activity.SessionEventHandler.WHAT_PLAYER_INPUT_UPDATED;
            msg.arg1 = nTurn;
            msg.arg2 = nTooth;
            g_evhGame1Activity.sendMessage(msg);
        }
        
        /// 플레이어가 폭탄을 누르거나, 타임아웃되어 게임이 끝난 경우
        if( nTooth == g_nGame1Bomb  ||  nTooth == -1 ) {
            new Thread(new Runnable(){
                @Override
                public void run()
                {
                    try {
                        Thread.sleep(1000);
                    }
                    catch(Exception ex) {
                        // NOP
                    }
                    SendAllPlayer(Constants.PROTOCOL_GAME1_GAME_FINISH + "@" + nTurn);
                    /// 호스트 자신의 게임1 액티비티에도 알린다.
                    if( g_evhGame1Activity != null ) {
                        Message msg = g_evhGame1Activity.obtainMessage();
                        msg.what = Game1Activity.SessionEventHandler.WHAT_GAME_FINISH;
                        msg.arg1 = nTurn;
                        msg.arg2 = nTooth;
                        g_evhGame1Activity.sendMessage(msg);
                    }
                }
            }).start();
        }
        /// 플레이어가 정상이빨을 누른 경우
        else {
            new Thread(new Runnable(){
                @Override
                public void run()
                {
                    try {
                        Thread.sleep(1000);
                    }
                    catch(Exception ex) {
                        // NOP
                    }
                    Game1_NextTurn();
                }
            }).start();
        }
    }
    
    public static void Game1_Retry()
    {
        SendAllPlayer(Constants.PROTOCOL_GAME1_RETRY);
    
        if( g_evhGame1Activity != null ) {
            g_evhGame1Activity.sendEmptyMessage(Game1Activity.SessionEventHandler.WHAT_GAME_RETRY);
        }
    }
    
    
    
    
    
    
    
    public static void Game2_Init()
    {
        g_nGame2GuestReadyCount = 0;
        g_nGame2InputCount = 0;
        g_game2InputSelect1List = new ArrayList<>();
        g_game2InputSelect2List = new ArrayList<>();
        g_game2InputTimeoutList = new ArrayList<>();
    }
    
    public static void Game2_GuestReady()
    {
        g_nGame2GuestReadyCount++;
        int nGuestPlayerCount = GetPlayerCount() - 1;
        
        /// 모든 플레이어가 데이터를 수신할 준비가 되면
        if( g_nGame2GuestReadyCount >= nGuestPlayerCount ) {
            Game2_InitGameData();
        }
    }
    
    public static void Game2_InitGameData()
    {
        synchronized(g_csSessionList) {
            ArrayList<Integer> playerList = new ArrayList<>();
            playerList.add(-1); // 호스트 자신
            for(GuestSession s : g_sessionList) {
                if(s.IsPlayer()) {
                    playerList.add(s.GetId());
                }
            }
    
            ArrayList<String> playerNicknameList = new ArrayList<>();
            for(int i = 0; i < playerList.size(); i++) {
                int nPlayerId = playerList.get(i);
                if(nPlayerId == -1) {
                    playerNicknameList.add(Runtime.GetNickname());
                    continue;
                }
                for(GuestSession s : g_sessionList) {
                    if(s.GetId() == nPlayerId) {
                        playerNicknameList.add(s.GetNickname());
                        break;
                    }
                }
            }
    
            String sNicknameInfo = "";
            for(int i = 0; i < playerNicknameList.size(); i++) {
                sNicknameInfo += playerNicknameList.get(i) + ((i < (playerNicknameList.size() - 1) ? "#" : ""));
            }
    
    
            int nShape = new Random().nextInt(10);
    
            /// 게스트에게 게임 정보를 전송
            for(GuestSession s : g_sessionList) {
                if(s.IsPlayer()) {
                    int nIdx = playerList.indexOf(s.GetId());
                    String sMessage = Constants.PROTOCOL_GAME2_INIT_GAME_DATA + "@" + sNicknameInfo + "@" + nIdx + "@" + nShape;
                    s.Send(sMessage);
                }
            }
            /// 호스트 자신의 액티비티에도 반영
            if(g_evhGame2Activity != null) {
                Message msg = g_evhGame2Activity.obtainMessage();
                msg.what = Game2Activity.SessionEventHandler.WHAT_RECEIVED_GAME_DATA;
                msg.obj = playerNicknameList.toArray(new String[playerNicknameList.size()]);
                msg.arg1 = 0;
                msg.arg2 = nShape;
                g_evhGame2Activity.sendMessage(msg);
            }
    
    
            /// 게스트에게 선택 시작하라고 알림
            SendAllPlayer(Constants.PROTOCOL_GAME2_ON_SELECTION_TIME);
            /// 호스트 자신의 액티비티에도 반영
            if(g_evhGame2Activity != null) {
                g_evhGame2Activity.sendEmptyMessage(Game2Activity.SessionEventHandler.WHAT_ON_SELECTION_TIME);
            }
        }
    }
    
    public static void Game2_PlayerInput(int nPlayerIdx, int nSelection)
    {
        g_nGame2InputCount++;
        switch(nSelection) {
        case 0:
            g_game2InputSelect1List.add(nPlayerIdx);
            break;
        case 1:
            g_game2InputSelect2List.add(nPlayerIdx);
            break;
        case -1:
            g_game2InputTimeoutList.add(nPlayerIdx);
            break;
        }
        
        if( g_nGame2InputCount >= GetPlayerCount() ) {
            // TODO : 모든 사용자가 입력했으므로, 결과에 따라 다음 게임을 할지, 패배자를 발표할지 정한다
            
            String sParamSelect1 = "";
            for(int i=0; i < g_game2InputSelect1List.size(); i++) {
                sParamSelect1 += g_game2InputSelect1List.get(i) + ((i<(g_game2InputSelect1List.size()-1)?"#":""));
            }
            String sParamSelect2 = "";
            for(int i=0; i < g_game2InputSelect2List.size(); i++) {
                sParamSelect2 += g_game2InputSelect2List.get(i) + ((i<(g_game2InputSelect2List.size()-1)?"#":""));
            }
            String sParamTimeout = "";
            for(int i=0; i < g_game2InputTimeoutList.size(); i++) {
                sParamTimeout += g_game2InputTimeoutList.get(i) + ((i<(g_game2InputTimeoutList.size()-1)?"#":""));
            }
    
    
            String sParamLoser = "";
            
            /// 시간초과한 사람이 패배
            if( g_game2InputTimeoutList.size() > 0 ) {
                sParamLoser = sParamTimeout;
            }
            /// 양쪽의 수가 같으면 모두 패배
            else if( g_game2InputSelect1List.size() == g_game2InputSelect2List.size() ) {
                sParamLoser = sParamSelect1 + "#" + sParamSelect2;
            }
            /// 양쪽의 수가 다르면 적은 쪽이 패배
            else if( g_game2InputSelect1List.size() < g_game2InputSelect2List.size() ) {
                sParamLoser = sParamSelect1;
            }
            /// 양쪽의 수가 다르면 적은 쪽이 패배
            else {
                sParamLoser = sParamSelect2;
            }
    
            /// 게임 결과 전송
            String sGameResultInfo = sParamSelect1+"%"+sParamSelect2+"%"+sParamTimeout+"%"+sParamLoser;
            SendAllPlayer(Constants.PROTOCOL_GAME2_GAME_FINISH+"@"+sGameResultInfo);
            /// 호스트 자신에게도 반영
            if( g_evhGame2Activity != null ) {
                Message msg = g_evhGame2Activity.obtainMessage();
                msg.what = Game2Activity.SessionEventHandler.WHAT_GAME_FINISH;
                msg.obj = sGameResultInfo;
                g_evhGame2Activity.sendMessage(msg);
            }
            
        }
        
    }
    
    public static void Game2_Retry()
    {
        SendAllPlayer(Constants.PROTOCOL_GAME2_RETRY);
        
        if( g_evhGame2Activity != null ) {
            g_evhGame2Activity.sendEmptyMessage(Game2Activity.SessionEventHandler.WHAT_GAME_RETRY);
        }
    }
    
    
    
    
    public static void Game3_Init()
    {
        g_nGame3GuestReadyCount = 0;
        g_game3PlayerInputList = new ArrayList<>();
    }
    
    public static void Game3_GuestReady()
    {
        g_nGame3GuestReadyCount++;
        int nGuestPlayerCount = GetPlayerCount() - 1;
        
        /// 모든 플레이어가 데이터를 수신할 준비가 되면
        if( g_nGame3GuestReadyCount >= nGuestPlayerCount ) {
            Game3_InitGameData();
        }
    }
    
    public static void Game3_InitGameData()
    {
        synchronized(g_csSessionList) {
            ArrayList<Integer> playerList = new ArrayList<>();
            playerList.add(-1); // 호스트 자신
            for(GuestSession s : g_sessionList) {
                if(s.IsPlayer()) {
                    playerList.add(s.GetId());
                }
            }
    
            ArrayList<String> playerNicknameList = new ArrayList<>();
            for(int i = 0; i < playerList.size(); i++) {
                int nPlayerId = playerList.get(i);
                if(nPlayerId == -1) {
                    playerNicknameList.add(Runtime.GetNickname());
                    continue;
                }
                for(GuestSession s : g_sessionList) {
                    if(s.GetId() == nPlayerId) {
                        playerNicknameList.add(s.GetNickname());
                        break;
                    }
                }
            }
    
            String sNicknameInfo = "";
            for(int i = 0; i < playerNicknameList.size(); i++) {
                sNicknameInfo += playerNicknameList.get(i) + ((i < (playerNicknameList.size() - 1) ? "#" : ""));
            }
    
    
            /// 게스트에게 게임 정보를 전송
            for(GuestSession s : g_sessionList) {
                if(s.IsPlayer()) {
                    int nIdx = playerList.indexOf(s.GetId());
                    String sMessage = Constants.PROTOCOL_GAME3_INIT_GAME_DATA + "@" + sNicknameInfo + "@" + nIdx;
                    s.Send(sMessage);
                }
            }
            /// 호스트 자신의 액티비티에도 반영
            if(g_evhGame3Activity != null) {
                Message msg = g_evhGame3Activity.obtainMessage();
                msg.what = Game3Activity.SessionEventHandler.WHAT_RECEIVED_GAME_DATA;
                msg.obj = playerNicknameList.toArray(new String[playerNicknameList.size()]);
                msg.arg1 = 0;
                g_evhGame3Activity.sendMessage(msg);
            }
    
    
            /// 카운트다운 후 1 to 25 시작
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try {
                        if(m_state == EHostState.GAME_PLAYING) {
                            SendAllPlayer(Constants.PROTOCOL_GAME3_1_TO_25_READY + "@3");
                            if(g_evhGame3Activity != null) {
                                Message msg = g_evhGame3Activity.obtainMessage();
                                msg.what = Game3Activity.SessionEventHandler.WHAT_1_TO_25_READY;
                                msg.arg1 = 3;
                                g_evhGame3Activity.sendMessage(msg);
                            }
                        }
                
                        Thread.sleep(1000);
                
                        if(m_state == EHostState.GAME_PLAYING) {
                            SendAllPlayer(Constants.PROTOCOL_GAME3_1_TO_25_READY + "@2");
                            if(g_evhGame3Activity != null) {
                                Message msg = g_evhGame3Activity.obtainMessage();
                                msg.what = Game3Activity.SessionEventHandler.WHAT_1_TO_25_READY;
                                msg.arg1 = 2;
                                g_evhGame3Activity.sendMessage(msg);
                            }
                        }
                
                        Thread.sleep(1000);
                
                        if(m_state == EHostState.GAME_PLAYING) {
                            SendAllPlayer(Constants.PROTOCOL_GAME3_1_TO_25_READY + "@1");
                            if(g_evhGame3Activity != null) {
                                Message msg = g_evhGame3Activity.obtainMessage();
                                msg.what = Game3Activity.SessionEventHandler.WHAT_1_TO_25_READY;
                                msg.arg1 = 1;
                                g_evhGame3Activity.sendMessage(msg);
                            }
                        }
                
                        Thread.sleep(1000);
                
                        if(m_state == EHostState.GAME_PLAYING) {
                            SendAllPlayer(Constants.PROTOCOL_GAME3_1_TO_25_START);
                            if(g_evhGame3Activity != null) {
                                g_evhGame3Activity.sendEmptyMessage(Game3Activity.SessionEventHandler.WHAT_1_TO_25_START);
                            }
                        }
                    } catch(Exception ex) {
                        // NOP
                    }
                }
            }).start();
        }
            
    }
        
    public static void Game3_PlayerFinish(int nPlayerIdx)
    {
        g_game3PlayerInputList.add(nPlayerIdx);
        
        if( g_game3PlayerInputList.size() >= GetPlayerCount()-1 ) {
    
            ArrayList<Integer> tmpList = new ArrayList<>();
            tmpList.add(-1); // 호스트 자신
            for(GuestSession s : g_sessionList) {
                if(s.IsPlayer()) {
                    tmpList.add(s.GetId());
                }
            }
            
            /// 꼴찌가 누구냐
            int nLoserIdx = -1;
            for(int i = 0; i < tmpList.size(); i++) {
                boolean bFound = false;
                for(int j=0; j < g_game3PlayerInputList.size(); j++) {
                    if( i == g_game3PlayerInputList.get(j) ) {
                        bFound = true;
                    }
                }
                if( bFound == false ) {
                    nLoserIdx = i;
                }
            }
            
            SendAllPlayer(Constants.PROTOCOL_GAME3_GAME_FINISH+"@"+nLoserIdx);
            if( g_evhGame3Activity != null ) {
                Message msg = g_evhGame3Activity.obtainMessage();
                msg.what = Game3Activity.SessionEventHandler.WHAT_GAME_FINISH;
                msg.arg1 = nLoserIdx;
                g_evhGame3Activity.sendMessage(msg);
            }
        }
        else {
            /// 남은 플레이어가 몇명이냐
            int nRemainPlayers = GetPlayerCount() - g_game3PlayerInputList.size();
            
            SendAllPlayer(Constants.PROTOCOL_GAME3_PLAYER_INPUT_UPDATE+"@"+nRemainPlayers);
            if( g_evhGame3Activity != null ) {
                Message msg = g_evhGame3Activity.obtainMessage();
                msg.what = Game3Activity.SessionEventHandler.WHAT_PLAYER_INPUT_UPDATED;
                msg.arg1 = nRemainPlayers;
                g_evhGame3Activity.sendMessage(msg);
            }
        }
    }
    
    public static void Game3_Retry()
    {
        SendAllPlayer(Constants.PROTOCOL_GAME3_RETRY);
        
        if( g_evhGame3Activity != null ) {
            g_evhGame3Activity.sendEmptyMessage(Game3Activity.SessionEventHandler.WHAT_GAME_RETRY);
        }
    }
        
        
        
        
    
    
    
    
}

package wooks.hanjanhaeyo;

import android.os.Message;
import android.support.annotation.IntegerRes;
import android.util.Log;

/**
 * Created by in2etv on 2017-06-09.
 */

public class GuestWorks
{
    private static LobbyActivity.SessionEventHandler g_evhLobbyActivity;
    private static RoomGuestActivity.SessionEventHandler g_evhRoomGuestActivity;
    private static Game1Activity.SessionEventHandler g_evhGame1Activity;
    private static Game2Activity.SessionEventHandler g_evhGame2Activity;
    private static Game3Activity.SessionEventHandler g_evhGame3Activity;
    
    private static IRemoteClosedEventHandler g_evhRemoteClosedCallback = new IRemoteClosedEventHandler()
    {
        @Override
        public void OnRemoteClosed(GuestSession session)
        {
            /// 호스트 측의 접속 종료 처리
            if( g_evhLobbyActivity != null ) {
                g_evhLobbyActivity.sendEmptyMessage(LobbyActivity.SessionEventHandler.WHAT_HOST_DISCONNECTED);
            }
            if( g_evhRoomGuestActivity != null ) {
                g_evhRoomGuestActivity.sendEmptyMessage(RoomGuestActivity.SessionEventHandler.WHAT_HOST_DISCONNECTED);
            }
            if( g_evhGame1Activity != null ) {
                g_evhGame1Activity.sendEmptyMessage(Game1Activity.SessionEventHandler.WHAT_HOST_DISCONNECTED);
            }
            if( g_evhGame2Activity != null ) {
                g_evhGame2Activity.sendEmptyMessage(Game2Activity.SessionEventHandler.WHAT_HOST_DISCONNECTED);
            }
            if( g_evhGame3Activity != null ) {
                g_evhGame3Activity.sendEmptyMessage(Game3Activity.SessionEventHandler.WHAT_HOST_DISCONNECTED);
            }
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
                /// 게스트의 방 입장 응답
                case Constants.PROTOCOL_ROOM_JOIN_RESPONSE: {
                    if( sp[1].equals("OK") ) {
                        // TODO : 입장 허가 (룸 화면 이동)
                        if( g_evhLobbyActivity != null )
                            g_evhLobbyActivity.sendEmptyMessage(LobbyActivity.SessionEventHandler.WHAT_JOIN_ROOM_OK);
                        Log.d("QQQQQ", "입장 허가!");
                    }
                    else {
                        // TODO : 입장 실패
                        if( g_evhLobbyActivity != null )
                            g_evhLobbyActivity.sendEmptyMessage(LobbyActivity.SessionEventHandler.WHAT_JOIN_ROOM_NO);
                        Log.d("QQQQQ", "입장 실패!");
                    }
                } break;
                
                /// 플레이어 목록 갱신
                case Constants.PROTOCOL_ROOM_PLAYERLIST_UPDATE: {
                    if( g_evhRoomGuestActivity != null ) {
                        String[] sPlayerList = sp[1].split("#");
                        
                        Message msg = g_evhRoomGuestActivity.obtainMessage();
                        msg.what = RoomGuestActivity.SessionEventHandler.WHAT_UPDATE_PLAYER_LIST;
                        msg.obj = sPlayerList;
                        g_evhRoomGuestActivity.sendMessage(msg);
                    }
                } break;
                
                /// 게임 선택/변경 됨
                case Constants.PROTOCOL_ROOM_SELECTED_GAME_UPDATE: {
                    g_nSelectedGame = Integer.parseInt(sp[1]);
                    if( g_evhRoomGuestActivity != null )
                        g_evhRoomGuestActivity.sendEmptyMessage(RoomGuestActivity.SessionEventHandler.WHAT_UPDATE_SELECTED_GAME);
                } break;

                /// 게임 시작함
                case Constants.PROTOCOL_ROOM_GAME_START: {
                    g_nSelectedGame = Integer.parseInt(sp[1]);
                    if( g_evhRoomGuestActivity != null )
                        g_evhRoomGuestActivity.sendEmptyMessage(RoomGuestActivity.SessionEventHandler.WHAT_GAMESTART);
                } break;
                
                
                /// 대기실로 돌아감
                case Constants.PROTOCOL_GAME_RETURN_TO_ROOM: {
                    if( g_evhGame1Activity != null ) {
                        g_evhGame1Activity.sendEmptyMessage(Game1Activity.SessionEventHandler.WHAT_RETURN_TO_ROOM);
                    }
                    else if( g_evhGame2Activity != null ) {
                        g_evhGame2Activity.sendEmptyMessage(Game2Activity.SessionEventHandler.WHAT_RETURN_TO_ROOM);
                    }
                    else if( g_evhGame3Activity != null ) {
                        g_evhGame3Activity.sendEmptyMessage(Game3Activity.SessionEventHandler.WHAT_RETURN_TO_ROOM);
                    }
                } break;
                
                
                
                /// 게임1 - 게임 데이터 받아오기
                case Constants.PROTOCOL_GAME1_INIT_GAME_DATA: {
                    Log.d("QQQQQ", "헐1");
                    if( g_evhGame1Activity != null ) {
                        Log.d("QQQQQ", "헐2");
                        String[] sPlayerTurnList = sp[1].split("#");
                        int nMyTurn = Integer.parseInt(sp[2]);
                        int nBomb = Integer.parseInt(sp[3]);
                        Log.d("QQQQQ", "게임1 정보 받음");
                        Message msg = g_evhGame1Activity.obtainMessage();
                        msg.what = Game1Activity.SessionEventHandler.WHAT_RECEIVED_GAME_DATA;
                        msg.obj = sPlayerTurnList;
                        msg.arg1 = nMyTurn;
                        msg.arg2 = nBomb;
                        g_evhGame1Activity.sendMessage(msg);
                    }
                } break;
                
                /// 게임1 - 턴 흐름
                case Constants.PROTOCOL_GAME1_TURN_CHANGED: {
                    if( g_evhGame1Activity != null ) {
                        int nTurn = Integer.parseInt(sp[1]);
                        Message msg = g_evhGame1Activity.obtainMessage();
                        msg.what = Game1Activity.SessionEventHandler.WHAT_TURN_CHANGED;
                        msg.arg1 = nTurn;
                        g_evhGame1Activity.sendMessage(msg);
                    }
                } break;
                
                /// 게임1 - 플레이어의 입력 업데이트
                case Constants.PROTOCOL_GAME1_PLAYER_INPUT_UPDATE: {
                    if( g_evhGame1Activity != null ) {
                        int nTurn = Integer.parseInt(sp[1]);
                        int nTooth = Integer.parseInt(sp[2]);
                        Message msg = g_evhGame1Activity.obtainMessage();
                        msg.what = Game1Activity.SessionEventHandler.WHAT_PLAYER_INPUT_UPDATED;
                        msg.arg1 = nTurn;
                        msg.arg2 = nTooth;
                        g_evhGame1Activity.sendMessage(msg);
                    }
                } break;
                
                /// 게임1 - 게임 종료
                case Constants.PROTOCOL_GAME1_GAME_FINISH: {
                    if( g_evhGame1Activity != null ) {
                        int nLosePlayerTurn = Integer.parseInt(sp[1]);
                        Message msg = g_evhGame1Activity.obtainMessage();
                        msg.what = Game1Activity.SessionEventHandler.WHAT_GAME_FINISH;
                        msg.arg1 = nLosePlayerTurn;
                        g_evhGame1Activity.sendMessage(msg);
                    }
                } break;
                
                /// 게임1 - 재시작
                case Constants.PROTOCOL_GAME1_RETRY: {
                    if( g_evhGame1Activity != null ) {
                        g_evhGame1Activity.sendEmptyMessage(Game1Activity.SessionEventHandler.WHAT_GAME_RETRY);
                    }
                } break;


                /// 게임2 - 게임 데이터 받아오기
                case Constants.PROTOCOL_GAME2_INIT_GAME_DATA: {
                    if( g_evhGame2Activity != null ) {
                        String[] nicknameList = sp[1].split("#");
                        int nMyIdx = Integer.parseInt(sp[2]);
                        int nShape = Integer.parseInt(sp[3]);
                        Message msg = g_evhGame2Activity.obtainMessage();
                        msg.what = Game2Activity.SessionEventHandler.WHAT_RECEIVED_GAME_DATA;
                        msg.obj = nicknameList;
                        msg.arg1 = nMyIdx;
                        msg.arg2 = nShape;
                        g_evhGame2Activity.sendMessage(msg);
                    }
                } break;
                
                /// 게임2 - 입력 시작
                case Constants.PROTOCOL_GAME2_ON_SELECTION_TIME: {
                    if( g_evhGame2Activity != null ) {
                        g_evhGame2Activity.sendEmptyMessage(Game2Activity.SessionEventHandler.WHAT_ON_SELECTION_TIME);
                    }
                } break;
                
                /// 게임2 - 게임 종료 알림
                case Constants.PROTOCOL_GAME2_GAME_FINISH: {
                    if( g_evhGame2Activity != null ) {
                        Message msg = g_evhGame2Activity.obtainMessage();
                        msg.what = Game2Activity.SessionEventHandler.WHAT_GAME_FINISH;
                        msg.obj = sp[1];
                        g_evhGame2Activity.sendMessage(msg);
                    }
                } break;

                /// 게임2 - 재시작
                case Constants.PROTOCOL_GAME2_RETRY: {
                    if( g_evhGame2Activity != null ) {
                        g_evhGame2Activity.sendEmptyMessage(Game2Activity.SessionEventHandler.WHAT_GAME_RETRY);
                    }
                } break;
                
                
                /// 게임3 - 게임 데이터 받아오기
                case Constants.PROTOCOL_GAME3_INIT_GAME_DATA: {
                    if( g_evhGame3Activity != null ) {
                        String[] nicknameList = sp[1].split("#");
                        int nMyIdx = Integer.parseInt(sp[2]);
                        Message msg = g_evhGame3Activity.obtainMessage();
                        msg.what = Game3Activity.SessionEventHandler.WHAT_RECEIVED_GAME_DATA;
                        msg.obj = nicknameList;
                        msg.arg1 = nMyIdx;
                        g_evhGame3Activity.sendMessage(msg);
                    }
                } break;
                
                /// 게임3 - 1 to 25 시작 카운트다운
                case Constants.PROTOCOL_GAME3_1_TO_25_READY: {
                    if( g_evhGame3Activity != null ) {
                        int nReadyCountdown = Integer.parseInt(sp[1]);
                        Message msg = g_evhGame3Activity.obtainMessage();
                        msg.what = Game3Activity.SessionEventHandler.WHAT_1_TO_25_READY;
                        msg.arg1 = nReadyCountdown;
                        g_evhGame3Activity.sendMessage(msg);
                    }
                } break;

                /// 게임3 - 1 to 25 시작
                case Constants.PROTOCOL_GAME3_1_TO_25_START: {
                    if( g_evhGame3Activity != null ) {
                        g_evhGame3Activity.sendEmptyMessage(Game3Activity.SessionEventHandler.WHAT_1_TO_25_START);
                    }
                } break;
                
                case Constants.PROTOCOL_GAME3_PLAYER_INPUT_UPDATE: {
                    if( g_evhGame3Activity != null ) {
                        int nRemainPlayers = Integer.parseInt(sp[1]);
                        Message msg = g_evhGame3Activity.obtainMessage();
                        msg.what = Game3Activity.SessionEventHandler.WHAT_PLAYER_INPUT_UPDATED;
                        msg.arg1 = nRemainPlayers;
                        g_evhGame3Activity.sendMessage(msg);
                    }
                } break;
                
                case Constants.PROTOCOL_GAME3_GAME_FINISH: {
                    if( g_evhGame3Activity != null ) {
                        int nLoserIdx = Integer.parseInt(sp[1]);
                        Message msg = g_evhGame3Activity.obtainMessage();
                        msg.what = Game3Activity.SessionEventHandler.WHAT_GAME_FINISH;
                        msg.arg1 = nLoserIdx;
                        g_evhGame3Activity.sendMessage(msg);
                    }
                } break;
                
                case Constants.PROTOCOL_GAME3_RETRY: {
                    if( g_evhGame3Activity != null ) {
                        g_evhGame3Activity.sendEmptyMessage(Game3Activity.SessionEventHandler.WHAT_GAME_RETRY);
                    }
                } break;
                
                
                }
            }
        }
    };
    
    private static GuestSession g_session;
    private static int g_nSelectedGame;
    
    
    private GuestWorks() {}
    
    
    public static void Register_LobbyActivityEventHandler(LobbyActivity.SessionEventHandler evh)
    {
        g_evhLobbyActivity = evh;
    }
    public static void Register_RoomGuestActivityEventHandler(RoomGuestActivity.SessionEventHandler evh)
    {
        g_evhRoomGuestActivity = evh;
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
    
    
    public static int GetSelectedGame()
    {
        return g_nSelectedGame;
    }
    
    
    
    
    public static void JoinRoom(final String sHostname)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                g_session = new GuestSession();
                boolean bConnected = g_session.Connect(sHostname, Constants.ROOM_HOST_PORT);
    
                Log.d("QQQQQ", "호스트와의 연결 성공여부 : " + bConnected);
                if( bConnected ) {
                    g_session.Register_OnRemoteClosed(g_evhRemoteClosedCallback);
                    g_session.Register_OnMessageReceived(g_evhMessageReceivedCallback);
                    g_session.Send(Constants.PROTOCOL_ROOM_JOIN_REQUEST+"@"+Runtime.GetNickname());
                }
                else {
                    if( g_evhLobbyActivity != null ) {
                        g_evhLobbyActivity.sendEmptyMessage(LobbyActivity.SessionEventHandler.WHAT_HOST_CONNECT_FAIL);
                    }
                }
            }
        }).start();
    }
    
    public static void QuitRoom()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if( g_session != null ) {
                    g_session.Close();
                    g_session = null;
                }
            }
        }).start();
    }
    
    
    
    public static void RequestPlayerList()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if( g_session != null ) {
                    g_session.Send(Constants.PROTOCOL_ROOM_PLAYERLIST_REQUEST);
                }
            }
        }).start();
    }
    
    public static void RequestSelectedGame()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if( g_session != null ) {
                    g_session.Send(Constants.PROTOCOL_ROOM_SELECTED_GAME_REQUEST);
                }
            }
        }).start();
    }
    

    
    public static void Game1_GuestReady()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if( g_session != null ) {
                    g_session.Send(Constants.PROTOCOL_GAME1_GUEST_READY);
                }
            }
        }).start();
    }
    
    public static void Game1_InputToothButton(final int nMyTurn, final int nTooth)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if( g_session != null ) {
                    g_session.Send(Constants.PROTOCOL_GAME1_PLAYER_INPUT_REQUEST + "@" + nMyTurn +"@" + nTooth);
                }
            }
        }).start();
    }
    
    
    public static void Game2_GuestReady()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if( g_session != null ) {
                    g_session.Send(Constants.PROTOCOL_GAME2_GUEST_READY);
                }
            }
        }).start();
    }
    
    public static void Game2_PlayerSelection(final int nPlayerIdx, final int nSelection)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if( g_session != null ) {
                    g_session.Send(Constants.PROTOCOL_GAME2_PLAYER_INPUT + "@" + nPlayerIdx + "@" + nSelection);
                }
            }
        }).start();
    }
    
    
    public static void Game3_GuestReady()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if( g_session != null ) {
                    g_session.Send(Constants.PROTOCOL_GAME3_GUEST_READY);
                }
            }
        }).start();
    }
    
    public static void Game3_PlayerFinish(final int nPlayerIdx)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if( g_session != null ) {
                    g_session.Send(Constants.PROTOCOL_GAME3_PLAYER_INPUT_REQUEST + "@" + nPlayerIdx);
                }
            }
        }).start();
    }
    
}

package wooks.hanjanhaeyo;

/**
 * Created by in2etv on 2017-06-09.
 */

public class Constants
{
    public static final int BROADCAST_PORT = 12300;
    public static final int ROOM_HOST_PORT = 12301;
    
    
    public static final String PROTOCOL_ROOM_JOIN_REQUEST = "ROOM_JOIN_REQUEST";
    public static final String PROTOCOL_ROOM_JOIN_RESPONSE = "ROOM_JOIN_RESPONSE";
    
    public static final String PROTOCOL_ROOM_PLAYERLIST_REQUEST = "ROOM_PLAYERLIST_REQUEST";
    public static final String PROTOCOL_ROOM_PLAYERLIST_UPDATE = "ROOM_PLAYERLIST_UPDATE";
    
    public static final String PROTOCOL_ROOM_SELECTED_GAME_REQUEST = "ROOM_SELECTED_GAME_REQUEST";
    public static final String PROTOCOL_ROOM_SELECTED_GAME_UPDATE = "ROOM_SELECTED_GAME_UPDATE";
    
    public static final String PROTOCOL_ROOM_GAME_START = "ROOM_GAME_START";
    
    public static final String PROTOCOL_GAME_RETURN_TO_ROOM = "GAME_RETURN_TO_ROOM";
    
    
    public static final String PROTOCOL_GAME1_GUEST_READY = "GAME1_GUEST_READY";
    public static final String PROTOCOL_GAME1_INIT_GAME_DATA = "GAME1_INIT_GAME_DATA";
    public static final String PROTOCOL_GAME1_TURN_CHANGED = "GAME1_TURN_CHANGED";
    public static final String PROTOCOL_GAME1_PLAYER_INPUT_REQUEST = "GAME1_PLAYER_INPUT_REQUEST";
    public static final String PROTOCOL_GAME1_PLAYER_INPUT_UPDATE = "GAME1_PLAYER_INPUT_UPDATE";
    public static final String PROTOCOL_GAME1_GAME_FINISH = "GAME1_GAME_FINISH";
    public static final String PROTOCOL_GAME1_RETRY = "GAME1_RETRY";
    
    public static final String PROTOCOL_GAME2_GUEST_READY = "GAME2_GUEST_READY";
    public static final String PROTOCOL_GAME2_INIT_GAME_DATA = "GAME2_INIT_GAME_DATA";
    public static final String PROTOCOL_GAME2_ON_SELECTION_TIME = "GAME2_ON_SELECTION_TIME";
    public static final String PROTOCOL_GAME2_PLAYER_INPUT = "GAME2_PLAYER_INPUT";
    public static final String PROTOCOL_GAME2_GAME_FINISH = "GAME2_GAME_FINISH";
    public static final String PROTOCOL_GAME2_RETRY = "GAME2_RETRY";
    
    public static final String PROTOCOL_GAME3_GUEST_READY = "GAME3_GUEST_READY";
    public static final String PROTOCOL_GAME3_INIT_GAME_DATA = "GAME3_INIT_GAME_DATA";
    public static final String PROTOCOL_GAME3_1_TO_25_READY = "GAME3_1_TO_25_READY";
    public static final String PROTOCOL_GAME3_1_TO_25_START = "GAME3_1_TO_25_START";
    public static final String PROTOCOL_GAME3_PLAYER_INPUT_REQUEST = "GAME3_PLAYER_INPUT_REQUEST";
    public static final String PROTOCOL_GAME3_PLAYER_INPUT_UPDATE = "GAME3_PLAYER_INPUT_UPDATE";
    public static final String PROTOCOL_GAME3_GAME_FINISH = "GAME3_GAME_FINISH";
    public static final String PROTOCOL_GAME3_RETRY = "GAME3_RETRY";
    
    
    public static final int COUNTDOWN_GAME1 = 10;
    public static final int COUNTDOWN_GAME2 = 10;
    
    
    private Constants() {}
    
    
    
}

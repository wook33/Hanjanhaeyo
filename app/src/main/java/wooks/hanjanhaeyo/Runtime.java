package wooks.hanjanhaeyo;

/**
 * Created by in2etv on 2017-06-08.
 */

public class Runtime
{
    private static String g_sNickname;
    private static boolean g_bIsHost;
    
    
    private Runtime() {}
    
    
    public static String GetNickname()
    {
        return g_sNickname;
    }
    public static void SetNickname(String sNickname)
    {
        g_sNickname = sNickname;
    }
    
    public static boolean IsHost()
    {
        return g_bIsHost;
    }
    public static void SetHost(boolean bHost)
    {
        g_bIsHost = bHost;
    }
    
}

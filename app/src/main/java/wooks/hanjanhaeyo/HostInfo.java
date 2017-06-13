package wooks.hanjanhaeyo;

/**
 * Created by in2etv on 2017-06-08.
 */

public class HostInfo
{
    private String m_sHostname;
    private String m_sNickname;
    
    
    public HostInfo(String sHostname, String sNickname)
    {
        m_sHostname = sHostname;
        m_sNickname = sNickname;
    }
    
    public String GetHostname()
    {
        return m_sHostname;
    }
    
    public String GetNickname()
    {
        return m_sNickname;
    }
    
    
    @Override
    public String toString()
    {
        return m_sNickname;
    }
}

package wooks.hanjanhaeyo;

/**
 * Created by in2etv on 2017-06-09.
 */

public interface IMessageReceivedEventHandler
{
    void OnMessageReceived(GuestSession session, String sMessage);
}

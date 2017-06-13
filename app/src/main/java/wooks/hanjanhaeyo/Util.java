package wooks.hanjanhaeyo;

import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Created by in2etv on 2017-06-10.
 */

public class Util
{
    
    
    
    private Util() {}
    
    
    public static String[] SplitWithoutEmptyStrings(String sOriginal, String sRegex)
    {
        String[] sp = sOriginal.split(sRegex);
        ArrayList<String> list = new ArrayList<>();
        for(int i=0; i < sp.length; i++) {
            if( sp[i].equals("") == false )
                list.add(sp[i]);
        }
        return list.toArray(new String[list.size()]);
    }
    
}

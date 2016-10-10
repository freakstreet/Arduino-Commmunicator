package freakycamper.com.freaky.arduino_commmunicator.ComponentManagers;

/**
 * Created by lsa on 08/12/14.
 */
public class MainManager {

    SendTcListener _sendTcListener;

    public interface SendTcListener {
        public void sendTC(char[] data);
    }

    public void updateFromTM(char[] tm){};

    public MainManager(SendTcListener listener){
        _sendTcListener = listener;
    }

    protected void sendTc(char[] data){
        if (_sendTcListener != null) _sendTcListener.sendTC(data);
    }

    public SendTcListener getSendTcListener(){
        return _sendTcListener;
    }

    public String getStringFromTm(char[] tm)
    {
        String str = "";
        for (int i=0; i<tm.length; i++)
            str += " 0x" + Integer.toHexString(tm[i]);
        return str + "\n";
    }


}

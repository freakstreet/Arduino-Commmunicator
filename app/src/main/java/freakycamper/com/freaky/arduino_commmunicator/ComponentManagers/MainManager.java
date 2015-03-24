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


}

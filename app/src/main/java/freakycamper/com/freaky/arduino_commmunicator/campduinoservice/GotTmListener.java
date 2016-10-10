package freakycamper.com.freaky.arduino_commmunicator.campduinoservice;

/**
 * Created by lsa on 10/10/16.
 */

public interface GotTmListener {
    public void onReceivedRawTM(char[] data);
}
package freakycamper.com.freaky.arduino_commmunicator.utils;

/**
 * Created by lsa on 17/01/15.
 */
public class CharCircularFifoBuffer {


// TODO: Auto-generated Javadoc
/**
 * The Class CharCircularFifoBuffer. This is a class of char data fifo implement on a circular buffer.
 * This class is used by FTDI_SerialPortReadWriteTask class for data exchange between background task
 * of USB operation and foreground task to read and write data to the serial port.
 * This class is NOT thread safe because it doesn't exclusively lock the buffer to prevent asynchronous
 * readings/writings. But for one reading task and one writing task going on asynchronously, this class
 * is safe under this condition.
 */

    //TODO: need to define my own exceptions to cover the exceptional cases.

    /** The last. */
    //"first" is the index for first element.
    //"last" is the index for last element+1. or say, it points to the first empty slot.
    int last;

    /** The fifo buffer. */
    char[] mFifoBuffer;

    /**
     * Instantiates a new char circular fifo buffer.
     *
     * @param size the size
     */
    public CharCircularFifoBuffer(int size)
    {
        last = 0;
        //Always leave one extra items, for marking "buffer full".
        //and when buffer is full, the actual filled-in size equals to "size"
        mFifoBuffer = new char[size+1];
    }

    public void add(char c){
        mFifoBuffer[last++] = c;
    }

    public void add(char[] cs){
        for (int i=0; i<cs.length; i++)
            mFifoBuffer[i+last] = cs[i];
        last+= cs.length;
    }

    public void add(byte[] data)
    {
        for (byte b : data)
            add((char)b);
    }

    public char get(int pos){
        return getFrame(pos, 1)[0];
    }

    public char charAt(int pos){
        return mFifoBuffer[pos];
    }

    public char[] getFrame(int pos, int length){
        if ((pos+length-1)<mFifoBuffer.length) {
            char[] ret = new char[length];
            for (int i=0; i<length; i++)
                ret[i] = mFifoBuffer[pos+i];
            for (int i = pos; i < last; i++)
                mFifoBuffer[i] = mFifoBuffer[i + length];
            last -= length;

            return ret;
        }
        else return null;
    }

    public int getDataCount(){
        return last;
    }

    public void insert(char[] cs, int pos){
        if ((pos < last) && (cs.length+last <= mFifoBuffer.length))
        {
            for (int i=last; i<pos;i--) mFifoBuffer[i+cs.length] = mFifoBuffer[i];
            for (int i=pos; i<cs.length;i++) mFifoBuffer[i] = cs[i];
            last += cs.length;
        }
    }

    public void insert(char c, int pos){
        char[] cs = {c};
        insert(cs, pos);
    }


    public void reset()
    {
        last = 0;
    }
}


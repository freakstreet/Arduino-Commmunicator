package freakycamper.com.freaky.arduino_commmunicator.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.ToggleButton;

import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.ColdManager;
import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.ElectricalManager;
import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.HeatManager;
import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.LightManager;
import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.TemperatureManager;
import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.WaterManager;
import freakycamper.com.freaky.arduino_commmunicator.R;
import freakycamper.com.freaky.arduino_commmunicator.campduinoservice.CampDuinoProtocol;
import freakycamper.com.freaky.arduino_commmunicator.campduinoservice.GotTmListener;

/**
 * Created by lsa on 10/10/16.
 */

public class DialogTelemetryView extends Dialog implements GotTmListener {

    TextView tv;
    ToggleButton tbScrool;
    ToggleButton tbRawFormat;
    String addedText = "";

    private ElectricalManager managerElectrical = null;
    private LightManager managerLights = null;
    private HeatManager managerHeat = null;
    private TemperatureManager managerTemp = null;
    private ColdManager managerCold = null;
    private WaterManager managerWater = null;

    public DialogTelemetryView(Context context) {
        super(context, android.R.style.Theme_Holo_Dialog);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
        this.setContentView(R.layout.layout_tm_console );
        tv = (TextView)findViewById(R.id.txtTMTC);
        tbScrool = (ToggleButton)findViewById(R.id.btAutoScrool);
        tbRawFormat = (ToggleButton)findViewById(R.id.btDecodeTm);

        Button bt = (Button)findViewById(R.id.bt_TMTC_dialog_Clear);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv.setText("");
            }
        });

        tv.setMovementMethod(new ScrollingMovementMethod());
    }

    public void refresh()
    {
        if (addedText.length() == 0)
            return;
        tv.append(addedText);

        if (tbScrool.isChecked())
        {
            final Layout layout = tv.getLayout();
            if(layout != null){
                int scrollDelta = layout.getLineBottom(tv.getLineCount() - 1)
                        - tv.getScrollY() - tv.getHeight();
                if(scrollDelta > 0)
                    tv.scrollBy(0, scrollDelta);
            }
        }

        addedText = "";
        tv.invalidate();
    }

    public boolean keepAlive(){
        CheckBox cb = (CheckBox)findViewById(R.id.cbKeepAlive);
        return cb.isChecked();
    }

    public void showDialog(){
        this.show();
    }

    public void setElectricalManager(ElectricalManager mgr)
    {
        managerElectrical = mgr;
    }
    public void setLightManager(LightManager mgr)
    {
        managerLights = mgr;
    }
    public void setHeatManager(HeatManager mgr)
    {
        managerHeat = mgr;
    }
    public void setTemperatureManager(TemperatureManager mgr)
    {
        managerTemp = mgr;
    }
    public void setColdManager(ColdManager mgr)
    {
        managerCold = mgr;
    }

    public void setWaterManager(WaterManager mgr)
    {
        managerWater = mgr;
    }

    private boolean checkIsTmDisplayingChecked(char tmHeader)
    {
        switch (tmHeader)
        {
            case CampDuinoProtocol.TM_IS_ALIVE :
                return ((CheckBox)findViewById(R.id.cb_tm_Alive)).isChecked();
            case CampDuinoProtocol.TM_MIRROR_TC :
                return ((CheckBox)findViewById(R.id.cb_tm_Mirror)).isChecked();
            case CampDuinoProtocol.TM_CURRENT :
                return ((CheckBox)findViewById(R.id.cb_tm_Current)).isChecked();
            case CampDuinoProtocol.TM_TENSION :
                return ((CheckBox)findViewById(R.id.cb_tm_Tension)).isChecked();
            case CampDuinoProtocol.TM_TEMPERATURE :
                return ((CheckBox)findViewById(R.id.cb_tm_Temps)).isChecked();
            case CampDuinoProtocol.TM_WATER :
                return ((CheckBox)findViewById(R.id.cb_tm_Water)).isChecked();
            case CampDuinoProtocol.TM_RELAY :
                return ((CheckBox)findViewById(R.id.cb_tm_Relays)).isChecked();
            case CampDuinoProtocol.TM_LIGHT :
                return ((CheckBox)findViewById(R.id.cb_tm_Lights)).isChecked();
            case CampDuinoProtocol.TM_COLD_HOT :
                return ((CheckBox)findViewById(R.id.cb_tm_Cold_Hot)).isChecked();
            case CampDuinoProtocol.TM_ELEC_CONF :
                return ((CheckBox)findViewById(R.id.cb_tm_ElecConf)).isChecked();
            default :
                return true;
        }
    }

    @Override
    public void onReceivedRawTM(char[] tm) {
        // skip is TM display not checked in the GUI
        if (!checkIsTmDisplayingChecked(tm[0]))
            return;

        // then select the formatter
        if (!tbRawFormat.isChecked())
        {
            for (int i=0; i<tm.length; i++)
                addedText += "0x" +(tm[i]<= 0xF?"0":"") +  Integer.toHexString(tm[i]).toUpperCase() + " ";
        }
        else
        {
            switch (tm[0])
            {
                case CampDuinoProtocol.TM_IS_ALIVE:
                    addedText += CampDuinoProtocol.getStringFromTmAlive(tm);
                    break;

                case CampDuinoProtocol.TM_MIRROR_TC:
                    break;

                case CampDuinoProtocol.TM_LIGHT :
                    addedText += managerLights.getStringFromTm(tm);
                    break;

                case CampDuinoProtocol.TM_WATER:
                    addedText += managerWater.getStringFromTm(tm);
                    break;

                case CampDuinoProtocol.TM_CURRENT:
                case CampDuinoProtocol.TM_TENSION:
                case CampDuinoProtocol.TM_RELAY:
                case CampDuinoProtocol.TM_ELEC_CONF:
                    addedText += managerElectrical.getStringFromTm(tm);
                    break;

                case CampDuinoProtocol.TM_TEMPERATURE:
                    addedText += managerTemp.getStringFromTm(tm);
                    break;

                case CampDuinoProtocol.TM_COLD_HOT:
                    addedText += managerCold.getStringFromTm(tm);
                    addedText += managerHeat.getStringFromTm(tm);
                    break;

                default :
                    break;
            }
        }
        addedText +="\n";
    }
}

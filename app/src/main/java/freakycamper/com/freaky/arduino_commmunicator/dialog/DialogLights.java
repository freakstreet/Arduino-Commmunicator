package freakycamper.com.freaky.arduino_commmunicator.dialog;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ToggleButton;


import java.util.ArrayList;

import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.LightManager;
import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.MainManager;
import freakycamper.com.freaky.arduino_commmunicator.R;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.LightItem;
import freakycamper.com.freaky.arduino_commmunicator.campduinoservice.CampDuinoProtocol;
import freakycamper.com.freaky.arduino_commmunicator.gui.FreakyLightLedRgbToggleButton;

/**
 * Created by lsa on 01/10/14.
 */
public class DialogLights extends DialogPopUpDelayed implements
        FreakyLightLedRgbToggleButton.OnLightItemChangeAsked
{

    ArrayList<View> buttonsList;
    LightManager manager;

    public DialogLights(Context context, LightManager managerLight) {
        super(context, "Lights", R.layout.layout_lights, android.R.style.Theme_DeviceDefault);

        this.manager = managerLight;

        setDimensions(700, 380);
        //_onSendTc = manager.getSendTcListener();

        buttonsList = new ArrayList<View>();

        buttonsList.add(this.findViewById(R.id.light_bt1));
        buttonsList.add(this.findViewById(R.id.light_bt2));
        buttonsList.add(this.findViewById(R.id.light_bt3));
        buttonsList.add(this.findViewById(R.id.light_bt4));
        buttonsList.add(this.findViewById(R.id.light_bt5));
        buttonsList.add(this.findViewById(R.id.light_bt6));

        updateGui(manager);

        for (View v : buttonsList) {
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ToggleButton bt = (ToggleButton)v;
                    boolean status = bt.isChecked();
                    int idx = buttonsList.indexOf(v);
                    manager.getSendTcListener().sendTC(CampDuinoProtocol.buildSwitchLightTC(idx, (status ? 55 : 0), 255, 255, 255));
                }
            });
        }

        for (int i=0; i<manager.getLightCount(); i++)
        {
            FreakyLightLedRgbToggleButton bt = (FreakyLightLedRgbToggleButton)buttonsList.get(i);
            bt.connectToLightItem(manager.getLight(i));
            bt.setListener(this);

            switch(i)
            {
                case 0 :
                    bt.setText(R.string.light_name_1);
                    break;
                case 1 :
                    bt.setText(R.string.light_name_2);
                    break;
                case 2 :
                    bt.setText(R.string.light_name_3);
                    break;
                default :
                    bt.setText("unknown");
                    break;
            }


        }

        int nbButtons = Math.min(manager.getLightCount(), buttonsList.size());

    }

   public void updateGui(LightManager manager)
   {
       for(int i=0; i<buttonsList.size(); i++)
       {
           LightItem l = manager.getLight(i);

       }
   }

    @Override
    public void updateLightRGB(int rgbColor, int lightId) {
        manager.getSendTcListener().sendTC(new char[]{
                CampDuinoProtocol.PROT_TC_LIGHT,
                (char)lightId,
                (char)Color.red(rgbColor),
                (char)Color.green(rgbColor),
                (char)Color.blue(rgbColor)
        });
    }

    @Override
    public void updateLightDIMM(int dimmVAl, int lightId) {
        manager.getSendTcListener().sendTC(new char[]{
                CampDuinoProtocol.PROT_TC_LIGHT,
                (char)lightId,
                (char)dimmVAl
        });
    }

    @Override
    public void updateLightSwitch(boolean isOn, int lightId) {
        manager.getSendTcListener().sendTC(new char[]{
                CampDuinoProtocol.PROT_TC_LIGHT,
                (char)lightId,
                (char)(isOn?255:0)
        });
    }
}

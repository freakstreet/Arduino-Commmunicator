package freakycamper.com.freaky.arduino_commmunicator.dialog;

import android.content.Context;
import android.view.View;
import android.widget.ToggleButton;


import java.util.ArrayList;

import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.LightManager;
import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.MainManager;
import freakycamper.com.freaky.arduino_commmunicator.R;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.LightItem;
import freakycamper.com.freaky.arduino_commmunicator.campduinoservice.CampDuinoProtocol;

/**
 * Created by lsa on 01/10/14.
 */
public class DialogLights extends DialogPopUpDelayed{

    ArrayList<View> buttonsList;
    MainManager.SendTcListener _onSendTc;

    public DialogLights(Context context, LightManager manager) {
        super(context, "Lights", R.layout.layout_lights, android.R.style.Theme_DeviceDefault);

        setDimensions(700, 380);
        _onSendTc = manager.getSendTcListener();

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
                    _onSendTc.sendTC(CampDuinoProtocol.buildSwitchLightTC(idx, (status ? 55 : 0), 255, 255, 255));
                }
            });
        }

        int nbButtons = Math.min(manager.getLightCount(), buttonsList.size());

        for (int i=0; i<nbButtons; i++){
            ToggleButton b = (ToggleButton)buttonsList.get(i);
            LightItem l = manager.getLight(i);
            boolean status = l.getIsOn();
            b.setChecked(status);
        }

    }

   public void updateGui(LightManager manager){
       for(int i=0; i<buttonsList.size(); i++){
           LightItem l = manager.getLight(i);
           if (l==null) buttonsList.get(i).setEnabled(false);
           else {
               if (l.getLightType() == LightItem.eLightTypes.NORMAL_ON_OFF) {
                   buttonsList.get(i).setEnabled(true);
                   ToggleButton tbt = (ToggleButton)buttonsList.get(i);
                   tbt.setChecked(l.getIsOn());
               }
               else buttonsList.get(i).setEnabled(false);
           }
       }
   }

}

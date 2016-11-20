package freakycamper.com.freaky.arduino_commmunicator.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.ElectricalManager;
import freakycamper.com.freaky.arduino_commmunicator.R;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.ElectricalItem;
import freakycamper.com.freaky.arduino_commmunicator.campduinoservice.CampDuinoProtocol;
import freakycamper.com.freaky.arduino_commmunicator.gui.CircleButton;
/**
 * Created by lsa on 29/01/15.
 */
public class DialogElectrical extends DialogPopUpDelayed implements ElectricalManager.ListenerRelayModuleUpdate {

    private ElectricalManager elec = null;


    public DialogElectrical(Context context, final ElectricalManager manager) {
        super(context, context.getText(R.string.dialog_elec).toString(), R.layout.layout_electrical, android.R.style.Theme_Holo_Dialog_NoActionBar);

        String lbl;
        setDimensions(490, 175);
        elec = manager;

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.dimAmount = 0.7f;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        CircleButton b;
        TextView t;

        b = (CircleButton)findViewById(R.id.bt_relay_water);
        t = (TextView)findViewById(R.id.text_relay_water) ;
        setMonitoredComponent(b);
        setMonitoredComponent(t);

        View.OnClickListener ocl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                elec.getSendTcListener().sendTC(CampDuinoProtocol.buildSwitchRelayTC(CampDuinoProtocol.eProtTcSwitch.PROT_SWITCH_WATER_MODULE, !elec.getRelayStatus(ElectricalItem.eRelayType.R_WATER)));
            }
        };

        b.setOnClickListener(ocl);
        t.setOnClickListener(ocl);



        b = (CircleButton)findViewById(R.id.bt_relay_cold);
        t = (TextView)findViewById(R.id.text_relay_cold) ;
        setMonitoredComponent(b);
        setMonitoredComponent(t);

        ocl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                elec.getSendTcListener().sendTC(CampDuinoProtocol.buildSwitchRelayTC(CampDuinoProtocol.eProtTcSwitch.PROT_SWITCH_COLD_MODULE, !elec.getRelayStatus(ElectricalItem.eRelayType.R_COLD)));
            }
        };
        b.setOnClickListener(ocl);
        t.setOnClickListener(ocl);


        b = (CircleButton)findViewById(R.id.bt_relay_heater);
        t = (TextView)findViewById(R.id.text_relay_heater);
        setMonitoredComponent(b);
        setMonitoredComponent(t);

        ocl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                elec.getSendTcListener().sendTC(CampDuinoProtocol.buildSwitchRelayTC(CampDuinoProtocol.eProtTcSwitch.PROT_SWITCH_HEAT_MODULE, !elec.getRelayStatus(ElectricalItem.eRelayType.R_HEATER)));
            }
        };
        b.setOnClickListener(ocl);
        t.setOnClickListener(ocl);

        b = (CircleButton)findViewById(R.id.bt_relay_light);
        t = (TextView)findViewById(R.id.text_relay_light);
        setMonitoredComponent(b);
        setMonitoredComponent(t);

        ocl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                elec.getSendTcListener().sendTC(CampDuinoProtocol.buildSwitchRelayTC(CampDuinoProtocol.eProtTcSwitch.PROT_SWITCH_LIGHT_MODULE, !elec.getRelayStatus(ElectricalItem.eRelayType.R_LIGHT)));
            }
        };
        b.setOnClickListener(ocl);
        t.setOnClickListener(ocl);

        b = (CircleButton)findViewById(R.id.bt_relay_aux);
        t = (TextView)findViewById(R.id.text_relay_aux);
        setMonitoredComponent(b);
        setMonitoredComponent(t);

        ocl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                elec.getSendTcListener().sendTC(CampDuinoProtocol.buildSwitchRelayTC(CampDuinoProtocol.eProtTcSwitch.PROT_SWITCH_AUX_MODULE, !elec.getRelayStatus(ElectricalItem.eRelayType.R_AUX)));
            }
        };
        b.setOnClickListener(ocl);
        t.setOnClickListener(ocl);

        b = (CircleButton)findViewById(R.id.bt_relay_spare);
        t = (TextView)findViewById(R.id.text_relay_spare);
        setMonitoredComponent(b);
        setMonitoredComponent(t);

        ocl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                elec.getSendTcListener().sendTC(CampDuinoProtocol.buildSwitchRelayTC(CampDuinoProtocol.eProtTcSwitch.PROT_SWITCH_SPARE_MODULE, !elec.getRelayStatus(ElectricalItem.eRelayType.R_SPARE)));
            }
        };
        b.setOnClickListener(ocl);
        t.setOnClickListener(ocl);

        b.setColor(getContext().getResources().getColor(android.R.color.darker_gray));
        b.setImageDrawable(null);

        this.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                removeManagerEventListener();
            }
        });

        updateGui();
    }

    private void removeManagerEventListener(){
        elec.removeRelayModuleListener(this);
    }

    public void updateGui(){

        CircleButton b;
        b = (CircleButton)findViewById(R.id.bt_relay_water);
        setCheckStatus(b, elec.getRelayStatus(ElectricalItem.eRelayType.R_WATER));

        b = (CircleButton)findViewById(R.id.bt_relay_cold);
        setCheckStatus(b, elec.getRelayStatus(ElectricalItem.eRelayType.R_COLD));

        b = (CircleButton)findViewById(R.id.bt_relay_heater);
        setCheckStatus(b, elec.getRelayStatus(ElectricalItem.eRelayType.R_HEATER));

        b = (CircleButton)findViewById(R.id.bt_relay_light);
        setCheckStatus(b, elec.getRelayStatus(ElectricalItem.eRelayType.R_LIGHT));

        b = (CircleButton)findViewById(R.id.bt_relay_aux);
        setCheckStatus(b, elec.getRelayStatus(ElectricalItem.eRelayType.R_AUX));

        b = (CircleButton)findViewById(R.id.bt_relay_spare);
        b.setEnabled(false);

    }

    public void setCheckStatus(CircleButton bt, boolean checked)
    {
        if (checked) {
            bt.setImageResource(R.drawable.ic_action_tick);
            bt.setColor(getContext().getResources().getColor(android.R.color.holo_green_light));
        }
        else {
            bt.setImageDrawable(null);
            bt.setColor(getContext().getResources().getColor(android.R.color.holo_red_light));
        }
    }

    @Override
    public void relayModuleUpdated() {
        updateGui();
    }



}

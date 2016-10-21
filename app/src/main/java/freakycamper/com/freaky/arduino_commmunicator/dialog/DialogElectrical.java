package freakycamper.com.freaky.arduino_commmunicator.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.WindowManager;

import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.ColdManager;
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
        super(context, context.getText(R.string.dialog_elec).toString(), R.layout.layout_electrical, android.R.style.Theme_DeviceDefault);

        String lbl;
        setDimensions(500, 260);
        elec = manager;

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.dimAmount = 0.7f;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        CircleButton b;
        b = (CircleButton)findViewById(R.id.bt_relay_water);
        lbl = context.getText(R.string.elec_water_module).toString();
        customizeButton(b, lbl);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                elec.getSendTcListener().sendTC(CampDuinoProtocol.buildSwitchRelayTC(CampDuinoProtocol.eProtTcSwitch.PROT_SWITCH_WATER_MODULE, !elec.getRelayStatus(ElectricalItem.eRelayType.R_WATER)));
            }
        });

        b = (CircleButton)findViewById(R.id.bt_relay_cold);
        lbl = context.getText(R.string.elec_cold_module).toString();
        customizeButton(b, lbl);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean status = elec.getRelayStatus(ElectricalItem.eRelayType.R_COLD);
                elec.getSendTcListener().sendTC(CampDuinoProtocol.buildSwitchRelayTC(CampDuinoProtocol.eProtTcSwitch.PROT_SWITCH_COLD_MODULE, !status));
                if (!status){
                    // sends the default temperature to manage the fridge
                    elec.getSendTcListener().sendTC(CampDuinoProtocol.buildSetFridgeConsigne(ColdManager.DEFAULT_TEMP_FRIDGE));
                }
            }
        });

        b = (CircleButton)findViewById(R.id.bt_relay_heater);
        lbl = context.getText(R.string.elec_heat_module).toString();
        customizeButton(b, lbl);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // heater button is only used to show relay status, not direct action outside the heater dialog
                elec.getSendTcListener().sendTC(CampDuinoProtocol.buildSwitchRelayTC(CampDuinoProtocol.eProtTcSwitch.PROT_SWITCH_HEAT_MODULE, !elec.getRelayStatus(ElectricalItem.eRelayType.R_HEATER)));
            }
        });

        b = (CircleButton)findViewById(R.id.bt_relay_light);
        lbl = context.getText(R.string.elec_light_module).toString();
        customizeButton(b, lbl);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                elec.getSendTcListener().sendTC(CampDuinoProtocol.buildSwitchRelayTC(CampDuinoProtocol.eProtTcSwitch.PROT_SWITCH_LIGHT_MODULE, !elec.getRelayStatus(ElectricalItem.eRelayType.R_LIGHT)));
            }
        });

        b = (CircleButton)findViewById(R.id.bt_relay_aux);
        lbl = context.getText(R.string.elec_aux_module).toString();
        customizeButton(b, lbl);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                elec.getSendTcListener().sendTC(CampDuinoProtocol.buildSwitchRelayTC(CampDuinoProtocol.eProtTcSwitch.PROT_SWITCH_AUX_MODULE, !elec.getRelayStatus(ElectricalItem.eRelayType.R_AUX)));
            }
        });

        b = (CircleButton)findViewById(R.id.bt_relay_spare);
        b.setColor(getContext().getResources().getColor(android.R.color.darker_gray));
        b.setImageDrawable(null);
        lbl = context.getText(R.string.elec_spare_module).toString();
        customizeButton(b, lbl);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                elec.getSendTcListener().sendTC(CampDuinoProtocol.buildSwitchRelayTC(CampDuinoProtocol.eProtTcSwitch.PROT_SWITCH_SPARE_MODULE, !elec.getRelayStatus(ElectricalItem.eRelayType.R_SPARE)));
            }
        });

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

    private void customizeButton(CircleButton b, String lbl){
        Context ctx= getContext();
 /*       b.setTypeface(FontUtils.loadFontFromAssets(ctx, FontUtils.FONT_DOSIS_MEDIUM));
        b.setText(lbl);
        b.setTextSize(20);
        b.setTextOn(lbl + "\n" + ctx.getText(R.string.elec_activated_module));
        b.setTextOff(lbl + "\n" + ctx.getText(R.string.elec_desactivated_module));*/
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

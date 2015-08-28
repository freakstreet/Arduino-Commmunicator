package freakycamper.com.freaky.arduino_commmunicator.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.ColdManager;
import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.ElectricalManager;
import freakycamper.com.freaky.arduino_commmunicator.R;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.ElectricalItem;
import freakycamper.com.freaky.arduino_commmunicator.campduinoservice.CampDuinoProtocol;
import freakycamper.com.freaky.arduino_commmunicator.utils.FontUtils;

/**
 * Created by lsa on 29/01/15.
 */
public class DialogElectrical extends DialogPopUpDelayed implements ElectricalManager.ListenerRelayModuleUpdate {

    private ElectricalManager elec = null;


    public DialogElectrical(Context context, final ElectricalManager manager) {
        super(context, context.getText(R.string.dialog_elec).toString(), R.layout.layout_electrical, android.R.style.Theme_DeviceDefault);

        String lbl;
        setDimensions(400, 350);
        elec = manager;

        ToggleButton b;
        b = (ToggleButton)findViewById(R.id.bt_relay_water);
        lbl = context.getText(R.string.elec_water_module).toString();
        customizeButton(b, lbl);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                elec.getSendTcListener().sendTC(CampDuinoProtocol.buildSwitchRelayTC(CampDuinoProtocol.eProtTcSwitch.PROT_SWITCH_WATER_MODULE, !elec.getRelayStatus(ElectricalItem.eRelayType.R_WATER)));
            }
        });

        b = (ToggleButton)findViewById(R.id.bt_relay_cold);
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

        b = (ToggleButton)findViewById(R.id.bt_relay_heater);
        lbl = context.getText(R.string.elec_heat_module).toString();
        customizeButton(b, lbl);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // heater button is only used to show relay status, not direct action outside the heater dialog
                boolean status = elec.getRelayStatus(ElectricalItem.eRelayType.R_HEATER);
                elec.getSendTcListener().sendTC(CampDuinoProtocol.buildSwitchRelayTC(CampDuinoProtocol.eProtTcSwitch.PROT_SWITCH_HEAT_MODULE, !status));
            }
        });

        b = (ToggleButton)findViewById(R.id.bt_relay_light);
        lbl = context.getText(R.string.elec_light_module).toString();
        customizeButton(b, lbl);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                elec.switchLightningFunction();
            }
        });

        b = (ToggleButton)findViewById(R.id.bt_relay_aux);
        lbl = context.getText(R.string.elec_aux_module).toString();
        customizeButton(b, lbl);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                elec.getSendTcListener().sendTC(CampDuinoProtocol.buildSwitchRelayTC(CampDuinoProtocol.eProtTcSwitch.PROT_SWITCH_AUX_MODULE, !elec.getRelayStatus(ElectricalItem.eRelayType.R_AUX)));
            }
        });

        b = (ToggleButton)findViewById(R.id.bt_relay_spare);
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

    private void customizeButton(ToggleButton b, String lbl){
        Context ctx= getContext();
        b.setTypeface(FontUtils.loadFontFromAssets(ctx, FontUtils.FONT_DOSIS_MEDIUM));
        b.setText(lbl);
        b.setTextSize(20);
        b.setTextOn(lbl + "\n" + ctx.getText(R.string.elec_activated_module));
        b.setTextOff(lbl + "\n" + ctx.getText(R.string.elec_desactivated_module));
    }

    public void updateGui(){

        ToggleButton b;
        b = (ToggleButton)findViewById(R.id.bt_relay_water);
        b.setChecked(elec.getRelayStatus(ElectricalItem.eRelayType.R_WATER));

        b = (ToggleButton)findViewById(R.id.bt_relay_cold);
        b.setChecked(elec.getRelayStatus(ElectricalItem.eRelayType.R_COLD));

        b = (ToggleButton)findViewById(R.id.bt_relay_heater);
        b.setChecked(elec.getRelayStatus(ElectricalItem.eRelayType.R_HEATER));

        b = (ToggleButton)findViewById(R.id.bt_relay_light);
        b.setChecked(elec.getRelayStatus(ElectricalItem.eRelayType.R_LIGHT));

        b = (ToggleButton)findViewById(R.id.bt_relay_aux);
        b.setChecked(elec.getRelayStatus(ElectricalItem.eRelayType.R_AUX));

        b = (ToggleButton)findViewById(R.id.bt_relay_spare);
        b.setEnabled(false);

    }

    @Override
    public void relayModuleUpdated() {
        updateGui();
    }

}

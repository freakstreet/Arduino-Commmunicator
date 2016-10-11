package freakycamper.com.freaky.arduino_commmunicator.dialog;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.HeatManager;
import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.MainManager;
import freakycamper.com.freaky.arduino_commmunicator.R;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.HeatItem;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.TemperatureItem;
import freakycamper.com.freaky.arduino_commmunicator.campduinoservice.CampDuinoProtocol;
import freakycamper.com.freaky.arduino_commmunicator.utils.FontUtils;


/**
 * Created by lsa on 17/12/14.
 */
public class DialogHeat extends DialogPopUpDelayed implements AdapterView.OnItemSelectedListener {

    // NOTE : the min allowable value for fan pwm is arbitrary set to 100
    private static int PWM_OFFSET = 100;
    private static int TEMP_OFFSET = 10;

    MainManager.SendTcListener TcSender;
    HeatItem _guiParams, _instantParams, _manuParams, _autoParams;
    float[] _infoTemp;

    public DialogHeat(final Context context, HeatManager manager, float[] temps) {
        super(context, context.getText(R.string.dialog_heat).toString(), R.layout.layout_heater, android.R.style.Theme_DeviceDefault);

        TcSender = manager.getSendTcListener();
        _guiParams = manager.getHeatParams();
        _instantParams = new HeatItem(_guiParams);

        _infoTemp = temps;


        // Spinner initialisation
        Spinner sp = (Spinner)findViewById(R.id.spinner_heat_mode);
        setMonitoredComponent(sp);
        List<String> spinnerArray = new ArrayList<String>();
        for (String s : HeatItem.STR_HEAT_MODE_TEXT){
            spinnerArray.add(s);
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, spinnerArray){
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                Typeface externalFont = FontUtils.loadFontFromAssets(getContext(), FontUtils.FONT_DOSIS_LIGHT);
                ((TextView) v).setTypeface(externalFont);
                ((TextView) v).setTextSize(24);
                return v;
            }
            public TextView getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView v = (TextView) super.getView(position, convertView, parent);
                v.setTypeface(FontUtils.loadFontFromAssets(getContext(), FontUtils.FONT_DOSIS_LIGHT));
                v.setTextSize(24);
                v.setHeight(40);
                v.setGravity(Gravity.CENTER_VERTICAL);
                return v;
            }
        };

        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down vieww
        sp.setAdapter(spinnerArrayAdapter);
        sp.setOnItemSelectedListener(this);

        spinnerChanged(_guiParams.getStatus());

        setMonitoredComponent(findViewById(R.id.text_heat_t1));
        setMonitoredComponent(findViewById(R.id.text_heat_t2));
        setMonitoredComponent(findViewById(R.id.text_heat_status));

        SeekBar sb = (SeekBar)findViewById(R.id.seek_z1);
        sb.setMax(255-PWM_OFFSET);
        sb.setProgress(_guiParams.getFanSpeed(0));
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                changePWMTarget(progress + PWM_OFFSET, true);
            }
        });
        changePWMTarget(_guiParams.getFanSpeed(0), true);
        setMonitoredComponent(sb);

        Switch sw = (Switch)findViewById(R.id.switch_z1);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setParamManualMode(isChecked, true);
                if (_guiParams.getFanSpeed(0) < PWM_OFFSET)
                    changePWMTarget(PWM_OFFSET, true);
                else
                    changePWMTarget(_guiParams.getFanSpeed(0), true);
                if (!isChecked){
                    Button bt = (Button)findViewById(R.id.button_heat_manual_z1);
                    if (_instantParams.getFanSpeed(0) >= PWM_OFFSET){
                        _guiParams.updateFanVal((char)0, true);
                        bt.setText(context.getText(R.string.heater_stop_fan));
                        bt.setEnabled(true);
                    }
                }
            }
        });
        sw.setChecked(_guiParams.getFanSpeed(0)>=PWM_OFFSET);
        sb.setEnabled(sw.isChecked());
        Button bt = (Button)findViewById(R.id.button_heat_manual_z1);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionSendTc(new HeatItem(
                        _guiParams.getStatus(),
                        _instantParams.getTempConsigne(),
                        (char)_guiParams.getFanSpeed(0),
                        (char)_guiParams.getFanSpeed(1)
                ));
            }
        });
        bt.setEnabled(sb.isEnabled());
        setMonitoredComponent(bt);
        setMonitoredComponent(sw);

        sb = (SeekBar)findViewById(R.id.seek_z2);
        sb.setMax(255-PWM_OFFSET);
        sb.setProgress(_guiParams.getFanSpeed(1));
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                changePWMTarget(progress + PWM_OFFSET, false);
            }
        });
        changePWMTarget(_guiParams.getFanSpeed(1), false);
        setMonitoredComponent(sb);

        sw = (Switch)findViewById(R.id.switch_z2);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setParamManualMode(isChecked, false);
                if (_guiParams.getFanSpeed(1) < PWM_OFFSET)
                    changePWMTarget(PWM_OFFSET, false);
                else
                    changePWMTarget(_guiParams.getFanSpeed(1), false);
                if (!isChecked){
                    Button bt = (Button)findViewById(R.id.button_heat_manual_z2);
                    if (_instantParams.getFanSpeed(1) >= PWM_OFFSET){
                        _guiParams.updateFanVal((char)1, false);
                        bt.setText(context.getText(R.string.heater_stop_fan));
                        bt.setEnabled(true);
                    }
                }
            }
        });

        sw.setChecked(_guiParams.getFanSpeed(1)>= PWM_OFFSET);
        sb.setEnabled(sw.isChecked());
        bt = (Button)findViewById(R.id.button_heat_manual_z2);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionSendTc(new HeatItem(
                        _guiParams.getStatus(),
                        _instantParams.getTempConsigne(),
                        (char)_guiParams.getFanSpeed(0),
                        (char)_guiParams.getFanSpeed(1)
                ));
            }
        });
        bt.setEnabled(sb.isEnabled());
        setMonitoredComponent(bt);
        setMonitoredComponent(sw);

        sb = (SeekBar)findViewById(R.id.seek_heat_auto);
        sb.setMax(25-TEMP_OFFSET);
        sb.setProgress(Math.max(TEMP_OFFSET, Math.round(_guiParams.getTempConsigne())));
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int newVal = seekBar.getProgress() + TEMP_OFFSET;
                Button bt = (Button)findViewById(R.id.button_heat_auto);
                changeTempTarget(seekBar, bt, newVal);
            }
        });
        changeTempTarget(sb, (Button)findViewById(R.id.button_heat_auto), Math.round(_guiParams.getTempConsigne()));
        setMonitoredComponent(sb);

        bt = (Button)findViewById(R.id.button_heat_auto);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionSendTc(new HeatItem(
                        _guiParams.getStatus(),
                        _guiParams.getTempConsigne(),
                        (char)_instantParams.getFanSpeed(0),
                        (char)_instantParams.getFanSpeed(1)
                ));
            }
        });
        setMonitoredComponent(bt);

        bt = (Button)findViewById(R.id.button_dismiss_heater);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionSendTc(new HeatItem(
                        HeatItem.eHeatModuleState.HEAT_MODULE_OFF,
                        _instantParams.getTempConsigne(),
                        (char)_instantParams.getFanSpeed(0),
                        (char)_instantParams.getFanSpeed(1)
                ));
                v.setVisibility(LinearLayout.GONE);
            }
        });
        setMonitoredComponent(bt);

        customizeComponents();
    }

    private void changePWMTarget(int newVal, boolean zone1){
        int ratio = newVal*100/255;
        Button bt;
        if (zone1) bt = (Button) findViewById(R.id.button_heat_manual_z1);
        else bt = (Button) findViewById(R.id.button_heat_manual_z2);
        bt.setText(getContext().getText(R.string.heater_zone) + (zone1 ? "1" : "2") + " : " + Integer.toString(ratio) + "%");
        _guiParams.updateFanVal((char)newVal,zone1);
        bt.setEnabled(_guiParams.getFanSpeed(zone1?0:1) != _instantParams.getFanSpeed(zone1?0:1));
    }

    private void setParamManualMode(boolean pwmActivated, boolean isZ1){
        Button bt;
        SeekBar sb;
        if (isZ1) {
            bt = (Button) findViewById(R.id.button_heat_manual_z1);
            sb = (SeekBar)findViewById(R.id.seek_z1);
        }
        else {
            bt = (Button) findViewById(R.id.button_heat_manual_z2);
            sb = (SeekBar)findViewById(R.id.seek_z2);
        }
        bt.setEnabled(pwmActivated);
        sb.setEnabled(pwmActivated);
    }

    private void changeTempTarget(SeekBar sb, Button bt, int newVal){
        bt.setText(getContext().getText(R.string.heater_target) + " : " + newVal + "°C");
        _guiParams.updateTempTarget(newVal);
    }

    public void updateHeatDialog(float newTemp[]){
        float t1, t2;
        t1 = newTemp[0];
        t2 = newTemp[1];

        TextView t = (TextView)findViewById(R.id.text_heat_t1);
        t.setText(TemperatureItem.getTemperatureStr(t1));

        t = (TextView)findViewById(R.id.text_heat_t2);
        t.setText(TemperatureItem.getTemperatureStr(t2));

    }

    private void actionSendTc(HeatItem param){
        char[] tc = new char[6];
        char tmp[] = CampDuinoProtocol.encodeFloatToTm(param.getTempConsigne());
        tc[0] = (char) CampDuinoProtocol.PROT_TC_HEATER;

        tc[1] = (char)param.getStatus().value;
        tc[2] = tmp[0];
        tc[3] = tmp[1];
        tc[4] = (char)param.getFanSpeed(0);
        tc[5] = (char)param.getFanSpeed(1);

        TcSender.sendTC(tc);
    }

    //**** interface actions ****

    private void onSpinClicked(int position){
        HeatItem.eHeatModuleState state = HeatItem.eHeatModuleState.values()[position];
        if (state != _guiParams.getStatus()){
            spinnerChanged(state);
        }
    }

   private void spinnerChanged(HeatItem.eHeatModuleState newState){
       int h = 200;
       LinearLayout lAuto, lManu;
       lAuto = (LinearLayout)findViewById(R.id.layout_heat_auto);
       lManu = (LinearLayout)findViewById(R.id.layout_heat_manual);

       Spinner sp = (Spinner)findViewById(R.id.spinner_heat_mode);
       sp.setSelection(newState.value);

       Button bt = (Button)findViewById(R.id.button_dismiss_heater);

       switch (newState){
           case HEAT_MODULE_OFF:
               h = 100;
               lAuto.setVisibility(LinearLayout.GONE);
               lManu.setVisibility(LinearLayout.GONE);

               if (_instantParams.getStatus() != HeatItem.eHeatModuleState.HEAT_MODULE_OFF)
                    bt.setVisibility(LinearLayout.VISIBLE);
                    //bt.setEnabled(true);
               else
                    bt.setVisibility(LinearLayout.GONE);
                    //bt.setEnabled(false);
               break;

           case HEAT_MODULE_HEAT_AUTO:
           case HEAT_MODULE_VENT_AUTO:
               h=170;
               lAuto.setVisibility(LinearLayout.VISIBLE);
               lManu.setVisibility(LinearLayout.GONE);
               bt.setVisibility(LinearLayout.GONE);
               break;

           case HEAT_MODULE_HEAT_MANUAL:
           case HEAT_MODULE_VENT_MANUAL:
               h=200;
               lAuto.setVisibility(LinearLayout.GONE);
               lManu.setVisibility(LinearLayout.VISIBLE);
               bt.setVisibility(LinearLayout.GONE);
               break;
       }
       setDimensions(400, h);
       _guiParams.updateState(newState);
   }

    public void updateFromTm(HeatItem newHeat){
        _instantParams = newHeat;

        Button btZ1, btZ2, btAuto;
        btZ1 = (Button)findViewById(R.id.button_heat_manual_z1);
        btZ2 = (Button)findViewById(R.id.button_heat_manual_z2);
        btAuto = (Button)findViewById(R.id.button_heat_auto);

        btZ1.setEnabled(_instantParams.getFanSpeed(0) != _guiParams.getFanSpeed(0));
        btZ2.setEnabled(_instantParams.getFanSpeed(1) != _guiParams.getFanSpeed(1));
        btAuto.setEnabled(_instantParams.getTempConsigne() != _guiParams.getTempConsigne());
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        onSpinClicked(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void customizeComponents(){
        Context context = getContext();

        Switch sw = (Switch)findViewById(R.id.switch_z1);
        sw.setTypeface(FontUtils.loadFontFromAssets(context, FontUtils.FONT_DOSIS_LIGHT));
        sw.setSwitchTypeface(sw.getTypeface());
        sw.setTextSize(20);

        sw = (Switch)findViewById(R.id.switch_z2);
        sw.setTypeface(FontUtils.loadFontFromAssets(context, FontUtils.FONT_DOSIS_LIGHT));
        sw.setSwitchTypeface(sw.getTypeface());
        sw.setTextSize(20);

        Button bt = (Button)findViewById(R.id.button_heat_manual_z1);
        bt.setTypeface(FontUtils.loadFontFromAssets(context, FontUtils.FONT_DOSIS_MEDIUM));
        bt.setTextSize(18);

        bt = (Button)findViewById(R.id.button_heat_manual_z2);
        bt.setTypeface(FontUtils.loadFontFromAssets(context, FontUtils.FONT_DOSIS_MEDIUM));
        bt.setTextSize(18);

        bt = (Button)findViewById(R.id.button_heat_auto);
        bt.setTypeface(FontUtils.loadFontFromAssets(context, FontUtils.FONT_DOSIS_MEDIUM));
        bt.setTextSize(20);

        bt = (Button)findViewById(R.id.button_dismiss_heater);
        bt.setTypeface(FontUtils.loadFontFromAssets(context, FontUtils.FONT_DOSIS_MEDIUM));
        bt.setTextSize(18);

        TextView v = (TextView)findViewById(R.id.text_heat_t1);
        v.setTypeface(FontUtils.loadFontFromAssets(context, FontUtils.FONT_DOSIS_LIGHT));
        v.setTextSize(20);

        v = (TextView)findViewById(R.id.text_heat_t2);
        v.setTypeface(FontUtils.loadFontFromAssets(context, FontUtils.FONT_DOSIS_LIGHT));
        v.setTextSize(20);

        v = (TextView)findViewById(R.id.text_heat_status);
        v.setTypeface(FontUtils.loadFontFromAssets(context, FontUtils.FONT_DOSIS_MEDIUM));
        v.setTextSize(24);

    }
}

package freakycamper.com.freaky.arduino_commmunicator.dialog;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.telerik.widget.chart.engine.axes.common.DateTimeComponent;
import com.telerik.widget.chart.engine.databinding.DataPointBinding;
import com.telerik.widget.chart.visualization.behaviors.ChartPanAndZoomBehavior;
import com.telerik.widget.chart.visualization.behaviors.ChartPanZoomMode;
import com.telerik.widget.chart.visualization.cartesianChart.RadCartesianChartView;
import com.telerik.widget.chart.visualization.cartesianChart.axes.DateTimeCategoricalAxis;
import com.telerik.widget.chart.visualization.cartesianChart.axes.LinearAxis;
import com.telerik.widget.chart.visualization.cartesianChart.series.categorical.SplineAreaSeries;

import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.ColdManager;
import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.MainManager;
import freakycamper.com.freaky.arduino_commmunicator.R;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.SQLDatasHelper;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.TemperatureItem;
import freakycamper.com.freaky.arduino_commmunicator.campduinoservice.CampDuinoProtocol;


/**
 * Created by lsa on 09/12/14.
 */
public class DialogFridge extends DialogPopUpDelayed  {

    public static final int FRIDGE_GRAPH_PLOTS_NB = 50;

    Switch _switchColdModule;

    public DialogFridge(Context context, final ColdManager manager) {
        super(context, "Frigo", R.layout.layout_fridge, android.R.style.Theme_DeviceDefault);

        int tempInt = Math.round(manager.getTempConsigne());

        setDimensions(400, 350);

        _switchColdModule = (Switch)findViewById(R.id.switch_activate_cold);
        _switchColdModule.setChecked(manager.getColdModuleStatus());
        _switchColdModule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch s = (Switch) v;
                boolean askedStatus = s.isChecked();
                s.setChecked(!askedStatus);
                manager.getSendTcListener().sendTC(CampDuinoProtocol.buildSwitchRelayTC(CampDuinoProtocol.eProtTcSwitch.PROT_SWITCH_COLD_MODULE, askedStatus));
            }
        });
        setMonitoredComponent(_switchColdModule);

        TextView txt = (TextView)findViewById(R.id.text_fridge_temp);
        txt.setText(String.valueOf(manager.getTempFridge()) + "°C");

        SeekBar sb = (SeekBar)findViewById(R.id.seekBar_fridge);
        sb.setProgress(tempInt);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Button bt = (Button)findViewById(R.id.button_set_temp_fridge);
                float newVal = getConsigneFromSeekBarStatus(progress);
                if (newVal != manager.getTempConsigne()){
                    bt.setText("Set temp to " + getConsigneFromSeekBarStatus(progress));
                    bt.setEnabled(true);
                }
                else{
                    bt.setText("Set temp");
                    bt.setEnabled(false);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        setMonitoredComponent(sb);

        Button bt = (Button)findViewById(R.id.button_set_temp_fridge);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeekBar sb = (SeekBar)findViewById(R.id.seekBar_fridge);
                float val = getConsigneFromSeekBarStatus(sb.getProgress());
                MainManager.SendTcListener tcSender = manager.getSendTcListener();
                tcSender.sendTC(CampDuinoProtocol.buildSetFridgeConsigne(val));
            }
        });

        setMonitoredComponent(bt);

        addGraph(context, manager);


    }

    private void addGraph(Context context, ColdManager manager){
        RadCartesianChartView chartView = new RadCartesianChartView(context);
        ViewGroup view = (ViewGroup)findViewById(R.id.graph_fridge_temp_Layout);
        view.addView(chartView);

        SplineAreaSeries areaSeries = new SplineAreaSeries(context);
        areaSeries.setCategoryBinding(new DataPointBinding() {
            @Override
            public Object getValue(Object o) throws IllegalArgumentException {
                return ((TemperatureItem) o).getDateTag();
            }
        });

        areaSeries.setValueBinding(new DataPointBinding() {
            @Override
            public Object getValue(Object o) throws IllegalArgumentException {
                return ((TemperatureItem) o).getTemperature();
            }
        });

        areaSeries.setData(manager.getFridgeTempHistoric());
        chartView.getSeries().add(areaSeries);

        DateTimeCategoricalAxis hAxis = new DateTimeCategoricalAxis(context);
        hAxis.setDateTimeComponent(DateTimeComponent.DAY);
        hAxis.setDateTimeFormat(SQLDatasHelper.DB_DATE_FORMAT);

        chartView.setHorizontalAxis(hAxis);

        LinearAxis vAxis = new LinearAxis(context);
        vAxis.setMaximum(25);
        chartView.setVerticalAxis(vAxis);

        ChartPanAndZoomBehavior panZoom = new ChartPanAndZoomBehavior();
        panZoom.setPanMode(ChartPanZoomMode.BOTH);
        panZoom.setZoomMode(ChartPanZoomMode.BOTH);
        chartView.getBehaviors().add(panZoom);
    }


    private static float getConsigneFromSeekBarStatus(int value){
        return value;
    }

    public void updateSwitchColdStatus(boolean status){
        if (status != _switchColdModule.isChecked()){
            _switchColdModule.setChecked(status);
        }
    }
}

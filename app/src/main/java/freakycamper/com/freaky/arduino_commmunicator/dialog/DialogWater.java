package freakycamper.com.freaky.arduino_commmunicator.dialog;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.ToggleButton;

import freakycamper.com.freaky.arduino_commmunicator.ComponentManagers.WaterManager;
import freakycamper.com.freaky.arduino_commmunicator.R;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.SQLDatasHelper;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.WaterItem;
import com.telerik.widget.chart.engine.axes.common.AxisHorizontalLocation;
import com.telerik.widget.chart.engine.axes.common.DateTimeComponent;
import com.telerik.widget.chart.engine.databinding.DataPointBinding;
import com.telerik.widget.chart.visualization.behaviors.ChartPanAndZoomBehavior;
import com.telerik.widget.chart.visualization.behaviors.ChartPanZoomMode;
import com.telerik.widget.chart.visualization.cartesianChart.RadCartesianChartView;
import com.telerik.widget.chart.visualization.cartesianChart.axes.DateTimeCategoricalAxis;
import com.telerik.widget.chart.visualization.cartesianChart.axes.LinearAxis;
import com.telerik.widget.chart.visualization.cartesianChart.series.categorical.SplineAreaSeries;
import com.telerik.widget.chart.visualization.cartesianChart.series.categorical.SplineSeries;
import com.telerik.widget.primitives.legend.RadLegendView;

/**
 * Created by lsa on 12/01/15.
 */
public class DialogWater extends DialogPopUpDelayed {

    public static final int WATER_LEVEL_GRAPH_PLOTS_NB = 50;

    public DialogWater(Context context, WaterManager manager) {
        super(context, context.getString(R.string.title_dialog_water), R.layout.layout_water, android.R.style.Theme_DeviceDefault);
        setDimensions(475, 355);

        addGraph(context, manager);

        Switch sw = (Switch)findViewById(R.id.switch_activate_water_module);
        if (manager.getIsWaterFunctionActivated())
            sw.setText(context.getString(R.string.water_lb_relay_on));
        else
            sw.setText(context.getString(R.string.water_lb_relay_off));

        ToggleButton tb = (ToggleButton)findViewById(R.id.toggle_grey_water_full);
        if (manager.getIsGreyWaterFull())
            tb.setText(context.getString(R.string.water_grey_water_full));
        else
            tb.setText(context.getString(R.string.water_grey_water_ok));

        setMonitoredComponent(sw);
        setMonitoredComponent(tb);
    }

    private void addGraph(Context context, WaterManager manager){
        RadCartesianChartView chartView = new RadCartesianChartView(context);

        // données niveau réservoir
        SplineAreaSeries splineWaterLevel = new SplineAreaSeries(context);
        splineWaterLevel.setCategoryBinding(new DataPointBinding() {
            @Override
            public Object getValue(Object o) throws IllegalArgumentException {
                return ((WaterItem) o).getDateTag();
            }
        });

        splineWaterLevel.setValueBinding(new DataPointBinding() {
            @Override
            public Object getValue(Object o) throws IllegalArgumentException {
                return ((WaterItem) o).getWaterLevel();
            }
        });
        splineWaterLevel.setData(manager.getWaterTmHistoric());
        splineWaterLevel.setLegendTitle(context.getString(R.string.water_graph_legend_level));
        chartView.getSeries().add(splineWaterLevel);

        // données consommation eau
        SplineSeries splineWaterFlow = new SplineSeries(context);
        splineWaterFlow.setCategoryBinding(new DataPointBinding() {
            @Override
            public Object getValue(Object o) throws IllegalArgumentException {
                return ((WaterItem) o).getDateTag();
            }
        });

        splineWaterFlow.setValueBinding(new DataPointBinding() {
            @Override
            public Object getValue(Object o) throws IllegalArgumentException {
                return ((WaterItem) o).getFlow();
            }
        });
        splineWaterFlow.setData(manager.getWaterTmHistoric());
        splineWaterFlow.setLegendTitle(context.getString(R.string.water_graph_legend_flow));
        chartView.getSeries().add(splineWaterFlow);

        // Axe horizontal
        DateTimeCategoricalAxis hAxis = new DateTimeCategoricalAxis(context);
        hAxis.setDateTimeComponent(DateTimeComponent.DAY);
        hAxis.setDateTimeFormat(SQLDatasHelper.DB_DATE_FORMAT);
        hAxis.setShowLabels(false);
        chartView.setHorizontalAxis(hAxis);

        // Axe vertical droit
        LinearAxis vAxisLevel = new LinearAxis(context);
        vAxisLevel.setHorizontalLocation(AxisHorizontalLocation.LEFT);
        splineWaterLevel.setVerticalAxis(vAxisLevel);

        // Axe vertical gauche
        LinearAxis vAxisFlow = new LinearAxis(context);
        vAxisFlow.setHorizontalLocation(AxisHorizontalLocation.RIGHT);
        splineWaterFlow.setVerticalAxis(vAxisFlow);

        // Légende
        RadLegendView legend = new RadLegendView(context);
        legend.setLegendProvider(chartView);

        // Zoom mode
        ChartPanAndZoomBehavior panZoom = new ChartPanAndZoomBehavior();
        panZoom.setPanMode(ChartPanZoomMode.BOTH);
        panZoom.setZoomMode(ChartPanZoomMode.BOTH);

        chartView.getBehaviors().add(panZoom);

        ViewGroup view = (ViewGroup)findViewById(R.id.graph_water_layout);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(legend);
        linearLayout.addView(chartView);

        view.addView(linearLayout);
    }
}

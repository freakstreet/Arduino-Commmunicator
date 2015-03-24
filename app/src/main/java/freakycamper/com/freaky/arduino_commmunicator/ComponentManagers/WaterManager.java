package freakycamper.com.freaky.arduino_commmunicator.ComponentManagers;

import android.view.View;
import android.widget.Switch;
import android.widget.ToggleButton;

import freakycamper.com.freaky.arduino_commmunicator.R;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.CurrentItem;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.ElectricalItem;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.SQLDatasHelper;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.WaterItem;
import freakycamper.com.freaky.arduino_commmunicator.dialog.DialogWater;
import freakycamper.com.freaky.arduino_commmunicator.gui.FreakyGauge;
import freakycamper.com.freaky.arduino_commmunicator.campduinoservice.CampDuinoProtocol;
import com.google.common.collect.EvictingQueue;

/**
 * Created by lsa on 08/12/14.
 */
public class WaterManager extends MainManager implements
        ElectricalManager.ListenerCurrentUpdate,
        WaterItem.ToggleSwitchWaterPumpRelay,
        View.OnLongClickListener,
        View.OnClickListener
{

    private static int TANK_LEVEL_TRESHOLD = 15;

    private EvictingQueue<WaterItem> _waterLevelArray;

    float       _consoPump          = 0;
    float       _flowPrincipal      = 0;
    int         _tankLevel          = 0;
    boolean     _isGreyWaterFull    = true;
    boolean     _pumpRelayClosed    = false;
    boolean     _evierOpened        = false;
    boolean     _showerOpened       = false;

    FreakyGauge _guiGauge           = null;
    ElectricalManager _elecManager  = null;
    DialogWater _dialog             = null;


    static public WaterManager initialiseWaterManager(MainManager.SendTcListener listener, ElectricalManager manager, SQLDatasHelper database) {
        return new WaterManager(listener, manager, database);
    }

      public WaterManager(SendTcListener listener, ElectricalManager manager, SQLDatasHelper database){
        super(listener);
        _elecManager = manager;
        _waterLevelArray = database.retrieveLastLoggedWaterLevels();
    }

    public void setGauge(FreakyGauge gauge){
        _guiGauge = gauge;
        _guiGauge.addIcon(R.drawable.icon_water);
        _guiGauge.addIcon(R.drawable.icon_no_water);
        _guiGauge.setIconIdx(1);
        _guiGauge.setOnClickListener(this);
        _guiGauge.setOnLongClickListener(this);
    }

    @Override
    public void updateFromTM(char[] tm){
        // etat electrique pompe à eau
        _pumpRelayClosed = tm[1] == 1;

        // niveau eaux propres
        updateTankLevel(tm[2]);

        // gestion eaux grises
        updateGreyWater(tm[3] == 1);

        _flowPrincipal = tm[4];
        _evierOpened = tm[5] == 1;
        _showerOpened = tm[6] == 1;

        updateGui();
    }


    private void greyWaterNotification(boolean activate){
        // TODO : remplir fonction
        // if activate, show persistant

        // if !activate hide notification
    }

    private void updateGreyWater(boolean isFull){
        if (isFull){
            if (!_isGreyWaterFull){
                greyWaterNotification(true);
            }
            _isGreyWaterFull = true;
        }
        else {
            if (_isGreyWaterFull){
                greyWaterNotification(false);
            }
            _isGreyWaterFull = false;
        }
    }

    private void updateTankLevel(int level){
        _tankLevel = level;
        _guiGauge.set_percentFill(_tankLevel);
        if (_tankLevel < TANK_LEVEL_TRESHOLD){
            // affichage persistant d'une notification avec le niveau restant

        }

        if (_tankLevel == 100){
            // notification du niveau de remplissage du réservoir
            // emet un son et une notification
        }
    }

    private void updateGui(){
        _guiGauge.invalidate();
        if (_dialog != null){

        }
    }

    @Override
    public void currentUpdated(float[] currentList) {
        CurrentItem.eCurrentType eCurrentWater = CurrentItem.eCurrentType.I_WATER;
        _consoPump = currentList[eCurrentWater.value];
        updateGui();
    }


    public EvictingQueue<WaterItem> getWaterTmHistoric(){
        return _waterLevelArray;
    }


    @Override
    public void actionToggleSwitchPumpRelay(boolean newStatus) {

    }

    @Override
    public boolean onLongClick(View v) {
        if (_dialog == null){
            _dialog = new DialogWater(v.getContext(), this);
        }
        _dialog.show();
        return false;
    }

    @Override
    public void onClick(View v) {
        boolean status = false;
        boolean sendTc = false;

        if (v instanceof Switch){
            Switch bt = (Switch) v;
            status = bt.isChecked();
            // the status must be updated from Tm reception
            bt.setChecked(!status);
            sendTc = true;
        }
        else if (v instanceof ToggleButton){
            ToggleButton bt = (ToggleButton) v;
            status = bt.isChecked();
            // the status must be updated from Tm reception
            bt.setChecked(!status);
            sendTc = true;
        }
        else if(v instanceof FreakyGauge){
            FreakyGauge fg = (FreakyGauge) v;
            status = !(fg.getIconIdx() == 0);
            sendTc = true;
        }
        if (sendTc)
            sendTc(CampDuinoProtocol.buildSwitchRelayTC(CampDuinoProtocol.eProtTcSwitch.PROT_SWITCH_WATER_MODULE, status));
    }

    public boolean getIsGreyWaterFull(){
        return _isGreyWaterFull;
    }

    public boolean getIsWaterFunctionActivated(){
        return _elecManager.getRelayStatus(ElectricalItem.eRelayType.R_WATER);
    }
}

package freakycamper.com.freaky.arduino_commmunicator.ComponentManagers;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Switch;
import android.widget.ToggleButton;

import freakycamper.com.freaky.arduino_commmunicator.R;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.CurrentItem;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.ElectricalItem;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.SQLDatasHelper;
import freakycamper.com.freaky.arduino_commmunicator.campdatas.WaterItem;
import freakycamper.com.freaky.arduino_commmunicator.dialog.DialogElectrical;
import freakycamper.com.freaky.arduino_commmunicator.dialog.DialogWater;
import freakycamper.com.freaky.arduino_commmunicator.gui.FreakyGauge;
import freakycamper.com.freaky.arduino_commmunicator.campduinoservice.CampDuinoProtocol;
import com.google.common.collect.EvictingQueue;

import java.util.ArrayList;
import java.util.List;

import static android.R.id.list;

/**
 * Created by lsa on 08/12/14.
 */
public class WaterManager extends MainManager implements
        ElectricalManager.ListenerCurrentUpdate,
        WaterItem.ToggleSwitchWaterPumpRelay
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

    private ArrayList<ListenerWaterUpdate> listenersWaterUpdate = null;

    public interface ListenerWaterUpdate {
        public void waterUpdated();
    }


    static public WaterManager initialiseWaterManager(MainManager.SendTcListener listener, ElectricalManager manager, SQLDatasHelper database) {
        return new WaterManager(listener, manager, database);
    }

     public WaterManager(SendTcListener listener, ElectricalManager manager, SQLDatasHelper database){
        super(listener);

          listenersWaterUpdate = new ArrayList<ListenerWaterUpdate>();
        _elecManager = manager;
        _waterLevelArray = database.retrieveLastLoggedWaterLevels();
    }

    public void setGauge(FreakyGauge gauge){
        _guiGauge = gauge;
        _guiGauge.addIcon(R.drawable.icon_water);
        _guiGauge.addIcon(R.drawable.icon_no_water);
        _guiGauge.setIconIdx(1);
        _guiGauge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(v.getContext());
            }
        });
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
        _evierOpened = tm[6] == 1;
        _showerOpened = tm[7] == 1;

        for (int i=0; i<listenersWaterUpdate.size(); i++) {
            listenersWaterUpdate.get(i).waterUpdated();
        }

    }

    public void addListenerWaterUpdate(ListenerWaterUpdate listener)
    {
        listenersWaterUpdate.add(listener);
    }

    public void removeListenerWaterUpdate(ListenerWaterUpdate listener)
    {
        listenersWaterUpdate.remove(listener);
    }

    @Override
    public String getStringFromTm(char[] tm)
    {
        String str = "";
        float tmp[];

        str += "Water: " + (tm[1] == 1?"pumping":"idle") + ", tank level: " + String.valueOf((byte)tm[2]) + ", grey water " + (tm[3] == 1?"OK":"FULL");


        return str;
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

    @Override
    public void currentUpdated(float[] currentList) {
        CurrentItem.eCurrentType eCurrentWater = CurrentItem.eCurrentType.I_WATER;
        _consoPump = currentList[eCurrentWater.value];
    }


    public EvictingQueue<WaterItem> getWaterTmHistoric(){
        return _waterLevelArray;
    }

    @Override public void updateDialog()
    {
        ((DialogWater)correspondingDialog).updateDialog();
    }

    @Override
    public void actionToggleSwitchPumpRelay(boolean newStatus) {

    }

    public boolean showDialog(Context context){
        correspondingDialog = new DialogWater(context, this);
        correspondingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                correspondingDialog = null;
            }
        });
        correspondingDialog.show();
        return false;
    }

    public boolean getIsWaterFunctionActivated(){
        return _elecManager.getRelayStatus(ElectricalItem.eRelayType.R_WATER);
    }

    public int getTankLevel()
    {
        return _tankLevel;
    }

    public float getWaterFlow()
    {
        return _flowPrincipal;
    }

    public float getConsoPump()
    {
        return _consoPump;
    }

    public boolean isGrayWaterFull()
    {
        return _isGreyWaterFull;
    }
}

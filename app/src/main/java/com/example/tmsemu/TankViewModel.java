package com.example.tmsemu;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.NonNull;

import com.google.dmzavr.TankMeasurer;
import com.google.dmzavr.TmsEmuStorage;

public class TankViewModel extends GlobalViewModel {

    private int channelId = -1;

    public static String viewModelKey( int tankId )
    {
        return "com.gmail.dmzavr.TankViewModel.tank." + Integer.toString(tankId);
    }

    @Override
    protected void onShareCleared() {

    }

    public TankViewModel(Runnable onShareCleared, Integer cId ) {
        super(onShareCleared, cId);
        channelId = null == cId ? -1 : cId;
        //super(application);
        //this.channelId = channelId;
    }

    // TODO: Implement the ViewModel
    static public class TankCfg {
        public Integer channelId;
        public Double volume;
        public Integer diameter;
        public boolean hasFuelVolume = true;
        public boolean hasTotalLevel = true;
        public boolean hasWaterVolume = true;
        public boolean hasWaterLevel = true;
        public boolean hasDensity = true;
        public boolean hasMass = true;
        public boolean hasTemperature = true;
        public boolean hasTcDensity = true;
        public boolean hasTcVolume = true;
        public boolean hasUllage = true;
        public TankCfg() {}
        public TankCfg(TankCfg o) {
            channelId = null == o.channelId ? null : o.channelId.intValue();
            volume = null == o.volume ? null : o.volume.doubleValue();
            diameter = null == o.diameter ? null : o.diameter.intValue();
            hasFuelVolume = o.hasFuelVolume;
            hasTotalLevel = o.hasTotalLevel;
            hasWaterVolume = o.hasWaterVolume;
            hasWaterLevel = o.hasWaterLevel;
            hasDensity = o.hasDensity;
            hasMass = o.hasMass;
            hasTemperature = o.hasTemperature;
            hasTcDensity = o.hasTcDensity;
            hasTcVolume = o.hasTcVolume;
            hasUllage = o.hasUllage;
        }
    };

    static public class TankSample {
        public Double fuelLevel = null;
        public Double waterLevel = null;
        public Double maxLevel = null;
        public Double fuelVolume = null;
        public Double waterVolume = null;
        public Double fuelDensity = null;
        public Double fuelMass = null;
        public Double freeVolume = null;
        public Double temperature = null;
        public Double tankVolume = null;
        public Double tcFuelDensity = null;
        public Double tcFuelVolume = null;
        public TankSample() {}
        public TankSample(TankSample o) {
            fuelLevel = null == o.fuelLevel ? null : o.fuelLevel.doubleValue();
            waterLevel = null == o.waterLevel ? null : o.waterLevel.doubleValue();
            maxLevel = null == o.maxLevel ? null : o.maxLevel.doubleValue();
            fuelVolume = null == o.fuelVolume ? null : o.fuelVolume.doubleValue();
            waterVolume = null == o.waterVolume ? null : o.waterVolume.doubleValue();
            fuelDensity = null == o.fuelDensity ? null : o.fuelDensity.doubleValue();
            fuelMass = null == o.fuelMass ? null : o.fuelMass.doubleValue();
            freeVolume = null == o.freeVolume ? null : o.freeVolume.doubleValue();
            temperature = null == o.temperature ? null : o.temperature.doubleValue();
            tankVolume = null == o.tankVolume ? null : o.tankVolume.doubleValue();
            tcFuelDensity = null == o.tcFuelDensity ? null : o.tcFuelDensity.doubleValue();
            tcFuelVolume = null == o.tcFuelVolume ? null : o.tcFuelVolume.doubleValue();
        }
    }

    static public class TankData {
        public TankCfg cfg;
        public TankSample sample;
        public TankMeasurer tm;
        TankData(@NonNull TankCfg tCfg, @NonNull TankSample tSample) {
            cfg = new TankCfg(tCfg);
            sample = new TankSample(tSample);
            tm = new TankMeasurer(cfg, sample);
        }
    }

    private MutableLiveData<TankData> tankData;

    public LiveData<TankData> getTankData() {
        if( tankData == null ) {
            tankData = new MutableLiveData<>();
            loadStorage();
        }
        return tankData;
    }
    public void setCfg(TankCfg cfg) {
        TankData td = tankData.getValue();
        if( td != null ) {
            TankData tdnew = new TankData(cfg, td.sample);
            tankData.postValue(tdnew);
        }
    }

    public void setFuelVolume(Double v, boolean doRecalc) {
        TankData td = tankData.getValue();
        if( null != v ) {
            td.tm.setFuelVol(v);
        }
        td.sample.fuelVolume = v;
        if( doRecalc ) {
            td.tm.recalcSample();
        }
        tankData.postValue( td );
    }

    public void setTotalLevel(Double v, boolean doRecalc) {
        TankData td = tankData.getValue();
        if( null != v )
            td.tm.setTotalLevel(v);
        td.sample.fuelLevel = v;
        if( doRecalc )
            td.tm.recalcSample();
        tankData.postValue( td );
    }
    public void setWaterVolume(Double v, boolean doRecalc) {
        TankData td = tankData.getValue();
        if( null != v )
            td.tm.setWaterVol(v);
        td.sample.waterVolume = v;
        if( doRecalc )
            td.tm.recalcSample();
        tankData.postValue( td );
    }
    public void setWaterLevel(Double v, boolean doRecalc) {
        TankData td = tankData.getValue();
        if( null != v )
            td.tm.setWaterLevel(v);
        td.sample.waterLevel = v;
        if( doRecalc )
            td.tm.recalcSample();
        tankData.postValue( td );
    }

    public void setDensity(Double v, boolean doRecalc) {
        TankData td = tankData.getValue();
        td.sample.fuelDensity = v;
        if( doRecalc )
            td.sample.fuelMass = (td.sample.fuelVolume == null || v == null) ? null : td.sample.fuelVolume * v;
        tankData.postValue( td );
    }
    public void setMass(Double v, boolean doRecalc) {
        TankData td = tankData.getValue();
        td.sample.fuelMass = v;
        if( doRecalc )
            td.sample.fuelDensity = (td.sample.fuelVolume == null  || td.sample.fuelVolume == 0 || v == null) ? null : v / td.sample.fuelVolume;
        tankData.postValue( td );
    }
    public void setTemp(Double v, boolean doRecalc) {
        TankData td = tankData.getValue();
        td.sample.temperature = v;
//        if( doRecalc )
//            td.sample.fuelDensity = (td.sample.fuelVolume == null  || td.sample.fuelVolume == 0 || v == null) ? null : v / td.sample.fuelVolume;
        tankData.postValue( td );
    }
    public void setTcDensity(Double v, boolean doRecalc) {
        TankData td = tankData.getValue();
        td.sample.tcFuelDensity = v;
//        if( doRecalc )
//            td.sample.fuelDensity = (td.sample.fuelVolume == null  || td.sample.fuelVolume == 0 || v == null) ? null : v / td.sample.fuelVolume;
        tankData.postValue( td );
    }
    public void setTcVolume(Double v, boolean doRecalc) {
        TankData td = tankData.getValue();
        td.sample.tcFuelVolume = v;
//        if( doRecalc )
//            td.sample.fuelDensity = (td.sample.fuelVolume == null  || td.sample.fuelVolume == 0 || v == null) ? null : v / td.sample.fuelVolume;
        tankData.postValue( td );
    }

    public boolean saveData(Context ctx, int targetChannel)
    {
        TankData td = tankData.getValue();
        if( null == td || td.cfg == null )
            return false;
        TmsEmuStorage.DataItem di = new TmsEmuStorage.DataItem(); //td.cfg, td.sample);
        di.tankCfg = td.cfg;
        di.tankCfg.channelId = targetChannel;
        di.tankSample = td.sample;
        TmsEmuStorage.getInstance().putItem(di);
        TmsEmuStorage.getInstance().saveData( ctx );
        return true;
    }

    private void loadStorage()
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(50);
                } catch (Exception ex) {
                    //
                }
                TmsEmuStorage.DataItem dataItem = TmsEmuStorage.getInstance().getItem( channelId );
                dataItem = (null == dataItem) ? new TmsEmuStorage.DataItem() : dataItem;

                dataItem.tankCfg = null == dataItem.tankCfg ? new TankCfg() : dataItem.tankCfg;
                dataItem.tankSample = null == dataItem.tankSample ? new TankSample() : dataItem.tankSample;

                TankCfg tc = dataItem.tankCfg;
                if( 0 > channelId ) {
                    tc.channelId = null;
//                } else {
                    // Test only
//                    tc.diameter = 2000;
                }
                TankSample ts = dataItem.tankSample;
//                if( 0 >= channelId ) {
//                    ts.fuelVolume = 12000.;
//                    ts.fuelDensity = 0.78611;
//                    ts.fuelLevel = 2000.;
//                    ts.waterLevel = 1.;
//                    ts.waterVolume = 300.;
//                }
                tankData.postValue( new TankData(tc, ts) );
            }
        };
        new Thread(runnable).start();
    }
}

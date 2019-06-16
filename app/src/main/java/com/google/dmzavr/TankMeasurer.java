package com.google.dmzavr;

import android.support.annotation.NonNull;

import com.example.tmsemu.TankViewModel;

public final class TankMeasurer {
    private static final float DEFAULT_TANK_RADIUS_MM = 250f;
    private static final float DEFAULT_TANK_VOLUME = 20000f;
    private static final float CALC_PRECISION = 0.5f; ///< Погрешность вычисления параметров сегмента, Литры
    private float calcPrecision;

    private double mTotalVol = 0f;    ///< Общий объем жидкости в резервуаре
    private double mTotalLevel = 0f;  ///< Общий уровень жидкости в резервуаре
    private double mFuelVol = 0f;   ///< Объем НП в резервуаре
    private double mWaterVol = 0f;   ///< Объем воды в резервуаре
    private double mWaterLevel = 0f; ///< Уровень воды в резервуаре
    private double mVolScale = 0;
    private double mGeomVolume = 0;

    private TankViewModel.TankCfg tankCfg;

    public TankViewModel.TankSample getTankSample() {
        return tankSample;
    }

    private TankViewModel.TankSample tankSample;
    private Segment.Def mVolGeom;
    private Segment.Def mVolWGeom;

    Segment.Def getTotalVolGuiGeom() {
        return mVolGeom;
    }
    Segment.Def getWaterGuiGeom() {
        return mVolWGeom;
    }

    public TankMeasurer(@NonNull TankViewModel.TankCfg tCfg, @NonNull TankViewModel.TankSample tSample)
    {
        tankCfg = tCfg; tankSample = tSample;
        mVolGeom = new Segment.Def(); mVolWGeom = new Segment.Def();
        initialRecalcGeoms();
        //if( tankCfg.autoRecalcSample )
        //    recalcSample();
    }

    void initialRecalcGeoms() {
        mVolGeom.radius = mVolWGeom.radius = (null == tankCfg.diameter) ? DEFAULT_TANK_RADIUS_MM : tankCfg.diameter / 2f;
        mGeomVolume = (null == tankCfg.volume) ? DEFAULT_TANK_VOLUME : tankCfg.volume;
        mVolScale = Math.PI * Math.pow(mVolWGeom.radius, 2) / mGeomVolume;
        calcPrecision = CALC_PRECISION * (float) mVolScale;

        if (tankCfg.hasFuelVolume && null != tankSample.fuelVolume) {
            setFuelVol(tankSample.fuelVolume);
        } else if (tankCfg.hasTotalLevel && null != tankSample.fuelLevel)
            setTotalLevel(tankSample.fuelLevel);
        else setFuelVol(mGeomVolume / 3);

        if (tankCfg.hasWaterVolume && null != tankSample.waterVolume)
            setWaterVol(tankSample.waterVolume);
        else if (tankCfg.hasWaterLevel && null != tankSample.waterLevel)
            setWaterLevel(tankSample.waterLevel);
        else if (!tankCfg.hasWaterLevel && !tankCfg.hasWaterVolume) {
            setWaterLevel(0);
        } else
            setWaterVol(mGeomVolume / 6);
    }

    public void recalcSample() {
        if( tankCfg.volume != null && tankCfg.diameter != null ) {
            if( null != tankSample.fuelVolume || null != tankSample.fuelLevel ) {
                if( tankCfg.hasFuelVolume )
                    tankSample.fuelVolume = mFuelVol;
                if( tankCfg.hasTotalLevel )
                    tankSample.fuelLevel = mTotalLevel;
            }
            if( null != tankSample.waterLevel || null != tankSample.waterVolume ) {
                if( tankCfg.hasWaterVolume )
                    tankSample.waterVolume = mWaterVol;
                if( tankCfg.hasWaterLevel )
                    tankSample.waterLevel = mWaterLevel;
            }
            tankSample.maxLevel = tankCfg.diameter.doubleValue();
            if( tankCfg.hasFuelVolume && null != tankSample.fuelVolume )
                tankSample.freeVolume = tankCfg.volume - mTotalVol;
            else tankSample.freeVolume = null;
            if( tankSample.fuelVolume != null && tankSample.fuelDensity != null )
                tankSample.fuelMass = tankSample.fuelVolume * tankSample.fuelDensity;
            else if( tankSample.fuelVolume != null && tankSample.fuelMass != null && tankSample.fuelMass != 0 )
                tankSample.fuelDensity = tankSample.fuelMass / tankSample.fuelVolume;

        } else {
            tankSample.fuelLevel = null;
            tankSample.fuelVolume = null;
            tankSample.waterLevel = null;
            tankSample.waterVolume = null;
            tankSample.maxLevel = null;
//            fuelDensity = null;
            tankSample.fuelMass = null;
            tankSample.freeVolume = null;
//            temperature = null;
            tankSample.tankVolume = null;

        }
    }

    static double round(double v, int scale )
    {
        return ( (int)(v * scale + 0.05) ) / (double)(scale);
    }

    public void setLevels(double totalLevel, double waterLevel ) {
        mTotalLevel = totalLevel;
        mWaterLevel = waterLevel;
        recalcOnChangedLevel();
        recalcOnChangedLevelW();
    }

    void setVolumes(double totalVol, double waterVol) {
        mTotalVol = totalVol;
        mWaterVol = waterVol;
        recalcOnChangedVol();
        recalcOnChangedVolW();
    }

    void setTotalVol(double v) {
        mTotalVol = v;
        recalcOnChangedVol();
    }

    public void setTotalLevel(double v) {
        mTotalLevel = v;
        recalcOnChangedLevel();
    }

    double getTotalLevel(){
        return mTotalLevel;
    }

    public void setFuelVol(double v) {
        mFuelVol = v;
        recalcOnChangedVolG();
    }
    double getFuelVol() {
        return mFuelVol;
    }

    public void setWaterVol(double v) {
        mWaterVol = v;
        recalcOnChangedVolW();
    }
    double getWaterVol(){
        return mWaterVol;
    }

    public void setWaterLevel(double v) {
        mWaterLevel = v;
        recalcOnChangedLevelW();
    }
    double getWaterLevel() {
        return mWaterLevel;
    }

    private void recalcOnChangedVol()
    {
        mFuelVol = mTotalVol - mWaterVol;
        mVolGeom.area = mTotalVol * mVolScale;
        Segment.recalcByAreaAndRadius(mVolGeom, calcPrecision);
        mTotalLevel = mVolGeom.height;
    }

    private void recalcOnChangedVolG()
    {
        mTotalVol = mFuelVol + mWaterVol;
        recalcOnChangedVol();
    }

    private void recalcOnChangedVolW()
    {
        mTotalVol = mFuelVol + mWaterVol;
        recalcOnChangedVol();

        mVolWGeom.area = mWaterVol * mVolScale;
        Segment.recalcByAreaAndRadius(mVolWGeom, calcPrecision);
        mWaterLevel = mVolWGeom.height;
    }

    private void recalcOnChangedLevel() {
        mVolGeom.height = mTotalLevel;
        Segment.recalcByHeightAndRadius(mVolGeom);
        mTotalVol = mVolGeom.area / mVolScale;
        if(mTotalLevel <= mWaterLevel ) {
            mWaterLevel = mWaterVol = 0;
            mFuelVol = mTotalVol;

            recalcOnChangedVolW();
        } else
            mFuelVol = mTotalVol - mWaterVol;
    }

    private void recalcOnChangedLevelW() {
        mVolWGeom.height = mWaterLevel;
        Segment.recalcByHeightAndRadius(mVolWGeom);
        mWaterVol = mVolWGeom.area / mVolScale;
        mTotalVol = mFuelVol + mWaterVol;
        recalcOnChangedVol();
    }

}

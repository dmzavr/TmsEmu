package com.google.dmzavr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.tmsemu.R;
import com.example.tmsemu.TankViewModel;

public class MyTankView extends View {
    private int measureDimension( int desiredSize, int measureSpec ) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if( MeasureSpec.EXACTLY == specMode )
            result = specSize;
        else {
            result = desiredSize;
            if( MeasureSpec.AT_MOST == specMode )
                result = Math.min( result, specSize );
        }
        Log.d("TankView", "Desired size: " + Integer.toString(desiredSize) + ", result: " + Integer.toString(result) );
        return result;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int desiredWidth = TANK_RADIUS_PX*2 + getPaddingLeft() + getPaddingRight();
        int desiredHeight = TANK_RADIUS_PX*2 + getPaddingTop() + getPaddingBottom();
        int w = measureDimension(desiredWidth, widthMeasureSpec);
        int h = measureDimension( desiredHeight, heightMeasureSpec);
        if( w > h )
            radius = (h - getPaddingTop() - getPaddingBottom())/2;
        else
            radius = (w - getPaddingLeft() - getPaddingRight())/2;
        setMeasuredDimension(w, h);
    }

    private int radius = TANK_RADIUS_PX;
    private static final int TANK_RADIUS_PX = 60;          ///< Radius, px
    private static final float TANK_RADIUS_MM = 250f;
    private static final float TANK_VOLUME = 20000f;

    private TankViewModel.TankCfg tCfg;
    private TankViewModel.TankSample tSample;
    private TankMeasurer mTankMeasurer;

    /**
     * Custom properties
     */

    public void setTankCfg(TankViewModel.TankCfg cfg, TankMeasurer tm1 )
    {
        tCfg = cfg;
        mChannel = (null == tCfg || null == tCfg.channelId) ? -1 : tCfg.channelId;
        mTankMeasurer = tm1;
        setTankSample( tSample );
    }

    public TankViewModel.TankCfg getTankCfg() { return tCfg; }
    public TankViewModel.TankSample getTankSample() { return tSample; }

    public void setTankSample(TankViewModel.TankSample ts)
    {
        tSample = ts;
        invalidate();
    }

    public void setChannel(int mChannel) {
        this.mChannel = mChannel;
    }

    private int mChannel;
    private Paint mWaterPaint;
    private Paint mGasPaint;
    private Paint mStrokePaint;

    private final Rect r = new Rect();

    public MyTankView(Context context) {
        this(context, null);
    }

    public MyTankView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyTankView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        tCfg = new TankViewModel.TankCfg();
        tCfg.diameter = (int)(TANK_RADIUS_MM * 2);
        tCfg.volume = (double)(TANK_VOLUME);
        tCfg.channelId = null;

        tSample = new TankViewModel.TankSample();
        tSample.fuelVolume = tCfg.volume / 2.5;
        tSample.waterVolume = tCfg.volume / 6;

        mTankMeasurer = new TankMeasurer(tCfg, tSample);
        mTankMeasurer.setVolumes(tSample.fuelVolume + tSample.waterVolume, tSample.waterVolume );

        mWaterPaint = new Paint();
        mWaterPaint.setColor(getResources().getColor(R.color.waterColor));
        mWaterPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mGasPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGasPaint.setColor(getResources().getColor(R.color.FuelColor));
        mGasPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setTextSize(30);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //int radius = (getHeight() - getPaddingTop() - getPaddingBottom())/2;

        int x0 = getPaddingLeft();
        int y0 = getPaddingTop();
        Segment.Def fuel_def = mTankMeasurer.getTotalVolGuiGeom();

        canvas.drawArc(x0 + 1, y0 + 1, x0 + radius*2-1, y0 + radius*2-1, 90 - (float)fuel_def.angle/2, (float)fuel_def.angle, false, mGasPaint);
        canvas.drawCircle( x0 + radius, y0 + radius, radius, mStrokePaint );

        if( null != tCfg && ( tCfg.hasWaterVolume || tCfg.hasWaterLevel ) ) {
            Segment.Def water_def = mTankMeasurer.getWaterGuiGeom();
            canvas.drawArc(x0 + 1, y0 + 1, x0 + radius * 2 - 1, y0 + radius * 2 - 1, 90 - (float) water_def.angle / 2, (float) water_def.angle, false, mWaterPaint);
        }

        String canlabel = 0 > mChannel ? "?" : Integer.toString(mChannel);
        mStrokePaint.getTextBounds( canlabel, 0, canlabel.length(), r );

        canvas.drawText( canlabel, x0 + radius - r.width()/2 - r.left, y0 + radius - r.height()/2 - r.top, mStrokePaint );
    }
}

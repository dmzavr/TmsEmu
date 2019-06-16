package com.example.tmsemu;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.dmzavr.MyTankView;
import com.google.dmzavr.TmsEmuStorage;

final class MyPageAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 2;
    private Context context;
    private int mChannelId = -1;

    public MyPageAdapter(FragmentManager fm, Context context, int channelId) {
        super(fm);
        this.context = context;
        mChannelId = channelId;
    }

    @Override public int getCount() {
        return PAGE_COUNT;
    }


    @Override public Fragment getItem(int position) {
        Bundle args = new Bundle();
        args.putInt("PAGE_IDX", position);
        args.putInt("ChannelId", mChannelId);

        Fragment fragment;
        if( 0 == position ) {
            fragment = new TankCfgFragment();
        } else
            fragment = new TankSampleFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override public CharSequence getPageTitle(int position) {
        // генерируем заголовок в зависимости от позиции
        if( 0 == position )
            return context.getString(R.string.tank_cfg);
        return context.getString(R.string.sample);
    }
}

public class TankEditorActivity extends AppCompatActivity
implements TankCfgFragment.OnFragmentInteractionListener, TankSampleFragment.OnFragmentInteractionListener
{
    public int getChannelId() {
        return mChannelId;
    }

    private int mChannelId = -1;
    private int mEditedChannelId = -1;
    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private MyPageAdapter mPageAdapter;
    private TankViewModel model;
    private TankListViewModel listModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tank_editor2);
        ViewPager vp = findViewById(R.id.viewPager);
        TabLayout tabs = findViewById(R.id.tabLayout);
        Intent intent = getIntent();

        mChannelId = intent.getIntExtra("ChannelId", -1);
        mPageAdapter = new MyPageAdapter( getSupportFragmentManager(), this, mChannelId );
        vp.setAdapter(mPageAdapter);

        listModel = ViewModelProviders.of( this, TmsEmuStorage.getInstance().getFactory(-1) ).get(TankListViewModel.class);
        model = ViewModelProviders.of(this, TmsEmuStorage.getInstance().getFactory(mChannelId)).get(TankViewModel.viewModelKey(mChannelId), TankViewModel.class);
        //model.setChannelId(mChannelId);

        final Observer<TankViewModel.TankData> tankDataObserver = new Observer<TankViewModel.TankData>() {
            @Override
            public void onChanged(@Nullable TankViewModel.TankData td) {
                onTankDataUpdated(td);
            }
        };
        model.getTankData().observe(this, tankDataObserver );
    }

    private void onTankDataUpdated(TankViewModel.TankData td)
    {
        MyTankView tv = findViewById(R.id.myTankView);
        tv.setTankCfg( td.cfg, td.tm );
        tv.setTankSample( td.sample );
        mEditedChannelId = null == td.cfg.channelId ? -1 : td.cfg.channelId;
    }

    public void saveButtonClick(View v) {
        model.saveData(getBaseContext(), mChannelId);
        if( -1 == mChannelId && -1 != mEditedChannelId) {
            MyTankView tv = findViewById(R.id.myTankView);
            TmsEmuStorage.DataItem dataItem = new TmsEmuStorage.DataItem(tv.getTankCfg(), tv.getTankSample());
//            dataItem.tankCfg = tv.getTankCfg();
            dataItem.tankCfg.channelId = mEditedChannelId;
//            dataItem.tankSample = tv.getTankSample();
            listModel.addChannel(getBaseContext(), dataItem);
        }
        finish();
    }

}

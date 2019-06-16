package com.example.tmsemu;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.tmsemu.ItemFragment.OnListFragmentInteractionListener;
import com.google.dmzavr.MyTankView;
import com.google.dmzavr.MyUtils;
import com.google.dmzavr.TmsEmuStorage;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private List<Integer> mTankChannels = null;
    private final OnListFragmentInteractionListener mListener;
    private final Fragment mFragment;

    public MyItemRecyclerViewAdapter(Fragment f, LiveData< List<Integer> > items, OnListFragmentInteractionListener listener) {
        mFragment = f;
//        mTankChannels = items;
        final Observer< List<Integer> > tankListObserver = new Observer< List<Integer> >() {
            @Override
            public void onChanged(@Nullable List<Integer> tList) {
                onTankListUpdated(tList);
            }
        };
        items.observe( mFragment, tankListObserver );

        mListener = listener;
    }

    private void onTankListUpdated( List<Integer> tList ) {
        mTankChannels = tList;
        notifyDataSetChanged();
        mListener.onTankListSizeChanged( null == mTankChannels ? 0 : mTankChannels.size() );
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mTankChannel = mTankChannels.get(position);
        holder.mIdView.setText(holder.mTankChannel.toString());
//        holder.mContentView.setText(mValues.get(position).content);
        holder.mTankView.setChannel(holder.mTankChannel);
        holder.model = ViewModelProviders.of(mFragment, TmsEmuStorage.getInstance().getFactory(null == holder.mTankChannel ? -1 : holder.mTankChannel)).get(TankViewModel.viewModelKey(holder.mTankChannel), TankViewModel.class);
//        model.setChannelId(holder.mTankChannel);
//        holder.mTankView.setViewModel( holder.model );
        holder.model.getTankData().observe( MyUtils.getLifecycleOwner(holder.mTankView ), holder.tankDataObserver );

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mTankChannel);
                }
            }
        });
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.model.getTankData().removeObserver(holder.tankDataObserver);
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return mTankChannels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mFuelContentView;
        public final TextView mWaterContentView;
        public final TextView mOtherContentView;
        public final MyTankView mTankView;
        public Integer mTankChannel;
        public TankViewModel model = null;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.item_number);
            mFuelContentView = (TextView) view.findViewById(R.id.fuel_content);
            mTankView = (MyTankView) view.findViewById(R.id.myTankView2);
            mWaterContentView = (TextView) view.findViewById(R.id.water_content);
            mOtherContentView = (TextView) view.findViewById(R.id.other_content);
        }

        private String d2s(Object d, int decpos) {
            return d == null ? "null" : MyUtils.numberToString(d, decpos);
        }

        final Observer<TankViewModel.TankData> tankDataObserver = new Observer<TankViewModel.TankData>() {
            @Override
            public void onChanged(@Nullable TankViewModel.TankData td) {
                mTankView.setTankCfg( td.cfg, td.tm );
                mTankView.setTankSample( td.sample );
                mFuelContentView.setText(
                        String.format("V:%s; L: %s",
                                td.cfg.hasFuelVolume ? d2s(td.sample.fuelVolume, 3) : "n/a",
                                td.cfg.hasTotalLevel ? d2s(td.sample.fuelLevel, 1) : "n/a")
                );
                mWaterContentView.setText(
                        String.format("W:%s; WL:%s",
                            td.cfg.hasWaterVolume ? d2s(td.sample.waterVolume, 3) : "n/a",
                                td.cfg.hasWaterLevel ? d2s(td.sample.waterLevel, 1) : "n/a"
                        )
                );
                mOtherContentView.setText(
                        String.format( "P:%s; M:%s; T:%s",
                            td.cfg.hasDensity ? d2s(td.sample.fuelDensity, 6) : "n/a",
                            td.cfg.hasMass ? d2s(td.sample.fuelMass, 3) : "n/a",
                            td.cfg.hasTemperature ? d2s(td.sample.temperature, 1) : "n/a"
                        )
                );
            }
        };

        @Override
        public String toString() {
            return super.toString() + " '" + mFuelContentView.getText() + "'";
        }
    }
}

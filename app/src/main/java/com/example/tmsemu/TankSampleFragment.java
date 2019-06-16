package com.example.tmsemu;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.google.dmzavr.MyUtils;
import com.google.dmzavr.TmsEmuStorage;

/**
 * A fragment with a Google +1 button.
 * Activities that contain this fragment must implement the
 * {@link TankSampleFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TankSampleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TankSampleFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private int mChannelId = -1;
    private TankViewModel model;

    private OnFragmentInteractionListener mListener;

    public TankSampleFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TankSampleFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TankSampleFragment newInstance(String param1, String param2) {
        TankSampleFragment fragment = new TankSampleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    final Observer<TankViewModel.TankData> tankDataObserver = new Observer<TankViewModel.TankData>() {
        @Override
        public void onChanged(@Nullable TankViewModel.TankData tankData) {
            onTankDataUpdated(tankData);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            mChannelId = getArguments().getInt("ChannelId", -1);
        }
        model = ViewModelProviders.of( getActivity(), TmsEmuStorage.getInstance().getFactory(mChannelId)).get( TankViewModel.viewModelKey(mChannelId), TankViewModel.class);
        //model.setChannelId(mChannelId);

        model.getTankData().observe( this, tankDataObserver );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tank_sample, container, false);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void setEdEnabled(@IdRes int resId, boolean isEnabled, @ColorRes int enabledColorId) {
        View v = getView();
        TextView tv = v.findViewById(resId);
        tv.setText("");
        tv.setEnabled(isEnabled);
        tv.setBackgroundColor(isEnabled ? getResources().getColor(enabledColorId) : Color.TRANSPARENT);
        tv.setOnKeyListener(isEnabled ? kl : null);
    }

    String getTextByResId( @IdRes int resId )
    {
        TextView tv = getActivity().findViewById(resId);
        if( null != tv && tv.isEnabled() )
            return tv.getText().toString();
        return null;
    }

    final void setTextByResId( @IdRes int rid, Object dbl, int dec_places ) {
        EditText ed = getActivity().findViewById( rid );
        if( null == dbl )
            ed.setText(null);
        else {
            ed.setText( MyUtils.numberToString(dbl, dec_places));
        }
    }

    Double getDoubleByResId( @IdRes int resId )
    {
        String st = getTextByResId( resId );
        return (null == st || st.isEmpty()) ? null : MyUtils.stringToDouble( st );
    }

    Integer getIntegerByResId( @IdRes int resId )
    {
        String st = getTextByResId( resId );
        return (null == st || st.isEmpty()) ? null : Integer.valueOf( st );
    }

    private TankViewModel.TankSample readWidgets()
    {
        TankViewModel.TankSample ts = new TankViewModel.TankSample();
        ts.fuelLevel = getDoubleByResId(R.id.editFuelVolume);
        ts.fuelVolume = getDoubleByResId(R.id.editFuelVolume);
        ts.waterLevel = getDoubleByResId(R.id.editWaterLevel);
        ts.waterVolume = getDoubleByResId(R.id.editWaterVolume);
        ts.fuelDensity = MyUtils.IntegerToDouble( getIntegerByResId(R.id.editDensity), 1000000 );
        ts.fuelMass = getDoubleByResId(R.id.editFuelMass);
        ts.temperature = getDoubleByResId(R.id.editTemp);
        ts.tcFuelDensity = MyUtils.IntegerToDouble( getIntegerByResId(R.id.editTcFuelDens), 1000000 );
        ts.tcFuelVolume = getDoubleByResId(R.id.editTcFuelVolume);
        return ts;
    }

    private void postSampleUpdated( TankViewModel.TankSample ts )
    {
        //model.setSample(ts);
    }

    boolean getBoolByResId( @IdRes int resId )
    {
        Switch sw = getActivity().findViewById(resId);
        return sw.isChecked();
    }

    private final View.OnKeyListener kl = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if( KeyEvent.ACTION_DOWN == event.getAction()
                    && KeyEvent.KEYCODE_ENTER == keyCode
            )
            {
                Double val = (null == v) || ((TextView)v).getText().toString().isEmpty()
                        ? null
                        : MyUtils.stringToDouble(((TextView)v).getText().toString());
                boolean doRecalc = getBoolByResId(R.id.swRecalc);
                switch (v.getId())
                {
                    case R.id.editFuelVolume:
                        model.setFuelVolume( val, doRecalc ); break;
                    case R.id.editTotalLevel:
                        model.setTotalLevel(val, doRecalc); break;
                    case R.id.editWaterVolume:
                        model.setWaterVolume(val, doRecalc); break;
                    case R.id.editWaterLevel:
                        model.setWaterLevel(val, doRecalc); break;
                    case R.id.editDensity:
                        Integer iDensity = getIntegerByResId(R.id.editDensity);
                        model.setDensity( MyUtils.IntegerToDouble(iDensity, 1000000), doRecalc); break;
                    case R.id.editFuelMass:
                        model.setMass(val, doRecalc); break;
                    case R.id.editTemp:
                        model.setTemp(val, false); break;
                    case R.id.editTcFuelDens:
                        Integer iDensity1 = getIntegerByResId(R.id.editTcFuelDens);
                        model.setTcDensity(MyUtils.IntegerToDouble(iDensity1, 1000000), false);
                        break;
                    case R.id.editTcFuelVolume:
                        model.setTcVolume(val, false); break;
                    default:
                        TankViewModel.TankSample ts = readWidgets();
                        postSampleUpdated(ts);
                        break;
                }
                //return true;
            }
            return false;
        }
    };

    private void onTankDataUpdated(TankViewModel.TankData td)
    {
        View v = getView();
        TankViewModel.TankCfg tcfg = td.cfg;
        if( null != v && null != tcfg ) {
            setEdEnabled(R.id.editFuelVolume, tcfg.hasFuelVolume, R.color.FuelColor);
            setEdEnabled(R.id.editDensity, tcfg.hasDensity, R.color.FuelColor);
            setEdEnabled(R.id.editFuelMass, tcfg.hasMass, R.color.FuelColor);
            setEdEnabled(R.id.editTotalLevel, tcfg.hasTotalLevel, R.color.FuelColor);
            setEdEnabled(R.id.editWaterLevel, tcfg.hasWaterLevel, R.color.waterColor);
            setEdEnabled(R.id.editWaterVolume, tcfg.hasWaterVolume, R.color.waterColor);
            setEdEnabled(R.id.editTemp, tcfg.hasTemperature, R.color.FuelColor);
            setEdEnabled(R.id.editTcFuelDens, tcfg.hasTcDensity, R.color.FuelColor);
            setEdEnabled(R.id.editTcFuelVolume, tcfg.hasTcVolume, R.color.FuelColor);
        }
        TankViewModel.TankSample ts = td.sample;
        if( ts != null ) {
            setTextByResId(R.id.editDensity, MyUtils.DoubleToInteger(ts.fuelDensity, 1000000), 0 );
            setTextByResId(R.id.editFuelMass, ts.fuelMass, 3);
            setTextByResId(R.id.editFuelVolume, ts.fuelVolume, 3);
            setTextByResId(R.id.editTotalLevel, ts.fuelLevel, 1);
            setTextByResId(R.id.editWaterVolume, ts.waterVolume, 3);
            setTextByResId(R.id.editWaterLevel, ts.waterLevel, 1);
            setTextByResId(R.id.editTemp, ts.temperature, 1);
            setTextByResId(R.id.editTcFuelDens, MyUtils.DoubleToInteger(ts.tcFuelDensity, 1000000), 0 );
            setTextByResId(R.id.editTcFuelVolume, ts.tcFuelVolume, 3);
        }
    }

}

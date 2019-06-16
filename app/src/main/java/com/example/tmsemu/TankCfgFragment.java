package com.example.tmsemu;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.google.dmzavr.MyTankView;
import com.google.dmzavr.TmsEmuStorage;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TankCfgFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TankCfgFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TankCfgFragment extends Fragment implements CompoundButton.OnCheckedChangeListener
{
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

    public TankCfgFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TankCfgFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TankCfgFragment newInstance(String param1, String param2) {
        TankCfgFragment fragment = new TankCfgFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private void setTextWatcher(int resId )
    {
        EditText ed = getActivity().findViewById(resId);
        if( null != ed ) {
            //ed.setOnEditorActionListener(eal);
            ed.setOnKeyListener(kl);
            //ed.setImeOptions(EditorInfo.IME_ACTION_DONE);
            //ed.setImeActionLabel("Ok", EditorInfo.IME_ACTION_DONE);
            ////ed.addTextChangedListener(tw);
        }
    }
    private void setSwitchWatcher(int resId)
    {
        Switch sw = getActivity().findViewById(resId);
        if( null != sw )
            sw.setOnCheckedChangeListener(this);
    }

    final Observer<TankViewModel.TankData> tankDataObserver = new Observer<TankViewModel.TankData>() {
        @Override
        public void onChanged(@Nullable TankViewModel.TankData td) {
            onCfgUpdated(td.cfg);
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

    final void setFText( @IdRes int rid, Object dbl ) {
        EditText ed = getActivity().findViewById( rid );
        if( null == dbl ) ed.setText(null);
        else ed.setText( dbl.toString() );
    }

    final void setSwitchPos( @IdRes int resId, boolean isChecked)
    {
        Switch sw = getActivity().findViewById(resId);
        sw.setChecked(isChecked);
    }

    private boolean byModelUpdate = false;

    private void onCfgUpdated(TankViewModel.TankCfg tcfg)
    {
        byModelUpdate = true;
        setFText( R.id.edChannel, tcfg.channelId );
        setFText( R.id.edDia, tcfg.diameter );
        setFText( R.id.edVol, tcfg.volume );
        setSwitchPos(R.id.swHasFuelVol, tcfg.hasFuelVolume);
        setSwitchPos(R.id.swHasTotalLvl, tcfg.hasTotalLevel);
        setSwitchPos(R.id.swHasWaterVol, tcfg.hasWaterVolume);
        setSwitchPos(R.id.swHasWaterLvl, tcfg.hasWaterLevel);
        setSwitchPos(R.id.swHasDensity, tcfg.hasDensity);
        setSwitchPos(R.id.swHasMass, tcfg.hasMass);
        setSwitchPos(R.id.swHasTemperature, tcfg.hasTemperature);
        setSwitchPos(R.id.swHasTcDensity, tcfg.hasTcDensity);
        setSwitchPos(R.id.swHasTcVolume, tcfg.hasTcVolume);
        setSwitchPos(R.id.swHasUllage, tcfg.hasUllage);

        model.getTankData().removeObserver(tankDataObserver);

        EditText channel_editor = getActivity().findViewById(R.id.edChannel);
        if( mChannelId >= 0 && null != tcfg.channelId && 0 <= tcfg.channelId ) {
            channel_editor.setText(Integer.toString(mChannelId));
            channel_editor.setFocusable(false);
            channel_editor.setEnabled(false);
            channel_editor.setBackgroundColor(Color.LTGRAY);
        }
        byModelUpdate = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View res = inflater.inflate(R.layout.fragment_tank_cfg, container, false);
        EditText channel_editor = res.findViewById(R.id.edChannel);
        return res;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTextWatcher( R.id.edChannel );
        setTextWatcher( R.id.edVol );
        setTextWatcher( R.id.edDia );
        setSwitchWatcher(R.id.swHasWaterVol);
        setSwitchWatcher(R.id.swHasWaterLvl);
        setSwitchWatcher(R.id.swHasTotalLvl);
        setSwitchWatcher(R.id.swHasMass);
        setSwitchWatcher(R.id.swHasDensity);
        setSwitchWatcher(R.id.swHasFuelVol);
        setSwitchWatcher(R.id.swHasTemperature);
        setSwitchWatcher(R.id.swHasTcVolume);
        setSwitchWatcher(R.id.swHasTcDensity);
        setSwitchWatcher(R.id.swHasUllage);

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

    String getTextByResId( @IdRes int resId )
    {
        TextView tv = getActivity().findViewById(resId);
        return tv.getText().toString();
    }

    Integer getIntegerByResId( @IdRes int resId )
    {
        String st = getTextByResId( resId );
        return (null == st || st.isEmpty()) ? null : Integer.valueOf( st );
    }

    Double getDoubleByResId( @IdRes int resId )
    {
        String st = getTextByResId( resId );
        return (null == st || st.isEmpty()) ? null : Double.valueOf( st );
    }

    boolean getBoolByResId( int resId )
    {
        Switch sw = getActivity().findViewById(resId);
        return sw.isChecked();
    }

    void postCfgUpdated()
    {
        TankViewModel.TankCfg tcfg = new TankViewModel.TankCfg();
        tcfg.channelId = getIntegerByResId(R.id.edChannel);
        tcfg.diameter = getIntegerByResId(R.id.edDia);
        tcfg.volume = getDoubleByResId(R.id.edVol);
        tcfg.hasDensity = getBoolByResId(R.id.swHasDensity);
        tcfg.hasFuelVolume = getBoolByResId(R.id.swHasFuelVol);
        tcfg.hasMass = getBoolByResId(R.id.swHasMass);
        tcfg.hasTotalLevel = getBoolByResId(R.id.swHasTotalLvl);
        tcfg.hasWaterLevel = getBoolByResId(R.id.swHasWaterLvl);
        tcfg.hasWaterVolume = getBoolByResId(R.id.swHasWaterVol);
        tcfg.hasTemperature = getBoolByResId(R.id.swHasTemperature);
        tcfg.hasTcVolume = getBoolByResId(R.id.swHasTcVolume);
        tcfg.hasTcDensity = getBoolByResId(R.id.swHasTcDensity);
        tcfg.hasUllage = getBoolByResId(R.id.swHasUllage);
        model.setCfg( tcfg );
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if( !byModelUpdate ) {
            if( buttonView == getActivity().findViewById(R.id.swHasFuelVol) ) {
                if( !isChecked && !getBoolByResId(R.id.swHasTotalLvl) ) {
                    Switch sw = getActivity().findViewById(R.id.swHasTotalLvl);
                    sw.setChecked(true);
                    return;
                }
            } else if(buttonView == getActivity().findViewById(R.id.swHasTotalLvl)) {
                if( !isChecked && !getBoolByResId(R.id.swHasFuelVol) ) {
                    Switch sw = getActivity().findViewById(R.id.swHasFuelVol);
                    sw.setChecked(true);
                    return;
                }

            }
            postCfgUpdated();
        }
    }

    private final View.OnKeyListener kl = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if( KeyEvent.ACTION_DOWN == event.getAction()
                && KeyEvent.KEYCODE_ENTER == keyCode
            ) {
                postCfgUpdated();
                //return true;
            }
            return false;
        }
    };

    private final EditText.OnEditorActionListener eal = new EditText.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if(EditorInfo.IME_ACTION_DONE == actionId ) {
                postCfgUpdated();
                return true;
            }
            return false;
        }
    };
    private final TextWatcher tw = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        private int inside = 0;

        @Override
        public void afterTextChanged(Editable s) {
            android.view.View focused = getActivity().getCurrentFocus();
            if( 0 != inside || null == focused )
                return;

            int id  = focused.getId();

            inside = 1;
            if( ! byModelUpdate )
                postCfgUpdated();

            inside = 0;
        }
    };
}

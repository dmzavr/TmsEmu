package com.google.dmzavr;

import android.app.Activity;
import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.View;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

public abstract class MyUtils {
    public static Activity getActivity(View view) {
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }
    public static LifecycleOwner getLifecycleOwner(View view) {
        Context context = view.getContext();
        while (!(context instanceof LifecycleOwner)) {
            context = ((ContextWrapper) context).getBaseContext();
        }
        return (LifecycleOwner) context;
    }

    public static Integer DoubleToInteger(Double d, int multiplier)
    {
        if( null == d || d * multiplier > Integer.MAX_VALUE || d * multiplier < Integer.MIN_VALUE)
            return null;
        return Double.valueOf(d * multiplier).intValue();
    }
    public static Double IntegerToDouble(Integer i, double divider)
    {
        if( null == i )
            return null;
        return i.doubleValue() / divider;
    }

    public static String numberToString( Object d, int decpos )
    {
        if( d == null )
            return "";
        if( d instanceof Double || d instanceof Float ) {
            String fmt = decpos > 0 ? new String( new char[decpos]).replace("\0", "#") : "";
            DecimalFormat df = new DecimalFormat( "#." + fmt );
            DecimalFormatSymbols dfs = new DecimalFormatSymbols();
            dfs.setDecimalSeparator('.');
            df.setDecimalFormatSymbols(dfs);
            return df.format( d );
        }

        return d.toString();
    }

    public static Double stringToDouble(String s) {
        if( null == s || s.isEmpty() )
            return null;
        DecimalFormat df = new DecimalFormat();
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(dfs);
        try {
            return df.parse(s).doubleValue();
        } catch (ParseException e) {
            ;
        }
        return null;
    }
}

package com.google.dmzavr;

import android.app.Application;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Map;

final public class TmsEmuApp extends Application {

    static public TmsEmuStorage storage;

    @Override
    public void onCreate() {
        super.onCreate();

        storage = TmsEmuStorage.getInstance();
        storage.readEmuData( getApplicationContext() );
    }
}

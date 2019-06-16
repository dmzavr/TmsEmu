package com.google.dmzavr;

import android.content.Context;
import android.provider.ContactsContract;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.tmsemu.GlobalViewModelFactory;
import com.example.tmsemu.TankViewModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import static java.util.Comparator.naturalOrder;

final public class TmsEmuStorage {
//    public static class TankStorageItem {
//        public double physVolume;
//        public double physRadius;
//        public double totalVolume;
//        public double totalLevel;
//        public double waterLevel;
//        public double waterVolume;
//        public double fuelVolume;
//        public double fuelDensity;
//        public double fuelMass;
//    }

    private Map< Integer, GlobalViewModelFactory> globalViewModelFactoryMap = new HashMap<>();

    public GlobalViewModelFactory getFactory(int channelId) {
        if( globalViewModelFactoryMap.containsKey( channelId ) )
            return globalViewModelFactoryMap.get(channelId);
        GlobalViewModelFactory factory = new GlobalViewModelFactory(channelId);
        globalViewModelFactoryMap.put(channelId, factory);
        return factory;
    }

    private TmsEmuStorage() {
    }

    public List<Integer> getChannelsList() {
        ArrayList<Integer> result = new ArrayList<>();
        for( Integer k : storage1.keySet() ) {
            if( null != k && k >= 0 )
                result.add(k);
        }
        Collections.sort(result);
        return result;
    }

    private static TmsEmuStorage intstance = null;

    public static TmsEmuStorage getInstance() {
        if( null == intstance )
            intstance = new TmsEmuStorage();
        return intstance;
    }

    public static class DataItem {
        public TankViewModel.TankCfg tankCfg;
        public  TankViewModel.TankSample tankSample;
        public DataItem() {}
        public DataItem(TankViewModel.TankCfg tc, TankViewModel.TankSample ts) {
            tankCfg = new TankViewModel.TankCfg(tc);
            tankSample = new TankViewModel.TankSample(ts);
        }
    }
    // private Context ctx;
//    private final Map<Integer, TankStorageItem> storage = new HashMap<>();
    // private SparseArray<DataItem> storage1 = new SparseArray<>(0);
    private Map<Integer, DataItem> storage1 = new HashMap<>();

    public DataItem getItem(int channelId)
    {
        return storage1.get(channelId);
    }

    public void putItem( DataItem item )
    {
        if( null != item && null != item.tankCfg && null != item.tankCfg.channelId )
            storage1.put(item.tankCfg.channelId, item);
    }
//    public TankStorageItem getTankData(int tankid) {
//        return storage.get(tankid);
//    }
//
//    public void setTankData(int tankid, TankStorageItem itm) {
//        storage.put(tankid, itm);
//    }

    static private final String storageFileName = "tmsemu.dat";

    public void readEmuData(Context ctx) {
        String jsonStr;
        try (FileInputStream fis = ctx.openFileInput(storageFileName)) {
            try {
                FileChannel fc = fis.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                jsonStr = Charset.defaultCharset().decode(bb).toString();
                /*
                JSONObject json = new JSONObject(jsonStr);
                JSONArray jsonCans = json.getJSONArray("cans");
                for (int i = 0; i < jsonCans.length(); ++i) {
                    JSONObject c = jsonCans.getJSONObject(i);
                    TankStorageItem itm = new TankStorageItem();
                    int id = c.getInt("id");
                    itm.physVolume = c.getDouble("physVolume");
                    itm.physRadius = c.getDouble("physRadius");
                    itm.totalVolume = c.getDouble("totalVolume");
                    itm.totalLevel = c.getDouble("totalLevel");
                    itm.waterVolume = c.getDouble("waterVolume");
                    itm.waterLevel = c.getDouble("waterLevel");
                    itm.fuelVolume = c.getDouble("fuelVolume");
                    itm.fuelDensity = c.getDouble("fuelDensity");
                    itm.fuelMass = c.getDouble("fuelMass");
                    storage.put(id, itm);
                }*/
                storage1 = fromJson(jsonStr);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        } catch (Exception e) {
            //
        }
        if( null == storage1 )
            storage1 = new HashMap<>();
    }

    public String toJson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();
        return gson.toJson(storage1);
    }

    public Map<Integer,DataItem> fromJson(String jsonText)
    {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        Type storageType = new TypeToken<HashMap<Integer, DataItem> >() {}.getType();
        Map<Integer, DataItem> stor = gson.fromJson(jsonText, storageType);
        return stor;
    }

    public SparseArray<DataItem> fromJsonSA(String jsonText)
    {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        Type storageType = new TypeToken<SparseArray<DataItem> >() {}.getType();
        SparseArray<DataItem> stor = gson.fromJson(jsonText, storageType);
        return stor;
    }

    public void saveData( Context ctx ) {

        try (FileOutputStream fos = ctx.openFileOutput(storageFileName, Context.MODE_PRIVATE)) {
            try {
                /*
                JSONArray jsonCans = new JSONArray();
                for(Map.Entry<Integer, TankStorageItem> centry : storage.entrySet() ) {
                    JSONObject c = new JSONObject();
                    TankStorageItem itm = centry.getValue();
                    c.put("id", centry.getKey() );
                    c.put("physVolume", itm.physVolume );
                    c.put("physRadius", itm.physRadius );
                    c.put("totalVolume", itm.totalVolume );
                    c.put("totalLevel", itm.totalLevel );
                    c.put("waterVolume", itm.waterVolume );
                    c.put("waterLevel", itm.waterLevel );
                    c.put("fuelVolume", itm.fuelVolume );
                    c.put("fuelDensity", itm.fuelDensity );
                    c.put("fuelMass", itm.fuelMass );
                    jsonCans.put(c);
                }
                JSONObject json = new JSONObject();
                json.put( "cans", jsonCans );
                fos.write( json.toString().getBytes() );
                fos.flush();
                */
                fos.write(toJson().getBytes());

            } catch (Exception e) {
                //e.printStackTrace();
            }
        } catch ( Exception e ) {
            //
        }
    }
}

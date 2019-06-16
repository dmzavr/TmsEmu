package com.example.tmsemu;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.util.Log;

import com.google.dmzavr.TmsEmuStorage;

import java.util.List;

public class TankListViewModel extends GlobalViewModel {

    public TankListViewModel(Runnable onClear, Integer fake)
    {
        super(onClear, fake);
    }

    private MutableLiveData<List<Integer>> tankList;

    @Override
    protected void onShareCleared() {

    }

    public LiveData< List<Integer> > getTankList()
    {
        if( tankList == null ) {
            tankList = new MutableLiveData<>();
            loadStorage();
        }
        return tankList;
    }

    private void loadStorage() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                tankList.postValue(
                        TmsEmuStorage.getInstance().getChannelsList()
                );
            }
        };
        new Thread(runnable).start();
    }

    public synchronized boolean addChannel(Context ctx, TmsEmuStorage.DataItem item )
    {
        List< Integer > lst = tankList.getValue();
        if( lst.contains(item.tankCfg.channelId) ) {
//            Log.w("TankListViewModel", "Уже есть резервуар с №" + item.tankCfg.channelId);
            return false;
        }
        TmsEmuStorage.getInstance().putItem( item );
        TmsEmuStorage.getInstance().saveData( ctx );
        tankList.postValue( TmsEmuStorage.getInstance().getChannelsList() );

        return true;
    }
}

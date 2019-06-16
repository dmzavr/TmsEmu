package com.example.tmsemu;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModel;

import android.support.annotation.NonNull;


import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class GlobalViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final Map</*Class<? extends ViewModel>*/ String, ViewModel> mGlobalCache = new HashMap<>();

    private Integer channelId = -1;

    public GlobalViewModelFactory(Integer cId)
    {
        channelId = cId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(final @NonNull Class<T> modelClass) {
        if( GlobalViewModel.class.isAssignableFrom(modelClass)) {
            GlobalViewModel globalVM;

            final String key = channelId.toString() + "/" + modelClass.getCanonicalName();
            if (mGlobalCache.containsKey(key)) {
                globalVM = (GlobalViewModel) mGlobalCache.get(key);
            } else {
                try {
                    globalVM = (GlobalViewModel) modelClass.getConstructor(Runnable.class, Integer.class ).newInstance(new Runnable() {
                        @Override
                        public void run() {
                            //String key = channelId.toString() + "/" + modelClass.getCanonicalName();
                            mGlobalCache.remove(key);
                        }
                    }, channelId);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("Cannot create an instance of " + modelClass, e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot create an instance of " + modelClass, e);
                } catch (InstantiationException e) {
                    throw new RuntimeException("Cannot create an instance of " + modelClass, e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException("Cannot create an instance of " + modelClass, e);
                }
                mGlobalCache.put(key, globalVM);
            }

            globalVM.incRefCount();
            return (T) globalVM;
        }
        return super.create(modelClass);
    }

}

package com.jancar.bluetooth.utils;

import android.text.TextUtils;

import com.jancar.sdk.utils.Logcat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SpendTimeUtils {
    public static class SpendTime{
        public long starttime;
        public String modelname;
        public int logcount = 0;
        public long spentime;

        @Override
        public String toString() {
            return "SpendTime{" +
                    "starttime=" + starttime +
                    ", modelname='" + modelname + '\'' +
                    ", logcount=" + logcount +
                    ", spentime=" + spentime +
                    '}';
        }
    }

    private enum singelton{
        INSTANCE;
        private final SpendTimeUtils mSpendTimeUtils;

        singelton() {
            mSpendTimeUtils = new SpendTimeUtils();
        }
        private SpendTimeUtils getInstance(){
            return mSpendTimeUtils;
        }
    }
    private List<SpendTime> mSpendList;
    private SpendTimeUtils(){
        mSpendList = new ArrayList<>();
    }

    public static SpendTimeUtils getInstance(){
        return singelton.INSTANCE.getInstance();
    }
    public void start(String name){
        SpendTime spendTime = new SpendTime();
        spendTime.starttime = System.currentTimeMillis();
        spendTime.modelname = name;
        synchronized (this){
            boolean has = false;
            for(SpendTime s:mSpendList){
                if(s.modelname.endsWith(name)){
                    has = true;
                    break;
                }
            }
            if(!has){
                mSpendList.add(spendTime);
            }
        }

    }
    public void spend(String name){
        SpendTime spendTime = null;
        for(SpendTime s:mSpendList){
            if(TextUtils.equals(s.modelname,name)){
                s.logcount ++;
                s.spentime = System.currentTimeMillis() - s.starttime;
                Logcat.d("SpendTimeUtils: in " + Thread.currentThread().getName() + ",  " + s.toString());
                break;
            }
        }
    }
    public void stop(String name){
        SpendTime spendTime = null;
        Iterator iterable =  mSpendList.iterator();
        while (iterable.hasNext()){
            if(TextUtils.equals((spendTime = (SpendTime)(iterable.next())).modelname,name)){
                mSpendList.remove(spendTime);
                break;
            }
        }
    }
    public void destroy(){
        mSpendList.clear();
    }




}

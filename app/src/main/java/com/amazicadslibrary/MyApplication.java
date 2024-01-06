package com.amazicadslibrary;

import com.lvt.ads.event.AppsflyerEvent;
import com.lvt.ads.util.AdsApplication;
import com.lvt.ads.util.AppOpenManager;

import java.util.List;

public class MyApplication extends AdsApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        AppOpenManager.getInstance().disableAppResumeWithActivity(Splash.class);
        AppsflyerEvent.getInstance().init(this, "1233",true);

    }

    @Override
    public boolean enableAdsResume() {
        return true;
    }

    @Override
    public List<String> getListTestDeviceId() {
        return null;
    }


    @Override
    public String getResumeAdId() {
        return "ca-app-pub-3940256099942544/9257395921";
    }

    @Override
    public Boolean buildDebug() {
        return true;
    }
}

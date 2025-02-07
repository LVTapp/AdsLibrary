package com.amazicadslibrary;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.ads.LoadAdError;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.lvt.ads.callback.AdCallback;
import com.lvt.ads.callback.ApiCallBack;
import com.lvt.ads.util.Admob;
import com.lvt.ads.util.AdsConsentManager;
import com.lvt.ads.util.AppOpenManager;

import java.util.ArrayList;
import java.util.List;

public class Splash extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    AdCallback adCallback;
    public static String PRODUCT_ID_MONTH = "android.test.purchased";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        String android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
       Admob.getInstance().setOpenShowAllAds(true);
       Admob.getInstance().setDisableAdResumeWhenClickAds(true);
        Admob.getInstance().setTimeLimitShowAds(60000);

        // Admob
      /*  AppPurchase.getInstance().setBillingListener(new BillingListener() {
            @Override
            public void onInitBillingListener(int code) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Admob.getInstance().loadSplashInterAds(Splash.this,"ca-app-pub-3940256099942544/1033173712",25000,5000, new InterCallback(){
                            @Override
                            public void onAdClosed() {
                                startActivity(new Intent(Splash.this,MainActivity.class));
                                finish();
                            }

                            @Override
                            public void onAdFailedToLoad(LoadAdError i) {
                                super.onAdFailedToLoad(i);
                                startActivity(new Intent(Splash.this,MainActivity.class));
                                finish();
                            }
                        });
                    }
                });
            }
        }, 5000);*/

        adCallback = new AdCallback(){
            @Override
            public void onNextAction() {
                super.onNextAction();
                startActivity(new Intent(Splash.this,MainActivity.class));
                finish();
            }

            @Override
            public void onAdFailedToLoad(@Nullable LoadAdError i) {
                super.onAdFailedToLoad(i);
                Log.e("xxxx load fall : ",i.toString());
            }
        };

        List listID = new ArrayList();
        listID.add("ca-app-pub-3940256099942544/9257395921");
        listID.add("ca-app-pub-3940256099942544/9257395921");

        AppOpenManager.getInstance().loadOpenAppAdSplash(this,listID,true, adCallback );






        initBilling();
    }

    private void initBilling() {
       /* List<String> listINAPId = new ArrayList<>();
        listINAPId.add(PRODUCT_ID_MONTH);
        List<String> listSubsId = new ArrayList<>();
        AppPurchase.getInstance().initBilling(getApplication(),listINAPId,listSubsId);*/

    }

    @Override
    protected void onResume() {
        super.onResume();
        AppOpenManager.getInstance().onCheckShowSplashWhenFail(this,adCallback,1000);
    }
}
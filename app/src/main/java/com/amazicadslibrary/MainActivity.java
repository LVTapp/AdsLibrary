package com.amazicadslibrary;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.ads.rewarded.RewardedAd;
import com.lvt.ads.callback.RewardCallback;
import com.lvt.ads.callback.InterCallback;
import com.lvt.ads.util.Admob;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.rewarded.RewardItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private InterstitialAd mInterstitialAd2=null;
    private FrameLayout native_ads;

    public static String PRODUCT_ID_YEAR = "android.test.purchased";
    public static String PRODUCT_ID_MONTH = "android.test.purchased";
    public static  List<String> listID;
    Runnable runnable;
    Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        native_ads   = findViewById(R.id.native_ads);
        listID = new ArrayList<>();
        listID.add(getString(R.string.admod_banner_collap_id));
       // Admob.getInstance().initRewardAds(this,getString(R.string.admod_app_reward_id));
       // loadAdsNative();

        findViewById(R.id.clickFGM).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,MainActivity2.class));
            }
        });
        loadAdsNative();
        loadInterNotLimit();


        findViewById(R.id.btnClickInter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Admob.getInstance().showInterAll(MainActivity.this, new InterCallback() {
                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        startActivity(new Intent(MainActivity.this,MainActivity3.class));
                    }

                    @Override
                    public void onAdImpression() {
                        super.onAdImpression();
                        Log.e("xxxx","onAdImpression");
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        Log.e("xxxx","onAdClicked");
                    }
                });
            }
        });


        findViewById(R.id.btnClickReward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Admob.getInstance().loadAndShowRewardAds(MainActivity.this, getString(R.string.admod_app_reward_id), new RewardCallback() {
                    @Override
                    public void onEarnedReward(RewardItem rewardItem) {
                            Log.e("xxxxx","onEarnedReward");
                    }

                    @Override
                    public void onAdClosed() {
                        Log.e("xxxxx","onAdClosed");
                    }

                    @Override
                    public void onAdFailedToShow(int codeError) {
                        Log.e("xxxxx","onAdFailedToShow");
                    }

                    @Override
                    public void onAdImpression() {
                        Log.e("xxxxx","onAdImpression");
                    }

                    @Override
                    public void onAdLoaded(RewardedAd rewardedAd) {
                        Log.e("xxxxx","onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad() {
                        Log.e("xxxxx","onAdFailedToLoad");
                    }
                });
                /*Admob.getInstance().showRewardAds(MainActivity.this,new RewardCallback(){
                    @Override
                    public void onEarnedReward(RewardItem rewardItem) {
                        Toast.makeText(MainActivity.this,"Trả thưởng thành công",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdClosed() {
                        Toast.makeText(MainActivity.this,"Close ads",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdFailedToShow(int codeError) {
                        Toast.makeText(MainActivity.this,"Loa ads err",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAdImpression() {
                        Toast.makeText(MainActivity.this,"onAdImpression",Toast.LENGTH_SHORT).show();
                    }
                });*/

            }
        });
        findViewById(R.id.btnClickShowInterNotLimit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Admob.getInstance().showInterAds(MainActivity.this,mInterstitialAd2,new InterCallback(){
                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        Log.e("xxxx", "onNextAction ");
                       loadInterNotLimit();
                    }
                });
            }
        });

        Admob.getInstance().loadCollapsibleBanner(MainActivity.this,getString(R.string.admod_banner_collap_id),20000);
    }

    private void loadInterNotLimit() {
        Log.e("xxxx", "loadInterNotLimit ");
        Admob.getInstance().loadInterAds(this,getString(R.string.ads_test_inter),new InterCallback(){
            @Override
            public void onAdLoadSuccess(InterstitialAd interstitialAd) {
                super.onAdLoadSuccess(interstitialAd);
                mInterstitialAd2 = interstitialAd;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadAdInter();

    }

    private void loadAdsNative(){
        List<String> listID = new ArrayList<>();
        listID.add("1");
        listID.add("2");
        listID.add(getString(R.string.ads_test_native));
        Admob.getInstance().loadNativeBanner(this, getString(R.string.ads_test_native), native_ads,10000,true);
        Admob.getInstance().loadNativeAd(this, "id native", native_ads,R.layout.ads_native);
    }

    private void loadAdInter() {
        Admob.getInstance().loadInterAll(this,getString(R.string.ads_test_inter));
    }

}
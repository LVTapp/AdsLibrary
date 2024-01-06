package com.amazicadslibrary;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.lvt.ads.callback.BannerCallBack;
import com.lvt.ads.callback.NativeCallback;
import com.lvt.ads.callback.PurchaseListioner;
import com.lvt.ads.callback.RewardCallback;
import com.lvt.ads.callback.InterCallback;
import com.lvt.ads.service.AdmobApi;
import com.lvt.ads.util.Admob;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private InterstitialAd mInterstitialAd;
    private FrameLayout native_ads;

    public static String PRODUCT_ID_YEAR = "android.test.purchased";
    public static String PRODUCT_ID_MONTH = "android.test.purchased";
    public static  List<String> listID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        native_ads   = findViewById(R.id.native_ads);
        listID = new ArrayList<>();
        listID.add(getString(R.string.admod_banner_collap_id));
        Admob.getInstance().initRewardAds(this,getString(R.string.admod_app_reward_id));
        Admob.getInstance().setTimeLimitShowAds(30000);
        loadAdInter();
        loadAdsNative();

        findViewById(R.id.clickFGM).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,MainActivity2.class));
            }
        });



        findViewById(R.id.btnClickInter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Admob.getInstance().showInterAds(MainActivity.this,AdsUtil.interAll, new InterCallback() {
                    @Override
                    public void onNextAction() {
                        super.onNextAction();
                        startActivity(new Intent(MainActivity.this,MainActivity3.class));
                        loadAdInter();
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
                Admob.getInstance().showRewardAds(MainActivity.this,new RewardCallback(){
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
                });
            }
        });




        findViewById(R.id.btnClickLoadAndShow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Admob.getInstance().loadAndShowInter(MainActivity.this,getString(R.string.admod_interstitial_id),0,10000, new InterCallback(){
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        startActivity(new Intent(MainActivity.this,MainActivity2.class));
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        startActivity(new Intent(MainActivity.this,MainActivity2.class));
                    }
                });
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        Admob.getInstance().loadBanner(this,getString(R.string.ads_test_banner));

    }

    private void loadAdsNative(){
        List<String> listID = new ArrayList<>();
        listID.add("1");
        listID.add("2");
        listID.add(getString(R.string.ads_test_native));
        Admob.getInstance().loadNativeAd(this, listID, native_ads,R.layout.ads_native_btn_ads_bot);

        //Admob.getInstance().loadNativeAd(this, "id native", native_ads,R.layout.ads_native);
    }

    private void loadAdInter() {
        Admob.getInstance().loadInterAds(this,getString(R.string.ads_test_inter),new InterCallback(){
            @Override
            public void onAdLoadSuccess(InterstitialAd interstitialAd) {
                super.onAdLoadSuccess(interstitialAd);
                AdsUtil.interAll =interstitialAd;

            }
        });
    }

}
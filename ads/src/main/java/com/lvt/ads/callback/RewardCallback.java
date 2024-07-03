package com.lvt.ads.callback;

import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;

public class RewardCallback {
    public void onEarnedReward(RewardItem rewardItem) {}

    public void onAdClosed(){};
    public void onAdFailedToShow(int codeError){};
    public void onAdImpression(){};
    public void onAdLoaded(RewardedAd rewardedAd){};
    public void onAdFailedToLoad(){};
}

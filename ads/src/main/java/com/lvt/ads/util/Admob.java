package com.lvt.ads.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.lvt.ads.BuildConfig;
import com.lvt.ads.R;
import com.lvt.ads.callback.BannerCallBack;
import com.lvt.ads.callback.InterCallback;
import com.lvt.ads.callback.NativeCallback;
import com.lvt.ads.callback.RewardCallback;
import com.lvt.ads.dialog.LoadingAdsDialog;
import com.lvt.ads.event.AdType;
import com.lvt.ads.event.FirebaseUtil;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class Admob {
    private static Admob INSTANCE;
    private static final String TAG = "Admob";
    private LoadingAdsDialog dialog;
    private int currentClicked = 0;
    private int numShowAds = 3;
    private int maxClickAds = 10;
    private Handler handlerTimeout;
    private Runnable rdTimeout;
    private boolean isTimeLimited;
    private boolean isShowLoadingSplash = false; //kiểm tra trạng thái ad splash, ko cho load, show khi đang show loading ads splash
    boolean checkTimeDelay = false;
    private boolean openActivityAfterShowInterAds = true;
    private Context context;
    boolean isTimeDelay = false; //xử lý delay time show ads, = true mới show ads
    private boolean isTimeout; // xử lý timeout show ads

    private RewardedAd rewardedAd;
    private String rewardedId;
    private String checkDeviceTest = "Test ad";
    public static boolean isShowAdsDeviceTest = false;
    //true : show ads
    //false : hide ads
    public static boolean isDeviceTest = false;
    private CountDownTimer countDownTimerNative;
    InterstitialAd mInterstitialSplash;
    InterstitialAd interstitialAd;
    private boolean disableAdResumeWhenClickAds = false;
    public static final String BANNER_INLINE_SMALL_STYLE = "BANNER_INLINE_SMALL_STYLE";
    public static final String BANNER_INLINE_LARGE_STYLE = "BANNER_INLINE_LARGE_STYLE";
    private static int MAX_SMALL_INLINE_BANNER_HEIGHT = 50;

    public static long timeLimitAds = 0; // Set > 1000 nếu cần limit ads click
    private boolean isShowInter = true;
    private boolean isShowNative = true;
    public static boolean isShowAllAds = true;
    private boolean checkLoadBanner = false;
    private boolean checkLoadBannerCollap = false;

    private Handler handler = new Handler();
    private Handler handlerIT = new Handler();
    private Handler handlerNT = new Handler();
    private Runnable runnable;
    private Runnable runnableNT;

    private long timeLimitShowAds = 0;
    String adsTestNative = "ca-app-pub-3940256099942544/2247696110";

    public static Admob getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Admob();
        }
        return INSTANCE;
    }

    public void initAdmob(Context context, List<String> testDeviceList) {
        Helper.setupAdmodData(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = Application.getProcessName();
            String packageName = context.getPackageName();
            if (!packageName.equals(processName)) {
                WebView.setDataDirectorySuffix(processName);
            }
        }
        MobileAds.initialize(context, initializationStatus -> {
        });
        MobileAds.setRequestConfiguration(new RequestConfiguration.Builder().setTestDeviceIds(testDeviceList).build());

        this.context = context;
    }

    public void initAdmob(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = Application.getProcessName();
            String packageName = context.getPackageName();
            if (!packageName.equals(processName)) {
                WebView.setDataDirectorySuffix(processName);
            }
        }

        MobileAds.initialize(context, initializationStatus -> {
        });
        if (BuildConfig.DEBUG) {
            MobileAds.setRequestConfiguration(new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList(getDeviceId((Activity) context))).build());
        }

        this.context = context;
    }


    /* =======================   Banner ================================= */

    /**
     * Set tắt ads resume khi click ads
     */
    public void setDisableAdResumeWhenClickAds(boolean disableAdResumeWhenClickAds) {
        this.disableAdResumeWhenClickAds = disableAdResumeWhenClickAds;
    }

    /**
     * Set time show ads
     **/

    public void setTimeLimitShowAds(long timeLimitAds) {
        this.timeLimitShowAds = timeLimitAds;
    }

    /**
     * Set tắt toàn bộ ads trong project
     **/
    public void setOpenShowAllAds(boolean isShowAllAds) {
        this.isShowAllAds = isShowAllAds;
    }

    public void setShowAdsDeviceTest(boolean isShowAds) {
        this.isShowAdsDeviceTest = isShowAds;
    }

    /**=========
     * ========================Banner ======================================**/
    /** Load ads Banner Trong Activity  **/

    /**
     * load banner in Activity
     **/
    public void loadBanner(final Activity mActivity, String id) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        adContainer.setVisibility(View.GONE);
        containerShimmer.setVisibility(View.VISIBLE);
        if (!isShowAllAds || !isNetworkConnected() || isDeviceTest) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(mActivity, id, adContainer, containerShimmer, new BannerCallBack() {
                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    loadBanner(mActivity, id);
                }
            }, false, BANNER_INLINE_LARGE_STYLE);
        }
    }

    /**
     * load banner in Activity return callBack
     **/
    public void loadBanner(final Activity mActivity, String id, BannerCallBack callback) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        adContainer.setVisibility(View.GONE);
        containerShimmer.setVisibility(View.VISIBLE);
        if (!isShowAllAds || !isNetworkConnected() || isDeviceTest) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(mActivity, id, adContainer, containerShimmer, callback, false, BANNER_INLINE_LARGE_STYLE);
        }
    }


    /**
     * load banner in Activity by List
     **/
    public void loadBanner(final Activity mActivity, List<String> listID) {
        Log.e("Admob", "Load Banner ID Floor");
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        adContainer.setVisibility(View.GONE);
        containerShimmer.setVisibility(View.VISIBLE);
        if (!isShowAllAds || !isNetworkConnected() || isDeviceTest) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            if (listID == null) {
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            if (listID.size() == 0) {
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            List idNew = new ArrayList();
            for (String id : listID) {
                idNew.add(id);
            }
            checkLoadBanner = false;
            loadBanner(mActivity, idNew, adContainer, containerShimmer, null, false, BANNER_INLINE_LARGE_STYLE);
        }
    }

    /**
     * load banner in Activity by List return CallBack
     **/
    public void loadBanner(final Activity mActivity, List<String> listID, BannerCallBack bannerCallBack) {
        Log.e("Admob", "Load Banner ID Floor");
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        adContainer.setVisibility(View.GONE);
        containerShimmer.setVisibility(View.VISIBLE);
        if (!isShowAllAds || !isNetworkConnected() || isDeviceTest) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            if (listID == null) {
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            if (listID.size() == 0) {
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            List idNew = new ArrayList();
            for (String id : listID) {
                idNew.add(id);
            }
            checkLoadBanner = false;
            loadBanner(mActivity, idNew, adContainer, containerShimmer, bannerCallBack, false, BANNER_INLINE_LARGE_STYLE);
        }
    }


    /**
     * Load ads Banner Trong Activity set Inline adaptive banners
     */
    public void loadBanner(final Activity mActivity, String id, Boolean useInlineAdaptive) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        adContainer.setVisibility(View.GONE);
        containerShimmer.setVisibility(View.VISIBLE);
        if (!isShowAllAds || !isNetworkConnected() || isDeviceTest) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(mActivity, id, adContainer, containerShimmer, null, useInlineAdaptive, BANNER_INLINE_LARGE_STYLE);
        }
    }

    /**
     * Load ads Banner Trong Activity set Inline adaptive banners
     */
    public void loadInlineBanner(final Activity activity, String id, String inlineStyle) {
        final FrameLayout adContainer = activity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = activity.findViewById(R.id.shimmer_container_banner);
        loadBanner(activity, id, adContainer, containerShimmer, null, true, inlineStyle);
    }

    /**
     * Load ads Banner Trong Activity set Inline adaptive banners
     */
    public void loadBanner(final Activity mActivity, String id, final BannerCallBack callback, Boolean useInlineAdaptive) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        adContainer.setVisibility(View.GONE);
        containerShimmer.setVisibility(View.VISIBLE);
        if (!isShowAllAds || !isNetworkConnected() || isDeviceTest) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(mActivity, id, adContainer, containerShimmer, callback, useInlineAdaptive, BANNER_INLINE_LARGE_STYLE);
        }
    }

    /**
     * Load ads Banner Trong Activity set Inline adaptive banners
     */
    public void loadInlineBanner(final Activity activity, String id, String inlineStyle, final BannerCallBack callback) {
        final FrameLayout adContainer = activity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = activity.findViewById(R.id.shimmer_container_banner);
        if (!isShowAllAds || !isNetworkConnected() || isDeviceTest) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(activity, id, adContainer, containerShimmer, callback, true, inlineStyle);
        }
    }

    /**
     * Load ads Collapsible Banner Trong Activity
     */
    public void loadCollapsibleBanner(final Activity mActivity, String id, String gravity) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        adContainer.setVisibility(View.GONE);
        containerShimmer.setVisibility(View.VISIBLE);
        if (!isShowAllAds || !isNetworkConnected() || isDeviceTest) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadCollapsibleBanner(mActivity, id, gravity, adContainer, containerShimmer);
        }
    }

    /**
     * Load ads Collapsible Banner Trong Activity Bottom
     */
    public void loadCollapsibleBanner(final Activity mActivity, String id) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        adContainer.setVisibility(View.GONE);
        containerShimmer.setVisibility(View.VISIBLE);
        if (!isShowAllAds || !isNetworkConnected() || isDeviceTest) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadCollapsibleBanner(mActivity, id, BannerGravity.bottom, adContainer, containerShimmer);
        }
    }

    public void loadCollapsibleBanner(final Activity mActivity, String id, int timeDelay) {
        Log.e(TAG, "loadCollapsibleBanner");
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        destroyCollapse(adContainer);
        adContainer.setVisibility(View.GONE);
        containerShimmer.setVisibility(View.VISIBLE);
        if (!isShowAllAds || !isNetworkConnected() || isDeviceTest) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadCollapsibleBanner(mActivity, id, BannerGravity.bottom, adContainer, containerShimmer, timeDelay, id);
        }
    }

    private void destroyCollapse(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof AdView) {
                ((AdView) child).destroy();
            } else if (child instanceof ViewGroup) {
                destroyCollapse((ViewGroup) child);
            }
        }
    }

    /**
     * Load ads Collapsible Banner Trong Activity By List
     */
    public void loadCollapsibleBannerFloor(final Activity mActivity, List<String> listID, String gravity) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        adContainer.setVisibility(View.GONE);
        containerShimmer.setVisibility(View.VISIBLE);
        if (!isShowAllAds || !isNetworkConnected() || isDeviceTest) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            if (listID == null) {
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            if (listID.size() < 1) {
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            List idNew = new ArrayList();
            for (String id : listID) {
                idNew.add(id);
            }
            checkLoadBannerCollap = false;
            loadCollapsibleBannerFloor(mActivity, idNew, gravity, adContainer, containerShimmer);
        }


    }

    /**
     * Load ads Collapsible Banner Trong Activity By List CallBack
     */
    public void loadCollapsibleBannerFloor(final Activity mActivity, List<String> listID, String gravity, BannerCallBack bannerCallBack) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        adContainer.setVisibility(View.GONE);
        containerShimmer.setVisibility(View.VISIBLE);
        if (!isShowAllAds || !isNetworkConnected() || isDeviceTest) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            if (listID == null) {
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            if (listID.size() < 1) {
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            List idNew = new ArrayList();
            for (String id : listID) {
                idNew.add(id);
            }
            checkLoadBannerCollap = false;
            loadCollapsibleBannerFloor(mActivity, idNew, gravity, adContainer, containerShimmer, bannerCallBack);
        }


    }

    /**
     * Load ads Banner Trong Fragment by List
     */
    public void loadBannerFragment(final Activity mActivity, List<String> listID, final View rootView) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        adContainer.setVisibility(View.GONE);
        containerShimmer.setVisibility(View.VISIBLE);
        if (!isShowAllAds || !isNetworkConnected() || isDeviceTest) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            if (listID == null) {
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            if (listID.size() == 0) {
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            List idNew = new ArrayList();
            for (String id : listID) {
                idNew.add(id);
            }
            checkLoadBanner = false;
            loadBanner(mActivity, idNew, adContainer, containerShimmer, null, false, BANNER_INLINE_LARGE_STYLE);
        }
    }

    /**
     * Load ads Banner Trong Fragment by List return CallBack
     */
    public void loadBannerFragment(final Activity mActivity, List<String> listID, final View rootView, BannerCallBack bannerCallBack) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        adContainer.setVisibility(View.GONE);
        containerShimmer.setVisibility(View.VISIBLE);
        if (!isShowAllAds || !isNetworkConnected() || isDeviceTest) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            if (listID == null) {
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            if (listID.size() == 0) {
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            List idNew = new ArrayList();
            for (String id : listID) {
                idNew.add(id);
            }
            checkLoadBanner = false;
            loadBanner(mActivity, idNew, adContainer, containerShimmer, bannerCallBack, false, BANNER_INLINE_LARGE_STYLE);
        }
    }

    /**
     * Load ads Banner Trong Fragment
     */
    public void loadBannerFragment(final Activity mActivity, String id, final View rootView) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        adContainer.setVisibility(View.GONE);
        containerShimmer.setVisibility(View.VISIBLE);
        if (!isShowAllAds || !isNetworkConnected() || isDeviceTest) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(mActivity, id, adContainer, containerShimmer, null, false, BANNER_INLINE_LARGE_STYLE);
        }
    }

    /**
     * Load ads Banner Trong Fragment return CallBack
     */
    public void loadBannerFragment(final Activity mActivity, String id, final View rootView, final BannerCallBack callback) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        adContainer.setVisibility(View.GONE);
        containerShimmer.setVisibility(View.VISIBLE);
        if (!isShowAllAds || !isNetworkConnected() || isDeviceTest) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(mActivity, id, adContainer, containerShimmer, callback, false, BANNER_INLINE_LARGE_STYLE);
        }
    }

    /**
     * Load ads Banner Trong Fragment set Inline adaptive banners
     */
    public void loadBannerFragment(final Activity mActivity, String id, final View rootView, Boolean useInlineAdaptive) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        adContainer.setVisibility(View.GONE);
        containerShimmer.setVisibility(View.VISIBLE);
        if (!isShowAllAds || !isNetworkConnected() || isDeviceTest) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(mActivity, id, adContainer, containerShimmer, null, useInlineAdaptive, BANNER_INLINE_LARGE_STYLE);
        }
    }

    /**
     * Load ads Banner Trong Fragment set Inline adaptive banners
     */
    public void loadInlineBannerFragment(final Activity activity, String id, final View rootView, String inlineStyle) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        adContainer.setVisibility(View.GONE);
        containerShimmer.setVisibility(View.VISIBLE);
        if (!isShowAllAds || !isNetworkConnected() || isDeviceTest) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(activity, id, adContainer, containerShimmer, null, true, inlineStyle);
        }
    }

    /**
     * Load ads Banner Trong Fragment set Inline adaptive banners
     */
    public void loadBannerFragment(final Activity mActivity, String id, final View rootView, final BannerCallBack callback, Boolean useInlineAdaptive) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        adContainer.setVisibility(View.GONE);
        containerShimmer.setVisibility(View.VISIBLE);
        if (!isShowAllAds || !isNetworkConnected() || isDeviceTest) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(mActivity, id, adContainer, containerShimmer, callback, useInlineAdaptive, BANNER_INLINE_LARGE_STYLE);
        }
    }

    /**
     * Load ads Banner Trong Fragment set Inline adaptive banners
     */
    public void loadInlineBannerFragment(final Activity activity, String id, final View rootView, String inlineStyle, final BannerCallBack callback) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        adContainer.setVisibility(View.GONE);
        containerShimmer.setVisibility(View.VISIBLE);
        if (!isShowAllAds || !isNetworkConnected() || isDeviceTest) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            loadBanner(activity, id, adContainer, containerShimmer, callback, true, inlineStyle);
        }
    }

    /**
     * Load ads Collapsible Banner Trong Fragment
     */
    public void loadCollapsibleBannerFragment(final Activity mActivity, String id, final View rootView, String gravity) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        adContainer.setVisibility(View.GONE);
        containerShimmer.setVisibility(View.VISIBLE);
        loadCollapsibleBanner(mActivity, id, gravity, adContainer, containerShimmer);
    }

    public void loadCollapsibleBannerFragment(final Activity mActivity, List<String> listID, final View rootView, String gravity) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        adContainer.setVisibility(View.GONE);
        containerShimmer.setVisibility(View.VISIBLE);
        if (!isShowAllAds || !isNetworkConnected() || isDeviceTest) {
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        } else {
            if (listID == null) {
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            if (listID.size() < 1) {
                adContainer.setVisibility(View.GONE);
                containerShimmer.setVisibility(View.GONE);
                return;
            }
            List idNew = new ArrayList();
            for (String id : listID) {
                idNew.add(id);
            }
            checkLoadBannerCollap = false;
            loadCollapsibleBannerFloor(mActivity, idNew, gravity, adContainer, containerShimmer);
        }
    }

    private void loadBanner(final Activity mActivity, String id, final FrameLayout adContainer, final ShimmerFrameLayout containerShimmer, final BannerCallBack callback, Boolean useInlineAdaptive, String inlineStyle) {
        containerShimmer.setVisibility(View.VISIBLE);
        containerShimmer.startShimmer();

        try {
            AdView adView = new AdView(mActivity);
            adView.setAdUnitId(id);
            adContainer.addView(adView);
            AdSize adSize = getAdSize(mActivity, useInlineAdaptive, inlineStyle);
            int adHeight;
            if (useInlineAdaptive && inlineStyle.equalsIgnoreCase(BANNER_INLINE_SMALL_STYLE)) {
                adHeight = MAX_SMALL_INLINE_BANNER_HEIGHT;
            } else {
                adHeight = adSize.getHeight();
            }
            containerShimmer.getLayoutParams().height = (int) (adHeight * Resources.getSystem().getDisplayMetrics().density + 0.5f);
            adView.setAdSize(adSize);
            adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    containerShimmer.stopShimmer();
                    adContainer.setVisibility(View.GONE);
                    containerShimmer.setVisibility(View.GONE);
                    if (callback != null) {
                        callback.onAdFailedToLoad(loadAdError);
                    }
                }


                @Override
                public void onAdLoaded() {

                    Log.d(TAG, "Banner adapter class name: " + adView.getResponseInfo().getMediationAdapterClassName());
                    containerShimmer.stopShimmer();
                    containerShimmer.setVisibility(View.GONE);
                    adContainer.setVisibility(View.VISIBLE);
                    if (callback != null) {
                        callback.onAdLoadSuccess();
                    }
                    if (adView != null) {
                        adView.setOnPaidEventListener(adValue -> {
                            Log.d(TAG, "OnPaidEvent banner:" + adValue.getValueMicros());
                            FirebaseUtil.logPaidAdImpression(context,
                                    adValue,
                                    adView.getAdUnitId(), AdType.BANNER);
                        });
                    }
                }

                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    if (callback != null) {
                        callback.onAdClicked();
                    }
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    if (callback != null) {
                        callback.onAdImpression();
                    }
                }
            });

            adView.loadAd(getAdRequest());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadBanner(final Activity mActivity, List<String> listID, final FrameLayout adContainer, final ShimmerFrameLayout containerShimmer, final BannerCallBack callback, Boolean useInlineAdaptive, String inlineStyle) {
        if (checkLoadBanner) {
            return;
        }
        if (listID.size() == 0) {
            containerShimmer.stopShimmer();
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
            return;
        }
        Log.e("Admob", "load banner ID : " + listID.get(0));


        containerShimmer.setVisibility(View.VISIBLE);
        containerShimmer.startShimmer();
        try {
            AdView adView = new AdView(mActivity);
            adView.setAdUnitId(listID.get(0));
            adContainer.addView(adView);
            AdSize adSize = getAdSize(mActivity, useInlineAdaptive, inlineStyle);
            int adHeight;
            if (useInlineAdaptive && inlineStyle.equalsIgnoreCase(BANNER_INLINE_SMALL_STYLE)) {
                adHeight = MAX_SMALL_INLINE_BANNER_HEIGHT;
            } else {
                adHeight = adSize.getHeight();
            }
            containerShimmer.getLayoutParams().height = (int) (adHeight * Resources.getSystem().getDisplayMetrics().density + 0.5f);
            adView.setAdSize(adSize);
            adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    if (callback != null) {
                        callback.onAdFailedToLoad(loadAdError);
                    }

                    if (listID.size() > 0) {
                        listID.remove(0);
                        loadBanner(mActivity, listID, adContainer, containerShimmer, callback, useInlineAdaptive, inlineStyle);
                    } else {
                        containerShimmer.stopShimmer();
                        adContainer.setVisibility(View.GONE);
                        containerShimmer.setVisibility(View.GONE);
                    }
                }


                @Override
                public void onAdLoaded() {
                    checkLoadBanner = true;
                    if (callback != null)
                        callback.onAdLoadSuccess();
                    Log.d(TAG, "Banner adapter class name: " + adView.getResponseInfo().getMediationAdapterClassName());
                    containerShimmer.stopShimmer();
                    containerShimmer.setVisibility(View.GONE);
                    adContainer.setVisibility(View.VISIBLE);
                    if (adView != null) {
                        adView.setOnPaidEventListener(adValue -> {
                            Log.d(TAG, "OnPaidEvent banner:" + adValue.getValueMicros());

                            FirebaseUtil.logPaidAdImpression(context,
                                    adValue,
                                    adView.getAdUnitId(), AdType.BANNER);
                        });
                    }
                }

                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    //lỗi: chưa kiểm tra null
                    if (callback != null)
                        callback.onAdClicked();
                    if (disableAdResumeWhenClickAds)
                        AppOpenManager.getInstance().disableAdResumeByClickAction();
                    FirebaseUtil.logClickAdsEvent(context, listID.get(0));
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    //lỗi: chưa kiểm tra null
                    if (callback != null)
                        callback.onAdImpression();
                    //end log
                }
            });

            adView.loadAd(getAdRequest());
        } catch (Exception e) {
            e.printStackTrace();
            containerShimmer.stopShimmer();
            adContainer.setVisibility(View.GONE);
            containerShimmer.setVisibility(View.GONE);
        }
    }

    private void loadCollapsibleBanner(final Activity mActivity, String id, String gravity, final FrameLayout adContainer, final ShimmerFrameLayout containerShimmer) {
        if (!isNetworkConnected()) {
            containerShimmer.setVisibility(View.GONE);
            return;
        }

        containerShimmer.setVisibility(View.VISIBLE);
        containerShimmer.startShimmer();
        try {
            AdView adView = new AdView(mActivity);
            adView.setAdUnitId(id);
            adContainer.addView(adView);
            AdSize adSize = getAdSize(mActivity, false, "");
            containerShimmer.getLayoutParams().height = (int) (adSize.getHeight() * Resources.getSystem().getDisplayMetrics().density + 0.5f);
            adView.setAdSize(adSize);
            adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            adView.loadAd(getAdRequestForCollapsibleBanner(gravity));
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    containerShimmer.stopShimmer();
                    adContainer.setVisibility(View.GONE);
                    containerShimmer.setVisibility(View.GONE);
                }

                @Override
                public void onAdLoaded() {
                    Log.d(TAG, "Banner adapter class name: " + adView.getResponseInfo().getMediationAdapterClassName());
                    containerShimmer.stopShimmer();
                    containerShimmer.setVisibility(View.GONE);
                    adContainer.setVisibility(View.VISIBLE);
                    adView.setOnPaidEventListener(adValue -> {
                        Log.d(TAG, "OnPaidEvent banner:" + adValue.getValueMicros());

                        FirebaseUtil.logPaidAdImpression(context,
                                adValue,
                                adView.getAdUnitId(), AdType.BANNER);
                    });

                }

                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    if (disableAdResumeWhenClickAds)
                        AppOpenManager.getInstance().disableAdResumeByClickAction();
                    FirebaseUtil.logClickAdsEvent(context, id);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCollapsibleBanner(final Activity mActivity, String id, String gravity, final FrameLayout adContainer, final ShimmerFrameLayout containerShimmer, int timeDelay, String idAds) {
        if (!isNetworkConnected()) {
            containerShimmer.setVisibility(View.GONE);
            return;
        }

        containerShimmer.setVisibility(View.VISIBLE);
        containerShimmer.startShimmer();
        try {
            AdView adView = new AdView(mActivity);
            adView.setAdUnitId(id);
            adContainer.addView(adView);
            AdSize adSize = getAdSize(mActivity, false, "");
            containerShimmer.getLayoutParams().height = (int) (adSize.getHeight() * Resources.getSystem().getDisplayMetrics().density + 0.5f);
            adView.setAdSize(adSize);
            adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            adView.loadAd(getAdRequestForCollapsibleBanner(gravity));
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    containerShimmer.stopShimmer();
                    adContainer.setVisibility(View.GONE);
                    containerShimmer.setVisibility(View.GONE);
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    if (runnable != null) {
                        handler.removeCallbacks(runnable); // Hủy bỏ runnable trước đó nếu có
                    }
                    Log.e(TAG, "onAdImpression run");
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "onAdImpression run: Performing action after 10 seconds");
                            loadCollapsibleBanner(mActivity, id, timeDelay);
                        }
                    };
                    handler.postDelayed(runnable,timeDelay); // Bắt đầu chạy runnable
                }

                @Override
                public void onAdLoaded() {
                    Log.d(TAG, "Banner adapter class name: " + adView.getResponseInfo().getMediationAdapterClassName());
                    containerShimmer.stopShimmer();
                    containerShimmer.setVisibility(View.GONE);
                    adContainer.setVisibility(View.VISIBLE);
                    adView.setOnPaidEventListener(adValue -> {
                        Log.d(TAG, "OnPaidEvent banner:" + adValue.getValueMicros());

                        FirebaseUtil.logPaidAdImpression(context,
                                adValue,
                                adView.getAdUnitId(), AdType.BANNER);
                    });

                }

                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    if (disableAdResumeWhenClickAds)
                        AppOpenManager.getInstance().disableAdResumeByClickAction();
                    FirebaseUtil.logClickAdsEvent(context, id);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCollapsibleBannerFloor(final Activity mActivity, List<String> listId, String gravity, final FrameLayout adContainer, final ShimmerFrameLayout containerShimmer) {
        if (checkLoadBannerCollap) {
            return;
        }
        containerShimmer.setVisibility(View.VISIBLE);
        containerShimmer.startShimmer();
        try {
            Log.e("Admob", "load collap banner ID : " + listId.get(0));
            AdView adView = new AdView(mActivity);
            adView.setAdUnitId(listId.get(0));
            adContainer.addView(adView);
            AdSize adSize = getAdSize(mActivity, false, "");
            containerShimmer.getLayoutParams().height = (int) (adSize.getHeight() * Resources.getSystem().getDisplayMetrics().density + 0.5f);
            adView.setAdSize(adSize);
            adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            adView.loadAd(getAdRequestForCollapsibleBanner(gravity));
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    Log.e("Admob", "load failed collap banner ID : " + listId.get(0));
                    if (listId.size() > 0) {
                        listId.remove(0);
                        loadCollapsibleBannerFloor(mActivity, listId, gravity, adContainer, containerShimmer);
                    } else {
                        containerShimmer.stopShimmer();
                        adContainer.setVisibility(View.GONE);
                        containerShimmer.setVisibility(View.GONE);
                    }

                }

                @Override
                public void onAdLoaded() {
                    checkLoadBannerCollap = true;
                    Log.d(TAG, "Banner adapter class name: " + adView.getResponseInfo().getMediationAdapterClassName());
                    containerShimmer.stopShimmer();
                    containerShimmer.setVisibility(View.GONE);
                    adContainer.setVisibility(View.VISIBLE);
                    adView.setOnPaidEventListener(adValue -> {
                        Log.d(TAG, "OnPaidEvent banner:" + adValue.getValueMicros());

                        FirebaseUtil.logPaidAdImpression(context,
                                adValue,
                                adView.getAdUnitId(), AdType.BANNER);
                    });

                }

                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    if (disableAdResumeWhenClickAds)
                        AppOpenManager.getInstance().disableAdResumeByClickAction();
                    FirebaseUtil.logClickAdsEvent(context, listId.get(0));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCollapsibleBannerFloor(final Activity mActivity, List<String> listId, String gravity, final FrameLayout adContainer, final ShimmerFrameLayout containerShimmer, BannerCallBack bannerCallBack) {
        if (checkLoadBannerCollap) {
            return;
        }
        containerShimmer.setVisibility(View.VISIBLE);
        containerShimmer.startShimmer();
        try {
            Log.e("Admob", "load collap banner ID : " + listId.get(0));
            AdView adView = new AdView(mActivity);
            adView.setAdUnitId(listId.get(0));
            adContainer.addView(adView);
            AdSize adSize = getAdSize(mActivity, false, "");
            containerShimmer.getLayoutParams().height = (int) (adSize.getHeight() * Resources.getSystem().getDisplayMetrics().density + 0.5f);
            adView.setAdSize(adSize);
            adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            adView.loadAd(getAdRequestForCollapsibleBanner(gravity));
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    bannerCallBack.onAdFailedToLoad(loadAdError);
                    Log.e("Admob", "load failed collap banner ID : " + listId.get(0));
                    if (listId.size() > 0) {
                        listId.remove(0);
                        loadCollapsibleBannerFloor(mActivity, listId, gravity, adContainer, containerShimmer);
                    } else {
                        containerShimmer.stopShimmer();
                        adContainer.setVisibility(View.GONE);
                        containerShimmer.setVisibility(View.GONE);
                    }

                }

                @Override
                public void onAdLoaded() {
                    checkLoadBannerCollap = true;
                    bannerCallBack.onAdLoadSuccess();
                    Log.d(TAG, "Banner adapter class name: " + adView.getResponseInfo().getMediationAdapterClassName());
                    containerShimmer.stopShimmer();
                    containerShimmer.setVisibility(View.GONE);
                    adContainer.setVisibility(View.VISIBLE);
                    adView.setOnPaidEventListener(adValue -> {
                        Log.d(TAG, "OnPaidEvent banner:" + adValue.getValueMicros());

                        FirebaseUtil.logPaidAdImpression(context,
                                adValue,
                                adView.getAdUnitId(), AdType.BANNER);
                    });

                }

                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    bannerCallBack.onAdClicked();
                    if (disableAdResumeWhenClickAds)
                        AppOpenManager.getInstance().disableAdResumeByClickAction();
                    FirebaseUtil.logClickAdsEvent(context, listId.get(0));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AdSize getAdSize(Activity mActivity, Boolean useInlineAdaptive, String inlineStyle) {

        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = mActivity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        if (useInlineAdaptive) {
            if (inlineStyle.equalsIgnoreCase(BANNER_INLINE_LARGE_STYLE)) {
                return AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(mActivity, adWidth);
            } else {
                return AdSize.getInlineAdaptiveBannerAdSize(adWidth, MAX_SMALL_INLINE_BANNER_HEIGHT);
            }
        }
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(mActivity, adWidth);

    }

    private AdRequest getAdRequestForCollapsibleBanner(String gravity) {
        AdRequest.Builder builder = new AdRequest.Builder();
        Bundle admobExtras = new Bundle();
        admobExtras.putString("collapsible", gravity);
        builder.addNetworkExtrasBundle(AdMobAdapter.class, admobExtras);
        return builder.build();
    }

    /*===========================  end Banner ========================================= */


    public boolean interstitialSplashLoaded() {
        return mInterstitialSplash != null;
    }

    public InterstitialAd getmInterstitialSplash() {
        return mInterstitialSplash;
    }


    /* ==========================  Inter Splash============================================== */


    public RewardedAd getRewardedAd() {
        return this.rewardedAd;
    }


    /**
     * Load ads in Splash
     */
    public void loadSplashInterAds(final Context context, String id, long timeOut, long timeDelay, final InterCallback adListener) {
        isTimeDelay = false;
        isTimeout = false;
        if (!isNetworkConnected()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (adListener != null) {
                        adListener.onAdClosed();
                        adListener.onNextAction();
                    }
                    return;
                }
            }, 3000);
        } else {
            AdsConsentManager adsConsentManager = new AdsConsentManager((Activity) context);
            adsConsentManager.requestUMP(b -> {
                if (b) {
                    Admob.getInstance().initAdmob(context, null);
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //check delay show ad splash
                        if (mInterstitialSplash != null) {
                            Log.d(TAG, "loadSplashInterAds:show ad on delay ");
                            onShowSplash((Activity) context, adListener);
                            return;
                        }
                        Log.d(TAG, "loadSplashInterAds: delay validate");
                        isTimeDelay = true;
                    }
                }, timeDelay);
                if (timeOut > 0) {
                    handlerTimeout = new Handler();
                    rdTimeout = new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "loadSplashInterstitalAds: on timeout");
                            isTimeout = true;
                            if (mInterstitialSplash != null) {
                                Log.i(TAG, "loadSplashInterstitalAds:show ad on timeout ");
                                onShowSplash((Activity) context, adListener);
                                return;
                            }
                            if (adListener != null) {
                                adListener.onAdClosed();
                                adListener.onNextAction();
                                isShowLoadingSplash = false;
                            }
                        }
                    };
                    handlerTimeout.postDelayed(rdTimeout, timeOut);
                }

                isShowLoadingSplash = true;
                loadInterAds(context, id, new InterCallback() {
                    @Override
                    public void onAdLoadSuccess(InterstitialAd interstitialAd) {
                        super.onAdLoadSuccess(interstitialAd);
                        Log.e(TAG, "loadSplashInterstitalAds  end time loading success:" + Calendar.getInstance().getTimeInMillis() + "     time limit:" + isTimeout);
                        if (isTimeout)
                            return;
                        if (interstitialAd != null) {
                            mInterstitialSplash = interstitialAd;
                            if (isTimeDelay) {
                                onShowSplash((Activity) context, adListener);
                                Log.i(TAG, "loadSplashInterstitalAds:show ad on loaded ");
                            }
                        }
                        if (interstitialAd != null) {
                            interstitialAd.setOnPaidEventListener(adValue -> {
                                Log.d(TAG, "OnPaidEvent loadInterstitialAds:" + adValue.getValueMicros());
                                FirebaseUtil.logPaidAdImpression(context,
                                        adValue,
                                        interstitialAd.getAdUnitId(), AdType.BANNER);
                            });
                        }
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        Log.e(TAG, "loadSplashInterstitalAds  end time loading error:" + Calendar.getInstance().getTimeInMillis() + "     time limit:" + isTimeout);
                        if (isTimeout)
                            return;
                        if (adListener != null) {
                            if (handlerTimeout != null && rdTimeout != null) {
                                handlerTimeout.removeCallbacks(rdTimeout);
                            }
                            if (i != null)
                                Log.e(TAG, "loadSplashInterstitalAds: load fail " + i.getMessage());
                            adListener.onAdFailedToLoad(i);
                            adListener.onNextAction();
                        }
                    }

                    @Override
                    public void onAdClicked() {
                        if (disableAdResumeWhenClickAds)
                            AppOpenManager.getInstance().disableAdResumeByClickAction();
                        super.onAdClicked();
                    }
                });
            });
        }
    }

    public void loadSplashInterAds2(final Context context, String id, long timeDelay, final InterCallback adListener) {
        if (!isNetworkConnected() || !isShowAllAds) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (adListener != null) {
                        adListener.onAdClosed();
                        adListener.onNextAction();
                    }
                    return;
                }
            }, 3000);
        } else {
            AdsConsentManager adsConsentManager = new AdsConsentManager((Activity) context);
            adsConsentManager.requestUMP(b -> {
                if (b) {
                    Admob.getInstance().initAdmob(context, null);
                }
                mInterstitialSplash = null;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        InterstitialAd.load(context, id, getAdRequest(),
                                new InterstitialAdLoadCallback() {
                                    @Override
                                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                        super.onAdLoaded(interstitialAd);
                                        mInterstitialSplash = interstitialAd;
                                        AppOpenManager.getInstance().disableAppResume();
                                        onShowSplash((Activity) context, adListener);
                                    }

                                    @Override
                                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                        super.onAdFailedToLoad(loadAdError);
                                        mInterstitialSplash = null;
                                        adListener.onAdFailedToLoad(loadAdError);
                                        adListener.onNextAction();
                                    }

                                });
                    }
                }, timeDelay);
            });
        }
    }

    public void onShowSplash(Activity activity, InterstitialAd interSplash, InterCallback adListener) {
        AppOpenManager.getInstance().disableAppResume();
        isShowLoadingSplash = true;
        mInterstitialSplash = interSplash;
        if (!isNetworkConnected()) {
            adListener.onAdClosed();
            return;
        } else {
            if (mInterstitialSplash == null) {
                adListener.onAdClosed();
                adListener.onNextAction();
                return;
            } else {
                mInterstitialSplash.setOnPaidEventListener(adValue -> {
                    Log.d(TAG, "OnPaidEvent splash:" + adValue.getValueMicros());
                    FirebaseUtil.logPaidAdImpression(context,
                            adValue,
                            mInterstitialSplash.getAdUnitId(), AdType.INTERSTITIAL);
                    adListener.onEarnRevenue((double) adValue.getValueMicros());
                });

                if (handlerTimeout != null && rdTimeout != null) {
                    handlerTimeout.removeCallbacks(rdTimeout);
                }

                if (adListener != null) {
                    adListener.onAdLoaded();
                }

                mInterstitialSplash.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        if (AppOpenManager.getInstance().isInitialized()) {
                            AppOpenManager.getInstance().disableAppResume();
                        }
                        isShowLoadingSplash = true;
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log.e(TAG, "DismissedFullScreenContent Splash");
                        if (AppOpenManager.getInstance().isInitialized()) {
                            AppOpenManager.getInstance().enableAppResume();
                        }
                        if (adListener != null) {
                            if (!openActivityAfterShowInterAds) {
                                adListener.onAdClosed();
                                adListener.onNextAction();
                            } else {
                                adListener.onAdClosedByUser();
                            }

                            if (dialog != null) {
                                dialog.dismiss();
                            }

                        }
                        mInterstitialSplash = null;
                        isShowLoadingSplash = true;
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        Log.e(TAG, "onAdFailedToShowFullScreenContent : " + adError);
                        //  mInterstitialSplash = null;
                        if (adError.getCode() == 1) {
                            mInterstitialSplash = null;
                            adListener.onAdClosed();
                        }
                        isShowLoadingSplash = false;
                        if (adListener != null) {
                            adListener.onAdFailedToShow(adError);

                            if (dialog != null) {
                                dialog.dismiss();
                            }
                        }
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        if (disableAdResumeWhenClickAds)
                            AppOpenManager.getInstance().disableAdResumeByClickAction();
                        FirebaseUtil.logClickAdsEvent(context, mInterstitialSplash.getAdUnitId());
                    }
                });
                if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                    try {
                        if (dialog != null && dialog.isShowing())
                            dialog.dismiss();
                        dialog = new LoadingAdsDialog(activity);
                        try {
                            dialog.show();
                        } catch (Exception e) {
                            adListener.onAdClosed();
                            adListener.onNextAction();
                            return;
                        }
                    } catch (Exception e) {
                        dialog = null;
                        e.printStackTrace();
                    }
                    new Handler().postDelayed(() -> {
                        if (AppOpenManager.getInstance().isInitialized()) {
                            AppOpenManager.getInstance().disableAppResume();
                        }

                        if (openActivityAfterShowInterAds && adListener != null) {
                            adListener.onAdClosed();
                            adListener.onNextAction();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (dialog != null && dialog.isShowing() && !activity.isDestroyed())
                                        dialog.dismiss();
                                }
                            }, 1500);
                        }

                        if (activity != null) {
                            mInterstitialSplash.show(activity);
                            Log.e(TAG, "onShowSplash: mInterstitialSplash.show");
                            isShowLoadingSplash = false;
                        } else if (adListener != null) {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                            adListener.onAdClosed();
                            adListener.onNextAction();
                            isShowLoadingSplash = false;
                        }
                    }, 300);
                } else {
                    isShowLoadingSplash = false;
                    Log.e(TAG, "onShowSplash: fail on background");
                }
            }

        }

    }

    private void onShowSplash(Activity activity, InterCallback adListener) {
        isShowLoadingSplash = true;
        if (mInterstitialSplash == null) {
            adListener.onAdClosed();
            adListener.onNextAction();
            return;
        }
        mInterstitialSplash.setOnPaidEventListener(adValue -> {
            Log.d(TAG, "OnPaidEvent splash:" + adValue.getValueMicros());
            FirebaseUtil.logPaidAdImpression(context,
                    adValue,
                    mInterstitialSplash.getAdUnitId(), AdType.INTERSTITIAL);
            adListener.onEarnRevenue((double) adValue.getValueMicros());
        });

        if (handlerTimeout != null && rdTimeout != null) {
            handlerTimeout.removeCallbacks(rdTimeout);
        }

        if (adListener != null) {
            adListener.onAdLoaded();
        }

        mInterstitialSplash.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdShowedFullScreenContent() {
                isShowLoadingSplash = false;
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                Log.e(TAG, "DismissedFullScreenContent Splash");
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().enableAppResume();
                }
                if (adListener != null) {
                    if (!openActivityAfterShowInterAds) {
                        adListener.onAdClosed();
                        adListener.onNextAction();
                    } else {
                        adListener.onAdClosedByUser();
                    }

                    if (dialog != null) {
                        dialog.dismiss();
                    }

                }
                mInterstitialSplash = null;
                isShowLoadingSplash = false;
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                mInterstitialSplash = null;
                isShowLoadingSplash = false;
                if (adListener != null) {
                    if (!openActivityAfterShowInterAds) {
                        adListener.onAdFailedToShow(adError);
                        adListener.onNextAction();
                    }

                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                if (disableAdResumeWhenClickAds)
                    AppOpenManager.getInstance().disableAdResumeByClickAction();
                FirebaseUtil.logClickAdsEvent(context, mInterstitialSplash.getAdUnitId());
            }
        });
        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            try {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
                dialog = new LoadingAdsDialog(activity);
                try {
                    dialog.show();
                } catch (Exception e) {
                    adListener.onAdClosed();
                    adListener.onNextAction();
                    return;
                }
            } catch (Exception e) {
                dialog = null;
                e.printStackTrace();
            }
            new Handler().postDelayed(() -> {
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().disableAppResume();
                }

                if (openActivityAfterShowInterAds && adListener != null) {
                    adListener.onAdClosed();
                    adListener.onNextAction();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (dialog != null && dialog.isShowing() && !activity.isDestroyed())
                                dialog.dismiss();
                        }
                    }, 1500);
                }

                if (activity != null) {
                    mInterstitialSplash.show(activity);
                    Log.e(TAG, "onShowSplash: mInterstitialSplash.show");
                    isShowLoadingSplash = false;
                } else if (adListener != null) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    adListener.onAdClosed();
                    adListener.onNextAction();
                    isShowLoadingSplash = false;
                }
            }, 500);
        } else {
            isShowLoadingSplash = false;
            Log.e(TAG, "onShowSplash: fail on background");
        }
    }

    /* =============================End Inter Splash==========================================*/

    /* =============================   Inter ==========================================*/

    /**
     * Load ads inter all
     */

    public void loadInterAll(Context context, String id) {
        if (!isShowAllAds) {
            return;
        }
        if (AdsUtils.interAllReady) {
            return;
        }
        Log.e(TAG, "load Inter ID : " + id);
        InterstitialAd.load(context, id, getAdRequest(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        AdsUtils.interstitialAd = interstitialAd;
                        //tracking adjust
                        interstitialAd.setOnPaidEventListener(adValue -> {
                            Log.d(TAG, "OnPaidEvent getInterstitalAds:" + adValue.getValueMicros());
                            FirebaseUtil.logPaidAdImpression(context,
                                    adValue,
                                    interstitialAd.getAdUnitId(), AdType.INTERSTITIAL);
                        });
                        AdsUtils.interAllReady = true;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i(TAG, loadAdError.getMessage());
                        AdsUtils.interAllReady = false;
                    }

                });
    }

    public void loadInterAds(Context context, String id, InterCallback adCallback) {
        if (!isShowAllAds) {
            adCallback.onNextAction();
            adCallback.onAdFailedToLoad(null);
            return;
        }
        adCallback.onAdLoaded();
            isTimeout = false;
            interstitialAd = null;
            InterstitialAd.load(context, id, getAdRequest(),
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            if (adCallback != null) {
                                adCallback.onAdLoadSuccess(interstitialAd);
                            }
                            //tracking adjust
                            interstitialAd.setOnPaidEventListener(adValue -> {
                                Log.d(TAG, "OnPaidEvent getInterstitalAds:" + adValue.getValueMicros());
                                FirebaseUtil.logPaidAdImpression(context,
                                        adValue,
                                        interstitialAd.getAdUnitId(), AdType.INTERSTITIAL);
                                adCallback.onEarnRevenue((double) adValue.getValueMicros());
                            });
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error
                            Log.i(TAG, loadAdError.getMessage());
                            if (adCallback != null) {
                                adCallback.onAdFailedToLoad(loadAdError);
                                adCallback.onNextAction();
                            }
                        }

                    });
    }
    public void loadInterAdsNotLimit(Context context, String id, InterCallback adCallback) {
        if (!isShowAllAds) {
            adCallback.onNextAction();
            adCallback.onAdFailedToLoad(null);
            return;
        }
        adCallback.onAdLoaded();
        //if (isShowInter) {
           // isTimeout = false;
            InterstitialAd.load(context, id, getAdRequest(),
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            if (adCallback != null) {
                                adCallback.onAdLoadSuccess(interstitialAd);
                            }
                            //tracking adjust
                            interstitialAd.setOnPaidEventListener(adValue -> {
                                Log.d(TAG, "OnPaidEvent getInterstitalAds:" + adValue.getValueMicros());
                                FirebaseUtil.logPaidAdImpression(context,
                                        adValue,
                                        interstitialAd.getAdUnitId(), AdType.INTERSTITIAL);
                                adCallback.onEarnRevenue((double) adValue.getValueMicros());
                            });
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error
                            Log.i(TAG, loadAdError.getMessage());
                            if (adCallback != null) {
                                adCallback.onAdFailedToLoad(loadAdError);
                                adCallback.onNextAction();
                            }
                        }

                    });
     //   }
    }

    public void loadInterAds(Context context, List<String> listID, InterCallback adCallback) {
        if (listID == null) {
            adCallback.onAdFailedToLoad(null);
            adCallback.onNextAction();
            return;
        }
        if (listID.size() < 1) {
            adCallback.onAdFailedToLoad(null);
            adCallback.onNextAction();
            return;
        }
        List<String> listIDNew = new ArrayList<>();
        for (String idNew : listID) {
            listIDNew.add(idNew);
        }
        loadInterAdsFloorByList(context, listIDNew, adCallback);
    }

    private void loadInterAdsFloorByList(Context context, List<String> listID, InterCallback adCallback) {
        if (!isShowAllAds) {
            adCallback.onNextAction();
            adCallback.onAdFailedToLoad(null);
            return;
        }
        if (listID.size() == 0) {
            adCallback.onAdFailedToLoad(null);
            adCallback.onNextAction();
            return;
        }
        Log.e("Admob", "load Inter ID : " + listID.get(0));

        adCallback.onAdLoaded();

        if (isShowInter) {
            isTimeout = false;
            interstitialAd = null;
            InterstitialAd.load(context, listID.get(0), getAdRequest(),
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            if (adCallback != null) {
                                adCallback.onAdLoadSuccess(interstitialAd);
                            }
                            //tracking adjust
                            interstitialAd.setOnPaidEventListener(adValue -> {
                                Log.d(TAG, "OnPaidEvent getInterstitalAds:" + adValue.getValueMicros());
                                FirebaseUtil.logPaidAdImpression(context,
                                        adValue,
                                        interstitialAd.getAdUnitId(), AdType.INTERSTITIAL);
                                adCallback.onEarnRevenue((double) adValue.getValueMicros());
                            });
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            listID.remove(0);
                            // Log event admob
                            adCallback.onAdFailedToLoad(loadAdError);
                            //end log
                            loadInterAdsFloorByList(context, listID, adCallback);
                        }

                    });
        }
    }

    /**
     * Show ads inter
     */
    public void showInterAds(Context context, InterstitialAd mInterstitialAd, final InterCallback callback) {
        showInterAds(context, mInterstitialAd, callback, false,false);
    }

    public void showInterAll(Context context, final InterCallback callback) {
        showInterAds(context, AdsUtils.interstitialAd, callback, true,true);
    }

    public void showInterAds(Context context, InterstitialAd mInterstitialAd, final InterCallback callback, boolean shouldReload,boolean limitTime) {
        currentClicked = numShowAds;
        showInterAdByTimes(context, mInterstitialAd, callback, shouldReload, limitTime);
    }

    private void showInterAdByTimes(final Context context, InterstitialAd mInterstitialAd, final InterCallback callback, final boolean shouldReloadAds,boolean limitTime) {
        Helper.setupAdmodData(context);
        if (!isShowAllAds) {
            Log.e(TAG, "mInterstitialAd isShowAllAds");
            callback.onAdClosed();
            callback.onNextAction();
            return;
        }
        if (mInterstitialAd == null) {
            Log.e(TAG, "mInterstitialAd null");
            if (callback != null) {
                callback.onAdClosed();
                callback.onNextAction();
            }
            return;
        }
       /* if(isShowInter==false){
            callback.onAdClosed();
            callback.onNextAction();
        }*/

        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent();
                // Called when fullscreen content is dismissed.
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().enableAppResume();
                }
                if (callback != null) {
                    if ((shouldReloadAds && limitTime)) {
                        AdsUtils.interAllReady = false;
                        loadInterAll(context, AdsUtils.interstitialAd.getAdUnitId());
                    }
                    if (!openActivityAfterShowInterAds) {
                        callback.onAdClosed();
                        callback.onNextAction();
                    } else {
                        callback.onAdClosedByUser();
                    }

                    if (dialog != null) {
                        dialog.dismiss();
                    }

                }
                Log.e(TAG, "onAdDismissedFullScreenContent");
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                super.onAdFailedToShowFullScreenContent(adError);
                Log.e(TAG, "onAdFailedToShowFullScreenContent: " + adError.getMessage());

                // Called when fullscreen content failed to show.
                if (callback != null) {
                    if ((shouldReloadAds && limitTime)) {
                        AdsUtils.interAllReady = false;
                        loadInterAll(context, AdsUtils.interstitialAd.getAdUnitId());
                    }
                    if (!openActivityAfterShowInterAds) {
                        callback.onAdClosed();
                        callback.onNextAction();
                    }

                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            }

            @Override
            public void onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent();
                // Called when fullscreen content is shown.
                callback.onAdImpression();
                if(limitTime){
                    if (timeLimitShowAds > 1000) {
                        setTimeLimitInter();
                    }
                }

            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                callback.onAdClicked();
                if (disableAdResumeWhenClickAds)
                    AppOpenManager.getInstance().disableAdResumeByClickAction();
                FirebaseUtil.logClickAdsEvent(context, mInterstitialAd.getAdUnitId());
            }
        });

        if (Helper.getNumClickAdsPerDay(context, mInterstitialAd.getAdUnitId()) < maxClickAds) {
            if(limitTime){
                showInterstitialAd(context, mInterstitialAd, callback);
            }else{
                showInterstitialAdNotLimit(context, mInterstitialAd, callback);
            }

            return;
        }
        if (callback != null) {
            callback.onAdClosed();
            callback.onNextAction();
        }

    }

    private void showInterstitialAd(Context context, InterstitialAd mInterstitialAd, InterCallback callback) {
        if (!isShowInter || !isShowAllAds) {
            callback.onAdClosed();
            callback.onNextAction();
            return;
        }
        if (!isNetworkConnected() || mInterstitialAd == null) {
            callback.onAdClosed();
            callback.onNextAction();
            return;
        }
        currentClicked++;
        if (currentClicked >= numShowAds) {
            if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                try {
                    if (dialog != null && dialog.isShowing())
                        dialog.dismiss();
                    dialog = new LoadingAdsDialog(context);
                    try {
                        dialog.show();
                    } catch (Exception e) {
                        callback.onAdClosed();
                        callback.onNextAction();
                        return;
                    }
                } catch (Exception e) {
                    dialog = null;
                    e.printStackTrace();
                }
                new Handler().postDelayed(() -> {
                    if (AppOpenManager.getInstance().isInitialized()) {
                        AppOpenManager.getInstance().disableAppResume();
                    }

                    if (openActivityAfterShowInterAds && callback != null) {
                        callback.onAdClosed();
                        callback.onNextAction();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (dialog != null && dialog.isShowing() && !((Activity) context).isDestroyed())
                                    dialog.dismiss();
                            }
                        }, 1500);
                    }
                    mInterstitialAd.show((Activity) context);

                }, 800);

            }
            currentClicked = 0;
        } else if (callback != null) {
            if (dialog != null) {
                dialog.dismiss();
            }
            callback.onAdClosed();
            callback.onNextAction();
        }
    }
    private void showInterstitialAdNotLimit(Context context, InterstitialAd mInterstitialAd, InterCallback callback) {
        if (!isShowAllAds) {
            callback.onAdClosed();
            callback.onNextAction();
            return;
        }
        if (!isNetworkConnected() || mInterstitialAd == null) {
            callback.onAdClosed();
            callback.onNextAction();
            return;
        }
        currentClicked++;
        if (currentClicked >= numShowAds && mInterstitialAd != null) {
            if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                try {
                    if (dialog != null && dialog.isShowing())
                        dialog.dismiss();
                    dialog = new LoadingAdsDialog(context);
                    try {
                        dialog.show();
                    } catch (Exception e) {
                        callback.onAdClosed();
                        callback.onNextAction();
                        return;
                    }
                } catch (Exception e) {
                    dialog = null;
                    e.printStackTrace();
                }
                new Handler().postDelayed(() -> {
                    if (AppOpenManager.getInstance().isInitialized()) {
                        AppOpenManager.getInstance().disableAppResume();
                    }

                    if (openActivityAfterShowInterAds && callback != null) {
                        callback.onAdClosed();
                        callback.onNextAction();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (dialog != null && dialog.isShowing() && !((Activity) context).isDestroyed())
                                    dialog.dismiss();
                            }
                        }, 1500);
                    }
                    mInterstitialAd.show((Activity) context);

                }, 800);

            }
            currentClicked = 0;
        } else if (callback != null) {
            if (dialog != null) {
                dialog.dismiss();
            }
            callback.onAdClosed();
            callback.onNextAction();
        }
    }

    /* ============================= End  Inter  ==========================================*/


    /* =============================  Rewarded Ads ==========================================*/

    public void showRewardAds(final Activity context, final RewardCallback adCallback) {
        if (!isShowAllAds || !isNetworkConnected()) {
            adCallback.onAdClosed();
            return;
        }
        if (rewardedAd == null) {
            initRewardAds(context, rewardedId);
            adCallback.onAdFailedToShow(0);
            return;
        } else {
            Admob.this.rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    if (adCallback != null)
                        adCallback.onAdClosed();

                    if (AppOpenManager.getInstance().isInitialized()) {
                        AppOpenManager.getInstance().enableAppResume();
                    }
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    if (adCallback != null)
                        adCallback.onAdFailedToShow(adError.getCode());
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent();
                    if (AppOpenManager.getInstance().isInitialized()) {
                        AppOpenManager.getInstance().disableAppResume();
                    }
                    initRewardAds(context, rewardedId);
                    rewardedAd = null;
                    adCallback.onAdImpression();
                }

                public void onAdClicked() {
                    super.onAdClicked();
                    if (disableAdResumeWhenClickAds)
                        AppOpenManager.getInstance().disableAdResumeByClickAction();
                    FirebaseUtil.logClickAdsEvent(context, rewardedAd.getAdUnitId());
                }
            });
            rewardedAd.show(context, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    if (adCallback != null) {
                        adCallback.onEarnedReward(rewardItem);

                    }
                }
            });
        }
    }

    public void initRewardAds(Context context, String id) {
        if (!isShowAllAds) {
            return;
        }
        this.rewardedId = id;
        RewardedAd.load(context, id, getAdRequest(), new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                Admob.this.rewardedAd = rewardedAd;
                Admob.this.rewardedAd.setOnPaidEventListener(adValue -> {

                    Log.d(TAG, "OnPaidEvent Reward:" + adValue.getValueMicros());
                    FirebaseUtil.logPaidAdImpression(context,
                            adValue,
                            rewardedAd.getAdUnitId(),
                            AdType.REWARDED);
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.e(TAG, "RewardedAd onAdFailedToLoad: " + loadAdError.getMessage());
            }
        });
    }

    public void loadAndShowRewardAds(Context context, String id, RewardCallback adCallback) {
        if (!isShowAllAds || !isNetworkConnected()) {
            adCallback.onAdFailedToLoad();
            return;
        }
        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            try {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
                dialog = new LoadingAdsDialog(context);
                try {
                    dialog.show();
                } catch (Exception e) {
                    adCallback.onAdClosed();
                    adCallback.onAdFailedToLoad();
                    return;
                }
            } catch (Exception e) {
                dialog = null;
                e.printStackTrace();
            }
            new Handler().postDelayed(() -> {
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().disableAppResume();
                }
                RewardedAd.load(context, id, getAdRequest(), new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent();
                                if (adCallback != null)
                                    adCallback.onAdClosed();
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                                if (AppOpenManager.getInstance().isInitialized()) {
                                    AppOpenManager.getInstance().enableAppResume();
                                }
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                super.onAdFailedToShowFullScreenContent(adError);
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                                if (adCallback != null)
                                    adCallback.onAdFailedToShow(adError.getCode());
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent();
                                if (AppOpenManager.getInstance().isInitialized()) {
                                    AppOpenManager.getInstance().disableAppResume();
                                }
                                adCallback.onAdImpression();
                            }

                            public void onAdClicked() {
                                super.onAdClicked();
                                if (disableAdResumeWhenClickAds)
                                    AppOpenManager.getInstance().disableAdResumeByClickAction();
                                FirebaseUtil.logClickAdsEvent(context, rewardedAd.getAdUnitId());
                            }
                        });
                        rewardedAd.show((Activity) context, new OnUserEarnedRewardListener() {
                            @Override
                            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                                if (adCallback != null) {
                                    adCallback.onEarnedReward(rewardItem);
                                }
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        adCallback.onAdFailedToLoad();
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        Log.e(TAG, "RewardedAd onAdFailedToLoad: " + loadAdError.getMessage());
                    }
                });

            }, 300);
        } else {
            isShowLoadingSplash = false;
            Log.e(TAG, "onShowSplash: fail on background");
        }

    }

    public void showRewardAds(final Activity context, final RewardCallback adCallback, RewardedAd rewardedAd) {
        if (!isShowAllAds || !isNetworkConnected()) {
            adCallback.onAdClosed();
            return;
        }
        if (rewardedAd == null) {
            initRewardAds(context, rewardedId);
            adCallback.onAdFailedToShow(0);
            return;
        } else {
            rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    if (adCallback != null)
                        adCallback.onAdClosed();

                    if (AppOpenManager.getInstance().isInitialized()) {
                        AppOpenManager.getInstance().enableAppResume();
                    }
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    if (adCallback != null)
                        adCallback.onAdFailedToShow(adError.getCode());
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent();
                    if (AppOpenManager.getInstance().isInitialized()) {
                        AppOpenManager.getInstance().disableAppResume();
                    }
                    adCallback.onAdImpression();
                }

                public void onAdClicked() {
                    super.onAdClicked();
                    if (disableAdResumeWhenClickAds)
                        AppOpenManager.getInstance().disableAdResumeByClickAction();
                    FirebaseUtil.logClickAdsEvent(context, rewardedAd.getAdUnitId());
                }
            });
            rewardedAd.show(context, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    if (adCallback != null) {
                        adCallback.onEarnedReward(rewardItem);

                    }
                }
            });
        }
    }
    /* =============================  End Rewarded Ads ==========================================*/




    /* =============================  Native Ads ==========================================*/

    public void loadNativeAd(Context context, String id, final NativeCallback callback) {
        Log.e("Load native id ", id);

        /*if (Helper.getNumClickAdsPerDay(context, id) > maxClickAds) {
            callback.onAdFailedToLoad();
        }*/
        if (!isShowAllAds || !isNetworkConnected() || isDeviceTest) {
            callback.onAdFailedToLoad();
        } else {
            if (isShowNative) {
                if (isNetworkConnected()) {
                    VideoOptions videoOptions = new VideoOptions.Builder()
                            .setStartMuted(true)
                            .build();

                    NativeAdOptions adOptions = new NativeAdOptions.Builder()
                            .setVideoOptions(videoOptions)
                            .build();
                    AdLoader adLoader = new AdLoader.Builder(context, id)
                            .forNativeAd(nativeAd -> {
                                if (!checkDeviceTest(nativeAd, id)) {
                                    callback.onNativeAdLoaded(nativeAd);
                                } else {
                                    callback.onAdFailedToLoad();
                                }

                                nativeAd.setOnPaidEventListener(adValue -> {
                                    Log.d(TAG, "OnPaidEvent getInterstitalAds:" + adValue.getValueMicros());
                                    FirebaseUtil.logPaidAdImpression(context,
                                            adValue,
                                            id,
                                            AdType.NATIVE);
                                    callback.onEarnRevenue((double) adValue.getValueMicros());
                                });
                            })
                            .withAdListener(new AdListener() {
                                @Override
                                public void onAdFailedToLoad(LoadAdError error) {
                                    Log.e(TAG, "NativeAd onAdFailedToLoad: " + error.getMessage());
                                    callback.onAdFailedToLoad();
                                }

                                @Override
                                public void onAdClicked() {
                                    super.onAdClicked();
                                    Log.e(TAG, "NativeAd onAdClicked: ");
                                    //Helper.increaseNumClickAdsPerDay(context, id);
                                    callback.onAdClicked();
                                    if (disableAdResumeWhenClickAds) {
                                        AppOpenManager.getInstance().disableAdResumeByClickAction();
                                    }
                                    FirebaseUtil.logClickAdsEvent(context, id);

                                }
                            })
                            .withNativeAdOptions(adOptions)
                            .build();
                    adLoader.loadAd(getAdRequest());
                } else {
                    callback.onAdFailedToLoad();
                }
            } else {
                callback.onAdFailedToLoad();
            }
        }

    }

    public void loadNativeAd(Context context, String id, FrameLayout frameLayout, int layoutNative) {
        if (!isShowAllAds || !isNetworkConnected() || isDeviceTest) {
            frameLayout.removeAllViews();
            return;
        }
        /*if (Helper.getNumClickAdsPerDay(context, id) > maxClickAds) {
            frameLayout.removeAllViews();
            return;
        }*/
        if (isShowNative) {
            if (isNetworkConnected()) {
                VideoOptions videoOptions = new VideoOptions.Builder()
                        .setStartMuted(true)
                        .build();

                NativeAdOptions adOptions = new NativeAdOptions.Builder()
                        .setVideoOptions(videoOptions)
                        .build();
                AdLoader adLoader = new AdLoader.Builder(context, id)
                        .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {

                            @Override
                            public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                                NativeAdView adView = (NativeAdView) LayoutInflater.from(context).inflate(layoutNative, null);
                                frameLayout.removeAllViews();
                                if (!checkDeviceTest(nativeAd, id)) {
                                    frameLayout.addView(adView);
                                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                                }
                                nativeAd.setOnPaidEventListener(adValue -> {
                                    Log.d(TAG, "OnPaidEvent getInterstitalAds:" + adValue.getValueMicros());
                                    FirebaseUtil.logPaidAdImpression(context,
                                            adValue,
                                            id,
                                            AdType.NATIVE);
                                });
                            }
                        })
                        .withAdListener(new AdListener() {
                            @Override
                            public void onAdFailedToLoad(LoadAdError error) {
                                Log.e(TAG, "NativeAd onAdFailedToLoad: " + error.getMessage());
                                frameLayout.removeAllViews();
                            }

                            @Override
                            public void onAdClicked() {
                                super.onAdClicked();
                                //Helper.increaseNumClickAdsPerDay(context, id);
                                if (disableAdResumeWhenClickAds)
                                    AppOpenManager.getInstance().disableAdResumeByClickAction();
                                FirebaseUtil.logClickAdsEvent(context, id);
                                if (timeLimitAds > 1000) {
                                    setTimeLimitNative();
                                }
                            }
                        })
                        .withNativeAdOptions(adOptions)
                        .build();
                adLoader.loadAd(getAdRequest());
            } else {
                frameLayout.removeAllViews();
            }
        } else {
            frameLayout.removeAllViews();
        }
    }

    /* =============================  Native Ads Floor  ==========================================*/
    public void loadNativeAd(Context context, List<String> listID, final NativeCallback callback) {
        if (!isShowAllAds || !isNetworkConnected() || isDeviceTest) {
            listID.clear();
            callback.onAdFailedToLoad();
            return;
        }
        if (listID == null) {
            callback.onAdFailedToLoad();
        } else if (listID.size() == 0) {
            callback.onAdFailedToLoad();
        } else {
            List<String> listIDNew = new ArrayList<>();
            for (String idNew : listID) {
                listIDNew.add(idNew);
            }
            Log.e("xxxx listID", listID.toString() + "");
            Log.e("xxxx listIDNew", listID.toString() + "");
            Log.e(TAG, listIDNew + listID.get(0));

            loadNativeAd(context, listIDNew.get(0), new NativeCallback() {
                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    super.onNativeAdLoaded(nativeAd);
                    if (checkDeviceTest(nativeAd, listIDNew.get(0))) {
                        callback.onAdFailedToLoad();
                    } else {
                        callback.onNativeAdLoaded(nativeAd);
                    }

                }

                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    callback.onAdClicked();
                }

                @Override
                public void onAdFailedToLoad() {
                    super.onAdFailedToLoad();
                    if (listIDNew.size() > 1) {
                        listIDNew.remove(0);
                        loadNativeAd(context, listIDNew, callback);
                    } else {
                        callback.onAdFailedToLoad();
                    }

                }
            });
        }
    }

    public void loadNativeAd(Context context, List<String> listID, FrameLayout frameLayout, int layoutNative) {
        if (!isShowAllAds || !isNetworkConnected() || isDeviceTest) {
            listID.clear();
            frameLayout.removeAllViews();
            return;
        }
        if (listID == null || listID.size() == 0) {
            frameLayout.removeAllViews();
        } else {
            if (!isNetworkConnected() || !isShowAllAds) {
                frameLayout.removeAllViews();
                return;
            }
            NativeCallback callback1 = new NativeCallback() {
                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    super.onNativeAdLoaded(nativeAd);
                    NativeAdView adView = (NativeAdView) LayoutInflater.from(context).inflate(layoutNative, null);
                    frameLayout.removeAllViews();
                    if (!checkDeviceTest(nativeAd, listID.get(0))) {
                        frameLayout.addView(adView);
                        Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                        nativeAd.setOnPaidEventListener(adValue -> {
                            Log.d(TAG, "OnPaidEvent getInterstitalAds:" + adValue.getValueMicros());
                            FirebaseUtil.logPaidAdImpression(context,
                                    adValue,
                                    listID.get(0),
                                    AdType.NATIVE);
                        });
                    }
                }

                @Override
                public void onAdFailedToLoad() {
                    super.onAdFailedToLoad();
                    if (listID.size() > 0) {
                        listID.remove(0);
                        loadNativeAd(context, listID, frameLayout, layoutNative);
                    }
                }
            };
            if (listID.size() > 0) {
                int position = 0;
                Log.e(TAG, "Load Native ID :" + listID.get(position));
                loadNativeAd(context, listID.get(position), callback1);
            } else {
                frameLayout.removeAllViews();
            }
        }
    }

    public void pushAdsToViewCustom(NativeAd nativeAd, NativeAdView adView) {
        adView.setMediaView(adView.findViewById(R.id.ad_media));
        if (adView.getMediaView() != null) {
            adView.getMediaView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (context != null && BuildConfig.DEBUG) {
                        float sizeMin = TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP,
                                120,
                                context.getResources().getDisplayMetrics()
                        );
                        Log.e(TAG, "Native sizeMin: " + sizeMin);
                        Log.e(TAG, "Native w/h media : " + adView.getMediaView().getWidth() + "/" + adView.getMediaView().getHeight());
                        if (adView.getMediaView().getWidth() < sizeMin || adView.getMediaView().getHeight() < sizeMin) {
                            Toast.makeText(context, "Size media native not valid", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }, 1000);

        }
        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline is guaranteed to be in every UnifiedNativeAd.
        try {
            ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        try {
            if (nativeAd.getBody() == null) {
                adView.getBodyView().setVisibility(View.INVISIBLE);
            } else {
                adView.getBodyView().setVisibility(View.VISIBLE);
                ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (nativeAd.getCallToAction() == null) {
                Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.INVISIBLE);
            } else {
                Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.VISIBLE);
                ((TextView) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (nativeAd.getIcon() == null) {
                Objects.requireNonNull(adView.getIconView()).setVisibility(View.GONE);
            } else {
                ((ImageView) adView.getIconView()).setImageDrawable(
                        nativeAd.getIcon().getDrawable());
                adView.getIconView().setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (nativeAd.getPrice() == null) {
                Objects.requireNonNull(adView.getPriceView()).setVisibility(View.INVISIBLE);
            } else {
                Objects.requireNonNull(adView.getPriceView()).setVisibility(View.VISIBLE);
                ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (nativeAd.getStore() == null) {
                Objects.requireNonNull(adView.getStoreView()).setVisibility(View.INVISIBLE);
            } else {
                Objects.requireNonNull(adView.getStoreView()).setVisibility(View.VISIBLE);
                ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (nativeAd.getStarRating() == null) {
                Objects.requireNonNull(adView.getStarRatingView()).setVisibility(View.INVISIBLE);
            } else {
                ((RatingBar) Objects.requireNonNull(adView.getStarRatingView()))
                        .setRating(nativeAd.getStarRating().floatValue());
                adView.getStarRatingView().setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (nativeAd.getAdvertiser() == null) {
                adView.getAdvertiserView().setVisibility(View.INVISIBLE);
            } else {
                ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
                adView.getAdvertiserView().setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad. The SDK will populate the adView's MediaView
        // with the media content from this native ad.
        adView.setNativeAd(nativeAd);

    }

    public void loadNativeAdHide(Context context, String id, FrameLayout frameLayout, int layoutNative,boolean iconDown) {
        if (!isShowAllAds || !isNetworkConnected() || isDeviceTest) {
            frameLayout.removeAllViews();
            return;
        }
        /*if (Helper.getNumClickAdsPerDay(context, id) > maxClickAds) {
            frameLayout.removeAllViews();
            return;
        }*/
        if (isShowNative) {
            if (isNetworkConnected()) {
                VideoOptions videoOptions = new VideoOptions.Builder()
                        .setStartMuted(true)
                        .build();

                NativeAdOptions adOptions = new NativeAdOptions.Builder()
                        .setVideoOptions(videoOptions)
                        .build();
                AdLoader adLoader = new AdLoader.Builder(context, id)
                        .forNativeAd(nativeAd -> {
                            NativeAdView adView = (NativeAdView) LayoutInflater.from(context).inflate(layoutNative, null);
                            frameLayout.removeAllViews();
                            if (!checkDeviceTest(nativeAd, id)) {
                                frameLayout.addView(adView);
                                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                                try {
                                    ImageView imgNativeAd = adView.findViewById(R.id.iv_down);
                                    if(imgNativeAd!=null){
                                        imgNativeAd.setVisibility(View.VISIBLE);
                                        if(!iconDown) {imgNativeAd.setRotation(180);}
                                        imgNativeAd.setOnClickListener(v -> {
                                            adView.findViewById(R.id.rl_collap).setVisibility(View.GONE);
                                        });
                                    }
                                } catch (Exception e) {}
                            }
                            nativeAd.setOnPaidEventListener(adValue -> {
                                Log.d(TAG, "OnPaidEvent getInterstitalAds:" + adValue.getValueMicros());
                                FirebaseUtil.logPaidAdImpression(context,
                                        adValue,
                                        id,
                                        AdType.NATIVE);
                            });
                        })
                        .withAdListener(new AdListener() {
                            @Override
                            public void onAdFailedToLoad(LoadAdError error) {
                                Log.e(TAG, "NativeAd onAdFailedToLoad: " + error.getMessage());
                                frameLayout.removeAllViews();
                            }

                            @Override
                            public void onAdClicked() {
                                super.onAdClicked();
                                //Helper.increaseNumClickAdsPerDay(context, id);
                                if (disableAdResumeWhenClickAds)
                                    AppOpenManager.getInstance().disableAdResumeByClickAction();
                                FirebaseUtil.logClickAdsEvent(context, id);
                                if (timeLimitAds > 1000) {
                                    setTimeLimitNative();
                                }
                            }
                        })
                        .withNativeAdOptions(adOptions)
                        .build();
                adLoader.loadAd(getAdRequest());
            } else {
                frameLayout.removeAllViews();
            }
        } else {
            frameLayout.removeAllViews();
        }
    }

    public void loadNativeBanner(Context context, String id, FrameLayout frameLayout,int timeDelay,boolean iconDown) {
        if (!isShowAllAds || !isNetworkConnected() || isDeviceTest) {
            frameLayout.removeAllViews();
            return;
        }
        /*if (Helper.getNumClickAdsPerDay(context, id) > maxClickAds) {
            frameLayout.removeAllViews();
            return;
        }*/
        Log.d(TAG, "loadNativeAdHide");
        if (isShowNative) {
            if (isNetworkConnected()) {
                VideoOptions videoOptions = new VideoOptions.Builder()
                        .setStartMuted(true)
                        .build();

                NativeAdOptions adOptions = new NativeAdOptions.Builder()
                        .setVideoOptions(videoOptions)
                        .build();
                AdLoader adLoader = new AdLoader.Builder(context, id)
                        .forNativeAd(nativeAd -> {
                            NativeAdView adView = (NativeAdView) LayoutInflater.from(context).inflate(R.layout.ads_native_hide_big, null);
                            NativeAdView adViewSmall = (NativeAdView) LayoutInflater.from(context).inflate(R.layout.ads_native_big_small, null);
                            frameLayout.removeAllViews();
                            if (!checkDeviceTest(nativeAd, id)) {
                                frameLayout.addView(adView);
                                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                                Admob.getInstance().pushAdsToViewCustom(nativeAd, adViewSmall);
                                try {
                                    ImageView imgNativeAd = adView.findViewById(R.id.iv_down);
                                    if(imgNativeAd!=null){
                                        imgNativeAd.setVisibility(View.VISIBLE);
                                        if(!iconDown){imgNativeAd.setRotation(180);}
                                        imgNativeAd.setOnClickListener(v -> {
                                            frameLayout.removeAllViews();
                                            frameLayout.addView(adViewSmall);
                                        });
                                    }
                                } catch (Exception e) {}
                            }
                            nativeAd.setOnPaidEventListener(adValue -> {
                                Log.d(TAG, "OnPaidEvent getInterstitalAds:" + adValue.getValueMicros());
                                FirebaseUtil.logPaidAdImpression(context,
                                        adValue,
                                        id,
                                        AdType.NATIVE);
                            });
                        })
                        .withAdListener(new AdListener() {
                            @Override
                            public void onAdFailedToLoad(LoadAdError error) {
                                Log.e(TAG, "NativeAd onAdFailedToLoad: " + error.getMessage());
                                frameLayout.removeAllViews();
                            }

                            @Override
                            public void onAdClicked() {
                                super.onAdClicked();
                                Log.e(TAG, "NativeAd onAdClicked: ");
                                //Helper.increaseNumClickAdsPerDay(context, id);
                                if (disableAdResumeWhenClickAds)
                                    AppOpenManager.getInstance().disableAdResumeByClickAction();
                                FirebaseUtil.logClickAdsEvent(context, id);
                                if (timeLimitAds > 1000) {
                                    setTimeLimitNative();
                                }
                            }

                            @Override
                            public void onAdImpression() {
                                super.onAdImpression();
                                Log.e(TAG, "NativeAd onAdImpression: ");
                                if (runnableNT != null) {
                                    handlerNT.removeCallbacks(runnableNT); // Hủy bỏ runnable trước đó nếu có
                                }
                                Log.e(TAG, "onAdImpression run");
                                runnableNT = () -> {
                                    Log.e(TAG, "onAdImpression run: Performing action after 10 seconds");
                                    loadNativeBanner(context,id,frameLayout, timeDelay,iconDown);
                                };
                                handlerNT.postDelayed(runnableNT,timeDelay); // Bắt đầu chạy runnable
                            }

                            @Override
                            public void onAdOpened() {
                                Log.e(TAG, "NativeAd onAdOpened: ");
                                super.onAdOpened();
                            }
                        })
                        .withNativeAdOptions(adOptions)
                        .build();
                adLoader.loadAd(getAdRequest());
            } else {
                frameLayout.removeAllViews();
            }
        } else {
            frameLayout.removeAllViews();
        }
    }


    public void loadNativeAdHide(Context context, String id, FrameLayout frameLayout, int layoutNative,int timeDelay,boolean iconDown) {
        if (!isShowAllAds || !isNetworkConnected() || isDeviceTest) {
            frameLayout.removeAllViews();
            return;
        }
        /*if (Helper.getNumClickAdsPerDay(context, id) > maxClickAds) {
            frameLayout.removeAllViews();
            return;
        }*/
        Log.d(TAG, "loadNativeAdHide");
        if (isShowNative) {
            if (isNetworkConnected()) {
                VideoOptions videoOptions = new VideoOptions.Builder()
                        .setStartMuted(true)
                        .build();

                com.google.android.gms.ads.nativead.NativeAdOptions adOptions = new com.google.android.gms.ads.nativead.NativeAdOptions.Builder()
                        .setVideoOptions(videoOptions)
                        .build();
                AdLoader adLoader = new AdLoader.Builder(context, id)
                        .forNativeAd(nativeAd -> {
                            NativeAdView adView = (NativeAdView) LayoutInflater.from(context).inflate(layoutNative, null);
                            frameLayout.removeAllViews();
                            if (!checkDeviceTest(nativeAd, id)) {
                                frameLayout.addView(adView);
                                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                                try {
                                ImageView imgNativeAd = adView.findViewById(R.id.iv_down);
                                if(imgNativeAd!=null){
                                    imgNativeAd.setVisibility(View.VISIBLE);
                                    if(!iconDown){imgNativeAd.setRotation(180);}
                                    imgNativeAd.setOnClickListener(v -> {
                                        adView.findViewById(R.id.rl_collap).setVisibility(View.GONE);
                                        imgNativeAd.setVisibility(View.GONE);
                                    });
                                }
                               } catch (Exception e) {}
                            }
                            nativeAd.setOnPaidEventListener(adValue -> {
                                Log.d(TAG, "OnPaidEvent getInterstitalAds:" + adValue.getValueMicros());
                                FirebaseUtil.logPaidAdImpression(context,
                                        adValue,
                                        id,
                                        AdType.NATIVE);
                            });
                        })
                        .withAdListener(new AdListener() {
                            @Override
                            public void onAdFailedToLoad(LoadAdError error) {
                                Log.e(TAG, "NativeAd onAdFailedToLoad: " + error.getMessage());
                                frameLayout.removeAllViews();
                            }

                            @Override
                            public void onAdClicked() {
                                super.onAdClicked();
                                //Helper.increaseNumClickAdsPerDay(context, id);
                                if (disableAdResumeWhenClickAds)
                                    AppOpenManager.getInstance().disableAdResumeByClickAction();
                                FirebaseUtil.logClickAdsEvent(context, id);
                                if (timeLimitAds > 1000) {
                                    setTimeLimitNative();
                                }
                            }

                            @Override
                            public void onAdImpression() {
                                super.onAdImpression();
                                if(timeDelay>0){
                                    countDownTimerNative = new CountDownTimer(timeDelay, 1000) {
                                        @Override
                                        public void onTick(long l) {
                                        }

                                        @Override
                                        public void onFinish() {
                                            loadNativeAdHide(context,id,frameLayout,layoutNative, timeDelay,iconDown);
                                        }
                                    };
                                    countDownTimerNative.start();
                                }
                            }
                        })
                        .withNativeAdOptions(adOptions)
                        .build();
                adLoader.loadAd(getAdRequest());
            } else {
                frameLayout.removeAllViews();
            }
        } else {
            frameLayout.removeAllViews();
        }
    }

    /* =============================  End Native Ads ==========================================*/


    public AdRequest getAdRequest() {
        AdRequest.Builder builder = new AdRequest.Builder();
        return builder.build();
    }

    private AdRequest getAdRequestTimeOut(int timeOut) {
        if (timeOut < 5000) timeOut = 5000;
        return (AdRequest) new AdRequest.Builder().setHttpTimeoutMillis(timeOut).build();
    }

    public void setOpenActivityAfterShowInterAds(boolean openActivityAfterShowInterAds) {
        this.openActivityAfterShowInterAds = openActivityAfterShowInterAds;
    }


    /* =============================  GET  INFO DEVICE  ==========================================*/
    @SuppressLint("HardwareIds")
    public String getDeviceId(Activity activity) {
        String android_id = Settings.Secure.getString(activity.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return md5(android_id).toUpperCase();
    }

    private String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
        }
        return "";
    }

    private boolean checkDeviceTest(NativeAd nativeAd, String id) {
        if (id.equals(adsTestNative) || isShowAdsDeviceTest) {
            return false;
        }
        if (nativeAd.getHeadline().toLowerCase().contains(checkDeviceTest.toLowerCase())) {
            setOpenShowAllAds(false);
            AppOpenManager.getInstance().disableAppResume();
            isDeviceTest = true;
            return true;
        } else {
            return false;
        }
    }

    /* ============================= END GET  INFO DEVICE  ==========================================*/
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }


    private void setTimeLimitInter() {
        if (timeLimitShowAds > 1000) {
            isShowInter = false;
            handlerIT.postDelayed(() -> isShowInter = true, timeLimitShowAds);
        }
    }


    private void setTimeLimitNative() {
        if (timeLimitAds > 1000) {
            isShowNative = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isShowNative = true;
                }
            }, timeLimitAds);
        }

    }

    public void onCheckShowSplashWhenFail(final AppCompatActivity activity, final InterCallback callback, int timeDelay) {
        if (isNetworkConnected()) {
            (new Handler(activity.getMainLooper())).postDelayed(new Runnable() {
                public void run() {
                    if (Admob.this.interstitialSplashLoaded() && !Admob.this.isShowLoadingSplash) {
                        Log.i("Admob", "show ad splash when show fail in background");
                        Admob.getInstance().onShowSplash(activity, callback);
                    }

                }
            }, (long) timeDelay);
        }
    }

    public void onCheckShowSplashWhenFailClickButton(final AppCompatActivity activity, InterstitialAd interstitialAd, final InterCallback callback, int timeDelay) {
        if (interstitialAd != null) {
            if (isNetworkConnected()) {
                (new Handler(activity.getMainLooper())).postDelayed(new Runnable() {
                    public void run() {
                        if (Admob.this.interstitialSplashLoaded() && !Admob.this.isShowLoadingSplash) {
                            Log.i("Admob", "show ad splash when show fail in background");
                            Admob.getInstance().onShowSplash(activity, interstitialAd, callback);
                        }

                    }
                }, (long) timeDelay);
            }
        }
    }
}

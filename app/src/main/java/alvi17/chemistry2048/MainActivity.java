package alvi17.chemistry2048;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

public class MainActivity extends Activity {

    private WebView mWebView;
    private long mLastBackPress;
    private static final long mBackPressThreshold = 3500;
    private static final String IS_FULLSCREEN_PREF = "is_fullscreen_pref";
    private static boolean DEF_FULLSCREEN = true;
    private long mLastTouch;
    private static final long mTouchThreshold = 2000;
    private Toast pressBackToast;
    InterstitialAd interstitialAd;
    Boolean adLoaded=false;
    @SuppressLint({ "SetJavaScriptEnabled", "NewApi", "ShowToast" })
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Don't show an action bar or title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // If on android 3.0+ activate hardware acceleration
        if (Build.VERSION.SDK_INT >= 11){
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        }

        // Apply previous setting about showing status bar or not
        applyFullScreen(isFullScreen());

        // Check if screen rotation is locked in settings
        boolean isOrientationEnabled = false;
        try {
            isOrientationEnabled = Settings.System.getInt(getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION) == 1;
        } catch (Settings.SettingNotFoundException e) { }

        // If rotation isn't locked and it's a LARGE screen then add orientation changes based on sensor
        int screenLayout = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        if ( (screenLayout == Configuration.SCREENLAYOUT_SIZE_LARGE
                || screenLayout == Configuration.SCREENLAYOUT_SIZE_XLARGE )
                && isOrientationEnabled) {
            setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }

        setContentView(R.layout.activity_main);

        // Look up the AdView as a resource and load a request.
        AdView adView = (AdView) this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("0754C239B1E2E19421FDE46BCEFB8855").build();
        adView.loadAd(adRequest);

        // Load webview with game
        mWebView = (WebView) findViewById(R.id.mainWebView);
        WebSettings settings = mWebView.getSettings();
        String packageName = getPackageName();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setDatabasePath("/data/data/" + packageName + "/databases");

        // If there is a previous instance restore it in the webview
        if (savedInstanceState != null) {
            mWebView.restoreState(savedInstanceState);
        } else {
            mWebView.loadUrl("file:///android_asset/chemistry/index.html");
        }

        Toast.makeText(getApplication(),
                R.string.toggle_fullscreen, Toast.LENGTH_SHORT).show();
        // Set fullscreen toggle on webview LongClick
        mWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Implement a long touch action by comparing
                // time between action up and action down
                long currentTime = System.currentTimeMillis();
                if(event.getAction() == MotionEvent.ACTION_UP &&
                        Math.abs(currentTime - mLastTouch) > mTouchThreshold) {
                    boolean toggledFullScreen = !isFullScreen();
                    saveFullScreen(toggledFullScreen);
                    applyFullScreen(toggledFullScreen);
                } else if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    mLastTouch = currentTime;
                }
                // return so that the event isn't consumed but used
                // by the webview as well
                return false;
            }});

        pressBackToast = Toast.makeText(getApplicationContext(),
                R.string.press_back_again_to_exit, Toast.LENGTH_SHORT);


        interstitialAd=new  InterstitialAd(this);
        interstitialAd.setAdUnitId("ca-app-pub-6508526601344465/7403857636");
        AdRequest aRequest = new AdRequest.Builder().addTestDevice("0754C239B1E2E19421FDE46BCEFB8855").build();

        // Begin loading your interstitial.
        interstitialAd.loadAd(aRequest);

        interstitialAd.setAdListener(
                new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        adLoaded=true;
                    }
                }
        );
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mWebView.saveState(outState);
    }



    private void saveFullScreen(boolean isFullScreen) {
        // save in preferences
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(this).edit();
        editor.putBoolean(IS_FULLSCREEN_PREF, isFullScreen);
        editor.commit();
    }

    private boolean isFullScreen() {
        return PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean(IS_FULLSCREEN_PREF, DEF_FULLSCREEN);
    }

    /**
     * Toggles the activitys fullscreen mode by setting the corresponding window flag
     * @param isFullScreen
     */
    private void applyFullScreen(boolean isFullScreen) {
        if(isFullScreen) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {

        if(adLoaded)
        {
            interstitialAd.show();
        }

        long currentTime = System.currentTimeMillis();
        if (Math.abs(currentTime - mLastBackPress) > mBackPressThreshold) {
            pressBackToast.show();
            mLastBackPress = currentTime;
        } else {
            pressBackToast.cancel();
            super.onBackPressed();
        }
    }
}

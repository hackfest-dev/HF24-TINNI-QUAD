package com.example.Musicmask;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import android.util.Log;
import java.util.ArrayList;
import android.content.res.Configuration;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.example.Musicmask.databinding.ActivityMainBinding;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.view.MenuItem;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import android.widget.LinearLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.widget.FrameLayout;
import android.view.View;
import android.widget.ImageButton;
import android.os.CountDownTimer;
import android.os.Build;
import androidx.core.content.res.ResourcesCompat;
import android.widget.TextView;
import java.util.Hashtable;
import android.annotation.TargetApi;
import android.os.Build;
import android.content.Context;
import android.media.AudioManager;

public class Musicmask extends AppCompatActivity implements OnFragmentInteractionListener  {
    String externalFilesDir = null;
    private InfoFragment infoFragment;
    private AppFragment appFragment;
     FragmentManager fm;
     Fragment current;
     private static ArrayList<AudioFileRead> AudioFileReader = new  ArrayList<AudioFileRead>();
     private Hashtable<Integer,TextView> textViews = new Hashtable<Integer,TextView>();
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 200;
    private boolean isAudioRecordPermissionGranted = false;
    private boolean isAudioRecordPermissionRequested = false;
    String nativeSampleRate;
    String nativeSampleBufSize;
    private String[] scopeTitles = {"SPL \nScope", "Scope"};

    private void registerAndSetChartSettingsOfaScope(int i) {
        FrameLayout frameLayout = (FrameLayout) findViewById(
                getResources().getIdentifier("scopeLayout" + i, "id", getPackageName()));
        if (null == frameLayout) {
            Log.e("MainActivity", "registerAndSetChartSettingsOfaScope: frameLayout is null.");
            return;
        }
        ScopeHelper.getInstance().putAChartInLayout(i, frameLayout, this);
    }

    private boolean checkIfAllPermissionsGranted()
    {
        return true && isAudioRecordPermissionGranted;
    }
    private void requestPermission() {
        String permissionRationale = "";
        // Here, thisClass is the current activity
        //request for audio record
        if (ContextCompat.checkSelfPermission(thisClass,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted. Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(thisClass,
                    Manifest.permission.RECORD_AUDIO)) {
                permissionRationale += "Audio capture, ";
            } else {
                // No explanation needed; request the permission
                if (!isAudioRecordPermissionRequested) {
                    isAudioRecordPermissionRequested = true;
                    ActivityCompat.requestPermissions(thisClass,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
                    return;
                }
            }
        } else {
            // Permission has already been granted
            isAudioRecordPermissionGranted = true;
        }
        if (!permissionRationale.isEmpty())
            if (infoFragment != null) {
                infoFragment.updateModelInfo(permissionRationale + "permission not granted. Model cannot start.");
            }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();

        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            // Handle the URI here
            // For example, you can extract parameters from the URI and perform actions accordingly
            String parameterValue = data.getQueryParameter("parameterName");
            // Open specific activity or perform desired action based on parameterValue
        }

        com.example.Musicmask.databinding.ActivityMainBinding activityMainBinding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(activityMainBinding.getRoot());
        externalFilesDir = getExternalFilesDir(null).getPath();
        fm = getSupportFragmentManager();
        Fragment navHostFragment = fm.findFragmentById(R.id.nav_host_fragment);
        navHostFragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
        current =  navHostFragment;
        appFragment  = (AppFragment) navHostFragment;
        infoFragment  = new InfoFragment();
        fm.beginTransaction().add(R.id.nav_host_fragment, infoFragment, "2").hide(infoFragment).commit();
        fm.beginTransaction().add(R.id.nav_host_fragment, appFragment, "1").commit();
        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        switch (menuItem.getItemId()) {                case R.id.navigation_app:
                fm.beginTransaction().hide(current).show(appFragment).commit();
                current = appFragment;
        return true;                case R.id.navigation_info:
                fm.beginTransaction().hide(current).show(infoFragment).commit();
                current = infoFragment;
        return true;                }
                return false;
                }
        });
        queryNativeAudioParameters();
        thisClass = this;
     }

    private Musicmask thisClass;
    private final Thread BgThread = new Thread() {
    @Override
    public void run() {
            String argv[] = new String[] {"MainActivity","Musicmask"};
            naMain(argv, thisClass);
        }
    };

    public void flashMessage(final String inMessage) {
        runOnUiThread(new Runnable() {
              public void run() {
                    Toast.makeText(getBaseContext(), inMessage, Toast.LENGTH_SHORT).show();
              }
        });
    }

    public void terminateApp() {
        finish();
    }

    protected void onDestroy() {
         if (BgThread.isAlive())
             naOnAppStateChange(6);
         super.onDestroy();
         System.exit(0); //to kill all our threads.
    }

	@Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof InfoFragment) {
            this.infoFragment = (InfoFragment) fragment;
            infoFragment.setFragmentInteractionListener(this);
        }
        if (fragment instanceof AppFragment) {
            ((AppFragment)fragment).setFragmentInteractionListener(this);
        }
        if (fragment instanceof CameraScopeFragment) {
            ((CameraScopeFragment)fragment).setFragmentInteractionListener(this);
        }
    }

	@Override
    public void onFragmentCreate(String name) {

    }

    @Override
    public void onFragmentStart(String name) {
        switch (name) {
            case "Info":
               break;
            case "App":
                break;
            default:
                break;
    }
    }

    @Override
    public void onFragmentResume(String name) {
        switch (name) {
            case "App":
                registerDataDisplays();
                if (checkIfAllPermissionsGranted()){
                    if (!BgThread.isAlive()) {
                        BgThread.start();
                    }
                }
                break;
            case "dot1":
                registerAndSetChartSettingsOfaScope(1);
                break;
            case "dot2":
                registerAndSetChartSettingsOfaScope(2);
                break;
            default:
                break;
        }
    }

    @Override
    public void onFragmentPause(String name) {
    }
    @Override
    protected void onResume() {
            requestPermission();
         super.onResume();
         if (BgThread.isAlive())
             naOnAppStateChange(3);
    }

    @Override
    protected void onPause() {
        if (BgThread.isAlive())
            naOnAppStateChange(4);
        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LinearLayout mylayout = thisClass.findViewById(R.id.layoutapp);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mylayout.setOrientation(LinearLayout.HORIZONTAL);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            mylayout.setOrientation(LinearLayout.VERTICAL);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: 
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do related task you need to do.
                    isAudioRecordPermissionGranted = true;
                } else {
                    // permission denied, boo!
                    flashMessage("Audio Record Permission not granted");
                }
                isAudioRecordPermissionRequested = false;
                break;

            // other case lines to check for other
            // permissions this app might request.
        }
        if (!checkIfAllPermissionsGranted() && !isAudioRecordPermissionRequested) {
            requestPermission();
        }
    }

    public void registerDataDisplays() {
    // bind text views for data display block;
    for (int i = 1; i <= 1; i++) {
            TextView textView = (TextView) findViewById(
            getResources().getIdentifier("DataDisplay" + i, "id", getPackageName()));
            textViews.put(i, textView);
        }
    }
    public void displayText(int id, byte[] data, byte[] format) {
        String formatString = new String(format);
        String toDisplay = String.format(formatString, data[0]);
        if (data.length > 1) {
            for (int i = 1; i < data.length; i++)
                toDisplay += "\n" + String.format(formatString, data[i]);
        }
        updateTextViewById(id, toDisplay);
    }

    public void displayText(int id, short[] data, byte[] format) {
        String formatString = new String(format);
        String toDisplay = String.format(formatString, data[0]);
        if (data.length > 1) {
            for (int i = 1; i < data.length; i++)
                toDisplay += "\n" + String.format(formatString, data[i]);
        }
        updateTextViewById(id, toDisplay);
    }

    public void displayText(int id, int[] data, byte[] format) {
        String formatString = new String(format);
        String toDisplay = String.format(formatString, data[0]);
        if (data.length > 1) {
            for (int i = 1; i < data.length; i++)
                toDisplay += "\n" + String.format(formatString, data[i]);
        }
        updateTextViewById(id, toDisplay);
    }

    public void displayText(int id, long[] data, byte[] format) {
        String formatString = new String(format);
        String toDisplay = String.format(formatString, data[0]);
        if (data.length > 1) {
            for (int i = 1; i < data.length; i++)
                toDisplay += "\n" + String.format(formatString, data[i]);
        }
        updateTextViewById(id, toDisplay);
    }

    public void displayText(int id, float[] data, byte[] format) {
        String formatString = new String(format);
        String toDisplay = String.format(formatString, data[0]);
        if (data.length > 1) {
            for (int i = 1; i < data.length; i++)
                toDisplay += "\n" + String.format(formatString, data[i]);
        }
        updateTextViewById(id, toDisplay);
    }

    public void displayText(int id, double[] data, byte[] format) {
        String formatString = new String(format);
        String toDisplay = String.format(formatString, data[0]);
        if (data.length > 1) {
            for (int i = 1; i < data.length; i++)
                toDisplay += "\n" + String.format(formatString, data[i]);
        }
        updateTextViewById(id, toDisplay);
    }

    private void updateTextViewById(final int id, final String finalStringToDisplay) {
        runOnUiThread(new Runnable() {
            @SuppressLint("LongLogTag")
            @Override
            public void run() {
                try {
                    TextView tv = textViews.get(id);
                    if(tv != null) {
                        tv.setText(finalStringToDisplay);
                    }
					
                } catch (Exception ex) {
                    Log.e("Musicmask.updateTextViewById", ex.getLocalizedMessage());
                }
            }
        });
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void queryNativeAudioParameters() {
        Log.d("audioEQ", "queryNativeAudioParameters called");
        AudioManager myAudioMgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        nativeSampleRate = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
        nativeSampleBufSize = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
    }

    public int getNativeSampleRate() {
        Log.d("audioEQ", "JNI getNativeSampleRate called");
        return Integer.parseInt(nativeSampleRate);
    }
    public int getNativeSampleBufSize() {
        Log.d("audioEQ", "JNI getNativeSampleBufSize called");
        return Integer.parseInt(nativeSampleBufSize);
    }
public int audioFileReadInit(String fileName, int frameSize) {
    AudioFileReader.add(new AudioFileRead(this, fileName, frameSize));
    return AudioFileReader.size() - 1;
}
public short[] audioFileReadStep(int idx) {
    return AudioFileReader.get(idx).AudioFileReadStep();
}
public void audioFileReadTerminate(int idx) {
    AudioFileReader.get(idx).AudioFileReadTerminate();
}
    public void initScope(final int scopeID, final int numInputPorts, final byte[] attribute, final float[] sampleTimes) {
        ScopeHelper scopeHelper = ScopeHelper.getInstance();
        scopeHelper.initScope(scopeID, numInputPorts, attribute, sampleTimes);
        if (!scopeHelper.checkIfScopeRegistered(scopeID)){
            Log.w("MainActivity", "Scope not registered.");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    registerAndSetChartSettingsOfaScope(scopeID);
                }
            });
        }
        scopeHelper.setTitle(scopeID, scopeTitles[scopeID-1]);
    }

    public void cachePlotData(int scopeID, int portIdx, float[] data, int sigNumDims, int[] sigDims) {
        ScopeHelper.getInstance().cachePlotData(scopeID, portIdx, data, sigNumDims, sigDims);
    }

    public void plotData(int scopeID) {
        ScopeHelper.getInstance().plotData(scopeID);
    }
    private native int naMain(String[] argv, Musicmask pThis);
    private native void naOnAppStateChange(int state);
    static {
        System.loadLibrary("Musicmask");
    }

}

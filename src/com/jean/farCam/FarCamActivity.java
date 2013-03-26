//    Copyright 2013 Giancarlo Todone
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.

package com.jean.farCam;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;

public class FarCamActivity extends Activity {

    private static final String TAG = "FarCam";

    private ServerThread _serverThread = null;

    PowerManager.WakeLock wakeLock = null;

    Camera _camera = null;
    
    TakePictureHelper _tph = null;
       
    private SurfaceView surfaceView;
    
    boolean _forceNoPreview = false;
    int _xResolution = 640;
    int _yResolution = 480;
    int _port = 1234;
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 0, 0, "Settings");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return false;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(FarCamActivity.this);
        _forceNoPreview = settings.getBoolean(getString(R.string.settings_key_force_no_preview), false);
        _xResolution = settings.getInt(getString(R.string.settings_key_x_resolution), 640);
        _yResolution = settings.getInt(getString(R.string.settings_key_y_resolution), 480);
        _port = settings.getInt(getString(R.string.settings_key_port), 1234);
        
        //requestWindowFeature(Window.FEATURE_NO_TITLE); 
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); 
        
        // DON'T SLEEP
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "no sleep");
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "FarCam_NoSleep");
        wakeLock.acquire();

        setContentView(R.layout.main);
        surfaceView = (SurfaceView)findViewById(R.id.preview_surface);
        
        _tph = new TakePictureHelper(surfaceView, !_forceNoPreview, _xResolution, _yResolution);
        
        _serverThread = ServerThread.NewServerThread(this, _tph, _port);
    }
	
    @Override
    public void onDestroy() {
    	super.onDestroy();

            wakeLock.release();
        
    	try {
            if (_serverThread.serversocket!=null) {
                _serverThread.stop();
            } else {
                Log.e(TAG, "serversocket null");
            }
        } catch (Exception ex) {
            Log.e(TAG, ""+ex);
        }
    }
}
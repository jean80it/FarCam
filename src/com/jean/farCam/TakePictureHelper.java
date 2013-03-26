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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class TakePictureHelper implements SurfaceHolder.Callback{

    private static final String TAG = "FarCam";
    
    private Camera _camera = null;

    private SurfaceHolder _surfaceHolder = null;
    
    boolean _isPreviewRunning = false;
    boolean _mustActivatePreview = false;
    
    boolean _running = true;
    
    Calendar _plannedPreviewShut = Calendar.getInstance();
    Object _previewLock = new Object();      
    int _previewShutTimeout = 10000;
    int _afterPreviewInitDelay = 100;
    int _waitForPic = 10000;
    
    int _width = 640;
    int _height = 480;
    
    Runnable _previewAutoShutdown = new Runnable() {

        public void run() {
            while (_running)
            {
                synchronized(_previewLock)
                {
                    if (Calendar.getInstance().after(_plannedPreviewShut))
                    {
                        if (_isPreviewRunning)
                        {
                            _camera.stopPreview();
                            _isPreviewRunning = false;
                        }
                    }
                }
                
                try {
                    Thread.sleep(Math.max(0, _plannedPreviewShut.getTimeInMillis()-Calendar.getInstance().getTimeInMillis())+1);
                } catch (InterruptedException ex) {}
            }
        }
    };
    
    TakePictureHelper(SurfaceView surfaceView, boolean mustActivatePreview, int width, int height) 
    {
        _width = width;
        _height = height;
        
        _surfaceHolder = surfaceView.getHolder();
        _surfaceHolder.addCallback(this);
        _surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); 
        
        _mustActivatePreview = mustActivatePreview;
        
        if (_mustActivatePreview)
        {
            new Thread(_previewAutoShutdown).start();
        }
    }
    
    private final Semaphore _ready = new Semaphore(1, true);
    private final Object _bufferLock = new Object();

    private byte[] _rawJpegData = null;

    Camera.ShutterCallback shutterCallback  = new Camera.ShutterCallback(){

        public void onShutter() {
            Log.d(TAG, "shutter!");
        }
    
    };
            
    Camera.PictureCallback mJPEGPictureCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera c) {
            Log.d(TAG, "got JPEG!");
            synchronized(_bufferLock)
            {
                _rawJpegData = data.clone();
                _ready.release();
            }
        }
    };

    Camera.PictureCallback mRAWPictureCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera c) {
            Log.d(TAG, "got RAW!");
            if (data != null) {
                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length );
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                _rawJpegData = bos.toByteArray();
                _ready.release();
            }
        }
    };

    private void ensurePreview()
    {
        synchronized(_previewLock)
        {
            if (!_isPreviewRunning)
            {
                _camera.startPreview();
                _isPreviewRunning = true;
            }
            
            _plannedPreviewShut.setTimeInMillis(Calendar.getInstance().getTimeInMillis()+_previewShutTimeout);
            try {
                Thread.sleep(_afterPreviewInitDelay);
            } catch (InterruptedException ex) {}
        }    
    }
    
    public byte[] GetPicture()
    {
        if (_camera == null)
        {
            return null;
        }
        
        try {
            if (_ready.tryAcquire())
            {
                if (_mustActivatePreview)
                {
                    ensurePreview();
                }
                
                //_isPreviewRunning = false;
                _camera.takePicture(null, null, mJPEGPictureCallback);

                _ready.tryAcquire(_waitForPic, TimeUnit.MILLISECONDS); // wait for semaphore release = picture taken
                
                // picture callback could have been called or not; 
                // regardless, mutex is released
                 _ready.release();
                
                if(_mustActivatePreview) // does camera stop preview after one frame?? it seems to be so...
                {
                    synchronized(_previewLock)
                    {
                        _camera.startPreview();
                        _isPreviewRunning = true;
                    }
                }
                
                return _rawJpegData;
            }
            else
            {
                synchronized(_bufferLock)
                {
                    return _rawJpegData;// .clone();
                }
            }

        } catch (InterruptedException ex) {
            Log.e(TAG, ""+ex);
        }

        return null;
    }
        
   public void surfaceCreated(SurfaceHolder arg0) {
        Log.d(TAG, "surface created");
    }

    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        Log.d(TAG, "surface changed");
        
        synchronized(_previewLock)
        {
            if (_isPreviewRunning) {
                _camera.stopPreview();
                _isPreviewRunning = false;
            }
        }
        
        _camera = Camera.open();
        
        try
        {
            Camera.Parameters params = _camera.getParameters();
            params.setPictureSize(_width, _height);
            _camera.setParameters(params);
        }
        catch(Exception ex)
        {
            Log.e(TAG, "Could not set given resolution.");
        }
        
        try {
            _camera.setPreviewDisplay(_surfaceHolder);
        } catch (IOException ex) {
            Log.e(TAG, ""+ex);
        }
        
        if (_mustActivatePreview)
        {
            _camera.startPreview();
            _isPreviewRunning = true;
        }
        
    }

    public void surfaceDestroyed(SurfaceHolder arg0) {
        Log.d(TAG, "surface destroyed");
        _running = false;
        
        synchronized(_previewLock)
        {
            if (_isPreviewRunning)
            {
                _camera.stopPreview();
                _isPreviewRunning = false;
            }
        }
        _camera.release();
    }
}

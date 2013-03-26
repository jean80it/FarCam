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

package com.jean.farCam.customUI;


import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

public class EditFloatPreference extends EditTextPreference
{
    public EditFloatPreference(Context context) {
        super(context); 
    }       

    public EditFloatPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditFloatPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public String getText() {
        try
        {
        return String.valueOf(getSharedPreferences().getFloat(getKey(), 0));
        }
        catch(Exception ex)
        {
            return null;
        }
    }

    @Override
    public void setText(String text) {
        getSharedPreferences().edit().putFloat(getKey(), Float.parseFloat(text)).commit();
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue)
            getEditText().setText(getText());
        else
            super.onSetInitialValue(restoreValue, defaultValue);
    }
}
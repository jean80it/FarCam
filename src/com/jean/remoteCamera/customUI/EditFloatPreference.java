package com.jean.remoteCamera.customUI;


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
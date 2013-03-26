package com.jean.remoteCamera.customUI;


import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

public class EditIntPreference extends EditTextPreference
{
    public EditIntPreference(Context context) {
        super(context); 
    }       

    public EditIntPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditIntPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public String getText() {
        try
        {
        return String.valueOf(getSharedPreferences().getInt(getKey(), 0));
        }
        catch(Exception ex)
        {
            return null;
        }
    }

    @Override
    public void setText(String text) {
        getSharedPreferences().edit().putInt(getKey(), Integer.parseInt(text)).commit();
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue)
            getEditText().setText(getText());
        else
            super.onSetInitialValue(restoreValue, defaultValue);
    }
}
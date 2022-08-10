package com.example.myapplication;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

public class DigitalTextView extends androidx.appcompat.widget.AppCompatTextView {
    public DigitalTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    private void init(Context context){
        String file = "Open 24 Display St.TTF";
        AssetManager assets = context.getAssets();
        Typeface font = Typeface.createFromAsset(assets, file);
        setTypeface(font);
    }
}


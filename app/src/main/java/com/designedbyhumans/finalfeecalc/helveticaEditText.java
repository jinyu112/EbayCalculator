package com.designedbyhumans.finalfeecalc;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;


public class helveticaEditText extends EditText implements View.OnFocusChangeListener {

    public helveticaEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public helveticaEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public helveticaEditText(Context context) {
        super(context);
        init();
    }

    private void init() {
        Typeface tf = Typefaces.get(getContext(), "fonts/HelveticaLTStd-Light.otf");
        setTypeface(tf);
        setOnFocusChangeListener(this);
        this.setCursorVisible(false);
    }

    @Override
    public void onFocusChange(View v,boolean hasFocus) {
        if (hasFocus) {
            this.setTextColor(Color.parseColor("#ffffff"));
            this.setCursorVisible(false);
        }
        else {
            this.setTextColor(Color.parseColor("#3298da"));
        }
    }

    @Override
    protected void onSelectionChanged(int start, int end) {
        CharSequence text = getText();
        if (text != null) {
            if (start != text.length() || end != text.length()) {
                setSelection(text.length(), text.length());
                return;
            }
        }
        super.onSelectionChanged(start, end);
    }
}

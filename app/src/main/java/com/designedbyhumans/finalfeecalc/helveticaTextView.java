package com.designedbyhumans.finalfeecalc;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;


public class helveticaTextView extends TextView {
    public helveticaTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public helveticaTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public helveticaTextView(Context context) {
        super(context);
        init();
    }

    private void init() {
        Typeface tf = Typefaces.get(getContext(), "fonts/HelveticaLTStd-Light.otf");
        setTypeface(tf);
    }
}

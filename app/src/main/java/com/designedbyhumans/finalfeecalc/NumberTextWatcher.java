package com.designedbyhumans.finalfeecalc;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.NumberFormat;

public class NumberTextWatcher implements TextWatcher {

    private EditText et;
    private String current = "";

    public NumberTextWatcher(EditText et)
    {
        et.setText("0.00");
        et.setTextColor(Color.parseColor("#3298da"));
        this.et = et;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(!s.toString().equals(current)){
            et.removeTextChangedListener(this);

            String cleanString = s.toString().replaceAll("[^\\d]", "");
            if (cleanString.equals("")) {
                cleanString = "0";
            }

            double parsed = Double.parseDouble(cleanString);
            String formatted = NumberFormat.getCurrencyInstance().format((parsed/100));

            formatted = formatted.toString().replaceAll("[^\\d.]", "");
            current = formatted;
            et.setText(formatted);
            et.setSelection(formatted.length());

            et.addTextChangedListener(this);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {
    }

    @Override
    public void afterTextChanged(Editable s)
    {
    }


}
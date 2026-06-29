package com.example.doancuoiki.utils;

import android.os.Build;
import android.os.LocaleList;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import java.text.Normalizer;
import java.util.Locale;

public final class VietnameseInputUtils {
    private VietnameseInputUtils() {
    }

    public static void setupSingleLine(EditText editText) {
        setupLocale(editText);
        editText.setShowSoftInputOnFocus(true);
        editText.setSingleLine(true);
        editText.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                | InputType.TYPE_TEXT_VARIATION_NORMAL);
        editText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        normalizeWhenDone(editText);
    }

    public static void setupMultiLine(EditText editText) {
        setupLocale(editText);
        editText.setShowSoftInputOnFocus(true);
        editText.setSingleLine(false);
        editText.setMinLines(3);
        editText.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_VARIATION_NORMAL);
        editText.setImeOptions(EditorInfo.IME_ACTION_NONE);
        normalizeWhenDone(editText);
    }

    private static void setupLocale(EditText editText) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            editText.setTextLocales(LocaleList.forLanguageTags("vi-VN"));
        } else {
            editText.setTextLocale(new Locale("vi", "VN"));
        }
    }

    private static void normalizeWhenDone(EditText editText) {
        editText.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                return;
            }
            String current = editText.getText().toString();
            String normalized = Normalizer.normalize(current, Normalizer.Form.NFC);
            if (!current.equals(normalized)) {
                editText.setText(normalized);
            }
        });
    }
}

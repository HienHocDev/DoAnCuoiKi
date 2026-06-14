package com.example.doancuoiki;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

public final class UiKit {
    public static final int PRIMARY = Color.rgb(93, 95, 239);
    public static final int PRIMARY_DARK = Color.rgb(70, 72, 210);
    public static final int BACKGROUND = Color.rgb(247, 248, 252);
    public static final int CARD = Color.WHITE;
    public static final int TEXT = Color.rgb(34, 38, 50);
    public static final int MUTED = Color.rgb(125, 132, 150);
    public static final int SUCCESS = Color.rgb(33, 181, 127);
    public static final int WARNING = Color.rgb(239, 173, 68);

    private UiKit() {
    }

    public static ScrollView screen(Context context) {
        ScrollView scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(BACKGROUND);
        return scrollView;
    }

    public static LinearLayout page(Context context) {
        LinearLayout page = vertical(context);
        page.setPadding(dp(context, 20), dp(context, 42), dp(context, 20), dp(context, 24));
        return page;
    }

    public static LinearLayout vertical(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    public static LinearLayout horizontal(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        return layout;
    }

    public static TextView text(Context context, String value, int sp, int color, boolean bold) {
        TextView textView = new TextView(context);
        textView.setText(value);
        textView.setTextSize(sp);
        textView.setTextColor(color);
        textView.setIncludeFontPadding(true);
        if (bold) {
            textView.setTypeface(Typeface.DEFAULT_BOLD);
        }
        return textView;
    }

    public static Button primaryButton(Context context, String value) {
        Button button = new Button(context);
        button.setText(value);
        button.setTextColor(Color.WHITE);
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setBackground(gradient(context, PRIMARY, PRIMARY_DARK));
        return button;
    }

    public static TextView lightButton(Context context, String value) {
        TextView button = text(context, value, 14, TEXT, true);
        button.setGravity(Gravity.CENTER);
        button.setBackground(round(context, Color.WHITE, 14, Color.rgb(232, 235, 244)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(context, 110), dp(context, 46));
        params.setMargins(dp(context, 6), 0, dp(context, 6), 0);
        button.setLayoutParams(params);
        return button;
    }

    public static EditText input(Context context, String hint) {
        EditText editText = new EditText(context);
        editText.setHint(hint);
        editText.setTextSize(14);
        editText.setSingleLine(false);
        editText.setTextColor(TEXT);
        editText.setHintTextColor(Color.rgb(175, 181, 194));
        editText.setPadding(dp(context, 14), 0, dp(context, 14), 0);
        editText.setBackground(round(context, Color.WHITE, 12, Color.rgb(232, 235, 244)));
        return editText;
    }

    public static TextView searchBox(Context context, String hint) {
        TextView box = text(context, "  " + hint, 13, MUTED, false);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setBackground(round(context, Color.WHITE, 14));
        return box;
    }

    public static LinearLayout tabs(Context context, String... labels) {
        HorizontalScrollView scroller = new HorizontalScrollView(context);
        scroller.setHorizontalScrollBarEnabled(false);
        LinearLayout row = horizontal(context);
        for (int i = 0; i < labels.length; i++) {
            TextView tab = text(context, labels[i], 13, i == 0 ? PRIMARY : MUTED, i == 0);
            tab.setGravity(Gravity.CENTER);
            tab.setPadding(dp(context, 16), 0, dp(context, 16), 0);
            row.addView(tab, lp(context, -2, 42));
        }
        scroller.addView(row);
        LinearLayout wrapper = vertical(context);
        wrapper.addView(scroller);
        return wrapper;
    }

    public static void addTopBar(Context context, LinearLayout page, String title, String action, View.OnClickListener backListener, View.OnClickListener actionListener) {
        LinearLayout bar = horizontal(context);
        bar.setGravity(Gravity.CENTER_VERTICAL);

        TextView back = text(context, "<", 24, TEXT, true);
        back.setGravity(Gravity.CENTER);
        back.setOnClickListener(backListener);
        bar.addView(back, lp(context, 36, 44));

        TextView heading = text(context, title, 22, TEXT, true);
        bar.addView(heading, new LinearLayout.LayoutParams(0, -2, 1));

        TextView actionView = text(context, action, 24, TEXT, true);
        actionView.setGravity(Gravity.CENTER);
        if (actionListener != null) {
            actionView.setOnClickListener(actionListener);
        }
        bar.addView(actionView, lp(context, 44, 44));
        page.addView(bar);
    }

    public static void addSectionTitle(Context context, LinearLayout page, String title, String action, View.OnClickListener listener) {
        LinearLayout row = horizontal(context);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(text(context, title, 18, TEXT, true), new LinearLayout.LayoutParams(0, -2, 1));
        TextView right = text(context, action, 13, PRIMARY, false);
        if (listener != null) {
            right.setOnClickListener(listener);
        }
        row.addView(right);
        page.addView(row, top(context, -1, -2, 22));
    }

    public static View projectRow(Context context, String name, String subtitle, int progress, View.OnClickListener listener) {
        LinearLayout card = vertical(context);
        card.setPadding(dp(context, 16), dp(context, 14), dp(context, 16), dp(context, 14));
        card.setBackground(round(context, CARD, 16));
        card.setOnClickListener(listener);
        card.addView(text(context, name, 15, TEXT, true));
        card.addView(text(context, subtitle, 12, MUTED, false), top(context, -1, -2, 4));
        card.addView(progress(context, progress), top(context, -1, 7, 9));
        return withTop(context, card, 12);
    }

    public static void addTask(Context context, LinearLayout page, String title, String project, String date, String status, int color, View.OnClickListener listener) {
        LinearLayout card = horizontal(context);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(context, 14), dp(context, 14), dp(context, 14), dp(context, 14));
        card.setBackground(round(context, CARD, 16));
        card.setOnClickListener(listener);

        LinearLayout textCol = vertical(context);
        textCol.addView(text(context, title, 15, TEXT, true));
        textCol.addView(text(context, project + " - " + date, 12, MUTED, false), top(context, -1, -2, 5));
        card.addView(textCol, new LinearLayout.LayoutParams(0, -2, 1));

        TextView badge = text(context, status, 12, color, true);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(round(context, Color.rgb(250, 250, 255), 14));
        card.addView(badge, lp(context, 104, 32));
        page.addView(card, top(context, -1, -2, 12));
    }

    public static ProgressBar progress(Context context, int value) {
        ProgressBar bar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        bar.setProgress(value);
        return bar;
    }

    public static TextView metric(Context context, String number, String label) {
        TextView view = text(context, number + "\n" + label, 14, TEXT, true);
        view.setGravity(Gravity.CENTER);
        view.setBackground(round(context, CARD, 18));
        return view;
    }

    public static TextView setting(Context context, String label) {
        TextView item = text(context, label + "  >", 15, TEXT, false);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(dp(context, 16), 0, dp(context, 16), 0);
        item.setBackground(round(context, CARD, 14));
        return item;
    }

    public static View withTop(Context context, View view, int marginTop) {
        view.setLayoutParams(top(context, -1, -2, marginTop));
        return view;
    }

    public static LinearLayout.LayoutParams lp(Context context, int width, int heightDp) {
        return new LinearLayout.LayoutParams(width, heightDp < 0 ? heightDp : dp(context, heightDp));
    }

    public static LinearLayout.LayoutParams top(Context context, int width, int height, int marginTopDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height < 0 ? height : dp(context, height));
        params.setMargins(0, dp(context, marginTopDp), 0, 0);
        return params;
    }

    public static GradientDrawable round(Context context, int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(context, radiusDp));
        return drawable;
    }

    public static GradientDrawable round(Context context, int color, int radiusDp, int strokeColor) {
        GradientDrawable drawable = round(context, color, radiusDp);
        drawable.setStroke(dp(context, 1), strokeColor);
        return drawable;
    }

    public static GradientDrawable gradient(Context context, int start, int end) {
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{start, end});
        drawable.setCornerRadius(dp(context, 18));
        return drawable;
    }

    public static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}

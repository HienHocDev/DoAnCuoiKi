package com.example.doancuoiki;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public final class ViewFactory {
    private ViewFactory() {
    }

    public static View projectCard(Context context, String name, String subtitle, int progress, View.OnClickListener listener) {
        LinearLayout card = card(context);
        card.setOnClickListener(listener);
        card.addView(text(context, name, 16, true, Color.rgb(34, 38, 50)));
        card.addView(text(context, subtitle, 12, false, Color.rgb(125, 132, 150)));

        ProgressBar progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(progress);
        card.addView(progressBar, topParams(-1, dp(context, 8), dp(context, 10)));
        return card;
    }

    public static View homeProjectCard(Context context, String name, String subtitle, int progress, View.OnClickListener listener) {
        LinearLayout card = card(context);
        card.setPadding(dp(context, 16), dp(context, 16), dp(context, 16), dp(context, 16));
        card.setOnClickListener(listener);

        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout textColumn = new LinearLayout(context);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        textColumn.addView(text(context, name, 16, true, Color.rgb(34, 38, 50)));
        textColumn.addView(text(context, subtitle, 12, false, Color.rgb(125, 132, 150)));
        header.addView(textColumn, new LinearLayout.LayoutParams(0, -2, 1));

        TextView percent = text(context, progress + "%", 14, true, Color.rgb(34, 197, 94));
        percent.setGravity(Gravity.CENTER);
        percent.setBackgroundResource(R.drawable.bg_soft_primary);
        header.addView(percent, new LinearLayout.LayoutParams(dp(context, 58), dp(context, 34)));
        card.addView(header);

        ProgressBar progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(Math.max(0, Math.min(progress, 100)));
        card.addView(progressBar, topParams(-1, dp(context, 8), dp(context, 12)));

        return card;
    }

    public static View taskCard(Context context, String title, String subtitle, String status, int badgeBackground, int badgeColor) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(context, 14), dp(context, 14), dp(context, 14), dp(context, 14));
        row.setBackgroundResource(R.drawable.bg_card);
        row.setLayoutParams(topParams(-1, -2, dp(context, 12)));

        LinearLayout textColumn = new LinearLayout(context);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        textColumn.addView(text(context, title, 15, true, Color.rgb(34, 38, 50)));
        textColumn.addView(text(context, subtitle, 12, false, Color.rgb(125, 132, 150)));
        row.addView(textColumn, new LinearLayout.LayoutParams(0, -2, 1));

        TextView badge = text(context, status, 12, true, badgeColor);
        badge.setGravity(Gravity.CENTER);
        badge.setBackgroundResource(badgeBackground);
        row.addView(badge, new LinearLayout.LayoutParams(dp(context, 104), dp(context, 32)));
        return row;
    }

    public static View notificationCard(Context context, String title, String body, String time) {
        LinearLayout card = card(context);
        card.addView(text(context, title, 15, true, Color.rgb(34, 38, 50)));
        card.addView(text(context, body, 13, false, Color.rgb(125, 132, 150)));
        card.addView(text(context, time, 12, false, Color.rgb(170, 176, 190)));
        return card;
    }

    public static View reportProgressCard(Context context, String title, String subtitle, int progress) {
        LinearLayout card = card(context);
        card.addView(text(context, title, 15, true, Color.rgb(34, 38, 50)));
        card.addView(text(context, subtitle, 12, false, Color.rgb(125, 132, 150)));

        ProgressBar progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(Math.max(0, Math.min(progress, 100)));
        card.addView(progressBar, topParams(-1, dp(context, 8), dp(context, 10)));

        TextView percent = text(context, progress + "%", 12, true, Color.rgb(34, 197, 94));
        percent.setGravity(Gravity.END);
        card.addView(percent);
        return card;
    }

    private static LinearLayout card(Context context) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(context, 16), dp(context, 14), dp(context, 16), dp(context, 14));
        card.setBackgroundResource(R.drawable.bg_card);
        card.setLayoutParams(topParams(-1, -2, dp(context, 12)));
        return card;
    }

    private static TextView text(Context context, String value, int sp, boolean bold, int color) {
        TextView textView = new TextView(context);
        textView.setText(value);
        textView.setTextSize(sp);
        textView.setTextColor(color);
        if (bold) {
            textView.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        return textView;
    }

    private static LinearLayout.LayoutParams topParams(int width, int height, int top) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.setMargins(0, top, 0, 0);
        return params;
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}

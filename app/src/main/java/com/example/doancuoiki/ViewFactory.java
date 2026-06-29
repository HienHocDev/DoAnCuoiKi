package com.example.doancuoiki;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

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

    public static View projectCard(Context context, String name, String subtitle, String dateText,
                                   String taskText, String statusText, int progress,
                                   List<String> memberNames, View.OnClickListener listener) {
        LinearLayout card = card(context);
        card.setPadding(dp(context, 16), dp(context, 15), dp(context, 16), dp(context, 15));
        card.setOnClickListener(listener);

        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout textColumn = new LinearLayout(context);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        textColumn.addView(text(context, name, 16, true, Color.rgb(34, 38, 50)));
        textColumn.addView(text(context, subtitle, 12, false, Color.rgb(125, 132, 150)));
        header.addView(textColumn, new LinearLayout.LayoutParams(0, -2, 1));

        TextView status = text(context, statusText, 12, true, statusColor(statusText));
        status.setGravity(Gravity.CENTER);
        status.setBackground(makeRoundBg(statusBgColor(statusText), dp(context, 16)));
        header.addView(status, new LinearLayout.LayoutParams(dp(context, 92), dp(context, 32)));
        card.addView(header);

        card.addView(text(context, "Hạn chót: " + dateText, 12, false, Color.rgb(125, 132, 150)),
                topParams(-1, -2, dp(context, 10)));
        card.addView(text(context, "Công việc: " + taskText, 12, true, Color.rgb(34, 38, 50)),
                topParams(-1, -2, dp(context, 6)));

        LinearLayout bottom = new LinearLayout(context);
        bottom.setOrientation(LinearLayout.HORIZONTAL);
        bottom.setGravity(Gravity.CENTER_VERTICAL);
        bottom.addView(avatarStack(context, memberNames), new LinearLayout.LayoutParams(0, dp(context, 34), 1));

        TextView percent = text(context, progress + "%", 12, true, Color.rgb(34, 197, 94));
        percent.setGravity(Gravity.CENTER);
        percent.setBackgroundResource(R.drawable.bg_soft_primary);
        bottom.addView(percent, new LinearLayout.LayoutParams(dp(context, 54), dp(context, 30)));
        card.addView(bottom, topParams(-1, dp(context, 34), dp(context, 10)));

        ProgressBar progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(Math.max(0, Math.min(progress, 100)));
        try {
            progressBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.custom_progress_bar, context.getTheme()));
        } catch (Exception e) {
            progressBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.custom_progress_bar));
        }
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

        View subtitleView = text(context, subtitle, 12, false, Color.rgb(125, 132, 150));
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(-1, -2);
        subtitleParams.topMargin = dp(context, 6);
        textColumn.addView(subtitleView, subtitleParams);

        header.addView(textColumn, new LinearLayout.LayoutParams(0, -2, 1));

        TextView percent = text(context, progress + "%", 14, true, Color.rgb(34, 197, 94));
        percent.setGravity(Gravity.CENTER);
        percent.setBackgroundResource(R.drawable.bg_soft_primary);
        header.addView(percent, new LinearLayout.LayoutParams(dp(context, 58), dp(context, 34)));
        card.addView(header);

        ProgressBar progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(Math.max(0, Math.min(progress, 100)));

        try {
            progressBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.custom_progress_bar, context.getTheme()));
        } catch (Exception e) {
            progressBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.custom_progress_bar));
        }

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

    public static View avatarStack(Context context, List<String> names) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int count = Math.min(names == null ? 0 : names.size(), 4);
        if (count == 0) {
            row.addView(text(context, "Chưa có thành viên", 12, false, Color.rgb(125, 132, 150)));
            return row;
        }
        for (int i = 0; i < count; i++) {
            TextView avatar = text(context, initials(names.get(i)), 12, true, Color.WHITE);
            avatar.setGravity(Gravity.CENTER);
            avatar.setBackground(makeRoundBg(avatarColor(i), dp(context, 28)));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(context, 32), dp(context, 32));
            if (i > 0) {
                params.leftMargin = -dp(context, 8);
            }
            row.addView(avatar, params);
        }
        if (names != null && names.size() > count) {
            TextView more = text(context, "+" + (names.size() - count), 12, true, Color.rgb(34, 38, 50));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
            params.leftMargin = dp(context, 8);
            row.addView(more, params);
        }
        return row;
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

    private static GradientDrawable makeRoundBg(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private static int statusColor(String status) {
        if ("Đã hoàn thành".equals(status)) {
            return Color.rgb(33, 181, 127);
        }
        if ("Chậm tiến độ".equals(status)) {
            return Color.rgb(231, 76, 60);
        }
        if ("Tạm dừng".equals(status)) {
            return Color.rgb(239, 173, 68);
        }
        return Color.rgb(34, 197, 94);
    }

    private static int statusBgColor(String status) {
        if ("Đã hoàn thành".equals(status)) {
            return Color.rgb(220, 252, 231);
        }
        if ("Chậm tiến độ".equals(status)) {
            return Color.rgb(254, 226, 226);
        }
        if ("Tạm dừng".equals(status)) {
            return Color.rgb(254, 249, 195);
        }
        return Color.rgb(220, 252, 231);
    }

    private static int avatarColor(int index) {
        int[] colors = {
                Color.rgb(34, 197, 94),
                Color.rgb(20, 184, 166),
                Color.rgb(59, 130, 246),
                Color.rgb(168, 85, 247)
        };
        return colors[index % colors.length];
    }

    private static String initials(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "?";
        }
        String[] parts = value.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        if (parts.length == 1) {
            return first.toUpperCase();
        }
        return (first + parts[parts.length - 1].substring(0, 1)).toUpperCase();
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

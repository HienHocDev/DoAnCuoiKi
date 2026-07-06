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
        
        // Icon Box
        LinearLayout iconBox = new LinearLayout(context);
        iconBox.setGravity(Gravity.CENTER);
        iconBox.setBackground(makeRoundBg(Color.parseColor("#E6F9ED"), dp(context, 16)));
        TextView icon = text(context, "🚀", 24, true, Color.parseColor("#15B759"));
        iconBox.addView(icon);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(context, 56), dp(context, 56));
        iconParams.rightMargin = dp(context, 16);
        header.addView(iconBox, iconParams);

        LinearLayout textColumn = new LinearLayout(context);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        textColumn.addView(text(context, name, 16, true, Color.rgb(34, 38, 50)));

        View subtitleView = text(context, subtitle, 12, false, Color.rgb(125, 132, 150));
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(-1, -2);
        subtitleParams.topMargin = dp(context, 6);
        textColumn.addView(subtitleView, subtitleParams);

        header.addView(textColumn, new LinearLayout.LayoutParams(0, -2, 1));

        TextView percent = text(context, progress + "%", 12, true, Color.parseColor("#15B759"));
        percent.setGravity(Gravity.CENTER);
        percent.setBackground(makeRoundBg(Color.parseColor("#E6F9ED"), dp(context, 24)));
        header.addView(percent, new LinearLayout.LayoutParams(dp(context, 48), dp(context, 48)));
        card.addView(header);

        LinearLayout bottomInfo = new LinearLayout(context);
        bottomInfo.setOrientation(LinearLayout.HORIZONTAL);
        bottomInfo.setGravity(Gravity.CENTER_VERTICAL);
        
        ProgressBar progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(Math.max(0, Math.min(progress, 100)));
        try {
            progressBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.custom_progress_bar, context.getTheme()));
        } catch (Exception e) {
            progressBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.custom_progress_bar));
        }
        bottomInfo.addView(progressBar, new LinearLayout.LayoutParams(0, dp(context, 6), 1));
        
        TextView memberCount = text(context, " 3 thành viên", 11, false, Color.rgb(125, 132, 150));
        LinearLayout.LayoutParams memberParams = new LinearLayout.LayoutParams(-2, -2);
        memberParams.leftMargin = dp(context, 12);
        bottomInfo.addView(memberCount, memberParams);

        card.addView(bottomInfo, topParams(-1, dp(context, 16), dp(context, 12)));

        return card;
    }

    
    public static View detailedTaskCard(Context context, String title, String subtitle, String date, String priority, String status, int badgeBackground, int badgeColor) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(context, 16), dp(context, 16), dp(context, 16), dp(context, 16));
        row.setBackgroundResource(R.drawable.bg_card);
        row.setLayoutParams(topParams(-1, -2, dp(context, 12)));

        // Icon Box
        LinearLayout iconBox = new LinearLayout(context);
        iconBox.setGravity(Gravity.CENTER);
        
        String iconText = "🚀";
        int boxBgColor = Color.parseColor("#E6F9ED"); // Light green
        if (title.toLowerCase().contains("code") || title.toLowerCase().contains("refactor")) {
            iconText = "</>";
            boxBgColor = Color.parseColor("#E0E7FF"); // Light indigo
        } else if (title.toLowerCase().contains("calendar") || title.toLowerCase().contains("lịch")) {
            iconText = "📅";
            boxBgColor = Color.parseColor("#FEF3C7"); // Light amber
        }
        
        iconBox.setBackground(makeRoundBg(boxBgColor, dp(context, 12)));
        TextView icon = text(context, iconText, 20, true, Color.parseColor("#15B759"));
        if (iconText.equals("</>")) icon.setTextColor(Color.parseColor("#4F46E5")); // Indigo
        if (iconText.equals("📅")) icon.setTextColor(Color.parseColor("#F59E0B")); // Amber
        iconBox.addView(icon);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(context, 48), dp(context, 48));
        iconParams.rightMargin = dp(context, 16);
        row.addView(iconBox, iconParams);

        // Center Column (Title, Subtitle, Date/Priority)
        LinearLayout centerColumn = new LinearLayout(context);
        centerColumn.setOrientation(LinearLayout.VERTICAL);
        
        centerColumn.addView(text(context, title, 15, true, Color.rgb(34, 38, 50)));
        TextView sub = text(context, subtitle, 12, false, Color.rgb(125, 132, 150));
        sub.setPadding(0, dp(context, 4), 0, dp(context, 8));
        centerColumn.addView(sub);
        
        // Date & Priority row
        LinearLayout datePriorityRow = new LinearLayout(context);
        datePriorityRow.setOrientation(LinearLayout.HORIZONTAL);
        datePriorityRow.addView(text(context, "📅 " + date, 12, false, Color.rgb(125, 132, 150)));
        
        TextView separator = text(context, "  |  ", 12, false, Color.rgb(200, 200, 200));
        datePriorityRow.addView(separator);
        
        String pIcon = "🚩 ";
        int pColor = Color.parseColor("#7D8496");
        if ("Cao".equalsIgnoreCase(priority) || "High".equalsIgnoreCase(priority)) pColor = Color.parseColor("#EF4444");
        else if ("Trung bình".equalsIgnoreCase(priority) || "Medium".equalsIgnoreCase(priority)) pColor = Color.parseColor("#F59E0B");
        else if ("Thấp".equalsIgnoreCase(priority) || "Low".equalsIgnoreCase(priority)) pColor = Color.parseColor("#3B82F6");
        
        datePriorityRow.addView(text(context, pIcon + priority, 12, true, pColor));
        centerColumn.addView(datePriorityRow);
        
        row.addView(centerColumn, new LinearLayout.LayoutParams(0, -2, 1));

        // Right side (Badge + Chevron)
        LinearLayout rightColumn = new LinearLayout(context);
        rightColumn.setOrientation(LinearLayout.HORIZONTAL);
        rightColumn.setGravity(Gravity.CENTER_VERTICAL);
        
        TextView badge = text(context, status, 12, true, badgeColor);
        badge.setGravity(Gravity.CENTER);
        badge.setBackgroundResource(badgeBackground);
        badge.setPadding(dp(context, 12), dp(context, 6), dp(context, 12), dp(context, 6));
        
        // Add icon to badge based on status
        if (status.toLowerCase().contains("xong") || status.toLowerCase().contains("hoàn thành")) {
            badge.setText(status + " ✓");
        } else if (status.toLowerCase().contains("đang làm") || status.toLowerCase().contains("progress")) {
            badge.setText(status + " 🔄");
        }
        
        rightColumn.addView(badge, new LinearLayout.LayoutParams(-2, -2));
        
        
        
        row.addView(rightColumn, new LinearLayout.LayoutParams(-2, -2));
        
        return row;
    }
    
    public static View taskCard(Context context, String title, String subtitle, String status, int badgeBackground, int badgeColor) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(context, 14), dp(context, 14), dp(context, 14), dp(context, 14));
        row.setBackgroundResource(R.drawable.bg_card); // Assuming bg_card is a white rounded rectangle
        row.setLayoutParams(topParams(-1, -2, dp(context, 12)));

        // Icon Box
        LinearLayout iconBox = new LinearLayout(context);
        iconBox.setGravity(Gravity.CENTER);
        iconBox.setBackground(makeRoundBg(Color.parseColor("#E6F9ED"), dp(context, 12)));
        TextView icon = text(context, "📅", 20, true, Color.parseColor("#15B759"));
        iconBox.addView(icon);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(context, 48), dp(context, 48));
        iconParams.rightMargin = dp(context, 12);
        row.addView(iconBox, iconParams);

        LinearLayout textColumn = new LinearLayout(context);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        textColumn.addView(text(context, title, 15, true, Color.rgb(34, 38, 50)));
        textColumn.addView(text(context, subtitle, 12, false, Color.rgb(125, 132, 150)));
        row.addView(textColumn, new LinearLayout.LayoutParams(0, -2, 1));

        TextView badge = text(context, status, 12, true, badgeColor);
        badge.setGravity(Gravity.CENTER);
        badge.setBackgroundResource(badgeBackground);
        // Custom padding for badge to look like design
        badge.setPadding(dp(context, 12), dp(context, 6), dp(context, 12), dp(context, 6));
        row.addView(badge, new LinearLayout.LayoutParams(-2, -2));
        
        return row;
    }

    public static View notificationCard(Context context, String title, String body, String time) {
        LinearLayout card = card(context);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        
        // Icon container
        LinearLayout iconBox = new LinearLayout(context);
        iconBox.setGravity(Gravity.CENTER);
        iconBox.setBackground(makeRoundBg(Color.parseColor("#E6F9ED"), dp(context, 12))); // Light green box
        
        TextView icon = text(context, "Activity".equals(time) ? "✓" : "🔔", 20, true, Color.parseColor("#15B759"));
        if (title.contains("bình luận") || title.contains("💬")) {
            iconBox.setBackground(makeRoundBg(Color.parseColor("#E0F2FE"), dp(context, 12))); // Light blue box
            icon.setText("💬");
        }
        
        iconBox.addView(icon);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(context, 48), dp(context, 48));
        iconParams.rightMargin = dp(context, 12);
        card.addView(iconBox, iconParams);
        
        // Text container
        LinearLayout textColumn = new LinearLayout(context);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        
        // Clean up title
        String displayTitle = title.replace("[ ] ", "").replace("💬 ", "").replace("✅ ", "");
        
        LinearLayout titleRow = new LinearLayout(context);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.addView(text(context, displayTitle, 15, true, Color.rgb(34, 38, 50)), new LinearLayout.LayoutParams(0, -2, 1));
        
        if ("Activity".equals(time) || "Task".equals(time)) {
             TextView tag = text(context, time, 10, true, Color.parseColor("#15B759"));
             tag.setBackground(makeRoundBg(Color.parseColor("#E6F9ED"), dp(context, 6)));
             tag.setPadding(dp(context, 6), dp(context, 2), dp(context, 6), dp(context, 2));
             titleRow.addView(tag);
        } else {
             titleRow.addView(text(context, time, 12, false, Color.rgb(170, 176, 190)));
        }
        textColumn.addView(titleRow, new LinearLayout.LayoutParams(-1, -2));
        textColumn.addView(text(context, body, 13, false, Color.rgb(125, 132, 150)));
        
        card.addView(textColumn, new LinearLayout.LayoutParams(-1, -2));
        
        return card;
    }

    public static View avatarStack(Context context, List<String> names) {
        return avatarStack(context, names, false, null);
    }
    
    public static View avatarStack(Context context, List<String> names, boolean showAdd, View.OnClickListener onAdd) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int count = Math.min(names == null ? 0 : names.size(), 4);
        
        for (int i = 0; i < count; i++) {
            TextView avatar = text(context, initials(names.get(i)), 12, true, Color.WHITE);
            avatar.setGravity(Gravity.CENTER);
            avatar.setBackground(makeRoundBg(avatarColor(i), dp(context, 100)));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(context, 32), dp(context, 32));
            if (i > 0) {
                params.leftMargin = -dp(context, 8);
            }
            row.addView(avatar, params);
        }
        
        if (showAdd) {
            TextView addBtn = text(context, "+", 18, false, Color.parseColor("#7D8496"));
            addBtn.setGravity(Gravity.CENTER);
            // We use a simple circular background for the + button, or just a round bg
            addBtn.setBackground(makeRoundBg(Color.parseColor("#F8FAFC"), dp(context, 100)));
            LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(dp(context, 32), dp(context, 32));
            if (count > 0) {
                addParams.leftMargin = dp(context, 8);
            }
            addBtn.setOnClickListener(onAdd);
            row.addView(addBtn, addParams);
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

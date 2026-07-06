package com.example.doancuoiki;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public final class NavigationUtils {
    public static final String HOME = "home";
    public static final String PROJECTS = "projects";
    public static final String TASKS = "tasks";
    public static final String CALENDAR = "calendar";
    public static final String ACCOUNT = "account";

    private NavigationUtils() {
    }

    public static void setupBottomNav(Activity activity, String selectedTab) {
        setupItem(activity, R.id.navHome, R.id.iconHome, R.id.textHome, HOME, selectedTab, HomeActivity.class);
        setupItem(activity, R.id.navProjects, R.id.iconProjects, R.id.textProjects, PROJECTS, selectedTab, ProjectsActivity.class);
        setupItem(activity, R.id.navTasks, R.id.iconTasks, R.id.textTasks, TASKS, selectedTab, TasksActivity.class);
        setupItem(activity, R.id.navCalendar, R.id.iconCalendar, R.id.textCalendar, CALENDAR, selectedTab, CalendarActivity.class);
        setupItem(activity, R.id.navAccount, R.id.iconAccount, R.id.textAccount, ACCOUNT, selectedTab, AccountActivity.class);
    }

    public static void open(Activity activity, Class<?> target) {
        activity.startActivity(new Intent(activity, target));
    }

    public static void openAndFinish(Activity activity, Class<?> target) {
        Intent intent = new Intent(activity, target);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void showDeveloping(Activity activity, String featureName) {
        com.example.doancuoiki.utils.DialogUtils.showSuccessNotification(activity, featureName + " đang phát triển");
    }

    public static void showMessage(Activity activity, String message) {
        com.example.doancuoiki.utils.DialogUtils.showSuccessNotification(activity, message);
    }

    private static void setupItem(Activity activity, int viewId, int iconId, int labelId,
                                  String tab, String selectedTab, Class<?> target) {
        View item = activity.findViewById(viewId);
        if (item == null) {
            return;
        }

        boolean selected = tab.equals(selectedTab);
        int color = selected ? Color.parseColor("#15B759") : Color.rgb(125, 132, 150);
        item.setBackgroundResource(selected ? R.drawable.bg_nav_active : 0);

        ImageView icon = activity.findViewById(iconId);
        if (icon != null) {
            icon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }

        TextView label = activity.findViewById(labelId);
        if (label != null) {
            label.setTextColor(color);
            label.setTypeface(null, selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }

        item.setOnClickListener(v -> {
            if (!selected) {
                open(activity, target);
            }
        });
    }
}

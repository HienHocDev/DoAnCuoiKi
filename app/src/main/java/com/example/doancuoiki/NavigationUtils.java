package com.example.doancuoiki;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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
        setupItem(activity, R.id.navHome, HOME, selectedTab, HomeActivity.class);
        setupItem(activity, R.id.navProjects, PROJECTS, selectedTab, ProjectsActivity.class);
        setupItem(activity, R.id.navTasks, TASKS, selectedTab, TasksActivity.class);
        setupItem(activity, R.id.navCalendar, CALENDAR, selectedTab, CalendarActivity.class);
        setupItem(activity, R.id.navAccount, ACCOUNT, selectedTab, AccountActivity.class);
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
        Toast.makeText(activity, featureName + " đang phát triển", Toast.LENGTH_SHORT).show();
    }

    public static void showMessage(Activity activity, String message) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }

    private static void setupItem(Activity activity, int viewId, String tab, String selectedTab, Class<?> target) {
        TextView item = activity.findViewById(viewId);
        if (item == null) {
            return;
        }

        boolean selected = tab.equals(selectedTab);
        item.setTextColor(selected ? Color.rgb(93, 95, 239) : Color.rgb(125, 132, 150));
        item.setTypeface(null, selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        item.setOnClickListener(v -> {
            if (!selected) {
                open(activity, target);
            }
        });
    }
}

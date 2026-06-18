package com.example.doancuoiki;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements TaskFlowNavigator {
    private FrameLayout root;
    private LinearLayout content;
    private LinearLayout bottomBar;
    private String currentTab = "home";

    private final LoginScreen loginScreen = new LoginScreen();
    private final HomeScreen homeScreen = new HomeScreen();
    private final ProjectsScreen projectsScreen = new ProjectsScreen();
    private final TasksScreen tasksScreen = new TasksScreen();
    private final CalendarScreen calendarScreen = new CalendarScreen();
    private final AccountScreen accountScreen = new AccountScreen();
    private final ProjectExtraScreens extraScreens = new ProjectExtraScreens();

    // Khai báo thêm màn hình Đăng ký
    private final RegisterScreen registerScreen = new RegisterScreen();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        showLogin();
    }

    // THỰC HIỆN HÀM CHUYỂN SANG MÀN HÌNH ĐĂNG KÝ THẬT
    @Override
    public void showRegister() {
        setContentView(registerScreen.create(this, this));
    }

    @Override
    public void showLogin() {
        setContentView(loginScreen.create(this, this));
    }

    @Override
    public void showHome() {
        showMainTab("home");
    }

    @Override
    public void showProjects() {
        showMainTab("projects");
    }

    @Override
    public void showTasks() {
        showMainTab("tasks");
    }

    @Override
    public void showCalendar() {
        showMainTab("calendar");
    }

    @Override
    public void showAccount() {
        showMainTab("account");
    }

    @Override
    public void showAddProject() {
        showInsideShell(extraScreens.addProject(this, this));
    }

    @Override
    public void showProjectDetail() {
        showInsideShell(extraScreens.projectDetail(this, this));
    }

    @Override
    public void showReport() {
        showInsideShell(extraScreens.report(this, this));
    }

    @Override
    public void showPerformance() {
        showInsideShell(extraScreens.performance(this, this));
    }

    @Override
    public void showNotifications() {
        showInsideShell(extraScreens.notifications(this, this));
    }

    @Override
    public void underDevelopment(String featureName) {
        Toast.makeText(this, featureName + " đang phát triển", Toast.LENGTH_SHORT).show();
    }

    private void showMainTab(String tab) {
        currentTab = tab;
        ensureShell();
        content.removeAllViews();

        if ("home".equals(tab)) {
            content.addView(homeScreen.create(this, this));
        } else if ("projects".equals(tab)) {
            content.addView(projectsScreen.create(this, this));
        } else if ("tasks".equals(tab)) {
            content.addView(tasksScreen.create(this, this));
        } else if ("calendar".equals(tab)) {
            content.addView(calendarScreen.create(this, this));
        } else {
            content.addView(accountScreen.create(this, this));
        }

        renderBottomBar();
    }

    private void showInsideShell(View screen) {
        ensureShell();
        content.removeAllViews();
        content.addView(screen);
        renderBottomBar();
    }

    private void ensureShell() {
        if (root != null && content != null && bottomBar != null) {
            return;
        }

        root = new FrameLayout(this);
        root.setBackgroundColor(UiKit.BACKGROUND);

        LinearLayout shell = UiKit.vertical(this);
        content = UiKit.vertical(this);
        shell.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));

        bottomBar = UiKit.horizontal(this);
        bottomBar.setGravity(Gravity.CENTER);
        bottomBar.setPadding(UiKit.dp(this, 4), UiKit.dp(this, 6), UiKit.dp(this, 4), UiKit.dp(this, 8));
        bottomBar.setBackgroundColor(android.graphics.Color.WHITE);
        shell.addView(bottomBar, new LinearLayout.LayoutParams(-1, UiKit.dp(this, 72)));

        root.addView(shell);
        setContentView(root);
    }

    private void renderBottomBar() {
        bottomBar.removeAllViews();
        addNavItem("home", "Trang chủ");
        addNavItem("projects", "Dự án");
        addNavItem("tasks", "Công việc");
        addNavItem("calendar", "Lịch");
        addNavItem("account", "Tài khoản");
    }

    private void addNavItem(String tab, String label) {
        boolean selected = tab.equals(currentTab);
        TextView item = UiKit.text(this, label, 12, selected ? UiKit.PRIMARY : UiKit.MUTED, selected);
        item.setGravity(Gravity.CENTER);
        item.setOnClickListener(v -> showMainTab(tab));
        bottomBar.addView(item, new LinearLayout.LayoutParams(0, -1, 1));
    }
}
package com.example.doancuoiki;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
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
        NavigationUtils.open(this, LoginActivity.class);
        finish();
    }
}

package cz.llinek.kalamari;

import static cz.llinek.kalamari.Controller.dpToPx;
import static cz.llinek.kalamari.Controller.login;
import static cz.llinek.kalamari.Controller.onActivityStart;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends Activity {

    private void basicScreen() {
        Toast.makeText(this, "afterlogin", Toast.LENGTH_SHORT).show();
        LinearLayout vBox = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 0);
        vBox.setOrientation(LinearLayout.VERTICAL);
        vBox.setLayoutParams(params);
        vBox.setPadding(0, 0, 0, 0);
        this.setContentView(vBox, params);
        MaterialButton logout = new MaterialButton(this);
        MaterialButton permanentTimetable = new MaterialButton(this);
        MaterialButton marks = new MaterialButton(this);
        logout.setMinHeight(dpToPx(this, Constants.DASHBOARD_BUTTON_MIN_HEIGHT));
        logout.setText("Logout");
        logout.setPadding(0, 0, 0, 0);
        logout.setLayoutParams(params);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Controller.logout();
                startActivity(new Intent(getApplicationContext(), LoginScreen.class));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Logout successful", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        permanentTimetable.setMinHeight(dpToPx(this, Constants.DASHBOARD_BUTTON_MIN_HEIGHT));
        permanentTimetable.setText("Permanent Timetable");
        permanentTimetable.setPadding(0, 0, 0, 0);
        permanentTimetable.setLayoutParams(params);
        permanentTimetable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Timetable.class));
            }
        });
        marks.setMinHeight(dpToPx(this, Constants.DASHBOARD_BUTTON_MIN_HEIGHT));
        marks.setPadding(0, 0, 0, 0);
        marks.setText("Marks");
        marks.setLayoutParams(params);
        vBox.addView(logout);
        vBox.addView(permanentTimetable);
        vBox.addView(marks);
    }

    @Override
    protected void onResume() {
        login(this, new Runnable() {
            @Override
            public void run() {
                basicScreen();
            }
        });
        super.onResume();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        FileManager.filesDir = getFilesDir();
        super.onCreate(savedInstanceState);
        onActivityStart();
        login(this, new Runnable() {
            @Override
            public void run() {
                basicScreen();
            }
        });
    }
}
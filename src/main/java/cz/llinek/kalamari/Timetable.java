package cz.llinek.kalamari;

import static cz.llinek.kalamari.Controller.updateTimetable;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.view.menu.MenuBuilder;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import cz.llinek.kalamari.dataTypes.Hour;

public class Timetable extends Activity {
    private void showEmptyTimetable() {
        LinearLayout contentView = new LinearLayout(this);
        LinearLayout toolbar = new LinearLayout(this);
        TextView emptyText = new TextView(this);
        ImageButton reload = new ImageButton(this);
        contentView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        contentView.setOrientation(LinearLayout.VERTICAL);
        contentView.addView(toolbar);
        contentView.addView(emptyText);
        toolbar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        toolbar.setBackgroundColor(Color.BLUE);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.addView(reload);
        emptyText.setText("Empty timetable");
        emptyText.setGravity(Gravity.CENTER);
        emptyText.setTextColor(getResources().getColor(R.color.element_background));
        emptyText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        emptyText.setBackgroundColor(getResources().getColor(R.color.black));
        reload.setBackgroundResource(R.drawable.outline_refresh_24);
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateTimetable(getApplicationContext());
                showTimetable();
            }
        });
        setContentView(contentView);
    }

    private void showTimetable() {
        try {
            Hour[][] timetable = Controller.parsePermanentHours(this);
            LinearLayout contentView = new LinearLayout(this);
            MaterialToolbar toolbar = new MaterialToolbar(this);
            @SuppressLint("RestrictedApi") MenuBuilder menu = new MenuBuilder(this);
            LinearLayout timetableView = new LinearLayout(this);
            ImageButton reloadButton = new ImageButton(this);
            ImageButton backButton = new ImageButton(this);
            contentView.setOrientation(LinearLayout.VERTICAL);
            contentView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            contentView.addView(toolbar);
            MaterialButton butt;
            contentView.addView(timetableView);
            toolbar.setNavigationIcon(R.drawable.outline_arrow_back_24);
            toolbar.setNavigationOnClickListener(v -> finish());

            toolbar.addView(reloadButton);
            toolbar.setTitle("Timetable");
            @SuppressLint("RestrictedApi") MenuItem reload = menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "");
            reload.setIcon(R.drawable.outline_refresh_24);
            reload.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    updateTimetable(getApplicationContext());
                    showTimetable();
                    return true;
                }
            });
            MaterialToolbar.LayoutParams backLayoutParams = new MaterialToolbar.LayoutParams(MaterialToolbar.LayoutParams.WRAP_CONTENT, MaterialToolbar.LayoutParams.WRAP_CONTENT);
            timetableView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            timetableView.setOrientation(LinearLayout.VERTICAL);
            reloadButton.setBackgroundResource(R.drawable.outline_refresh_24);
            reloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateTimetable(getApplicationContext());
                    showTimetable();
                }
            });
            backLayoutParams.gravity = Gravity.RIGHT;
            backButton.setBackgroundResource(R.drawable.outline_arrow_back_24);
            backButton.setLayoutParams(backLayoutParams);
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
            for (Hour[] row : timetable) {
                LinearLayout daybox = new LinearLayout(this);
                daybox.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                daybox.setOrientation(LinearLayout.HORIZONTAL);
                timetableView.addView(daybox);
                /*for (Hour hour : row) {
                    if (hour == null) {
                        Button hourButton = new Button(this);
                        hourButton.setBackgroundColor(getResources().getColor(R.color.black));
                        continue;
                    }
                    Button hourButton = new Button(this);
                    hourButton.setBackgroundColor(getResources().getColor(R.color.element_background));
                    hourButton.setText(getSubjectById(this, hour.getHourId()).getAbbrev());
                    hourButton.setTextSize(Constants.SUBJECT_TEXT_SIZE);
                    hourButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    hourButton.setMinHeight(dpToPx(this, Constants.TIMETABLE_CELL_DP));
                    hourButton.setMinWidth(dpToPx(this, Constants.TIMETABLE_CELL_DP));
                    if (hour.getChange() == null) {
                        hourButton.setBackgroundColor(getResources().getColor(R.color.change));
                    }
                    daybox.addView(hourButton);
                }*/
                for (Hour hour : row) {
                    daybox.addView(hour.getView());
                }
            }
            setContentView(contentView);
        } catch (NullPointerException e) {
            showEmptyTimetable();
            return;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showTimetable();
    }
}

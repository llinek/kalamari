package cz.llinek.kalamari;

import static cz.llinek.kalamari.Controller.getSubjectById;
import static cz.llinek.kalamari.Controller.updateTimetable;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

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
        reload.setBackgroundResource(R.drawable.ic_baseline_refresh_24);
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
            Hour[][] timetable = Controller.parseHours(this);
            LinearLayout contentView = new LinearLayout(this);
            LinearLayout toolbar = new LinearLayout(this);
            LinearLayout timetableView = new LinearLayout(this);
            ImageButton reloadButton = new ImageButton(this);
            ImageButton backButton = new ImageButton(this);
            contentView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            contentView.setOrientation(LinearLayout.VERTICAL);
            contentView.addView(toolbar);
            contentView.addView(timetableView);
            toolbar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            toolbar.setBackgroundColor(getResources().getColor(R.color.element_background));
            toolbar.setOrientation(LinearLayout.HORIZONTAL);
            toolbar.addView(backButton);
            toolbar.addView(reloadButton);
            toolbar.setGravity(Gravity.END);
            LinearLayout.LayoutParams backLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            timetableView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            timetableView.setOrientation(LinearLayout.VERTICAL);
            timetableView.setBackgroundColor(getResources().getColor(R.color.black));
            reloadButton.setBackgroundResource(R.drawable.ic_baseline_refresh_24);
            reloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateTimetable(getApplicationContext());
                    showTimetable();
                }
            });
            backLayoutParams.gravity = Gravity.LEFT;
            backButton.setBackgroundResource(R.drawable.outline_arrow_back_24);
            backButton.setLayoutParams(backLayoutParams);
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
            for (
                    Hour[] row : timetable) {
                LinearLayout daybox = new LinearLayout(this);
                daybox.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                daybox.setOrientation(LinearLayout.HORIZONTAL);
                contentView.addView(daybox);
                for (Hour hour : row) {
                    if (hour == null) {
                        Button hourButton = new Button(this);
                        hourButton.setBackgroundColor(getResources().getColor(R.color.black));
                        continue;
                    }
                Button hourButton = new Button(this);
                hourButton.setBackgroundColor(getResources().getColor(R.color.element_background));
                hourButton.setText(getSubjectById(this, hour.getId()).getAbbrev());
                    hourButton.setTextSize(Constants.SUBJECT_TEXT_SIZE);
                    hourButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    hourButton.setMinHeight(Constants.TIMETABLE_CELL_SIZE);
                    hourButton.setMinWidth(Constants.TIMETABLE_CELL_SIZE);
                    if (hour.getChange() == null) {
                        hourButton.setBackgroundColor(getResources().getColor(R.color.change));
                    }
                    daybox.addView(hourButton);
                }
            }

            setContentView(contentView);
        } catch (
                NullPointerException e) {
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

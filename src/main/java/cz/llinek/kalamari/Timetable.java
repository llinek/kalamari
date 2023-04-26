package cz.llinek.kalamari;

import static cz.llinek.kalamari.Controller.updateTimetable;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import cz.llinek.kalamari.dataTypes.Hour;

public class Timetable extends Activity {
    private void buildTimetable() {
        Hour[][] timetable = Controller.parseTimetable(this);
        LinearLayout contentView = new LinearLayout(this);
        LinearLayout toolbar = new LinearLayout(this);
        LinearLayout timetableView = new LinearLayout(this);
        ImageButton reload = new ImageButton(this);
        contentView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        contentView.setOrientation(LinearLayout.VERTICAL);
        contentView.addView(toolbar);
        contentView.addView(timetableView);
        toolbar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        toolbar.setBackgroundColor(Color.BLUE);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.addView(reload);
        timetableView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        timetableView.setOrientation(LinearLayout.VERTICAL);
        reload.setBackgroundResource(R.drawable.ic_baseline_refresh_24);
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateTimetable(getApplicationContext());
                buildTimetable();
            }
        });
        for (Hour[] row : timetable) {
            LinearLayout daybox = new LinearLayout(this);
            daybox.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            daybox.setOrientation(LinearLayout.HORIZONTAL);
            contentView.addView(daybox);
            for (Hour hour : row) {
                Button hourButton = new Button(this);
                !!continue here
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildTimetable();
    }
}

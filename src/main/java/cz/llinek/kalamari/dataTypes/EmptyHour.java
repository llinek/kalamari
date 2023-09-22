package cz.llinek.kalamari.dataTypes;

import static cz.llinek.kalamari.Controller.dpToPx;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import cz.llinek.kalamari.Constants;
import cz.llinek.kalamari.Controller;

public class EmptyHour {
    private Context context;

    public EmptyHour(Context context) {
        this.context = context;
    }

    public View getView() {
        FrameLayout layout = new FrameLayout(context);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.setMinimumWidth(Controller.dpToPx(context, Constants.TIMETABLE_CELL_DP));
        layout.setMinimumHeight(Controller.dpToPx(context, Constants.TIMETABLE_CELL_DP));
        layout.setPadding(dpToPx(context, Constants.TIMETABLE_CELL_PADDING_DP), dpToPx(context, Constants.TIMETABLE_CELL_PADDING_DP), dpToPx(context, Constants.TIMETABLE_CELL_PADDING_DP), dpToPx(context, Constants.TIMETABLE_CELL_PADDING_DP));
        return layout;
    }
}

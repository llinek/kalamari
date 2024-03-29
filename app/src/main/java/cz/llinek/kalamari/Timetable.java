package cz.llinek.kalamari;

import static cz.llinek.kalamari.Controller.updateActualTimetable;
import static cz.llinek.kalamari.Controller.updatePermanentTimetable;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

import androidx.appcompat.view.menu.MenuBuilder;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textview.MaterialTextView;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import cz.llinek.kalamari.dataTypes.EmptyHour;
import cz.llinek.kalamari.dataTypes.Hour;

public class Timetable extends Activity {
    private void showEmptyTimetable() {
        LinearLayout contentView = new LinearLayout(this);
        LinearLayout toolbar = new LinearLayout(this);
        MaterialTextView emptyText = new MaterialTextView(this);
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
                updatePermanentTimetable(getApplicationContext());
                showTimetable();
            }
        });
        setContentView(contentView);
    }

    private void onHourClicked(Hour hour) {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
        MaterialTextView descriptionView = new MaterialTextView(this);
        StringBuilder description = new StringBuilder();
        descriptionView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        try {
            description.append("Teacher: " + (hour.getTeacherName() == null ? hour.getTeacherAbbrev() : hour.getTeacherName()));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        try {
            description.append("\nTheme: " + hour.getTheme());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        try {
            description.append("\nGroup: " + (hour.getHumanGroupNames() == null ? hour.getHumanGroupAbbrevs() : hour.getHumanGroupNames()));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        try {
            description.append("\nChange Description: " + hour.getChange().getDescription());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        try {
            description.append("\nChange Subject: " + hour.getChange().getChangeSubject());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        try {
            description.append("\nChange Type: " + hour.getChange().getChangeType());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        try {
            description.append("\nChange Hours: " + hour.getChange().getHours());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        try {
            description.append("\nChange Text: " + hour.getChange().getTime());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        try {
            description.append("\nChange Day: " + hour.getChange().getDay().toString());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        try {
            description.append("\nChange Type Name: " + hour.getChange().getTypeName() == null ? hour.getChange().getTypeAbbrev() : hour.getChange().getTypeName());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        try {
            description.append("\nClass: " + hour.getClassName());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        descriptionView.setText(description.toString());
        dialogBuilder.setTitle((hour.getSubjectName() == null ? hour.getSubjectAbbrev() : hour.getSubjectName()) + "  " + hour.getBeginTime() + " - " + hour.getEndTime());
        dialogBuilder.setMessage(description.toString());
        dialogBuilder.setPositiveButton("Ok", null);
        dialogBuilder.show();
    }

    private View generatePermanentTimetable() {
        Hour[][] timetable = Controller.parsePermanentHours(this);
        HorizontalScrollView timetableScroll = new HorizontalScrollView(this);
        LinearLayout timetableView = new LinearLayout(this);
        timetableScroll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        timetableScroll.addView(timetableView);
        timetableView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        timetableView.setOrientation(LinearLayout.VERTICAL);
        for (Hour[] row : timetable) {
            LinearLayout daybox = new LinearLayout(this);
            daybox.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            daybox.setOrientation(LinearLayout.HORIZONTAL);
            timetableView.addView(daybox);
            for (Hour hour : row) {
                if (hour != null) {
                    View hourView = hour.getView();
                    hourView.setOnClickListener(v -> onHourClicked(hour));
                    daybox.addView(hourView);
                } else {
                    daybox.addView(new EmptyHour(this).getView());
                }
            }
        }
        return timetableScroll;
    }

    private View generateActualTimetable() {
        System.out.println("generate actual");
        Hour[][] timetable = Controller.parseActualHours(this, new Date());
        HorizontalScrollView timetableScroll = new HorizontalScrollView(this);
        LinearLayout timetableView = new LinearLayout(this);
        timetableScroll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        timetableScroll.addView(timetableView);
        timetableView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        timetableView.setOrientation(LinearLayout.VERTICAL);
        for (Hour[] row : timetable) {
            LinearLayout daybox = new LinearLayout(this);
            daybox.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            daybox.setOrientation(LinearLayout.HORIZONTAL);
            timetableView.addView(daybox);
            for (Hour hour : row) {
                if (hour != null) {
                    View hourView = hour.getView();
                    hourView.setOnClickListener(v -> onHourClicked(hour));
                    daybox.addView(hourView);
                } else {
                    daybox.addView(new EmptyHour(this).getView());
                }
            }
        }
        return timetableScroll;
    }

    private View generateLoadingTimetable() {
        FrameLayout layout = new FrameLayout(this);
        CircularProgressIndicator loading = new CircularProgressIndicator(this);
        layout.addView(loading);
        return layout;
    }
    private Thread getLoadPrimary(Runnable loadPrimary) {
        Thread thread = new Thread(loadPrimary);
        thread.setDaemon(true);
        return thread;
    }
    private Thread getLoadSecondary(Runnable loadSecondary) {
        Thread thread = new Thread(loadSecondary);
        thread.setDaemon(true);
        return thread;
    }
    private void showTimetable() {
        try {
            final boolean[] isPermLoaded = {false};
            final Boolean[] isTempLoaded = {false};
            final boolean[] isTempShown = {false};
            FrameLayout loadingView = new FrameLayout(this);
            FrameLayout.LayoutParams loadingParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout contentView = new LinearLayout(this);
            MaterialToolbar toolbar = new MaterialToolbar(this);
            MaterialButtonToggleGroup modeSwitch = new MaterialButtonToggleGroup(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            MaterialButton permanentButton = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            MaterialButton actualButton = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            @SuppressLint("RestrictedApi") MenuBuilder menu = new MenuBuilder(this);
            ImageButton reloadButton = new ImageButton(this);
            ImageButton backButton = new ImageButton(this);
            final View[] permanentTimetable = new View[1];
            final View[] actualTimetable = new View[1];
            loadingParams.gravity = Gravity.CENTER;
            CircularProgressIndicator loadingIndicator = new CircularProgressIndicator(this);
            loadingIndicator.setIndeterminate(true);
            loadingIndicator.setLayoutParams(loadingParams);
            loadingView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            loadingView.addView(loadingIndicator);
            Runnable loadPrimary = () -> {
                runOnUiThread(() -> {
                    if (!isTempShown[0] || !isTempLoaded[0]) {
                        contentView.removeViewAt(1);
                        contentView.addView(loadingView);
                    }
                });
                permanentTimetable[0] = generatePermanentTimetable();
                runOnUiThread(() -> {
                    if (!isTempShown[0]) {
                        contentView.removeViewAt(1);
                        contentView.addView(permanentTimetable[0]);
                    }
                    isPermLoaded[0] = true;
                });
            };
            Runnable loadSecondary = () -> {
                runOnUiThread(() -> {
                    if (isTempShown[0] || !isTempLoaded[0]) {
                        contentView.removeViewAt(1);
                        contentView.addView(loadingView);
                    }
                });
                actualTimetable[0] = generateActualTimetable();
                runOnUiThread(() -> {
                    if (isTempShown[0]) {
                        contentView.removeViewAt(1);
                        contentView.addView(actualTimetable[0]);
                    }
                    isTempLoaded[0] = true;
                });
            };
            contentView.setOrientation(LinearLayout.VERTICAL);
            contentView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            contentView.addView(toolbar);
            contentView.addView(loadingView);
            toolbar.setNavigationIcon(R.drawable.outline_arrow_back_24);
            toolbar.setNavigationOnClickListener(v -> finish());
            toolbar.addView(modeSwitch);
            toolbar.addView(reloadButton);
            toolbar.setTitle("Timetable");
            @SuppressLint("RestrictedApi") MenuItem reload = menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "");
            reload.setIcon(R.drawable.outline_refresh_24);
            reload.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (isTempShown[0]) {
                        updateActualTimetable(getApplicationContext(), new Date());
                        getLoadSecondary(loadSecondary).start();
                    } else {
                        updatePermanentTimetable(getApplicationContext());
                        getLoadPrimary(loadPrimary).start();
                    }
                    return true;
                }
            });
            reloadButton.setBackgroundResource(R.drawable.outline_refresh_24);
            reloadButton.setLayoutParams(new MaterialToolbar.LayoutParams(MaterialToolbar.LayoutParams.WRAP_CONTENT, MaterialToolbar.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL));
            reloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isTempShown[0]) {
                        updateActualTimetable(getApplicationContext(), new Date());
                        getLoadSecondary(loadSecondary).start();
                    } else {
                        updatePermanentTimetable(getApplicationContext());
                        getLoadPrimary(loadPrimary).start();
                    }
                }
            }); backButton.setBackgroundResource(R.drawable.outline_arrow_back_24);
            backButton.setLayoutParams(new MaterialToolbar.LayoutParams(MaterialToolbar.LayoutParams.WRAP_CONTENT, MaterialToolbar.LayoutParams.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL));
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
            permanentButton.setText("Permanent");
            actualButton.setText("Actual");
            modeSwitch.addView(permanentButton);
            modeSwitch.addView(actualButton);
            modeSwitch.setSingleSelection(true);
            modeSwitch.setSelectionRequired(true);
            modeSwitch.check(permanentButton.getId());
            modeSwitch.setLayoutParams(new MaterialToolbar.LayoutParams(MaterialToolbar.LayoutParams.WRAP_CONTENT, MaterialToolbar.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
            modeSwitch.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (modeSwitch.getCheckedButtonId() == permanentButton.getId()) {
                    isTempShown[0] = false;
                    if (!isPermLoaded[0]) {
                        getLoadPrimary(loadPrimary).start();
                    } else {
                        contentView.removeViewAt(1);
                        contentView.addView(permanentTimetable[0]);
                    }
                } else {
                    isTempShown[0] = true;
                    if (!isTempLoaded[0]) {
                        getLoadSecondary(loadSecondary).start();
                    } else {
                        contentView.removeViewAt(1);
                        contentView.addView(actualTimetable[0]);
                    }
                }
            });
            getLoadPrimary(loadPrimary).start();
            getLoadSecondary(loadSecondary).start();
            setContentView(contentView);
        } catch (NullPointerException e) {
            e.printStackTrace();
            showEmptyTimetable();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showTimetable();
    }
}

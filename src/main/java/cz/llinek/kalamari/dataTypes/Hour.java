package cz.llinek.kalamari.dataTypes;

import static cz.llinek.kalamari.Controller.dpToPx;
import static cz.llinek.kalamari.Controller.getTimestampFormatter;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.google.android.material.textview.MaterialTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

import cz.llinek.kalamari.Constants;
import cz.llinek.kalamari.Controller;
import cz.llinek.kalamari.FileManager;

public class Hour {
    private int hourId;
    private String[] groupIds;
    private String subjectId;
    private String subjectAbbrev;
    private String subjectName;
    private String teacherId;
    private String teacherAbbrev;
    private String teacherName;
    private String[] groupAbbrevs;
    private String[] groupNames;
    private String classId;
    private String classAbbrev;
    private String className;
    private String roomId;
    private String caption;
    private String beginTime;
    private String endTime;
    private String[] cycleIds;
    private Change change;
    private String theme;
    private String timetableFilename;
    private FrameLayout view;
    private Context context;

    public Hour(Context context) {
        /*this.hourId = -1;
        this.groupIds = new String[]{};
        this.subjectId = "";
        this.subjectAbbrev = "";
        this.subjectName = "";
        this.teacherId = "";
        this.teacherAbbrev = "";
        this.teacherName = "";
        this.groupAbbrev = "";
        this.groupName = "";
        this.classId = "";
        this.classAbbrev = "";
        this.className = "";
        this.roomId = "";
        this.cycleIds = new String[]{};
        this.change = null;
        this.theme = "";
        this.timetableFilename = "";*/
        this.context = context;
        updateView();
    }

    public Hour(@NonNull Context context, JSONObject hour, String timetableFilename) {
        try {
            String[] groupIds = new String[hour.getJSONArray("GroupIds").length()];
            String[] cycleIds = new String[hour.getJSONArray("CycleIds").length()];
            groupAbbrevs = new String[groupIds.length];
            groupNames = new String[groupIds.length];
            for (int k = 0; k < groupIds.length; k++) {
                groupIds[k] = hour.getJSONArray("GroupIds").getString(k);
            }
            for (int k = 0; k < cycleIds.length; k++) {
                cycleIds[k] = hour.getJSONArray("CycleIds").getString(k);
            }
            try {
                JSONObject c = hour.getJSONObject("Change");
                Change change = new Change(c.getString("ChangeSubject"), getTimestampFormatter().parse(c.getString("Day")), c.getString("Hours"), c.getString("ChangeType"), c.getString("Description"), c.getString("Time"), c.getString("TypeAbbrev"), c.getString("TypeName"));
                this.hourId = hour.getInt("HourId");
                this.groupIds = groupIds;
                this.teacherId = hour.getString("TeacherId");
                this.subjectId = hour.getString("SubjectId");
                this.roomId = hour.getString("RoomId");
                this.cycleIds = cycleIds;
                this.change = change;
                this.theme = hour.getString("Theme");
                this.timetableFilename = timetableFilename;
            } catch (JSONException e) {
                this.hourId = hour.getInt("HourId");
                this.groupIds = groupIds;
                this.teacherId = hour.getString("TeacherId");
                this.subjectId = hour.getString("SubjectId");
                this.roomId = hour.getString("RoomId");
                this.cycleIds = cycleIds;
                this.change = null;
                this.theme = hour.getString("Theme");
                this.timetableFilename = timetableFilename;
            } catch (ParseException e) {
                e.printStackTrace();
                System.err.println("\n\n\nchange err, fallback to timetable without changes\n\n\n\n\n" + e.getMessage());
                this.hourId = hour.getInt("HourId");
                this.groupIds = groupIds;
                this.teacherId = hour.getString("TeacherId");
                this.subjectId = hour.getString("SubjectId");
                this.roomId = hour.getString("RoomId");
                this.cycleIds = cycleIds;
                this.change = null;
                this.theme = hour.getString("Theme");
                this.timetableFilename = timetableFilename;
            }
            String timetable = FileManager.readFile(Constants.PERMANENT_TIMETABLE_FILENAME);
            JSONArray hours = new JSONObject(timetable).getJSONArray("Hours");
            JSONArray subjects = new JSONObject(timetable).getJSONArray("Subjects");
            JSONArray teachers = new JSONObject(timetable).getJSONArray("Teachers");
            JSONArray groups = new JSONObject(timetable).getJSONArray("Groups");
            JSONArray classes = new JSONObject(timetable).getJSONArray("Classes");
            for (int i = 0; i < subjects.length(); i++) {
                JSONObject subject = subjects.getJSONObject(i);
                if (subject.getString("Id").equals(subjectId)) {
                    subjectAbbrev = subject.getString("Abbrev");
                    subjectName = subject.getString("Name");
                    break;
                }
            }
            for (int i = 0; i < teachers.length(); i++) {
                JSONObject teacher = teachers.getJSONObject(i);
                if (teacher.getString("Id").equals(teacherId)) {
                    teacherAbbrev = teacher.getString("Abbrev");
                    teacherName = teacher.getString("Name");
                    break;
                }
            }
            for (int i = 0; i < groups.length(); i++) {
                JSONObject group = groups.getJSONObject(i);
                for (int j = 0; j < getGroupIds().length; j++) {
                    if (group.getString("Id").equals(getGroupIds()[j])) {
                        groupAbbrevs[j] = group.getString("Abbrev");
                        groupNames[j] = group.getString("Name");
                        classId = group.getString("ClassId");
                    }
                }
            }
            for (int i = 0; i < classes.length(); i++) {
                JSONObject clas = classes.getJSONObject(i);
                if (clas.getString("Id").equals(classId)) {
                    classAbbrev = clas.getString("Abbrev");
                    className = clas.getString("Name");
                }
            }
            for (int i = 0; i < hours.length(); i++) {
                JSONObject houri = hours.getJSONObject(i);
                if (houri.getInt("Id") == hourId) {
                    caption = houri.getString("Caption");
                    beginTime = houri.getString("BeginTime");
                    endTime = houri.getString("EndTime");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
        this.context = context;
        updateView();
    }

    private void updateView() {
        FrameLayout layout = new FrameLayout(context);
        MaterialTextView subject = new MaterialTextView(context);
        MaterialTextView teacher = new MaterialTextView(context);
        MaterialTextView group = new MaterialTextView(context);
        FrameLayout.LayoutParams subjectLayout = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        FrameLayout.LayoutParams teacherLayout = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        FrameLayout.LayoutParams groupLayout = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        layout.setOnClickListener(v -> {
            Controller.setClickedHour(this);
        });
        subjectLayout.gravity = Gravity.CENTER;
        teacherLayout.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        groupLayout.gravity = Gravity.LEFT | Gravity.BOTTOM;
        subject.setText(getSubjectAbbrev());
        subject.setTextSize(Controller.dpToPx(context, Constants.TIMETABLE_CELL_SUBJECT_DP));
        subject.setGravity(Gravity.CENTER);
        subject.setLayoutParams(subjectLayout);
        teacher.setText(getTeacherAbbrev());
        teacher.setTextSize(Controller.dpToPx(context, Constants.TIMETABLE_CELL_TEACHER_DP));
        teacher.setGravity(Gravity.CENTER);
        teacher.setLayoutParams(teacherLayout);
        if (getGroupAbbrevs() != null) {
            StringBuilder groupText = new StringBuilder();
            for (int i = 0; i < getGroupAbbrevs().length; i++) {
                groupText.append(getGroupAbbrevs()[i]);
                if (i + 1 < getGroupAbbrevs().length) {
                    groupText.append(", ");
                }
            }
            group.setText(groupText.toString());
        }
        group.setTextSize(Controller.dpToPx(context, Constants.TIMETABLE_CELL_GROUP_DP));
        group.setGravity(Gravity.CENTER);
        group.setLayoutParams(groupLayout);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        layout.setMinimumWidth(Controller.dpToPx(context, Constants.TIMETABLE_CELL_DP));
        layout.setMinimumHeight(Controller.dpToPx(context, Constants.TIMETABLE_CELL_DP));
        layout.setPadding(dpToPx(context, Constants.TIMETABLE_CELL_PADDING_DP), dpToPx(context, Constants.TIMETABLE_CELL_PADDING_DP), dpToPx(context, Constants.TIMETABLE_CELL_PADDING_DP), dpToPx(context, Constants.TIMETABLE_CELL_PADDING_DP));
        layout.addView(subject);
        layout.addView(teacher);
        layout.addView(group);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        view = layout;
    }

    public String getCaption() {
        return caption;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
        updateView();
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
        updateView();
    }

    public String getTimetableFilename() {
        return timetableFilename;
    }

    public String getSubjectAbbrev() {
        return subjectAbbrev;
    }

    public void setSubjectAbbrev(String subjectAbbrev) {
        this.subjectAbbrev = subjectAbbrev;
        updateView();
    }

    public String[] getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(String[] groupIds) {
        this.groupIds = groupIds;
        updateView();
    }

    public String getTeacherAbbrev() {
        return teacherAbbrev;
    }

    public void setTeacherAbbrev(String teacherAbbrev) {
        this.teacherAbbrev = teacherAbbrev;
        updateView();
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
        updateView();
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
        updateView();
    }

    public int getHourId() {
        return hourId;
    }

    public void setHourId(int hourId) {
        this.hourId = hourId;
        updateView();
    }

    public String[] getCycleIds() {
        return cycleIds;
    }

    public void setCycleIds(String[] cycleIds) {
        this.cycleIds = cycleIds;
        updateView();
    }

    public Change getChange() {
        return change;
    }

    public void setChange(Change change) {
        this.change = change;
        updateView();
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
        updateView();
    }

    public String[] getGroupAbbrevs() {
        return groupAbbrevs;
    }

    public void setGroupAbbrevs(String[] groupAbbrevs) {
        this.groupAbbrevs = groupAbbrevs;
        updateView();
    }

    public String[] getGroupNames() {
        return groupNames;
    }

    public void setGroupNames(String[] groupNames) {
        this.groupNames = groupNames;
        updateView();
    }

    public String getClassId() {
        return classId;
    }

    public String getClassAbbrev() {
        return classAbbrev;
    }

    public void setClassAbbrev(String classAbbrev) {
        this.classAbbrev = classAbbrev;
        updateView();
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
        updateView();
    }

    public FrameLayout getView() {
        return view;
    }

    public void setContext(Context context) {
        this.context = context;
        updateView();
    }

}

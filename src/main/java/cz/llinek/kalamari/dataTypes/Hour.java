package cz.llinek.kalamari.dataTypes;

import static cz.llinek.kalamari.Controller.dpToPx;
import static cz.llinek.kalamari.Controller.getTimestampFormatter;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.textview.MaterialTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Arrays;

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
    private String roomName;
    private String roomAbbrev;
    private String caption;
    private String beginTime;
    private String endTime;
    private String[] cycleIds;
    private String[] cycleNames;
    private String[] cycleAbbrevs;
    private Change change;
    private String theme;
    private String timetableFilename;
    private FrameLayout view;
    private Context context;

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
                System.out.println("groupIds = " + Arrays.toString(getGroupIds()));
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
                System.out.println("groupIds = " + Arrays.toString(getGroupIds()));
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
                System.out.println("groupIds = " + Arrays.toString(getGroupIds()));
                this.teacherId = hour.getString("TeacherId");
                this.subjectId = hour.getString("SubjectId");
                this.roomId = hour.getString("RoomId");
                this.cycleIds = cycleIds;
                this.change = null;
                this.theme = hour.getString("Theme");
                this.timetableFilename = timetableFilename;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
        this.context = context;
        updateView();
    }

    public String getRoomName() {
        if (roomName == null) {
            try {
                JSONArray rooms = new JSONObject(getTimetableFilename()).getJSONArray("Rooms");
                for (int i = 0; i < rooms.length(); i++) {
                    JSONObject room = rooms.getJSONObject(i);
                    if (room.getString("Id").equals(roomId)) {
                        roomName = room.getString("Name");
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return roomName;
    }

    public String getRoomAbbrev() {
        if (roomAbbrev == null) {
            try {
                JSONArray rooms = new JSONObject(FileManager.readFile(getTimetableFilename())).getJSONArray("Rooms");
                for (int i = 0; i < rooms.length(); i++) {
                    JSONObject room = rooms.getJSONObject(i);
                    if (room.getString("Id").equals(getRoomId())) {
                        roomAbbrev = room.getString("Name");
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return roomAbbrev;
    }

    public String[] getCycleNames() {
        if (cycleNames == null) {
            try {
                JSONArray cycles = new JSONObject(FileManager.readFile(getTimetableFilename())).getJSONArray("Cycles");
                for (int i = 0; i < cycles.length(); i++) {
                    JSONObject cycle = cycles.getJSONObject(i);
                    for (int j = 0; j < getCycleIds().length; j++) {
                        if (cycle.getString("Id").equals(getCycleIds()[j])) {
                            cycleAbbrevs[j] = cycle.getString("Abbrev");
                            cycleNames[j] = cycle.getString("Name");
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return cycleNames;
    }

    public String[] getCycleAbbrevs() {
        if (cycleAbbrevs == null) {
            try {
                JSONArray cycles = new JSONObject(FileManager.readFile(getTimetableFilename())).getJSONArray("Cycles");
                for (int i = 0; i < cycles.length(); i++) {
                    JSONObject cycle = cycles.getJSONObject(i);
                    for (int j = 0; j < getCycleIds().length; j++) {
                        if (cycle.getString("Id").equals(getCycleIds()[j])) {
                            cycleAbbrevs[j] = cycle.getString("Abbrev");
                            cycleNames[j] = cycle.getString("Name");
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return cycleAbbrevs;
    }

    private TableRow generateDialogMessageRow(String keyText, String valueText) {
        TableRow row = new TableRow(context);
        TextView key = new TextView(context);
        TextView value = new TextView(context);
        ViewGroup.MarginLayoutParams valueLayoutParams = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.MATCH_PARENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
        row.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        valueLayoutParams.leftMargin = dpToPx(context, 8);
        key.setText(keyText);
        key.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        value.setText(valueText);
        return row;
    }

    private void updateView() {
        FrameLayout layout = new FrameLayout(context);
        MaterialTextView subject = new MaterialTextView(context);
        MaterialTextView teacher = new MaterialTextView(context);
        MaterialTextView group = new MaterialTextView(context);
        MaterialTextView room = new MaterialTextView(context);
        FrameLayout.LayoutParams subjectLayout = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        FrameLayout.LayoutParams teacherLayout = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        FrameLayout.LayoutParams groupLayout = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        FrameLayout.LayoutParams roomLayout = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        subjectLayout.gravity = Gravity.CENTER;
        teacherLayout.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        groupLayout.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        roomLayout.gravity = Gravity.BOTTOM | Gravity.LEFT;
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
        !!fix this room.setText(getRoomAbbrev());
        room.setTextSize(Controller.dpToPx(context, Constants.TIMETABLE_CELL_ROOM_DP));
        room.setGravity(Gravity.CENTER);
        room.setLayoutParams(roomLayout);
        layout.setMinimumWidth(Controller.dpToPx(context, Constants.TIMETABLE_CELL_DP));
        layout.setMinimumHeight(Controller.dpToPx(context, Constants.TIMETABLE_CELL_DP));
        layout.setPadding(dpToPx(context, Constants.TIMETABLE_CELL_PADDING_DP), dpToPx(context, Constants.TIMETABLE_CELL_PADDING_DP), dpToPx(context, Constants.TIMETABLE_CELL_PADDING_DP), dpToPx(context, Constants.TIMETABLE_CELL_PADDING_DP));
        layout.addView(subject);
        layout.addView(teacher);
        layout.addView(group);
        layout.addView(room);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        view = layout;
    }

    public String getCaption() {
        if (caption == null) {
            try {
                JSONArray hours = new JSONObject(FileManager.readFile(getTimetableFilename())).getJSONArray("Hours");
                for (int i = 0; i < hours.length(); i++) {
                    JSONObject houri = hours.getJSONObject(i);
                    if (houri.getInt("Id") == hourId) {
                        caption = houri.getString("Caption");
                        beginTime = houri.getString("BeginTime");
                        endTime = houri.getString("EndTime");
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return caption;
    }

    public String getBeginTime() {
        if (beginTime == null) {
            try {
                JSONArray hours = new JSONObject(FileManager.readFile(getTimetableFilename())).getJSONArray("Hours");
                for (int i = 0; i < hours.length(); i++) {
                    JSONObject houri = hours.getJSONObject(i);
                    if (houri.getInt("Id") == hourId) {
                        caption = houri.getString("Caption");
                        beginTime = houri.getString("BeginTime");
                        endTime = houri.getString("EndTime");
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return beginTime;
    }

    public String getEndTime() {
        if (endTime == null) {
            try {
                JSONArray hours = new JSONObject(FileManager.readFile(getTimetableFilename())).getJSONArray("Hours");
                for (int i = 0; i < hours.length(); i++) {
                    JSONObject houri = hours.getJSONObject(i);
                    if (houri.getInt("Id") == hourId) {
                        caption = houri.getString("Caption");
                        beginTime = houri.getString("BeginTime");
                        endTime = houri.getString("EndTime");
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return endTime;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public String getSubjectName() {
        if (subjectName == null) {
            try {
                JSONArray subjects = new JSONObject(FileManager.readFile(getTimetableFilename())).getJSONArray("Subjects");
                for (int i = 0; i < subjects.length(); i++) {
                    JSONObject subject = subjects.getJSONObject(i);
                    if (subject.getString("Id").equals(subjectId)) {
                        subjectAbbrev = subject.getString("Abbrev");
                        subjectName = subject.getString("Name");
                        break;
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
        updateView();
    }

    public String getTeacherName() {
        if (teacherName == null) {
            try {
                JSONArray teachers = new JSONObject(FileManager.readFile(getTimetableFilename())).getJSONArray("Teachers");
                for (int i = 0; i < teachers.length(); i++) {
                    JSONObject teacher = teachers.getJSONObject(i);
                    if (teacher.getString("Id").equals(getTeacherId())) {
                        teacherAbbrev = teacher.getString("Abbrev");
                        teacherName = teacher.getString("Name");
                        break;
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
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
        if (subjectAbbrev == null) {
            try {
                JSONArray subjects = new JSONObject(FileManager.readFile(getTimetableFilename())).getJSONArray("Subjects");
                for (int i = 0; i < subjects.length(); i++) {
                    JSONObject subject = subjects.getJSONObject(i);
                    if (subject.getString("Id").equals(subjectId)) {
                        subjectAbbrev = subject.getString("Abbrev");
                        subjectName = subject.getString("Name");
                        break;
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
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
        if (teacherAbbrev == null) {
            try {
                JSONArray teachers = new JSONObject(FileManager.readFile(getTimetableFilename())).getJSONArray("Teachers");
                for (int i = 0; i < teachers.length(); i++) {
                    JSONObject teacher = teachers.getJSONObject(i);
                    if (teacher.getString("Id").equals(getTeacherId())) {
                        teacherAbbrev = teacher.getString("Abbrev");
                        teacherName = teacher.getString("Name");
                        break;
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
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
        if (groupAbbrevs != null) {
            if (groupAbbrevs[0] != null) {
                return groupAbbrevs;
            }
        }
        try {
            JSONArray groups = new JSONObject(FileManager.readFile(getTimetableFilename())).getJSONArray("Groups");
            for (int i = 0; i < groups.length(); i++) {
                JSONObject group = groups.getJSONObject(i);
                groupAbbrevs = new String[getGroupIds().length];
                for (int j = 0; j < getGroupIds().length; j++) {
                    if (group.getString("Id").equals(getGroupIds()[j])) {
                        groupAbbrevs[j] = group.getString("Abbrev");
                        groupNames[j] = group.getString("Name");
                        classId = group.getString("ClassId");
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return groupAbbrevs;
    }

    public void setGroupAbbrevs(String[] groupAbbrevs) {
        this.groupAbbrevs = groupAbbrevs;
        updateView();
    }

    public String[] getGroupNames() {
        if (groupNames != null) {
            if (groupNames[0] != null) {
                return groupNames;
            }
        }
        try {
            JSONArray groups = new JSONObject(FileManager.readFile(getTimetableFilename())).getJSONArray("Groups");
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
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return groupNames;
    }

    public void setGroupNames(String[] groupNames) {
        this.groupNames = groupNames;
        updateView();
    }

    public String getClassId() {
        if (classId == null) {
            getGroupAbbrevs();
        }
        return classId;
    }

    public String getClassAbbrev() {
        if (classAbbrev == null) {
            try {
                JSONArray classes = new JSONObject(FileManager.readFile(getTimetableFilename())).getJSONArray("Classes");
                for (int i = 0; i < classes.length(); i++) {
                    JSONObject clas = classes.getJSONObject(i);
                    if (clas.getString("Id").equals(getClassId())) {
                        classAbbrev = clas.getString("Abbrev");
                        className = clas.getString("Name");
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return classAbbrev;
    }

    public void setClassAbbrev(String classAbbrev) {
        this.classAbbrev = classAbbrev;
        updateView();
    }

    public String getClassName() {
        if (className == null) {
            try {
                JSONArray classes = new JSONObject(FileManager.readFile(getTimetableFilename())).getJSONArray("Classes");
                for (int i = 0; i < classes.length(); i++) {
                    JSONObject clas = classes.getJSONObject(i);
                    if (clas.getString("Id").equals(getClassId())) {
                        classAbbrev = clas.getString("Abbrev");
                        className = clas.getString("Name");
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
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

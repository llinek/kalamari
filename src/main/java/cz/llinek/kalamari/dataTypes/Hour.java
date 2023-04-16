package cz.llinek.kalamari.dataTypes;

public class Hour {
    private int id;
    private String[] groupIds;
    private String teacherId;
    private String roomId;
    private String[] cycleIds;
    private Change change;
    private String theme;

    public Hour(int id, String[] groupIds, String teacherId, String roomId, String[] cycleIds, Change change, String theme) {
        this.id = id;
        this.groupIds = groupIds;
        this.teacherId = teacherId;
        this.roomId = roomId;
        this.cycleIds = cycleIds;
        this.change = change;
        this.theme = theme;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String[] getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(String[] groupIds) {
        this.groupIds = groupIds;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String[] getCycleIds() {
        return cycleIds;
    }

    public void setCycleIds(String[] cycleIds) {
        this.cycleIds = cycleIds;
    }

    public Change getChange() {
        return change;
    }

    public void setChange(Change change) {
        this.change = change;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
}

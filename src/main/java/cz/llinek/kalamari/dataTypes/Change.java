package cz.llinek.kalamari.dataTypes;

import java.util.Date;

public class Change {
    private String changeSubject;
    private Date day;
    private String hours;
    private String changeType;
    private String description;
    private String time;
    private String typeAbbrev;
    private String typeName;

    public Change(String changeSubject, Date day, String hours, String changeType, String description, String time, String typeAbbrev, String typeName) {
        this.changeSubject = changeSubject;
        this.day = day;
        this.hours = hours;
        this.changeType = changeType;
        this.description = description;
        this.time = time;
        this.typeAbbrev = typeAbbrev;
        this.typeName = typeName;
    }

    public String getChangeSubject() {
        return changeSubject;
    }

    public void setChangeSubject(String changeSubject) {
        this.changeSubject = changeSubject;
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }

    public String getHours() {
        return hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTypeAbbrev() {
        return typeAbbrev;
    }

    public void setTypeAbbrev(String typeAbbrev) {
        this.typeAbbrev = typeAbbrev;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}

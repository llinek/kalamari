package cz.llinek.kalamari.dataTypes;

public class Subject {
    private int id;
    private String abbrev;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAbbrev() {
        return abbrev;
    }

    public void setAbbrev(String abbrev) {
        this.abbrev = abbrev;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Subject(int id, String abbrev, String name) {
        this.id = id;
        this.abbrev = abbrev;
        this.name = name;
    }
}

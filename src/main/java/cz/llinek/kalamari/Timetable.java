package cz.llinek.kalamari;

import static cz.llinek.kalamari.Controller.getTimestampFormatter;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

import cz.llinek.kalamari.dataTypes.Change;
import cz.llinek.kalamari.dataTypes.Hour;

public class Timetable extends Activity {
    private void buildTimetable() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildTimetable();
    }
}

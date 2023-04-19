package cz.llinek.kalamari;

import static cz.llinek.kalamari.Controller.getTimestampFormatter;
import static cz.llinek.kalamari.Controller.login;
import static cz.llinek.kalamari.Controller.onActivityStart;
import static cz.llinek.kalamari.Controller.performRequest;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;

import cz.llinek.kalamari.dataTypes.Change;
import cz.llinek.kalamari.dataTypes.Hour;
import cz.llinek.kalamari.dataTypes.RequestCallback;

public class MainActivity extends Activity {

    private void basicScreen() {
        Toast.makeText(this, "afterlogin", Toast.LENGTH_SHORT).show();
        LinearLayout vBox = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 0);
        vBox.setOrientation(LinearLayout.VERTICAL);
        vBox.setLayoutParams(params);
        vBox.setPadding(0, 0, 0, 0);
        this.setContentView(vBox, params);
        Button logout = new Button(this);
        Button timetable = new Button(this);
        Button marks = new Button(this);
        logout.setMinHeight(100);
        logout.setText("Logout");
        logout.setLayoutParams(params);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    FileManager.deleteFile(Constants.CREDENTIALSFILENAME);
                    loginScreen();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Logout succesful", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Exception", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
        timetable.setMinHeight(100);
        timetable.setText("Timetable");
        timetable.setPadding(0, 0, 0, 0);
        timetable.setLayoutParams(params);
        timetable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performRequest(getApplicationContext(), "/api/3/timetable/permanent", new RequestCallback() {
                    @Override
                    public void run(String response) {
                        timetable(response);
                    }
                });
            }
        });
        marks.setMinHeight(100);
        marks.setPadding(0, 0, 0, 0);
        marks.setText("Marks");
        marks.setLayoutParams(params);
        timetable.setMinHeight(100);
        vBox.addView(logout);
        vBox.addView(timetable);
        vBox.addView(marks);
    }

    private void timetable(String response) {
        try {
            JSONObject rozvrh = new JSONObject(response);
            Toast.makeText(this, response, Toast.LENGTH_LONG).show();
            System.out.println(response.replaceAll(",", ",\n"));
            int minHours = -1;
            int maxHours = -1;
            int days = 0;
            for (int i = rozvrh.getJSONArray("Days").length(); i < 0; i--) {
                if (rozvrh.getJSONArray("Days").getJSONObject(i).getJSONArray("Atoms").length() > 0) {
                    days++;
                    for (int j = rozvrh.getJSONArray("Days").getJSONObject(i).getJSONArray("Atoms").length(); j > 0; j--) {
                        if (rozvrh.getJSONArray("Days").getJSONObject(i).getJSONArray("Atoms").getJSONObject(j).getInt("HourId") < minHours || minHours == -1) {
                            minHours = rozvrh.getJSONArray("Days").getJSONObject(i).getJSONArray("Atoms").getJSONObject(j).getInt("HourId");
                        }
                        if (rozvrh.getJSONArray("Days").getJSONObject(i).getJSONArray("Atoms").getJSONObject(j).getInt("HourId") > maxHours || maxHours == -1) {
                            maxHours = rozvrh.getJSONArray("Days").getJSONObject(i).getJSONArray("Atoms").getJSONObject(j).getInt("HourId");
                        }
                    }
                }
            }
            Hour[][] hours = new Hour[maxHours - minHours][days];
            for (int i = 0; i < hours.length; i++) {
                for (int j = 0; j < hours[i].length; j++) {
                    JSONObject hour = rozvrh.getJSONArray("Days").getJSONObject(j).getJSONArray("Atoms").getJSONObject(i);
                    String[] groupIds = new String[hour.getJSONArray("GroupIds").length()];
                    String[] cycleIds = new String[hour.getJSONArray("CycleIds").length()];
                    for (int k = 0; k < groupIds.length; k++) {
                        groupIds[k] = hour.getJSONArray("GroupIds").getString(k);
                    }
                    for (int k = 0; k < cycleIds.length; k++) {
                        cycleIds[k] = hour.getJSONArray("CycleIds").getString(k);
                    }
                    try {
                        JSONObject c = hour.getJSONObject("Change");
                        Change change = new Change(c.getString("ChangeSubject"), getTimestampFormatter().parse(c.getString("Day")), c.getString("Hours"), c.getString("ChangeType"), c.getString("Description"), c.getString("Time"), c.getString("TypeAbbrev"), c.getString("TypeName"));
                        hours[i][j] = new Hour(hour.getInt("HourId"), groupIds, hour.getString("TeacherId"), hour.getString("RoomId"), cycleIds, change, hour.getString("Theme"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> System.err.println("\n\n\nno change\n\n\n\n\n" + e.getMessage()));
                        hours[i][j] = new Hour(hour.getInt("HourId"), groupIds, hour.getString("TeacherId"), hour.getString("RoomId"), cycleIds, null, hour.getString("Theme"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> System.err.println("\n\n\nchange err, fallback to timetable without changes\n\n\n\n\n" + e.getMessage()));
                        hours[i][j] = new Hour(hour.getInt("HourId"), groupIds, hour.getString("TeacherId"), hour.getString("RoomId"), cycleIds, null, hour.getString("Theme"));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(() -> System.err.println(e.getMessage()));
        }
    }

    private void loginScreen() {
        EditText urlField = new EditText(this);
        EditText userField = new EditText(this);
        EditText pwdField = new EditText(this);
        LinearLayout vBox = new LinearLayout(this);
        LinearLayout pwdBox = new LinearLayout(this);
        CheckBox clearPwd = new CheckBox(this);
        Button confirm = new Button(this);
        Button schoolList = new Button(this);
        clearPwd.setMinWidth(0);
        clearPwd.setMinHeight(0);
        clearPwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {
                    pwdField.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                } else {
                    pwdField.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                }
            }
        });
        confirm.setText("Confirm");
        confirm.setTextSize(30);
        confirm.setPadding(0, 0, 0, 0);
        confirm.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        schoolList.setText("Select school from list");
        schoolList.setTextSize(25);
        schoolList.setPadding(0, 0, 0, 0);
        schoolList.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        schoolList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Not supported yet", Toast.LENGTH_SHORT);
                    }
                });
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = urlField.getText().toString().trim();
                if (url.endsWith("/")) {
                    url = url.substring(0, url.length() - 1);
                }
                if (!url.startsWith("https://")) {
                    url = "https://" + url;
                }
                final String url1 = url;
                /*performRequest(url + "/api/3", new RequestCallback() {
                    @Override
                    public void run(String response) {
                        Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                        login(url1, userField.getText().toString(), pwdField.getText().toString());
                    }
                });*/
                login(getApplicationContext(), url1, userField.getText().toString(), pwdField.getText().toString());
            }
        });
        vBox.setOrientation(LinearLayout.VERTICAL);
        pwdBox.setOrientation(LinearLayout.HORIZONTAL);
        urlField.setHint("School url:");
        urlField.setText("gymnazium-opatov.bakalari.cz");
        urlField.setLines(1);
        urlField.setInputType(InputType.TYPE_TEXT_VARIATION_URI | InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        urlField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    userField.requestFocus();
                    return true;
                }
                return false;
            }
        });
        userField.setHint("User name:");
        userField.setText("luklin.p");
        userField.setLines(1);
        userField.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL | InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        userField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    pwdField.requestFocus();
                    return true;
                }
                return false;
            }
        });
        pwdField.setHint("Password:");
        pwdField.setText("");
        pwdField.setLines(1);
        pwdField.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        pwdField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    confirm.performClick();
                    return true;
                }
                return false;
            }
        });
        pwdBox.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        pwdBox.addView(pwdField);
        pwdBox.addView(clearPwd);
        vBox.addView(urlField);
        vBox.addView(userField);
        vBox.addView(pwdBox);
        //vBox.addView(pwdField);
        vBox.addView(confirm);
        vBox.addView(schoolList);
        this.setContentView(vBox);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onActivityStart();
        Toast.makeText(this, "start", Toast.LENGTH_SHORT).show();
        login(new Runnable() {
            @Override
            public void run() {
                basicScreen();
            }
        });
    }
}
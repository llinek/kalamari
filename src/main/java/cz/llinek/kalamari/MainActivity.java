package cz.llinek.kalamari;

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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.net.ssl.HttpsURLConnection;

import cz.llinek.kalamari.dataTypes.Change;
import cz.llinek.kalamari.dataTypes.Hour;
import cz.llinek.kalamari.dataTypes.RequestCallback;

public class MainActivity extends Activity {
    private String url;
    private String token;
    private SimpleDateFormat timestampFormatter;

    private void performRequest(String appendix, RequestCallback runLater) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StringBuilder response = new StringBuilder();
                    HttpsURLConnection connection = (HttpsURLConnection) new URL(url + appendix).openConnection();
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setRequestProperty("Authorization", "Bearer " + token);
                    System.out.println(token);
                    connection.connect();
                    if (connection.getErrorStream() == null) {
                        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String temp = input.readLine();
                        while (temp != null) {
                            response.append(temp);
                            temp = input.readLine();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                runLater.run(response.toString());
                            }
                        });
                        connection.disconnect();
                        input.close();
                    } else {
                        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                        String temp = input.readLine();
                        while (temp != null) {
                            response.append(temp);
                            temp = input.readLine();
                        }
                        response.append("Error: ");
                        response.append(connection.getResponseCode());
                        response.append(", ");
                        response.append(connection.getResponseMessage());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Wrong request", Toast.LENGTH_LONG).show();
                                System.err.println(response.toString());
                            }
                        });
                    }
                } catch (Throwable e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Exception", Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void loginRefreshToken() {

    }

    private void login(String url, String user, String pwd) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StringBuilder response = new StringBuilder();
                    HttpsURLConnection connection = (HttpsURLConnection) new URL(url + "/api/login").openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    PrintWriter output = new PrintWriter(connection.getOutputStream());
                    output.print("client_id=ANDR&grant_type=password&username=" + URLEncoder.encode(user, "utf-8") + "&password=" + URLEncoder.encode(pwd, "utf-8"));
                    output.close();
                    connection.connect();
                    if (connection.getErrorStream() == null) {
                        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String temp = input.readLine();
                        while (temp != null) {
                            response.append(temp);
                            temp = input.readLine();
                        }
                        connection.disconnect();
                        input.close();
                    } else {
                        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                        String temp = input.readLine();
                        while (temp != null) {
                            response.append(temp);
                            temp = input.readLine();
                        }
                        response.append("Error: ");
                        response.append(connection.getResponseCode());
                        response.append(", ");
                        response.append(connection.getResponseMessage());
                        System.err.println(response.toString());
                    }

                    if (connection.getResponseCode() == 200) {
                        JSONObject res = new JSONObject(response.toString());
                        setUrl(url);
                        setToken(res.getString("access_token"));
                        FileManager.fileWrite(Constants.CREDENTIALSFILENAME, url + '\n' + res.getString("access_token") + "\n" + (System.currentTimeMillis() + res.getInt("expires_in") * 1000) + "\n" + res.getString("refresh_token") + "\n" + user + "\n" + pwd);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                basicScreen();
                            }
                        });
                    }
                } catch (Throwable e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Exception", Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                    System.err.println(e.getMessage());
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void login(Runnable runAfter) {
        Toast.makeText(this, "login", Toast.LENGTH_SHORT).show();
        if (FileManager.exists(Constants.CREDENTIALSFILENAME)) {
            Toast.makeText(this, "loginexists", Toast.LENGTH_SHORT).show();
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new FileReader(FileManager.editFile(Constants.CREDENTIALSFILENAME)));
                        String url = bufferedReader.readLine();
                        bufferedReader.readLine();
                        long expirationTime = Long.parseLong(bufferedReader.readLine());
                        if (System.currentTimeMillis() < expirationTime - 100000) {
                            runOnUiThread(runAfter);
                            return;
                        }
                        bufferedReader.readLine();
                        String user = bufferedReader.readLine();
                        String pwd = bufferedReader.readLine();
                        StringBuilder response = new StringBuilder();
                        HttpsURLConnection connection = (HttpsURLConnection) new URL(getUrl() + "/api/login").openConnection();
                        connection.setRequestMethod("POST");
                        connection.setDoInput(true);
                        connection.setDoOutput(true);
                        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        PrintWriter output = new PrintWriter(connection.getOutputStream());
                        output.print("client_id=ANDR&grant_type=password&username=" + URLEncoder.encode(user, "utf-8") + "&password=" + URLEncoder.encode(pwd, "utf-8"));
                        output.close();
                        connection.connect();
                        if (connection.getErrorStream() == null) {
                            BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            String temp = input.readLine();
                            while (temp != null) {
                                response.append(temp);
                                temp = input.readLine();
                            }
                            connection.disconnect();
                            input.close();
                        } else {
                            BufferedReader input = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                            String temp = input.readLine();
                            while (temp != null) {
                                response.append(temp);
                                temp = input.readLine();
                            }
                            response.append("Error: ");
                            response.append(connection.getResponseCode());
                            response.append(", ");
                            response.append(connection.getResponseMessage());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Wrong login", Toast.LENGTH_LONG).show();
                                    System.err.println(response.toString());
                                    loginScreen();
                                }
                            });
                        }

                        if (connection.getResponseCode() == 200) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT).show();
                                }
                            });
                            JSONObject res = new JSONObject(response.toString());
                            setUrl(url);
                            setToken(res.getString("access_token"));
                            FileManager.fileWrite(Constants.CREDENTIALSFILENAME, url + "\n" + res.getString("access_token") + "\n" + (System.currentTimeMillis() + res.getInt("expires_in") * 1000) + "\n" + res.getString("refresh_token") + "\n" + user + "\n" + pwd);
                            runOnUiThread(runAfter);
                        }
                    } catch (Throwable e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Exception", Toast.LENGTH_SHORT).show();
                            }
                        });
                        e.printStackTrace();
                        System.err.println(e.getMessage());
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
        } else {
            Toast.makeText(this, "else", Toast.LENGTH_SHORT).show();
            loginScreen();
        }
    }

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
                performRequest("/api/3/timetable/permanent", new RequestCallback() {
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
                        Change change = new Change(c.getString("ChangeSubject"), timestampFormatter.parse(c.getString("Day")), c.getString("Hours"), c.getString("ChangeType"), c.getString("Description"), c.getString("Time"), c.getString("TypeAbbrev"), c.getString("TypeName"));
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
                login(url1, userField.getText().toString(), pwdField.getText().toString());
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

    private String getUrl() {
        return url;
    }

    private void setUrl(String url) {
        this.url = url;
    }

    private String getToken() {
        return token;
    }

    private void setToken(String token) {
        this.token = token;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            System.err.println("null");
        } else {
            System.err.println(savedInstanceState.getString("url"));
        }
        timestampFormatter = new SimpleDateFormat(Constants.TIMESTAMP);
        FileManager.filesDir = getFilesDir();
        if (FileManager.exists(Constants.CREDENTIALSFILENAME)) {
            try {
                BufferedReader input = new BufferedReader(new FileReader(FileManager.editFile(Constants.CREDENTIALSFILENAME)));
                String temp = input.readLine();
                setUrl(temp);
                setToken(input.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Toast.makeText(this, "start", Toast.LENGTH_SHORT).show();
        login(new Runnable() {
            @Override
            public void run() {
                basicScreen();
            }
        });
    }
}
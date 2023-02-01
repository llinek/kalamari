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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends Activity {
    private void performRequest(String url, RequestCallback runLater) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StringBuilder response = new StringBuilder();
                    HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
                    connection.setRequestProperty("Accept", "application/json");
                    connection.connect();
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
                        /*System.err.println("Body:" + "client_id=ANDR&grant_type=password&username=" + URLEncoder.encode(user, "utf-8") + "&password=" + URLEncoder.encode(pwd, "utf-8"));
                        response.append("Message: ");
                        response.append(connection.getResponseCode());
                        response.append(", ");
                        response.append(connection.getResponseMessage());
                        System.err.println(response.toString());*/
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
                        FileWriter out = new FileWriter(FileManager.editFile(Constants.CREDENTIALSFILENAME));
                        JSONObject res = new JSONObject(response.toString());
                        out.write(res.getString("access_token") + '\n' + (System.currentTimeMillis() + res.getInt("expires_in") * 1000) + '\n' + res.getString("refresh_token") + '\n' + user + '\n' + pwd);
                        out.close();
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

    private void basicScreen() {
        LinearLayout vBox = new LinearLayout(this);
        this.setContentView(vBox);
        Button rozvrh = new Button(this);
        Button znamky = new Button(this);
        rozvrh.setMinHeight(100);
        rozvrh.setText("Rozvrh");
        rozvrh.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        //rozvrh.setMinHeight(40);
        vBox.addView(rozvrh);
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
                performRequest(url + "/api/3", new RequestCallback() {
                    @Override
                    public void run(String response) {
                        Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                        login(url1, userField.getText().toString(), pwdField.getText().toString());
                    }
                });
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

    //@SuppressLint("MissingSuperCall")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FileManager.filesDir = getFilesDir();
        loginScreen();
    }
}
package cz.llinek.kalamari;

import static cz.llinek.kalamari.Controller.login;

import android.app.Activity;
import android.content.Intent;
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

public class LoginScreen extends Activity {
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
        startActivity(new Intent(this, Timetable.class));
        vBox.addView(pwdBox);
        //vBox.addView(pwdField);
        vBox.addView(confirm);
        vBox.addView(schoolList);
        this.setContentView(vBox);
        !!continue here
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}

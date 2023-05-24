package cz.llinek.kalamari;

import static com.google.android.material.textfield.TextInputLayout.END_ICON_PASSWORD_TOGGLE;
import static cz.llinek.kalamari.Controller.checkSchoolUrl;
import static cz.llinek.kalamari.Controller.login;
import static cz.llinek.kalamari.Controller.parseUrl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginScreen extends Activity {
    private void loginScreen() {
        EditText urlEditText = new EditText(this);
        EditText userEditText = new EditText(this);
        TextInputLayout pwdField = new TextInputLayout(this);
        TextInputLayout userField = new TextInputLayout(this);
        TextInputLayout urlField = new TextInputLayout(this);
        TextInputEditText pwdEditText = new TextInputEditText(this);
        LinearLayout vBox = new LinearLayout(this);
        MaterialButton confirm = new MaterialButton(this);
        MaterialButton schoolList = new MaterialButton(this);
        confirm.setIcon(getResources().getDrawable(R.drawable.outline_check_24));
        confirm.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
        confirm.setIconSize((int) (confirm.getTextSize() * 2 + 0.5));
        confirm.setText("Confirm");
        confirm.setTextSize(Constants.LOGIN_CONFIRM_TEXT_SIZE);
        confirm.setPadding(0, 0, 0, 0);
        confirm.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        schoolList.setText("Select school from list");
        schoolList.setTextSize(Constants.LOGIN_SCHOOLLIST_TEXT_SIZE);
        schoolList.setPadding(0, 0, 0, 0);
        schoolList.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        schoolList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Not supported yet", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String url = parseUrl(urlEditText.getText().toString());
                if (!login(getApplicationContext(), url, userEditText.getText().toString(), pwdEditText.getText().toString())) {
                    if (checkSchoolUrl(url)) {
                        urlField.setErrorEnabled(false);
                        userField.setError("failed");
                        pwdField.setError("failed");
                    } else {
                        urlField.setError("Url doesn't work.");
                        userField.setErrorEnabled(false);
                        pwdField.setErrorEnabled(false);
                    }
                } else {
                    userField.setErrorEnabled(false);
                    pwdField.setErrorEnabled(false);
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
            }
        });
        vBox.setOrientation(LinearLayout.VERTICAL);
        urlEditText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        urlEditText.setText("gymnazium-opatov.bakalari.cz");
        urlEditText.setInputType(InputType.TYPE_TEXT_VARIATION_URI | InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        urlEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                if (checkSchoolUrl(parseUrl(urlEditText.getText().toString()))) {
                    urlField.setErrorEnabled(false);
                } else {
                    urlField.setError("Url doesn't work.");
                    userField.setErrorEnabled(false);
                    pwdField.setErrorEnabled(false);
                }
                userEditText.requestFocus();
                return true;
            }
            return false;
        });
        urlEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (checkSchoolUrl(parseUrl(s.toString()))) {
                    urlField.setErrorEnabled(false);
                } else {
                    urlField.setError("Url doesn't work.");
                    userField.setErrorEnabled(false);
                    pwdField.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        userEditText.setText("luklin.p");
        userEditText.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL | InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        userEditText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        userEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    pwdEditText.requestFocus();
                    return true;
                }
                return false;
            }
        });
        pwdEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        pwdEditText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        pwdEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    confirm.performClick();
                    return true;
                }
                return false;
            }
        });
        urlField.setHint("School url:");
        urlField.setErrorIconDrawable(R.drawable.outline_cross_24);
        urlField.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
        urlField.setEndIconDrawable(R.drawable.outline_check_24);
        urlField.setErrorEnabled(false);
        urlField.addView(urlEditText);
        urlField.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        userField.setHint("Username:");
        userField.setErrorEnabled(false);
        userField.addView(userEditText);
        userField.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        pwdField.setHint("Password:");
        pwdField.setErrorEnabled(false);
        pwdField.addView(pwdEditText);
        pwdField.setEndIconMode(END_ICON_PASSWORD_TOGGLE);
        pwdField.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        vBox.addView(urlField);
        vBox.addView(userField);
        vBox.addView(pwdField);
        vBox.addView(confirm);
        vBox.addView(schoolList);
        this.setContentView(vBox);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginScreen();
    }
}

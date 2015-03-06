/*
 * Copyright (C) 2011-2015 The Literatim authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.troi.literatim;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.provider.Settings;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

enum State { DISABLED, NOT_DONE, DONE }

public class ConfigScreen extends Activity {
    Button enableButton;
    Button selectButton;
    TextView successTickTextView;
    TextView successTextView;
    boolean selecting;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configscreen);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshState();
        selecting = false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus) return;
        if (!selecting) return;
        selecting = false;
        refreshState();
    }

    public void showInputMethodSettings(View view) {
        startActivity(new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS));
    }

    public void showInputMethodPicker(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showInputMethodPicker();
        selecting = true;
    }

    void refreshState() {
        enableButton = (Button) findViewById(R.id.button_enable);
        selectButton = (Button) findViewById(R.id.button_select);
        successTickTextView = (TextView) findViewById(R.id.textview_success_tick);
        successTextView = (TextView) findViewById(R.id.textview_success);
        Resources res = getResources();
        if (!isEnabled()) {
            enableButton.setText(res.getString(R.string.label_enable));
            selectButton.setText(res.getString(R.string.label_select));
            setButtonState(enableButton, State.NOT_DONE);
            setButtonState(selectButton, State.DISABLED);
            successTickTextView.setVisibility(View.GONE);
            successTextView.setVisibility(View.GONE);
        } else if (!isSelected()) {
            enableButton.setText(res.getString(R.string.label_enable_done));
            setButtonState(enableButton, State.DONE);
            selectButton.setText(res.getString(R.string.label_select));
            setButtonState(selectButton, State.NOT_DONE);
            successTickTextView.setVisibility(View.GONE);
            successTextView.setVisibility(View.GONE);
        } else {
            enableButton.setText(res.getString(R.string.label_enable_done));
            setButtonState(enableButton, State.DONE);
            selectButton.setText(res.getString(R.string.label_select_done));
            setButtonState(selectButton, State.DONE);
            successTickTextView.setVisibility(View.VISIBLE);
            successTextView.setVisibility(View.VISIBLE);
        }
    }

    void setButtonState(Button button, State state) {
        switch (state) {
            case DISABLED:
                button.getBackground().clearColorFilter();
                button.setEnabled(false);
                break;
            case NOT_DONE:
                button.getBackground().clearColorFilter();
                button.setEnabled(true);
                break;
            case DONE:
                button.getBackground().setColorFilter(0xff77ff77, PorterDuff.Mode.MULTIPLY);
                button.setEnabled(true);
                break;
        }
    }

    boolean isEnabled() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        for (InputMethodInfo imi : imm.getEnabledInputMethodList()) {
            if (imi.getId().equals("org.troi.literatim/.SoftKeyboard")) {
                return true;
            }
        }
        return false;
    }
    
    boolean isSelected() {
        String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        return ("org.troi.literatim/.SoftKeyboard".equals(id));
    }
}

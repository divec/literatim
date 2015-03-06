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

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LexiconManager {
    Context imContext;
    SharedPreferences settings;
    Map<String, Lexicon> lexiconForName; 

    public LexiconManager(Context imContext) {
        this.imContext = imContext;
        settings = imContext.getSharedPreferences("literatim", Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
        lexiconForName = new HashMap<String, Lexicon>();
    }

    public String getCurrentLexiconName() {
        return settings.getString("currentLexicon", null);
    }

    public Lexicon getCurrentLexicon() {
        String currentLexiconName = getCurrentLexiconName();
        if (currentLexiconName == null) return null;
        return getLexicon(currentLexiconName);
    }

    public String getCurrentLexiconId() {
        Lexicon currentLexicon = getCurrentLexicon();
        if (currentLexicon == null) return null;
        return currentLexicon.getId();
    }

    public void startUpdateCurrentLexicon(final Object softKeyboard) {
        final SoftKeyboard sk = (SoftKeyboard) softKeyboard;
        Lexicon lexicon = getCurrentLexicon();
        UpdateLexiconTask ult = new UpdateLexiconTask(lexicon, settings) {
            public void onProgressUpdate(Progress... msgList) {
                Progress msg = msgList[0];
                sk.showProgress(msg);
            }

            public void onPostExecute(Void result) {
                Throwable thrown = getThrown();
                if (thrown != null) {
                    sk.showToast("Update failed: " + thrown);
                }
            }
        };
        ult.execute(sk);
    }

    public Lexicon getLexicon(String lexiconName) {
        if (! lexiconForName.containsKey(lexiconName)) loadLexicon(lexiconName);
        return lexiconForName.get(lexiconName);
    }

    public void setCurrentLexicon(String lexiconName) {
        if (lexiconName == null) {
            settings.edit().putString("currentLexicon", "x").remove("currentLexicon").commit();
            return;
        }
        if (! Arrays.asList(getAllLexiconNames()).contains(lexiconName)) {
            throw new IllegalArgumentException("No such lexicon: " + lexiconName);
        }
        settings.edit().putString("currentLexicon", lexiconName).commit();
    }

    public void outdateCurrentLexicon() {
        String id = getCurrentLexiconId();
        if (id == null) return;
        settings.edit().putInt("lexVersion_" + id, 0).putLong("lastCheck_" + id, 0).commit();
    }

    public String[] getAllLexiconNames() {
        return new String[]{"English-Cymraeg"};
    }

    private void loadLexicon(String lexiconName) {
        if (lexiconName.equals("English-Cymraeg")) {
            lexiconForName.put(lexiconName, new EnCyLexicon(imContext.getFilesDir()));
        }
        else {
            throw new IllegalArgumentException("Unknown lexicon: " + lexiconName);
        }
    }
}

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
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.view.inputmethod.InputConnection;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class EnCyLexicon extends Lexicon {
    final int fullSearchThreshhold = 3;
    final static Map<Integer, Integer> colors;
    static {
        colors = new HashMap<Integer, Integer>();
        colors.put(1, 0xFF000088);
        colors.put(2, 0xFF000000);
        colors.put(3, 0xFF008800);
    }

    public EnCyLexicon(File fileDir) {
        super("EnCy", fileDir);
    }

    @Override
    public String getName() {
        return "Cymraeg-English";
    }

    @Override
    public Map<Integer, Integer> getColors() {
        return colors;
    }

    @Override
    public String getSuggestionText(String code, Suggestion suggestion) {
        boolean isUpperFirst = code.length() > 0 && Character.isUpperCase(code.codePointAt(0));
        String text = suggestion.getText();
        if (! isUpperFirst || text.length() == 0) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    @Override
    public String getContextFast(InputConnection connection) {
        CharSequence preChars = connection.getTextBeforeCursor(2, 0);
        if (preChars == null || preChars.length() == 0) return "";
        char pre = preChars.charAt(preChars.length() - 1);
        if (pre == ' ' && preChars.length() >= 2) {
            pre = preChars.charAt(preChars.length() - 2);
        }
        if (! Character.isLetterOrDigit(pre)) return "#";
        if ("aeiouwyâêiôûŵŷáéíóúẃýàèìòùẁỳ".indexOf(Character.toLowerCase(pre)) > -1) return "LV";
        return "L";
    }

    @Override
    public List<Suggestion> getSuggestions(String search, String context, int maxSuggestions, Lexicon.SearchTask asyncTask) throws DictException {
        if (search.length() == 0 && context.contains("V")) {
            List<Suggestion> suggestions = new ArrayList<Suggestion>();
            for(String s : new String[] {"'n", "'r", "'i", "'u", "'ch", "'m", "'th", "'w"}) {
                suggestions.add(new Suggestion(1, s));
            }
            return suggestions;
        }
        
        String flatSearch = flatten(search);
        String precalcSql = "SELECT distinct p.keystrokes as keystrokes, p.prediction as prediction, p.lang_code as lang_code from predictions p inner join precalc c on c.prediction_id = p.id where c.keystrokes=? order by p.keystrokes <> ?, p.lang_code = 2, p.id";
        String[] precalcArgs = new String[] {flatSearch, flatSearch};
        List<Suggestion> precalcSuggestions = getSuggestionsFromSql(search, precalcSql, precalcArgs, maxSuggestions, asyncTask);
        if (precalcSuggestions != null && precalcSuggestions.size() > 0) return precalcSuggestions;

        String flatNext = getIncremented(flatSearch);
        String sql = "SELECT distinct p.keystrokes as keystrokes, p.prediction as prediction, p.lang_code as lang_code from predictions p where p.keystrokes >= ? and p.keystrokes < ? order by p.keystrokes <> ?, p.lang_code = 2, p.id limit " + maxSuggestions;
        String[] args = new String[]{flatSearch, flatNext, flatSearch};
        List<Suggestion> suggestions = getSuggestionsFromSql(search, sql, args, maxSuggestions, asyncTask); // possibly null; ok
        return suggestions;
    }

    @Override
    public List<Entry> getEntries(String search, String context, int maxEntries, Lexicon.SearchTask asyncTask) throws DictException {
        SQLiteDatabase db = getDbOrNull();
        if (asyncTask.publishProgressAndCheckCancelled("") == true) return null;
        if (db == null) return new ArrayList<Entry>();

        String flatSearch = flatten(search);
        String precalcSql = "SELECT distinct e.info as info from entries e inner join predictions p on p.lemma_id = e.lemma_id inner join precalc c on p.id = c.prediction_id where c.keystrokes=? order by p.keystrokes <> ?, p.lang_code = 2, p.id";
        String[] precalcArgs = new String[] {flatSearch, flatSearch};
        List<Entry> precalcEntries = getEntriesFromSql(precalcSql, precalcArgs, asyncTask);
        if (precalcEntries != null && precalcEntries.size() > 0) return precalcEntries;

        String flatNext = getIncremented(flatSearch);
        String sql = "SELECT distinct e.info as info from entries e inner join predictions p on p.lemma_id = e.lemma_id where p.keystrokes >= ? and p.keystrokes < ? order by p.keystrokes <> ?, p.lang_code = 2, p.id limit " + maxEntries;
        String[] args = new String[]{flatSearch, flatNext, flatSearch};
        List<Entry> entries = getEntriesFromSql(sql, args, asyncTask); // could be null; ok
        return entries;
    }

    List<Suggestion> getSuggestionsFromSql(String search, String sql, String[] args, int maxSuggestions, Lexicon.SearchTask asyncTask) throws DictException {
        SQLiteDatabase db = getDbOrNull();
        if (asyncTask.publishProgressAndCheckCancelled("") == true) return null;
        if (db == null) {
            return Arrays.asList(new Suggestion[]{});
        }
        Cursor cur = db.rawQuery(sql, args);
        try {
            int keystrokesColumn = cur.getColumnIndex("keystrokes");
            int predictionColumn = cur.getColumnIndex("prediction");
            int langCodeColumn = cur.getColumnIndex("lang_code");
            LinkedHashSet<Suggestion> suggestions = new LinkedHashSet<Suggestion>(maxSuggestions);
            for(int i = 0; cur.moveToNext(); i++) {
                if (asyncTask.publishProgressAndCheckCancelled("") == true) return null;
                String keystrokes = cur.getString(keystrokesColumn);
                String prediction = cur.getString(predictionColumn);
                int langCode = cur.getInt(langCodeColumn);
                if (langCode == 1) { // cy only, like yr|null or dwr|dŵr
                    String p = (prediction == null) ? keystrokes : prediction;
                    suggestions.add(new Suggestion(1, p));
                }
                else if (langCode == 2) { // en|cy, like dog|ci
                    if (keystrokes.length() >= 4 || keystrokes.length() == search.length()) {
                        suggestions.add(new Suggestion(1, prediction)); // cy
                    }
                    if (suggestions.size() == maxSuggestions) break;
                    suggestions.add(new Suggestion(2, keystrokes)); // en
                }
                else if (langCode == 3 && prediction == null) { // cy+en, e.g. angel|null
                    suggestions.add(new Suggestion(3, keystrokes)); // cy+en
                }
                else if (langCode == 3 && prediction != null) { // par|pâr, nod|nòd
                    suggestions.add(new Suggestion(1, prediction)); // cy
                    if (suggestions.size() == maxSuggestions) break;
                    suggestions.add(new Suggestion(3, keystrokes)); // en
                }
                else {
                    throw new DictException("No such langCode: " + langCode);
                }
                if (suggestions.size() == maxSuggestions) break;
            }
            return new ArrayList<Suggestion>(suggestions);
        }
        catch (SQLException ex) {
            throw new DictException(ex);
        }
        finally {
            cur.close();
        }
    }

    List<Entry> getEntriesFromSql(String sql, String[] args, Lexicon.SearchTask asyncTask) throws DictException {
        SQLiteDatabase db = getDbOrNull();
        if (asyncTask.publishProgressAndCheckCancelled("") == true) return null;
        if (db == null) return null;
        Cursor cur = db.rawQuery(sql, args);
        try {
            int infoColumn = cur.getColumnIndex("info");
            List<Entry> entries = new ArrayList<Entry>();
            for (int i = 0; cur.moveToNext(); i++) {
                if (asyncTask.publishProgressAndCheckCancelled("") == true) return null;
                String info = cur.getString(infoColumn);
                entries.add(new EnCyEntry(info));
            }
            return entries;
        }
        catch (SQLException ex) {
            throw new DictException(ex);
        }
        finally {
            cur.close();
        }
    }

    // 'next string': dog -> doh etc
    private String getIncremented(String s) {
        if (s == null || s.equals("")) return s;
        return s.substring(0, s.length()-1) + ((char) (s.charAt(s.length()-1) + 1));
    }

    private String flatten(String s) {
        return s.toLowerCase();
    }
}

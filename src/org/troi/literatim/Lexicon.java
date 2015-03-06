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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.view.inputmethod.InputConnection;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Lexicon {

    public abstract class SearchTask<D> extends AsyncTask<String, String, D> {
        protected Throwable thrown = null;

        @Override
        public D doInBackground(String... searchList) {
            try {
                return doInBackground2(searchList);
            }
            catch (Throwable thrown) {
                this.thrown = thrown;
                return null;
            }
        }

        public abstract D doInBackground2(String... searchList) throws Throwable;

        public boolean publishProgressAndCheckCancelled(String value) {
            if (isCancelled()) return true;
            publishProgress(value);
            return false;
        }

        public Throwable getThrown() {
            return thrown;
        }
    }

    public abstract class GetSuggestionsTask extends SearchTask<List<Suggestion>> {
        @Override
        public List<Suggestion> doInBackground2(String... searchList) throws DictException {
           String code = searchList[0];
           String context = searchList[1];
           return getSuggestions(code, context, CandidateView.MAX_SUGGESTIONS, this);
        }
    }

    public abstract class GetEntriesTask extends SearchTask<List<Entry>> {
        @Override
        public List<Entry> doInBackground2(String... searchList) throws DictException {
            String code = searchList[0];
            String context = searchList[1];
            return getEntries(code, context, CandidateView.MAX_SUGGESTIONS, this);
        }
    }

    private String id;
    private File dbFile;
    private SQLiteDatabase dbOrNull;

    public Lexicon(String id, File fileDir) {
        this.id = id;
        dbFile = new File(fileDir, id + ".bin");
        dbOrNull = null;
    }

    public String getId() {
        return id;
    }

    public File getDbFile() {
        return dbFile;
    }

    public boolean needsFile() {
        return ! dbFile.exists();
    }

    public abstract String getName();
    
    public abstract Map<Integer, Integer> getColors();

    public abstract String getContextFast(InputConnection connection);

    public String getSuggestionText(String code, Suggestion suggestion) {
        return suggestion.getText();
    }

    public abstract List<Suggestion> getSuggestions(String search, String context, int maxSuggestions, SearchTask asyncTask) throws DictException;

    public abstract List<Entry> getEntries(String search, String context, int maxSuggestions, SearchTask asyncTask) throws DictException;

    /**
     * @return null if the db is simply not present
     * @throws DictException if the db is present but cannot be read properly
     */
    protected SQLiteDatabase getDbOrNull() throws DictException {
        tryOpen();
        return dbOrNull;
    }

    private void tryOpen() throws DictException {
        if (dbOrNull == null) {
            if (! dbFile.exists()) {
                return;
            }
            try {
                dbOrNull = SQLiteDatabase.openDatabase(dbFile.toString(), null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            }
            catch (SQLiteException ex) {
                return;
            }
        }
    }
}

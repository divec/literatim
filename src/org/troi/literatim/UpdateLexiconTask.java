/*
 * Copyright (C) 2011-2015 The Literatim authors
 * Copyright (C) 2008-2009 Google Inc.
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
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateLexiconTask extends AsyncTask<Context, Progress, Void> {
    //private final String baseUrl = "http://troi.org/literatim/data/yue-en";
    private final String baseUrl = "http://troi.org/literatim/data";
    private final String dbFilename = "literatim.bin";
    public Lexicon lexicon;
    SharedPreferences settings;
    final long CHECK_INTERVAL = 86400 * 1000; // millis between checks
    private Throwable thrown = null;

    public static int[] parseInts (String s) {
        if (s == null) return null;
        if ("".equals(s)) return new int[]{};
        String[] parts = s.split("\\|", -1);
        int ints[] = new int[parts.length];
        for(int i = 0; i < parts.length; i++) {
            ints[i] = Integer.parseInt(parts[i]);
        }
        return ints;
    }

    public static String[] parseStrings (String s) {
        if (s == null) return null;
        if ("".equals(s)) return new String[]{};
        String[] parts = s.split("\\|", -1);
        return parts;
    }

    public UpdateLexiconTask(Lexicon lexicon, SharedPreferences settings) {
        this.lexicon = lexicon;
        this.settings = settings;
    }

    @Override
    public Void doInBackground(Context... contextList) {
        Context context = contextList[0];
        try {
            inBackgroundDownload(context);
            publishProgress(new Progress[]{null});
        }
        catch(IOException ex) {
            thrown = ex;
            publishProgress(new Progress[]{null});
        }
        return null;
    }

    public void inBackgroundDownload(Context context) throws IOException {
        String name = lexicon.getName();
        String id = lexicon.getId();

        long lastCheck = settings.getLong("lastCheck_" + id, 0);
        int lastVersion = settings.getInt("lexVersion_" + id, 0);
        long now = System.currentTimeMillis();
        if (now - lastCheck <= CHECK_INTERVAL) return;

        publishProgress(new Progress(-1, -1, "checking latest " + name));
        LineDownloader latestLd = new LineDownloader(baseUrl + "/" + id + ".latest");
        int lexVersion;
        try {
            lexVersion = Integer.parseInt(latestLd.readLine());
        }
        finally {
            latestLd.close();
        }

        if (lexVersion <= lastVersion) { // up to date
            settings.edit().putLong("lastCheck_" + id, now).commit();
            return;
        }
        BytesDownloader dictBd = null;
        OutputStream output = null;
        File dbFile = lexicon.getDbFile();
        File tmpFile = new File(dbFile.toString() + ".tmp");

        try {
            dictBd = new BytesDownloader(baseUrl + "/" + id + "-" + lexVersion + ".bin");
            if (tmpFile.exists()) ioCheckTrue(tmpFile.delete(), "Could not delete " + tmpFile);
            output = new BufferedOutputStream(new FileOutputStream(tmpFile));
            byte[] buffer = new byte[8192];
            int length = (int) dictBd.getLength();
            int byteCount = 0;
            int readCount = 0;
            while(true) {
                int count = dictBd.read(buffer);
                if (count == -1) break;
                output.write(buffer, 0, count);
                byteCount += count;
                readCount++;
                if (readCount % 100 == 0) publishProgress(new Progress(length, byteCount, "Downloading " + name));
            }
            if (dbFile.exists()) ioCheckTrue(dbFile.delete(), "Could not delete " + dbFile);
            ioCheckTrue(tmpFile.renameTo(dbFile), "Could not rename " + tmpFile + " to " + dbFile);
            settings.edit().putInt("lexVersion_" + id, lexVersion).putLong("lastCheck_" + id, now).commit();
        }
        finally {
            if (dictBd != null) dictBd.close();
            if (output != null) output.close();
            if (tmpFile.exists()) tmpFile.delete(); // no check for failure; ok
        }
    }

    protected Throwable getThrown() {
        return thrown;
    }

    private void ioCheckTrue(boolean result, String message) throws IOException {
        if (! result) throw new IOException(message);
    }
}

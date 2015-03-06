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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.URL;
import java.net.URLConnection;

import android.util.Log;


public class LineDownloader {
    final String url;
    final URLConnection conn;
    final BufferedReader br;

    public LineDownloader(String url) throws IOException {
        this.url = url;
        conn = new URL(url).openConnection();
        br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    }

    public String readLine() throws IOException {
        return br.readLine();
    }

    public void close() {
        if (conn == null) return;
        try {
            conn.getInputStream().close();
        }
        catch (IOException ex) {
            // swallow
        }
    }

    protected void finalize() {
        close();
    }
}

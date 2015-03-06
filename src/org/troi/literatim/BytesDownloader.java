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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class BytesDownloader {
    final BufferedInputStream bis;
    long length;

    public BytesDownloader(String urlString) throws IOException {
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(10 * 1000);
        conn.setReadTimeout(30 * 1000);
        conn.connect();
        length = conn.getContentLength();
        bis = new BufferedInputStream(url.openStream());
    }

    public long getLength() {
        return length;
    }

    public int read(byte[] buffer) throws IOException {
        return bis.read(buffer);
    }

    public void close() {
        if (bis == null) return;
        try {
            bis.close();
        }
        catch (IOException ex) {
            // swallow
        }
    }

    protected void finalize() {
        close();
    }
}

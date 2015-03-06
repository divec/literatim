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

import android.app.ProgressDialog;

public class Progress {
    public int max;
    public int progress;
    public String message;

    public Progress(int max, int progress, String message) {
        this.max = max;
        this.progress = progress;
        this.message = message;
    }

    public int getStyle() {
        return (max == -1) ? ProgressDialog.STYLE_SPINNER : ProgressDialog.STYLE_HORIZONTAL;
    }
}

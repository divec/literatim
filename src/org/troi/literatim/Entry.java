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
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.Spannable;

public abstract class Entry {
    public static String join(String sep, String[] parts) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(sep);
            sb.append(parts[i]);
        }
        return sb.toString();
    }

    public static void addSpannedText(SpannableStringBuilder builder, String text, int typeface, int fgColor) {
        if (text == null || text.equals("")) return;
        int i = builder.length();
        builder.append(text + " ");
        int j = builder.length() - 1;
        builder.setSpan(new StyleSpan(typeface), i, j, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (fgColor != 0) {
            builder.setSpan(new ForegroundColorSpan(fgColor), i, j, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (typeface == Typeface.ITALIC) builder.append(" "); // extra space
    }
    
    public abstract CharSequence render();

    public abstract CharSequence[] getParts();

    public abstract String getForm(int item);
}

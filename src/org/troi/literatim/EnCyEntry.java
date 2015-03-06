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

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;


public class EnCyEntry extends Entry {
    public String lemma;
    public String pos;
    public String[] nounPlurals;
    public String disamb;
    public String[] ens;

    private int posColor = Color.rgb(127, 0, 0);
    private int disambColor = Color.rgb(96, 96, 96);
    
    public EnCyEntry(String info) {
        // fields:  lemma|pos|nounPlurals|disamb|en1; en2;...
        // example: ardal|nf|ardaloedd|region|area; quarter
        String[] fields = info.split("\\|", -1);
        if (fields.length != 5) throw new RuntimeException("Expected 5 fields: '" + info + "'");
        lemma = fields[0];
        pos = fields[1];
        nounPlurals = fields[2].equals("") ? new String[] {} : fields[2].split(";\\s*");
        disamb = fields[3];
        ens = fields[4].equals("") ? new String[] {} : fields[4].split(";\\s*");
    }

    public CharSequence render() {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        addSpannedText(sb, lemma, Typeface.BOLD, 0);
        addSpannedText(sb, pos, Typeface.ITALIC, posColor);
        addSpannedText(sb, join("; ", nounPlurals), Typeface.BOLD, 0);
        addSpannedText(sb, disamb, Typeface.ITALIC, disambColor);
        addSpannedText(sb, join("; ", ens), Typeface.NORMAL, 0);
        return sb;
    }

    public CharSequence[] getParts() {
        int n = 1 + nounPlurals.length + ens.length;
        if (! disamb.equals("")) n += 1;
        CharSequence[] parts = new CharSequence[n];
        int i = 0;
        SpannableStringBuilder sbLemmaPos = new SpannableStringBuilder();
        addSpannedText(sbLemmaPos, lemma, Typeface.BOLD, 0);
        addSpannedText(sbLemmaPos, pos, Typeface.ITALIC, posColor);
        parts[i++] = sbLemmaPos;
        for (String plural : nounPlurals) {
            SpannableStringBuilder sbPlural = new SpannableStringBuilder();
            addSpannedText(sbPlural, plural, Typeface.BOLD, 0);
            parts[i++] = sbPlural;
        }
        if (! disamb.equals("")) {
            SpannableStringBuilder sbDisamb = new SpannableStringBuilder();
            addSpannedText(sbDisamb, disamb, Typeface.ITALIC, disambColor);
            parts[i++] = sbDisamb;
        }
        for (String en : ens) {
            SpannableStringBuilder sbEn = new SpannableStringBuilder();
            addSpannedText(sbEn, en, Typeface.NORMAL, 0);
            parts[i++] = sbEn;
        }
        return parts;
    }

    public String getForm(int i) {
        if (i < 0) return null;
        if (i == 0) return lemma;
        i--;
        if (i < nounPlurals.length) return nounPlurals[i];
        i -= nounPlurals.length;
        if (! disamb.equals("")) {
            if (i == 0) return null;
            i--;
        }
        if (i < ens.length) return ens[i];
        return null;
    }
}

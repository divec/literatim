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

public class Suggestion {
    final int classCode;
    final String text;
    
    public Suggestion(int classCode, String text) {
        this.classCode = classCode;
        this.text = text;
    }

    public int getClassCode() {
        return classCode;
    }

    public String getText() {
        return text;
    }

    public String toString() {
        return "Suggestion(" + classCode + ", " + text + ")";
    }

    public int hashCode() {
        return 13 * classCode + 17 * text.hashCode();
    } 

    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Suggestion)) return false;
        Suggestion s = (Suggestion) other;
        return classCode == s.classCode && text.equals(s.text);
    }
}

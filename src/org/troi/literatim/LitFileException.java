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

import java.io.IOException;

public class LitFileException extends IOException {
    public LitFileException() {
    }

    public LitFileException(String message) {
        super(message);
    }

// XXX Not in java 1.5!
//    public LitFileException(String message, Throwable cause) {
//        super(message, cause);
//    }
//
//    public LitFileException(Throwable cause) {
//        super(cause);
//    }
}

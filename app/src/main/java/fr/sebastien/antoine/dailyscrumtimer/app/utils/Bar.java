/*
 * 	   Created by Daniel Nadeau
 * 	   daniel.nadeau01@gmail.com
 * 	   danielnadeau.blogspot.com
 * 
 * 	   Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package fr.sebastien.antoine.dailyscrumtimer.app.utils;

import android.graphics.Path;
import android.graphics.Region;

public class Bar {
    private int mColor;
    private String mName = null;
    private float mValue;
    private String mValueString = null;
    private Path mPath = null;
    private Region mRegion = null;

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        this.mColor = color;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public float getValue() {
        return mValue;
    }

    public void setValue(float value) {
        this.mValue = value;
    }

    public String getValueString() {
        if (mValueString != null) {
            return mValueString;
        } else {
            return String.valueOf(mValue);
        }
    }

    public void setValueString(final String valueString) {
        mValueString = valueString;
    }

    public Path getPath() {
        return mPath;
    }

    public void setPath(Path path) {
        this.mPath = path;
    }

    public Region getRegion() {
        return mRegion;
    }

    public void setRegion(Region region) {
        this.mRegion = region;
    }
}
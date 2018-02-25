/*
 * Copyright 2018 Albert Santos.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.leeboardtools.util;

import com.leeboardtools.json.JSONValue;

/**
 * Some classes for tracking version numbering.
 * @author Albert Santos
 */
public class Version {
    
    public static class VersionFormatException extends IllegalArgumentException {
        public VersionFormatException() {
        }
        public VersionFormatException(String msg) {
            super(msg);
        }
    }
    
    
    /**
     * Tracks a version of the format major.minor.build, as in 1.2.3.
     * These objects are immutable.
     */
    public static class Full {
        private final int majorVersion;
        private final int minorVersion;
        private final int buildNumber;
        
        public static final int NO_BUILD_NUMBER = -1;

        /**
         * Constructor taking all three version numbers.
         * @param majorVersion  The major version.
         * @param minorVersion  The minor version.
         * @param buildNumber The build number.
         */
        public Full(int majorVersion, int minorVersion, int buildNumber) {
            this.majorVersion = majorVersion;
            this.minorVersion = minorVersion;
            this.buildNumber = buildNumber;
        }
        

        /**
         * Constructor that ignores the build number.
         * @param majorVersion  The major version.
         * @param minorVersion  The minor version.
         */
        public Full(int majorVersion, int minorVersion) {
            this(majorVersion, minorVersion, NO_BUILD_NUMBER);
        }

        /**
         * @return The major version number.
         */
        public final int getMajorVersion() {
            return majorVersion;
        }

        /**
         * @return The minor version number.
         */
        public final int getMinorVersion() {
            return minorVersion;
        }

        /**
         * @return The build number, {@link #NO_BUILD_NUMBER} if the build number should be ignored.
         */
        public final int getBuildNumber() {
            return buildNumber;
        }
        
        /**
         * @return The next builder number if build numbers are not ignored.
         */
        public final int nextBuildNumber() {
            return (buildNumber != NO_BUILD_NUMBER) ? buildNumber + 1 : buildNumber;
        }

        /**
         * @return Returns a version object with the build number incremented.
         * @throws IllegalStateException if the build number is {@link #NO_BUILD_NUMBER}.
         */
        public final Full incrementBuildNumber() {
            if (buildNumber == NO_BUILD_NUMBER) {
                throw new IllegalStateException("The build number is ignored and cannot be incremented.");
            }
            return new Full(majorVersion, minorVersion, nextBuildNumber());
        }

        /**
         * @return Returns a version object with the minor version number incremented. The build
         * number is also incremented if build numbers are not ignored.
         */
        public final Full incrementMinorVersion() {
            return new Full(majorVersion, minorVersion + 1, nextBuildNumber());
        }

        /**
         * @return Returns a version object with the major version number incremented. The minor
         * version is set to 0. The build number is incremented if build numbers are not ignored.
         */
        public final Full incrementMajorVersion() {
            return new Full(majorVersion + 1, 0, nextBuildNumber());
        }
        
        
        /**
         * Compares this version against another version.
         * <p>The major version numbers have precedence, followed by the minor version numbers.
         * The build numbers are compared only if the major and minor version numbers are identical.
         * @param other The version to compare to.
         * @return &lt; 0 if this version is 'older' than other, 0 if the same, 
         * &gt; 0 if this version is 'newer' than other.
         */
        int compare(Full other) {
            int result = majorVersion - other.majorVersion;
            if (result != 0) {
                return result;
            }
            
            result = minorVersion - other.minorVersion;
            if (result != 0) {
                return result;
            }
            
            return buildNumber - other.buildNumber;
        }
        
        /**
         * Retrieves a {@link Full} version object from a string. The string should be in the
         * format generated by {@link #toString() }.
         * @param text  The text to parse.
         * @return The version.
         * @throws VersionFormatException if there's a problem with the text format.
         */
        public static Full parse(String text) {
            text = text.trim();
            String [] versions = text.split(".");
            if (versions.length < 2) {
                throw new VersionFormatException();
            }
            
            try {
                int majorVersion = Integer.parseInt(versions[0]);
                int minorVersion = Integer.parseInt(versions[1]);
                int buildNumber = (versions.length > 2) ? Integer.parseInt(versions[2]) : NO_BUILD_NUMBER;
                return new Full(majorVersion, minorVersion, buildNumber);
            } catch (NumberFormatException ex) {
                throw new VersionFormatException(ex.getLocalizedMessage());
            }
            
        }

        
        @Override
        public String toString() {
            if (buildNumber >= 0) {
                return majorVersion + "." + minorVersion + "." + buildNumber;
            }
            else {
                return majorVersion + "." + minorVersion;
            }
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 53 * hash + this.majorVersion;
            hash = 53 * hash + this.minorVersion;
            hash = 53 * hash + this.buildNumber;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Full other = (Full) obj;
            if (this.majorVersion != other.majorVersion) {
                return false;
            }
            if (this.minorVersion != other.minorVersion) {
                return false;
            }
            if (this.buildNumber != other.buildNumber) {
                return false;
            }
            return true;
        }
    }

    
    /**
     * Generates a {@link JSONValue} object from a {@link Full} version object.
     * @param version   The version, may be <code>null</code>
     * @return The JSON value object.
     */
    public static JSONValue toJSONValue(Full version) {
        if (version == null) {
            return new JSONValue();
        }
        return new JSONValue(version.toString());
    }
    
    /**
     * Retrieves the {@link Full} version object represented by a JSON value.
     * @param jsonValue The JSON value.
     * @return The version object, <code>null</code> if jsonValue is <code>null</code>
     * or its {@link JSONValue#isNull() } returns <code>true</code>.
     */
    public static Full fullFromJSON(JSONValue jsonValue) {
        if ((jsonValue == null) || jsonValue.isNull()) {
            return null;
        }
        return Full.parse(jsonValue.getStringValue());
    }

    
    /**
     * Tracks a simple version using a <code>long</code>.
     * These objects are immutable.
     */
    public static class Simple {
        private final long version;
        
        /**
         * Constructor.
         * @param version   The version number.
         */
        public Simple(long version) {
            this.version = version;
        }
        
        /**
         * @return The version number.
         */
        public final long getVersion() {
            return version;
        }
        
        /**
         * @return Retrieves a {@link Simple} version object with the next version number.
         */
        public final Simple incrementVersion() {
            return new Simple(version + 1);
        }
        
        /**
         * Compares this version with another version.
         * @param other The version to compare to.
         * @return -1 if this version is less than other, 0 if the same, 1 if greater.
         */
        public final int compare(Simple other) {
            long result = version - other.version;
            if (result == 0) {
                return 0;
            }
            return (result < 0) ? -1 : 1;
        }
        
        /**
         * Parses a string into a {@link Simple} version.
         * @param text  The text to parse.
         * @return The version object.
         */
        public static Simple parse(String text) {
            return new Simple(Long.parseLong(text));
        }

        @Override
        public String toString() {
            return Long.toString(version);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + (int) (this.version ^ (this.version >>> 32));
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Simple other = (Simple) obj;
            if (this.version != other.version) {
                return false;
            }
            return true;
        }
        
    }

    
    /**
     * Retrieves a {@link JSONValue} object representing a {@link Simple} version.
     * @param version   The version, may be <code>null</code>.
     * @return The JSON value object.
     */
    public static JSONValue toJSONValue(Simple version) {
        if (version == null) {
            return new JSONValue();
        }
        return new JSONValue(version.toString());
    }
    
    /**
     * Retrieves a {@link Simple} version from a {@Link JSONValue} object.
     * @param jsonValue The JSON value.
     * @return The version object, <code>null</code> if jsonValue is <code>null</code>
     * or its {@link JSONValue#isNull() } returns <code>true</code>.
     */
    public static Simple simpleFromJSON(JSONValue jsonValue) {
        if ((jsonValue == null) || jsonValue.isNull()) {
            return null;
        }
        return Simple.parse(jsonValue.getStringValue());
    }
}

/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.ai.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Utilities for AI resource ZIP artifacts.
 *
 * @author nacos
 */
public final class AiResourceStorageUtils {
    
    private static final char[] HEX = "0123456789ABCDEF".toCharArray();
    
    public static final String OSS_PROVIDER = "oss";
    
    public static final String ZIP_FORMAT = "zip";
    
    public static final String BUNDLE_FILE_NAME = "bundle.zip";
    
    private AiResourceStorageUtils() {
    }
    
    /**
     * Build the OSS artifact key shared by all AI resource types.
     *
     * @param namespaceId namespace ID
     * @param resourceType resource type
     * @param resourceName resource name
     * @param version version
     * @return encoded artifact key
     */
    public static String buildBundleKey(String namespaceId, String resourceType,
        String resourceName, String version) {
        return encodePathSegment(namespaceId) + "/" + encodePathSegment(resourceType) + "/"
            + encodePathSegment(resourceName) + "/" + encodePathSegment(version) + "/"
            + BUNDLE_FILE_NAME;
    }
    
    /**
     * Create a deterministic ZIP containing one file.
     *
     * @param entryName entry name
     * @param content entry content
     * @return ZIP bytes
     * @throws IOException when the ZIP cannot be created
     */
    public static byte[] zipSingleEntry(String entryName, byte[] content) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
            ZipOutputStream zipOutput = new ZipOutputStream(output)) {
            ZipEntry entry = new ZipEntry(entryName);
            entry.setTime(0L);
            zipOutput.putNextEntry(entry);
            zipOutput.write(content);
            zipOutput.closeEntry();
            zipOutput.finish();
            return output.toByteArray();
        }
    }
    
    /**
     * Read the only file from a single-entry ZIP.
     *
     * @param zipBytes ZIP bytes
     * @param expectedEntryName expected entry name
     * @return entry content
     * @throws IOException when the ZIP does not contain exactly the expected file
     */
    public static byte[] readSingleEntry(byte[] zipBytes, String expectedEntryName)
        throws IOException {
        byte[] result = null;
        try (ZipInputStream zipInput =
            new ZipInputStream(new ByteArrayInputStream(zipBytes), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zipInput.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                if (result != null || !expectedEntryName.equals(entry.getName())) {
                    throw new IOException("Unexpected AI resource ZIP entry: " + entry.getName());
                }
                result = zipInput.readAllBytes();
            }
        }
        if (result == null) {
            throw new IOException("AI resource ZIP entry not found: " + expectedEntryName);
        }
        return result;
    }
    
    static String encodePathSegment(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        StringBuilder result = new StringBuilder(bytes.length);
        for (byte current : bytes) {
            int unsigned = current & 0xFF;
            if (isUnreserved(unsigned)) {
                result.append((char) unsigned);
            } else {
                result.append('%');
                result.append(HEX[unsigned >>> 4]);
                result.append(HEX[unsigned & 0x0F]);
            }
        }
        if (".".contentEquals(result)) {
            return "%2E";
        }
        if ("..".contentEquals(result)) {
            return "%2E%2E";
        }
        return result.toString();
    }
    
    private static boolean isUnreserved(int value) {
        return value >= 'a' && value <= 'z' || value >= 'A' && value <= 'Z'
            || value >= '0' && value <= '9' || value == '-' || value == '_'
            || value == '.' || value == '~';
    }
}

/* 
 * The MIT License
 * Copyright (c) 2011 Paul Soucy (paul@dev-smart.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package com.mottimotti.fungo;


import android.content.Context;
import android.os.Build;
import android.os.Environment;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.security.MessageDigest;

public class JsonFileCache {
    private static JsonFileCache mSingleton;
    protected static final byte[] Hexhars = {
            '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f'
    };

    public static JsonFileCache getFileCache(String cacheDirPath) throws IOException {
        synchronized (JsonFileCache.class) {
            if (mSingleton == null) mSingleton = new JsonFileCache(cacheDirPath);
            return mSingleton;
        }
    }

    private static final String CACHE_DIR = "cache";

    private final File mCacheDir;

    public File getCacheDir() {
        return mCacheDir;
    }

    private JsonFileCache(String cacheDirPath) throws IOException {
        mCacheDir = new File(cacheDirPath, CACHE_DIR);
        if (!mCacheDir.exists()) {
            if (!mCacheDir.mkdir()) {
                throw new IOException("Cannot create cache directory: " + mCacheDir.getAbsolutePath());
            }
        }
    }

    public String getCacheFilename(String key) {
        try {
            return getSha1Hash(key);
        } catch (Exception e) {
            return null;
        }
    }

    public synchronized void put(String key, JSONObject jsonObject) throws IOException {
        String cacheFilename = getCacheFilename(key);
        File cacheFile = getCacheFile(cacheFilename);

        if (!mCacheDir.exists()) mCacheDir.mkdir();
        if (!cacheFile.exists()) cacheFile.createNewFile();

        FileOutputStream os = new FileOutputStream(cacheFile);
        os.write(jsonObject.toString().getBytes("UTF-8"));
        os.close();
    }

    private File getCacheFile(String filename) {
        return new File(mCacheDir, filename);
    }

    public synchronized JSONObject get(String key) {
        File cacheFile = getCacheFile(getCacheFilename(key));
        return fileToJSON(cacheFile);
    }

    public synchronized JSONObject get(File cacheFile) {
        return fileToJSON(cacheFile);
    }

    private JSONObject fileToJSON(File cacheFile) {
        if (cacheFile.exists() && cacheFile.isFile() && cacheFile.canRead()) {
            try {
                FileInputStream fis = new FileInputStream(cacheFile);
                JSONObject jo = new JSONObject(readAll(fis));
                fis.close();
                return jo;
            } catch (FileNotFoundException e) {
                return null;
            } catch (JSONException e) {
                return null;
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    public String readAll(final InputStream is) throws IOException {
        if (null == is) {
            throw new IllegalArgumentException(JsonFileCache.class.getName() + ".readAll() was passed a null stream!");
        }
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        return builder.toString();
    }

    public static String getCacheDir(Context context) {
        String cacheDirPath = "";
        int currentapiVersion = Build.VERSION.SDK_INT;

        if( currentapiVersion >= Build.VERSION_CODES.ECLAIR_MR1){
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                cacheDirPath = Environment.getExternalStorageDirectory() + "/Android/data/" + context.getPackageName();
            }
        }  else {
            if (context.getExternalCacheDir() != null) {
                cacheDirPath = context.getExternalCacheDir().getParent();
            } else {
                cacheDirPath = context.getCacheDir().getParent();
            }
        }

        File cacheDir = new File(cacheDirPath);
        if (!cacheDir.exists()) cacheDir.mkdir();

        return cacheDirPath;
    }

    private static String getSha1Hash(String input) throws Exception {
        String retval = null;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes("UTF-8"));
        MessageDigest hash = MessageDigest.getInstance("SHA1");
        byte[] buffer = new byte[1024];

        int numRead=0;
        while((numRead=inputStream.read(buffer)) != -1){
            hash.update(buffer, 0, numRead);
        }

        retval = toHexString(hash.digest());
        return retval;
    }

    private static String toHexString(byte[] input) {
        StringBuilder buf = new StringBuilder(2 * input.length);
        for(int i=0;i<input.length;i++) {
            int v = input[i] & 0xff;
            buf.append((char)Hexhars[v >> 4]);
            buf.append((char)Hexhars[v & 0xf]);
        }
        return buf.toString();
    }
}

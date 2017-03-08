package com.jifalops.localization.util;


import android.util.Log;

import com.jifalops.localization.App;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Override's ArrayList.
 * Only {@link #add(String)}, {@link #addAll(Collection)}, and {@link #clear()} are supported.
 */
public class FileBackedArrayList extends ArrayList<String> {
    private static final String TAG = FileBackedArrayList.class.getSimpleName();

    protected final File file;

    public FileBackedArrayList(File file, Runnable onLoad) {
        this.file = file;
        this.loadFromDisk(onLoad);
    }

    @Override
    public boolean add(String s) {
        writeLines(Collections.singletonList(s));
        return super.add(s);
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        writeLines(c);
        return super.addAll(c);
    }

    @Override
    public void clear() {
        truncate();
        super.clear();
    }

    private void loadFromDisk(final Runnable onLoad) {
        if (!file.exists()) return;
        App.getInstance().getService().post(new Runnable() {
            @Override
            public void run() {
                BufferedReader reader = null;
                synchronized (file) {
                    try {
                        reader = new BufferedReader(new FileReader(file));
                        String line;
                        while ((line = reader.readLine()) != null && line.length() > 0) {
                            FileBackedArrayList.super.add(line);
                        }
                    } catch (FileNotFoundException ignored) {
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to read file: " + e.getMessage());
                    } finally {
                        try {
                            if (reader != null) reader.close();
                        } catch (IOException e) {
                            Log.e(TAG, "Failed to close reader: " + e.getMessage());
                        }
                    }
                }
                if (onLoad != null) App.getInstance().post(onLoad);
            }
        });
    }

    private void writeLines(final Collection<? extends String> lines) {
        if (!file.exists()) return;
        App.getInstance().getService().post(new Runnable() {
            @Override
            public void run() {
                BufferedWriter writer = null;
                synchronized (file) {
                    try {
                        writer = new BufferedWriter(new FileWriter(file, true));
                        for (String line : lines) {
                            writer.write(line);
                            writer.newLine();
                        }
                        writer.flush();
                    } catch (FileNotFoundException ignored) {
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to write lines: " + e.getMessage());
                    } finally {
                        try {
                            if (writer != null) writer.close();
                        } catch (IOException e) {
                            Log.e(TAG, "Failed to close writer: " + e.getMessage());
                        }
                    }
                }
            }
        });
    }

    private void truncate() {
        if (!file.exists()) return;
        App.getInstance().getService().post(new Runnable() {
            @Override
            public void run() {
                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(new FileWriter(file, false));
                } catch (FileNotFoundException ignored) {
                } catch (IOException e) {
                    Log.e(TAG, "Failed to truncate file: " + e.getMessage());
                } finally {
                    try {
                        if (writer != null) writer.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to close writer: " + e.getMessage());
                    }
                }
            }
        });
    }


    @Override
    public void add(int index, String element) {
//        throw new Exception("Insertion not allowed.");
    }

    @Override
    public boolean addAll(int index, Collection<? extends String> c) {
        return false;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {

    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public String remove(int index) {
        return null;
    }
}

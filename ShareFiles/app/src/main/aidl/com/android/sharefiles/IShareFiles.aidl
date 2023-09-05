package com.android.sharefiles;

import android.os.ParcelFileDescriptor;

interface IShareFiles {
    List<String> getAvailableFiles();
    ParcelFileDescriptor getFileDescriptor(String file);
}
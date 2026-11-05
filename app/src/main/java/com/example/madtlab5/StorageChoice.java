package com.example.madtlab5;

enum StorageChoice {
    SQLITE,
    SHARED_PREFERENCES;

    static StorageChoice fromPosition(int position) {
        return position == 1 ? SHARED_PREFERENCES : SQLITE;
    }

    int position() { return this == SQLITE ? 0 : 1; }
}

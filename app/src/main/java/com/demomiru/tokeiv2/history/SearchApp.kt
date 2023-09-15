package com.demomiru.tokeiv2.history

import android.app.Application

class SearchApp : Application() {
    val db by lazy {
        SearchDatabase.getInstance(this)
    }
}
package com.mohithash.cram

import android.app.Application
import androidx.room.Room
import com.mohithash.cram.ai.AiClient
import com.mohithash.cram.ai.StudyAi
import com.mohithash.cram.data.AppDb
import com.mohithash.cram.data.JsonStore

class App : Application() {
    lateinit var db: AppDb
    lateinit var store: JsonStore
    val client = AiClient()
    val study by lazy { StudyAi(client) }
    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(this, AppDb::class.java, "cram.db").build()
        store = JsonStore(this)
    }
}

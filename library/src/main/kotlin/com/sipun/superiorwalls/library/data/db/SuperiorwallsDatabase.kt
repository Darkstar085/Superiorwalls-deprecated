package com.sipun.superiorwalls.library.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sipun.superiorwalls.library.data.models.Favorite
import com.sipun.superiorwalls.library.data.models.Wallpaper

@Database(
    entities = [Wallpaper::class, Favorite::class],
    version = 4,
    exportSchema = false
)
abstract class SuperiorwallsDatabase : RoomDatabase() {
    abstract fun wallpapersDao(): WallpaperDao?
    abstract fun favoritesDao(): FavoritesDao?

    companion object {
        private var INSTANCE: SuperiorwallsDatabase? = null

        fun getAppDatabase(context: Context): SuperiorwallsDatabase? {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    SuperiorwallsDatabase::class.java,
                    context.applicationInfo.name ?: "Superiorwalls"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}

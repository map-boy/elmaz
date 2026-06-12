package com.nyumbahub.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nyumbahub.core.data.local.dao.ListingDao
import com.nyumbahub.core.data.local.entity.ListingEntity

@Database(
    entities = [ListingEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NyumbaHubDatabase : RoomDatabase() {
    abstract fun listingDao(): ListingDao
}

package io.github.arenoros.anime365.core.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.arenoros.anime365.core.database.CacheDatabase
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

fun inMemoryCacheDatabase(): CacheDatabase =
    Room
        .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), CacheDatabase::class.java)
        .allowMainThreadQueries()
        .build()

/** 23.10.2026 15:00 по Москве — сезон «Осень 2026». */
class MutableClock(
    var instant: Instant = Instant.parse("2026-10-23T12:00:00Z"),
) : Clock() {
    override fun getZone(): ZoneId = ZoneOffset.UTC

    override fun withZone(zone: ZoneId): Clock = fixed(instant, zone)

    override fun instant(): Instant = instant

    fun advanceMinutes(minutes: Long) {
        instant = instant.plusSeconds(minutes * 60)
    }
}

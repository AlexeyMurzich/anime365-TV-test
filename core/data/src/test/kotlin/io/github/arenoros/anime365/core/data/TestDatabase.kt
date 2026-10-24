package io.github.arenoros.anime365.core.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.arenoros.anime365.core.database.CacheDatabase
import io.github.arenoros.anime365.core.database.library.LibraryDatabase
import io.github.arenoros.anime365.core.datastore.AccountStore
import io.github.arenoros.anime365.core.datastore.AppSettings
import io.github.arenoros.anime365.core.datastore.SettingsStore
import io.github.arenoros.anime365.core.datastore.StoredAccount
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

fun inMemoryCacheDatabase(): CacheDatabase =
    Room
        .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), CacheDatabase::class.java)
        .allowMainThreadQueries()
        .build()

fun inMemoryLibraryDatabase(): LibraryDatabase =
    Room
        .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), LibraryDatabase::class.java)
        .allowMainThreadQueries()
        .build()

/** Android Keystore в Robolectric нет: Токен хранится как есть. */
class FakeAccountStore(
    initial: StoredAccount? = null,
) : AccountStore {
    override val account = MutableStateFlow(initial)

    override suspend fun save(account: StoredAccount) {
        this.account.value = account
    }

    override suspend fun clear() {
        account.value = null
    }
}

class FakeSettingsStore : SettingsStore {
    override val libraryHintShown = MutableStateFlow(false)

    override suspend fun setLibraryHintShown() {
        libraryHintShown.value = true
    }

    override val settings = MutableStateFlow(AppSettings())

    override suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        settings.value = transform(settings.value)
    }
}

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

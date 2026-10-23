package io.github.arenoros.anime365.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DataStoreAccountStore
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
        private val cipher: TokenCipher,
    ) : AccountStore {
        override val account: Flow<StoredAccount?> =
            dataStore.data.map { prefs ->
                val token = prefs[TOKEN]?.let(cipher::decrypt) ?: return@map null
                StoredAccount(token, prefs[NAME].orEmpty(), prefs[SUBSCRIPTION_UNTIL])
            }

        override suspend fun save(account: StoredAccount) {
            dataStore.edit { prefs ->
                prefs[TOKEN] = cipher.encrypt(account.accessToken)
                prefs[NAME] = account.name
                val until = account.subscriptionUntil
                if (until != null) prefs[SUBSCRIPTION_UNTIL] = until else prefs.remove(SUBSCRIPTION_UNTIL)
            }
        }

        override suspend fun clear() {
            dataStore.edit { it.clear() }
        }

        private companion object {
            val TOKEN = stringPreferencesKey("access_token")
            val NAME = stringPreferencesKey("name")
            val SUBSCRIPTION_UNTIL = longPreferencesKey("subscription_until")
        }
    }

package io.github.arenoros.anime365.core.datastore

import kotlinx.coroutines.flow.Flow

/** Вошедший пользователь. [subscriptionUntil] — миллисекунды эпохи, `null` — Подписки нет. */
data class StoredAccount(
    val accessToken: String,
    val name: String,
    val subscriptionUntil: Long?,
)

/**
 * Токен доступа и то, что о пользователе сказал `/api/me`. Токен на диске зашифрован ключом
 * Android Keystore (ADR 0005), файл не попадает в резервную копию: ключ в неё не входит.
 */
interface AccountStore {
    /** `null` — гость, или Токен больше не расшифровать (ключ Keystore пропал). */
    val account: Flow<StoredAccount?>

    suspend fun save(account: StoredAccount)

    suspend fun clear()
}

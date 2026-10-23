package io.github.arenoros.anime365.feature.account

import android.net.Uri
import io.github.arenoros.anime365.core.data.repository.ImportMode
import io.github.arenoros.anime365.core.data.repository.LibraryFileSummary
import io.github.arenoros.anime365.core.data.repository.UnsupportedLibraryFileException
import io.github.arenoros.anime365.core.testing.FakeAccountRepository
import io.github.arenoros.anime365.core.testing.FakeLibraryTransfer
import io.github.arenoros.anime365.core.testing.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/** Экспорт и Импорт Списка из Профиля; Robolectric — ради настоящего [Uri]. */
@RunWith(RobolectricTestRunner::class)
class LibraryTransferViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val transfer = FakeLibraryTransfer()
    private val file = Uri.parse("content://downloads/library.json")
    private val viewModel =
        AccountViewModel(
            FakeAccountRepository(),
            transfer,
            Clock.fixed(Instant.parse("2026-10-23T22:30:00Z"), ZoneId.of("UTC")),
        )

    @Test
    fun fileNameHasLocalDate() {
        assertEquals("anime365-library-2026-10-24.json", viewModel.exportFileName(ZoneId.of("Europe/Moscow")))
    }

    @Test
    fun resultsAreReportedForSnackbar() =
        runTest(mainDispatcherRule.dispatcher) {
            val results = mutableListOf<TransferResult>()
            backgroundScope.launch { viewModel.transferResults.collect { results += it } }

            transfer.exportResult = Result.success(LibraryFileSummary(inList = 1, total = 5))
            viewModel.export(file)
            transfer.exportResult = Result.failure(IOException("disk full"))
            viewModel.export(file)
            transfer.importResult = Result.success(LibraryFileSummary(inList = 3, total = 3))
            viewModel.import(file, ImportMode.Merge)
            transfer.importResult = Result.failure(UnsupportedLibraryFileException(2))
            viewModel.import(file, ImportMode.Replace)
            transfer.importResult = Result.failure(IllegalArgumentException("not a library"))
            viewModel.import(file, ImportMode.Replace)

            assertEquals(
                listOf(
                    TransferResult.Exported(LibraryFileSummary(1, 5)),
                    TransferResult.ExportFailed,
                    TransferResult.Imported(LibraryFileSummary(3, 3)),
                    TransferResult.NewerFile,
                    TransferResult.ImportFailed,
                ),
                results,
            )
            assertEquals(
                listOf(file to ImportMode.Merge, file to ImportMode.Replace, file to ImportMode.Replace),
                transfer.imports,
            )
        }
}

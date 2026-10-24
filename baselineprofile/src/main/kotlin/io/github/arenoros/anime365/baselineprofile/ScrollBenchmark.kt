package io.github.arenoros.anime365.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Первая прокрутка Главной и Каталога после холодного запуска, без профиля и с ним. Без профиля
 * код списков в этот момент ещё интерпретируется — кадры дольше. Меряется на телефоне.
 */
@RunWith(AndroidJUnit4::class)
class ScrollBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun scrollWithoutProfile() = scroll(CompilationMode.None())

    @Test
    fun scrollWithProfile() = scroll(CompilationMode.Partial(BaselineProfileMode.Require))

    private fun scroll(mode: CompilationMode) =
        rule.measureRepeated(
            packageName = PACKAGE_NAME,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = mode,
            iterations = ITERATIONS,
            // Не StartupMode.COLD: он убивает процесс уже после setupBlock, и прокручивать было бы нечего.
            // Каждый замер — с нового процесса: первая прокрутка, пока код списков не прогрет.
            setupBlock = {
                killProcess()
                startActivityAndWait()
                waitForHome()
            },
        ) {
            scrollList()
            openTab("Каталог")
            scrollList()
        }

    private companion object {
        const val ITERATIONS = 10
    }
}

package io.github.arenoros.anime365.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until

internal const val PACKAGE_NAME = "io.github.arenoros.anime365"

private const val TIMEOUT_MS = 10_000L

/**
 * Обычный путь по приложению: Главная, Каталог и первое аниме в нём, Список, Профиль.
 * Без сети вместо списков — экран ошибки; путь всё равно проходится до конца.
 */
internal fun MacrobenchmarkScope.browse() {
    scrollList()
    openTab("Каталог")
    scrollList()
    device.findObject(By.scrollable(true))?.children?.firstOrNull()?.let { card ->
        card.click()
        device.waitForIdle()
        scrollList()
        device.pressBack()
        device.waitForIdle()
    }
    openTab("Список")
    openTab("Профиль")
    openTab("Главная")
}

private fun MacrobenchmarkScope.openTab(label: String) {
    device.findObject(By.text(label))?.click()
    device.waitForIdle()
}

private fun MacrobenchmarkScope.scrollList() {
    val list = device.wait(Until.findObject(By.scrollable(true)), TIMEOUT_MS) ?: return
    // Отступ от краёв: жест от самого края экрана система принимает за «Назад».
    list.setGestureMargin(device.displayWidth / 5)
    list.fling(Direction.DOWN)
    device.waitForIdle()
    list.fling(Direction.UP)
    device.waitForIdle()
}

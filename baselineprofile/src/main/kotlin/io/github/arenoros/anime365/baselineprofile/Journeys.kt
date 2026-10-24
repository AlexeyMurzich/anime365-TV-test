package io.github.arenoros.anime365.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import java.util.regex.Pattern

internal const val PACKAGE_NAME = "io.github.arenoros.anime365"

private const val TIMEOUT_MS = 10_000L

// Главная кнопка экрана Аниме: «НАЧАТЬ ПРОСМОТР» или «ПРОДОЛЖИТЬ · 7 серия».
private val WATCH_BUTTON = Pattern.compile("(?i)(начать|продолжить).*")

/**
 * Обычный путь по приложению: Главная, Каталог и первое аниме в нём, Список, Профиль.
 * Без сети вместо списков — экран ошибки; путь всё равно проходится до конца.
 */
internal fun MacrobenchmarkScope.browse() {
    scrollList()
    openTab("Каталог")
    scrollList()
    mainList()?.findObject(By.clickable(true))?.let { card ->
        card.click()
        device.wait(Until.hasObject(By.text(WATCH_BUTTON)), TIMEOUT_MS)
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

/** Самый высокий прокручиваемый список: в Каталоге над сеткой аниме — ряд фильтров, тоже прокручиваемый. */
private fun MacrobenchmarkScope.mainList(): UiObject2? {
    device.wait(Until.hasObject(By.scrollable(true)), TIMEOUT_MS)
    return device.findObjects(By.scrollable(true)).maxByOrNull { it.visibleBounds.height() }
}

private fun MacrobenchmarkScope.scrollList() {
    val list = mainList() ?: return
    // Отступ от краёв: жест от самого края экрана система принимает за «Назад».
    list.setGestureMargin(device.displayWidth / 5)
    list.fling(Direction.DOWN)
    device.waitForIdle()
    list.fling(Direction.UP)
    device.waitForIdle()
}

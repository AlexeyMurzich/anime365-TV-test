# Галерея anime365

28 снимков работающего приложения: основные экраны, нижние листы, меню и
диалог импорта. Каждое изображение открывает исходный PNG.

Снято 25 октября 2026 года на Samsung Galaxy Note10 (SM-N970F), Android 12,
в тёмной теме. Портретные снимки — 1080 × 2280, плеер в ландшафте —
2280 × 1080. Использовались релизные сборки 0.1.0/0.1.1 и отдельная локальная
dev-сборка для гостевых сценариев и демонстрационной Библиотеки.
Постеры, список переводов и кадр видео получены в приложении.

## Основные экраны

| Главная | Каталог | Список |
|:---:|:---:|:---:|
| [<img src="home.png" width="260" alt="Главная: продолжить просмотр и новые серии">](home.png) | [<img src="catalog.png" width="260" alt="Каталог с постерами, рейтингами и фильтрами">](catalog.png) | [<img src="library.png" width="260" alt="Список аниме со статусом Смотрю">](library.png) |

| Гостевой профиль | Настройки | Скачанное |
|:---:|:---:|:---:|
| [<img src="profile.png" width="260" alt="Профиль без входа в аккаунт">](profile.png) | [<img src="settings.png" width="260" alt="Настройки уведомлений, просмотра и скачивания">](settings.png) | [<img src="downloads.png" width="260" alt="Пустая вкладка Скачанное">](downloads.png) |

## Фильтры каталога

Выбор сезона доступен после выбора года.

| Тип | Год |
|:---:|:---:|
| [<img src="catalog-type.png" width="300" alt="Выбор типа аниме: ТВ, Фильм, OVA, ONA, Спешл, Клип">](catalog-type.png) | [<img src="catalog-year.png" width="300" alt="Выбор года выпуска">](catalog-year.png) |

| Сезон | Жанры |
|:---:|:---:|
| [<img src="catalog-season.png" width="300" alt="Выбор сезона за 2026 год">](catalog-season.png) | [<img src="catalog-genres.png" width="300" alt="Жанры и режим совпадения всех или любого выбранного жанра">](catalog-genres.png) |

## Страница аниме

| Описание | Серии | Статус в Списке |
|:---:|:---:|:---:|
| [<img src="series.png" width="260" alt="Фрирен: постер, рейтинг, жанры и описание">](series.png) | [<img src="series-episodes.png" width="260" alt="Трейлер и сетка из 28 серий">](series-episodes.png) | [<img src="series-status.png" width="260" alt="Выбор статуса: Смотрю, Запланировано, Просмотрено, Брошено">](series-status.png) |

| Действия с серией | Меню страницы |
|:---:|:---:|
| [<img src="episode-actions.png" width="300" alt="Скачать серию, скачать следующие или отметить просмотренной">](episode-actions.png) | [<img src="series-menu.png" width="300" alt="Открыть аниме на сайте или поделиться ссылкой">](series-menu.png) |

## Выбор перевода

| После входа | Без входа |
|:---:|:---:|
| [<img src="translations.png" width="300" alt="Команды озвучки и отметка ранее выбранного перевода">](translations.png) | [<img src="translations-guest.png" width="300" alt="Озвучки с пояснением о необходимости входа">](translations-guest.png) |

| Русские субтитры | Скачивание серии |
|:---:|:---:|
| [<img src="translations-subtitles.png" width="300" alt="Выбор перевода с русскими субтитрами">](translations-subtitles.png) | [<img src="translations-download.png" width="300" alt="Выбор перевода для скачивания одной серии">](translations-download.png) |

## Плеер

| Портрет | Управление |
|:---:|:---:|
| [<img src="player.png" width="300" alt="Видео и список серий в портретном режиме">](player.png) | [<img src="player-controls.png" width="300" alt="Пауза, перемотка, прогресс и переход в полный экран">](player-controls.png) |

| Качество и скорость | Мини-плеер |
|:---:|:---:|
| [<img src="player-settings.png" width="300" alt="Настройки разрешения и скорости воспроизведения">](player-settings.png) | [<img src="player-mini.png" width="300" alt="Мини-плеер поверх Главной">](player-mini.png) |

Полноэкранный режим:

[<img src="player-landscape.png" width="960" alt="Ландшафтный плеер с видео и управлением">](player-landscape.png)

## Вход, импорт и дополнительные состояния

| Вход на сайт | Импорт Списка |
|:---:|:---:|
| [<img src="login.png" width="300" alt="Вход через сайт во встроенном браузере">](login.png) | [<img src="import-mode.png" width="300" alt="Диалог: объединить Списки или заменить текущий">](import-mode.png) |

| Первый запуск Библиотеки | Отчёты о сбоях |
|:---:|:---:|
| [<img src="library-empty.png" width="300" alt="Подсказка о локальном хранении и пустой Список">](library-empty.png) | [<img src="settings-privacy.png" width="300" alt="Настройка отправки отчётов о сбоях в релизной сборке">](settings-privacy.png) |

## Условные состояния: тестовые рендеры

Ниже — существующие эталоны Roborazzi с тестовыми данными, отдельно от
28 снимков устройства. Они показывают состояния, для которых нужны другое
состояние аккаунта, скачанные эпизоды, новая версия или конец серии.
В тестах постеры и видеокадр заменены заглушками.

| Профиль с подпиской | Профиль без подписки | Нужна подписка |
|:---:|:---:|:---:|
| [<img src="../../feature/account/src/test/screenshots/profile_user.png" width="260" alt="Тестовый рендер: профиль с активной подпиской">](../../feature/account/src/test/screenshots/profile_user.png) | [<img src="../../feature/account/src/test/screenshots/profile_no_subscription.png" width="260" alt="Тестовый рендер: профиль без подписки">](../../feature/account/src/test/screenshots/profile_no_subscription.png) | [<img src="../../feature/account/src/test/screenshots/subscription.png" width="260" alt="Тестовый рендер: экран необходимости подписки">](../../feature/account/src/test/screenshots/subscription.png) |

| Обновление приложения | Очередь скачивания | Конец серии |
|:---:|:---:|:---:|
| [<img src="../../feature/account/src/test/screenshots/update_dialog.png" width="260" alt="Тестовый рендер: доступно обновление">](../../feature/account/src/test/screenshots/update_dialog.png) | [<img src="../../feature/library/src/test/screenshots/library_downloads.png" width="260" alt="Тестовый рендер: состояния скачивания эпизодов">](../../feature/library/src/test/screenshots/library_downloads.png) | [<img src="../../feature/player/src/test/screenshots/player_countdown.png" width="260" alt="Тестовый рендер: отсчёт до следующей серии">](../../feature/player/src/test/screenshots/player_countdown.png) |

## Покрытие

Сняты четыре основных таба, страница аниме и её меню, четыре фильтра,
выбор перевода для просмотра и скачивания, вход, настройки, импорт и
режимы плеера. Подтверждение удаления скачанного не снято: на устройстве
нет скачанных эпизодов. Системные файловые диалоги и PiP поверх рабочего
стола не включены в галерею.

«Публикация», «Мои публикации», подтверждение 18+ и «О приложении» описаны
в [проекте экранов](../screens.md), но в текущем интерфейсе ещё не
реализованы.

## Как обновить снимки

Откройте нужный экран, дождитесь загрузки постеров и завершения анимации.
Снимите PNG без потери бинарных данных через временный файл на устройстве:

```bash
adb shell screencap -p /sdcard/anime365-demo.png
adb pull /sdcard/anime365-demo.png docs/screenshots/home.png
adb shell rm /sdcard/anime365-demo.png
```

Для плеера поставьте видео на паузу перед съёмкой. Для гостевого профиля и
импорта используйте dev-сборку, чтобы не менять основной аккаунт и Список.

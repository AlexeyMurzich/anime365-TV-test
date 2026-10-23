# anime365

Неофициальный Android-клиент [anime365](https://smotret-anime.co): каталог
аниме, просмотр эпизодов, локальная Библиотека, скачивание и публикация
переводов. Для просмотра видео нужен аккаунт с подпиской на сайте.

Статус: веха M0 — скелет проекта ([docs/roadmap.md](docs/roadmap.md)).

## Сборка

Нужны JDK 17+ (подойдёт JBR из Android Studio) и Android SDK.

```bash
./gradlew assembleDevDebug
```

Проверки, которые гоняет CI:

```bash
./gradlew spotlessCheck detekt lint test buildHealth assembleDevDebug
```

`spotlessApply` правит форматирование автоматически.

Флейворы: `dev` (`io.github.arenoros.anime365.dev`, ставится рядом с
релизной) и `prod`.

## Документация

- [GLOSSARY.md](GLOSSARY.md) — термины предметной области.
- [docs/adr](docs/adr) — архитектурные решения и почему они такие.
- [docs/screens.md](docs/screens.md) — экраны приложения.
- [docs/roadmap.md](docs/roadmap.md) — вехи.
- [docs/api-notes.md](docs/api-notes.md) — поведение API сверх спецификации.

## Тесты против живого API

Появятся вместе с сетевым слоем (M1). Для них нужен свой API-клиент на сайте и
токен в `secrets.properties` в корне (файл в `.gitignore`):

```properties
ANIME365_APP=<id API-клиента>
ANIME365_ACCESS_TOKEN=<токен>
```

Без этого файла такие тесты пропускаются; CI их не запускает.

## Лицензия

[GPL-3.0](LICENSE).

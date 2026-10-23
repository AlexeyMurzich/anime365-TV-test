# anime365

Неофициальный Android-клиент [anime365](https://smotret-anime.co): каталог
аниме, просмотр эпизодов, локальная Библиотека, скачивание и публикация
переводов. Для просмотра видео нужен аккаунт с подпиской на сайте.

Статус: веха M1 — Главная, Каталог и экран Аниме на живом API
([docs/roadmap.md](docs/roadmap.md)).

## Сборка

Нужны JDK 17+ (подойдёт JBR из Android Studio) и Android SDK.

```bash
./gradlew assembleDevDebug
```

Проверки, которые гоняет CI:

```bash
./gradlew spotlessCheck detekt lint test buildHealth assembleDevDebug -Proborazzi.test.verify=true
```

`spotlessApply` правит форматирование автоматически.

Скриншот-тесты экранов (Roborazzi) сверяются с эталонами в
`feature/*/src/test/screenshots`. Экран изменился намеренно — перезаписать
эталоны и посмотреть разницу в коммите:

```bash
./gradlew recordRoborazziDebug
```

Флейворы: `dev` (`io.github.arenoros.anime365.dev`, ставится рядом с
релизной) и `prod`.

## Документация

- [GLOSSARY.md](GLOSSARY.md) — термины предметной области.
- [docs/adr](docs/adr) — архитектурные решения и почему они такие.
- [docs/screens.md](docs/screens.md) — экраны приложения.
- [docs/roadmap.md](docs/roadmap.md) — вехи.
- [docs/api-notes.md](docs/api-notes.md) — поведение API сверх спецификации.

## Тесты против живого API

Ловят изменения API, которых не видят записанные ответы сервера. CI их не
запускает:

```bash
./gradlew :core:network:testDebugUnitTest -PliveApi
```

Каталогу Токен не нужен. С M2 (Видеопоток) понадобится свой API-клиент на
сайте и Токен в `secrets.properties` в корне (файл в `.gitignore`):

```properties
ANIME365_APP=<id API-клиента>
ANIME365_ACCESS_TOKEN=<токен>
```

## Лицензия

[GPL-3.0](LICENSE).

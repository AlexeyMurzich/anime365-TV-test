# Поведение API, которого нет в спецификации

Проверено живыми запросами 23.10.2026. Спецификация —
[anime365-openapi.yaml](anime365-openapi.yaml). Термины — из
[GLOSSARY.md](../GLOSSARY.md).

## Общее

- Базовый адрес из спецификации — `https://smotret-anime.app/api`;
  `https://smotret-anime.co/api` отвечает тем же.
- Без `fields` каждый элемент `/translations` содержит полные вложенные
  объекты `series` (с внешними ссылками и описаниями) и `episode`. `fields`
  вырезает и их.
- Метода со списком жанров нет: `/api/genres` → 404. У аниме жанры приходят
  как `[{id, title, url}]`.
- `/api/videos/{id}` клиенту не нужен.
- В `links` аниме бывают дубли (две ссылки «википедия (ja)»).
- `worldArtScore: -1` означает «оценки нет». У `myAnimeListScore` два знака
  после точки (`9.24`).
- Онгоингов с `isActive=1` на дату проверки — 195.

## Переводы

- У одного Эпизода — до 97 Переводов (1-я серия Фрирен): `voiceRu` 31,
  `raw` 28, `voiceOther` 13, `subRu` 10, `subEn` 6, `voiceEn` 3, `voiceUk` 3,
  `subJa` 3.
- У `voiceOther` странные производные значения: `typeKind: "voiceOth"`,
  `typeLang: "er"`.
- `priority` — порядок сортировки сайта.
- `authorsList` бывает пустым массивом.

## Видеопоток — `GET /api/translations/embed/{id}`

```json
{
  "data": {
    "embedUrl": "https://smotret-anime.co/translations/embed/5987618",
    "download": [{ "height": 1080, "url": "https://smotret-anime.co/translations/mp4/5987618?download=1&height=1080" }],
    "stream":   [{ "height": 1080, "urls": ["https://bcdn.melon-soda.org/route/?m=…&token=HS256-…&expires=1792836419"] }],
    "subtitlesUrl": "/episodeTranslations/5987619.ass?willcache"
  }
}
```

- В проверенных Переводах — разрешения 1080, 720, 360.
- `stream[].urls` — подписанные ссылки CDN с параметром `expires`, живут
  **ровно 24 часа**. Массив — зеркала одного файла.
- Поток — обычный MP4 (`video/mp4`), не HLS. Перемотка через `Range`
  работает (ответ 206). Файл 1080p на 26 минут — 529 МБ.
- `download[].url` без `access_token` → 403, с `access_token` → 302 на свежую
  подписанную ссылку CDN. Сама ссылка с токеном не истекает.
- `subtitlesUrl` — **относительный путь** двух видов:
  - `/episodeTranslations/{id}.ass?willcache` — `Content-Encoding: gzip`;
  - `/translations/ass/{id}?willcache=1` — `Content-Type: text/html`,
    хотя внутри ASS. MIME-тип надо задавать явно.
- `subtitlesUrl: null` у Перевода с субтитрами — Вшитые субтитры. На 1-й
  серии Фрирен — 4 из 12 Переводов с субтитрами.
- Фансаб-ASS (9volt, BD): 14 стилей, 8264 строки `Dialogue`, из них 8004 с
  тегами оформления — надписи, фуригана, караоке опенинга. Шрифты из стилей
  (Noto Serif, Aver, Footlight MT Light) на устройстве обычно отсутствуют.

## Публикация

- Созданный Перевод приходит с `isActive: 0` — виден после проверки на сайте.
- Ключи `error.fields` не совпадают с именами полей запроса: номер серии
  в запросе — `episodeNumber`, в ошибке — `episodeNumberNew`.

## Аккаунт

- `/api/me` без токена → `200` с `isLogined: false` и `name: "Гость"`.

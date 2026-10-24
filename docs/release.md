# Релиз

Приложение раздаётся через GitHub Releases (ADR 0006). Релиз собирает
[release.yml](../.github/workflows/release.yml) по тегу `vX.Y.Z`.

## Версия

`versionName` — из последнего git-тега: `v1.2.0` даёт `1.2.0`. `versionCode` =
`major·10000 + minor·100 + patch`, то есть `10200`; minor и patch — меньше 100.
Сборка без тегов — `0.0.0`. По той же формуле проверка обновлений сравнивает тег
релиза с установленной версией.

## Выпустить

1. На `main` всё зелёное.
2. Аннотированный тег, в сообщении — «что нового» по-русски: его покажет диалог
   «Доступно обновление».

   ```bash
   git tag -a v0.2.0 -m "- Скачивание серий
   - Окно Плеера поверх приложения"
   git push origin v0.2.0
   ```

3. Workflow соберёт `assembleProdRelease`, подпишет и выложит три APK:
   `anime365-0.2.0-arm64-v8a.apk`, `…-armeabi-v7a.apk`, `…-x86_64.apk`. Имя
   файла важно: по нему проверка обновлений выбирает APK под устройство.

## Ключ подписи

Ключ — единственный способ доставить обновление уже установленным копиям: с
другим ключом Android обновление не поставит, а переустановка сотрёт Библиотеку.
Поэтому копия ключа и пароля хранится вне рабочей машины: менеджер паролей и
офлайн-носитель.

Создать один раз (пароль спросит `keytool`; у PKCS12 пароль ключа совпадает с
паролем хранилища):

```bash
keytool -genkeypair -v -keystore anime365-release.jks -storetype PKCS12 -alias anime365 -keyalg RSA -keysize 4096 -validity 36500 -dname "CN=anime365"
```

Секреты репозитория (`gh secret set` без `--body` спрашивает значение, и оно не
остаётся в истории команд):

```bash
base64 -w0 anime365-release.jks | gh secret set SIGNING_KEYSTORE_BASE64
gh secret set SIGNING_PASSWORD
```

Без ключа (локально, в форках) `release` подписывается debug-ключом: такую сборку
можно поставить и снять с неё профиль, но обновить ею релиз нельзя.

## Sentry

Отчёты о сбоях — только по согласию в Настройках, по умолчанию выключены. DSN
есть только в релизах из CI: в сборках без него, локальных и в форках,
переключателя нет.

```bash
gh secret set SENTRY_DSN
gh secret set SENTRY_AUTH_TOKEN
gh variable set SENTRY_ORG --body <организация>
gh variable set SENTRY_PROJECT --body <проект>
```

Проект — `arenoros/anime365`, регион EU (de.sentry.io). `SENTRY_DSN`, `SENTRY_ORG` и
`SENTRY_PROJECT` уже заданы.

`SENTRY_AUTH_TOKEN` — токен организации (Settings → Developer Settings →
Organization Tokens в arenoros.sentry.io): он нужен, чтобы `mapping.txt` ушёл в Sentry и стектрейсы читались
без обфускации. Без него релиз собирается, но стектрейсы будут с короткими
именами R8.

## Baseline Profile

Профиль ускоряет запуск и первые экраны: код из него компилируется при установке.
Снимают его заново, когда заметно поменялись Главная, Каталог или экран Аниме:

1. Actions → Baseline Profile → Run workflow. Эмулятор API 34 проходит путь из
   `baselineprofile/…/Journeys.kt`.
2. Из артефакта `baseline-profile` положить `baseline-prof.txt` и
   `startup-prof.txt` в `app/src/main/generated/baselineProfiles/` и закоммитить.

Снять профиль на телефоне без root можно только с Android 13: поэтому эмулятор.
Выигрыш на запуске меряет `StartupBenchmark` на подключённом телефоне:

```bash
./gradlew :baselineprofile:connectedBenchmarkReleaseAndroidTest
```

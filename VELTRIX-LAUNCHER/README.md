# VELTRIX LAUNCHER — Android project

Стартовый Android-проект на Kotlin + Jetpack Compose для Android 8.0+ (включая Android 16).

## Что уже реализовано
- Тёмный интерфейс VELTRIX с фиолетовыми и бирюзовыми акцентами.
- Главная страница и нижняя навигация.
- Каталог модов и сборок: поиск и фильтр по категориям.
- Вкладка APK: автоматически читает APK из `app/src/main/assets/apks/`.
- Установка встроенного APK через системный установщик Android и FileProvider.
- Вкладка локальных файлов приложения.
- Экран настроек.

## Важно о текущей версии
Каталог модов и сборок содержит демонстрационные записи. Реальные файлы и ссылки для скачивания не предоставлены, поэтому загрузка модов/сборок из интернета пока не подключена. APK-папка изначально пустая: добавьте только APK, которые имеете право распространять. APK устанавливаются только через системный интерфейс Android; лаунчер не обходит защиту устройства.

## Как добавить APK
1. Скопируйте нужный `.apk` в `app/src/main/assets/apks/`.
2. Откройте проект в Android Studio.
3. Выполните Sync Project with Gradle Files.
4. Выберите Build → Build Bundle(s) / APK(s) → Build APK(s).
5. Установка APK может потребовать разрешение Android «Установка неизвестных приложений» для VELTRIX.

## Как собрать
Требуется Android Studio, JDK 17 и Android SDK Platform 35. Откройте папку проекта `VELTRIX-LAUNCHER` в Android Studio, дождитесь синхронизации Gradle и соберите APK через меню Build.

## Основные параметры
- Application ID: `com.veltrix.launcher`
- Version: `1.0.0`
- minSdk: 26 (Android 8.0)
- targetSdk / compileSdk: 35

## Автоматическая сборка через GitHub Actions
В проект добавлен workflow `.github/workflows/android.yml`. После загрузки проекта в GitHub можно открыть вкладку Actions, запустить `Build Android APK` и скачать артефакт `veltrix-debug-apk` после успешной сборки. Это требует аккаунт GitHub и загрузку проекта в свой репозиторий.

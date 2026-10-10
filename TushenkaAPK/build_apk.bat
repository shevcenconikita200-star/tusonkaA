@echo off
setlocal EnableExtensions
cd /d "%~dp0"
title Сборка APK — Учёт тушёнки

echo ==========================================
echo    СБОРКА APK — УЧЁТ ТУШЁНКИ
echo ==========================================
echo.

set "SDK="
if defined ANDROID_HOME set "SDK=%ANDROID_HOME%"
if not defined SDK if defined ANDROID_SDK_ROOT set "SDK=%ANDROID_SDK_ROOT%"
if not defined SDK if exist "%LOCALAPPDATA%\Android\Sdk\platforms\android-35" set "SDK=%LOCALAPPDATA%\Android\Sdk"
if not defined SDK if exist "%USERPROFILE%\AppData\Local\Android\Sdk\platforms\android-35" set "SDK=%USERPROFILE%\AppData\Local\Android\Sdk"

if not defined SDK (
  echo Android SDK не найден.
  echo Установи Android Studio и Android SDK Platform 35 через SDK Manager,
  echo затем запусти этот файл ещё раз.
  echo.
  pause
  exit /b 1
)

if not exist "%SDK%\platforms\android-35\android.jar" (
  echo В Android SDK отсутствует Android Platform 35.
  echo Открой Android Studio ^> Tools ^> SDK Manager ^> SDK Platforms
  echo и установи Android 15 / API 35.
  echo.
  pause
  exit /b 1
)
set "ANDROID_HOME=%SDK%"
set "ANDROID_SDK_ROOT=%SDK%"

set "GRADLE_HOME=%LOCALAPPDATA%\TushenkaBuild\gradle-8.9"
if not exist "%GRADLE_HOME%\bin\gradle.bat" (
  echo Скачиваю Gradle для сборки. Это делается один раз...
  if not exist "%LOCALAPPDATA%\TushenkaBuild" mkdir "%LOCALAPPDATA%\TushenkaBuild"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest 'https://services.gradle.org/distributions/gradle-8.9-bin.zip' -OutFile ($env:LOCALAPPDATA + '\TushenkaBuild\gradle.zip'); Expand-Archive -LiteralPath ($env:LOCALAPPDATA + '\TushenkaBuild\gradle.zip') -DestinationPath ($env:LOCALAPPDATA + '\TushenkaBuild') -Force; Remove-Item ($env:LOCALAPPDATA + '\TushenkaBuild\gradle.zip') -Force"
  if errorlevel 1 (
    echo Не удалось скачать Gradle. Проверь интернет и запусти файл ещё раз.
    pause
    exit /b 1
  )
)

if not exist "%GRADLE_HOME%\bin\gradle.bat" (
  echo Не удалось подготовить Gradle.
  pause
  exit /b 1
)

echo.
echo Собираю APK. Не закрывай это окно...
call "%GRADLE_HOME%\bin\gradle.bat" --no-daemon assembleDebug
if errorlevel 1 (
  echo.
  echo Сборка завершилась с ошибкой. Сфотографируй последние строки и пришли мне.
  pause
  exit /b 1
)

echo.
echo ГОТОВО! APK находится здесь:
echo %~dp0app\build\outputs\apk\debug\app-debug.apk
if exist "%~dp0app\build\outputs\apk\debug\app-debug.apk" start "" explorer /select,"%~dp0app\build\outputs\apk\debug\app-debug.apk"
echo Скопируй app-debug.apk на телефон и открой его для установки.
pause

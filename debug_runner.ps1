# Local Mind Debug Runner
# This script builds, installs, and starts live logging for the app.

$PACKAGE_NAME = "com.tk854.localmind"
$MAIN_ACTIVITY = "com.tk854.localmind.MainActivity"

# Use stable Android Studio JBR (JDK 21) to avoid AccessDeniedException with JDK 24
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH

# ADB Path Fix: ensure adb is in path for environmental consistency
$env:PATH = "C:\Users\tk854\AppData\Local\Android\Sdk\platform-tools;" + $env:PATH

function Invoke-GradleTask {
    param(
        [Parameter(Mandatory = $true)]
        [string]$TaskName
    )

    $taskOutput = & .\gradlew.bat $TaskName
    $taskOutput | ForEach-Object { Write-Host $_ }

    return @{
        ExitCode = $LASTEXITCODE
        Output   = ($taskOutput | Out-String)
    }
}

Write-Host "--- Step 1: Building Debug APK ---" -ForegroundColor Cyan
$buildResult = Invoke-GradleTask "assembleDebug"
if ($buildResult.ExitCode -ne 0) { Write-Error "Build failed!"; exit 1 }

Write-Host "--- Step 2: Installing APK ---" -ForegroundColor Cyan
$installResult = Invoke-GradleTask "installDebug"

if ($installResult.ExitCode -ne 0 -and $installResult.Output -match "INSTALL_FAILED_UPDATE_INCOMPATIBLE") {
    Write-Warning "Installed app signature does not match this build. Uninstalling the existing app and retrying."
    Write-Warning "This removes the current app installation and its on-device data."

    $uninstallOutput = & adb uninstall $PACKAGE_NAME
    $uninstallOutput | ForEach-Object { Write-Host $_ }

    if ($LASTEXITCODE -ne 0 -or ($uninstallOutput | Out-String) -notmatch "Success") {
        Write-Error "Automatic uninstall failed. Run 'adb uninstall $PACKAGE_NAME' manually, then rerun this script."
        exit 1
    }

    Write-Host "--- Step 2b: Reinstalling APK ---" -ForegroundColor Cyan
    $installResult = Invoke-GradleTask "installDebug"
}

if ($installResult.ExitCode -ne 0) { Write-Error "Installation failed!"; exit 1 }

Write-Host "--- Step 3: Starting App ---" -ForegroundColor Cyan
adb shell am start -n "$PACKAGE_NAME/$MAIN_ACTIVITY"
if ($LASTEXITCODE -ne 0) { Write-Warning "Could not start app automatically. Please open it manually." }

Write-Host "--- Step 4: Catching Live Logs (Press Ctrl+C to stop) ---" -ForegroundColor Green
Write-Host "Filtering for: $PACKAGE_NAME" -ForegroundColor Yellow

# Clear old logs first
adb logcat -c

# Run logcat and filter for the package name
adb logcat | Select-String $PACKAGE_NAME

@echo off
title Push Minimalist Home to GitHub & Build Android APK
echo ========================================================
echo  Minimalist Home (Couple Edition) - Auto APK Builder
echo ========================================================
echo.
echo Step 1: Open https://github.com/new in your browser
echo         Name the repository (e.g. "minimalist-home")
echo         (You can choose Public or Private)
echo.
echo Step 2: Paste your GitHub repository URL below:
echo         (e.g., https://github.com/YourUsername/minimalist-home.git)
echo.
set /p REPO_URL="Enter GitHub Repo URL: "
if "%REPO_URL%"=="" goto end

git init
git config user.email "user@minimalist.home"
git config user.name "MinimalistUser"
git add .
git commit -m "Initial commit for Minimalist Home Launcher" >nul 2>&1
git branch -M main
git remote remove origin >nul 2>&1
git remote add origin %REPO_URL%
git push -u origin main

echo.
echo ========================================================
echo SUCCESS! Your code is uploaded to GitHub.
echo ========================================================
echo.
echo WHAT HAPPENS NEXT (Automated Cloud APK Build):
echo 1. Open your repo on GitHub: %REPO_URL%
echo 2. Click the "Actions" tab at the top.
echo 3. You will see "Build Minimalist Launcher APK" running.
echo 4. In ~2 minutes, the build will finish!
echo 5. Go to the "Releases" section (or "Actions" -> Artifacts).
echo 6. Download 'app-debug.apk' on both your phone and your wife's phone.
echo 7. Tap 'Install', press your phone's Home button, and choose 'Always'!
echo.
echo You both now have a 100%% free, forever minimalist launcher!
echo ========================================================
pause
:end

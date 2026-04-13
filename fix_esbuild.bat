@echo off
set PATH=D:\node;%PATH%
cd /d C:\Users\15716\Desktop\tech-blog\blog-frontend
echo Node version:
node --version
echo.
echo Rebuilding esbuild...
npm rebuild esbuild
echo.
echo Done. Exit code: %ERRORLEVEL%

@echo off
set PATH=D:\node;%PATH%
cd /d C:\Users\15716\Desktop\tech-blog\blog-frontend
echo Checking vite...
npx vite --version
echo Exit code: %ERRORLEVEL%

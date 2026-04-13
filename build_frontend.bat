@echo off
set PATH=D:\node;%PATH%
cd /d C:\Users\15716\Desktop\tech-blog\blog-frontend
echo Building frontend...
npm run build
echo Exit code: %ERRORLEVEL%

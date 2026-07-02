@echo off
cd /d "C:\Users\qiu\Desktop\english-assistant-server"

set "MAVEN_HOME=C:\Users\qiu\apache-maven-3.9.9"
set "PATH=%MAVEN_HOME%\bin;%PATH%"
set "DB_PASSWORD=root123"
set "DASHSCOPE_API_KEY=sk-fd3bc6d110c74e128f1191087cd13c08"
set "JWT_SECRET=9a60abc63c4deab3b37d352e59aa6bf7e77542c322c23fd2907ed0312de1de4a"

taskkill /F /IM java.exe 1>nul 2>nul

title English Assistant
echo ============================================
echo   English Assistant
echo   http://localhost:3000
echo   Ctrl+C to stop
echo ============================================

call mvn.cmd spring-boot:run
pause

@echo off
title He Thong Chuan Hoa CSDL - Do An 10
echo ======================================================================
echo    DO AN 10 - PHAN TICH PHU THUOC HAM VA CHUAN HOA CSDL
echo ======================================================================
echo.

if not defined JAVA_HOME (
    if exist "C:\Program Files\Java\jdk-25" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-25"
    ) else if exist "C:\Program Files\Java\jdk-21" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-21"
    ) else if exist "C:\Program Files\Java\jdk-17" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-17"
    )
)

if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
    echo [OK] Su dung Java tai: %JAVA_HOME%
) else (
    echo [CANH BAO] Khong tim thay JAVA_HOME, thu chay voi Java mac dinh trong PATH...
)

cd /d "%~dp0db-normalization-tool"
echo [INFO] Dang khoi dong may chu Spring Boot tren cong 8080...
echo [INFO] Sau khi khoi dong xong, hay truy cap: http://localhost:8080/
echo.
call mvnw.cmd spring-boot:run
pause

@echo off
chcp 65001 > nul
echo ======================================================================
echo     CHAY KHOI TAO CSDL VA KIEM THU TRANSACTION OBJECTDB (TASK 2)
echo ======================================================================
cd /d "%~dp0"
call mvnw.cmd compile exec:java "-Dexec.mainClass=com.doan10.db_normalization_tool.daotao.Main"
pause

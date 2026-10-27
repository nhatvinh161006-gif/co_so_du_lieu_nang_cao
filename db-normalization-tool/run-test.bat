@echo off
chcp 65001 > nul
echo ===================================================
echo     DANG CHAY TEST KET NOI OBJECTDB (TUAN 1)
echo ===================================================
cd /d "%~dp0"
java -cp "target/classes;libs/objectdb-2.9.5.jar;libs/objectdb.jar;%USERPROFILE%\.m2\repository\javax\persistence\javax.persistence-api\2.2\javax.persistence-api-2.2.jar" com.doan10.db_normalization_tool.daotao.Main
pause

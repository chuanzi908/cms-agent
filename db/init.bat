@echo off
REM ============================================================
REM CMS 数据库初始化脚本（Windows 使用）
REM 用法：双击运行，或在 db 目录执行 init.bat
REM 注意：请先配置 mysql 命令到 PATH，或改用下方完整路径
REM ============================================================
setlocal

set MYSQL_USER=root
set MYSQL_PASS=123456
set DB_NAME=cms

echo ==^> Creating database %DB_NAME%
mysql -u%MYSQL_USER% -p%MYSQL_PASS% -e "CREATE DATABASE IF NOT EXISTS %DB_NAME% DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

echo ==^> Importing schema.sql
mysql -u%MYSQL_USER% -p%MYSQL_PASS% %DB_NAME% < ..\sql\schema.sql

echo ==^> Done
pause

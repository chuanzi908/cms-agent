#!/bin/bash
# ============================================================
# CMS 数据库初始化脚本（Mac/Linux 使用）
# 用法：在 db 目录执行  bash init.sh
# 如需指定密码：MYSQL_PASS=你的密码 bash init.sh
# ============================================================
set -e

MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASS="${MYSQL_PASS:-123456}"
DB_NAME="cms"

echo "==> 创建数据库 ${DB_NAME}"
mysql -u"${MYSQL_USER}" -p"${MYSQL_PASS}" -e "CREATE DATABASE IF NOT EXISTS ${DB_NAME} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

echo "==> 导入表结构与初始数据"
mysql -u"${MYSQL_USER}" -p"${MYSQL_PASS}" "${DB_NAME}" < ../sql/schema.sql

echo "==> 初始化完成"

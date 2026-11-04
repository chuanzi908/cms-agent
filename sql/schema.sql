-- ============================================================
-- CMS 数据库初始化脚本（统一维护入口，禁止手动改库）
-- 由 db/init.sh 或 db/init.bat 执行
-- 约定：所有模块的表结构统一维护在本文件，变更需同步全员
-- ============================================================

USE cms;

-- ------------------------------------------------------------
-- M1 账号与权限（sys_user / sys_role / sys_permission / sys_user_role / sys_role_permission）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username    VARCHAR(50)  NOT NULL COMMENT '用户名',
    password    VARCHAR(100) NOT NULL COMMENT '密码',
    nickname    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='系统用户';

CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(50) NOT NULL COMMENT '角色编码',
    name        VARCHAR(50) NOT NULL COMMENT '角色名',
    deleted     TINYINT     NOT NULL DEFAULT 0,
    create_time DATETIME    DEFAULT NULL,
    update_time DATETIME    DEFAULT NULL,
    UNIQUE KEY uk_role_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='角色';

CREATE TABLE IF NOT EXISTS sys_permission (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(100) NOT NULL COMMENT '权限编码',
    name        VARCHAR(50)  NOT NULL COMMENT '权限名',
    deleted     TINYINT      NOT NULL DEFAULT 0,
    create_time DATETIME     DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    UNIQUE KEY uk_perm_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='权限';

CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户-角色';

CREATE TABLE IF NOT EXISTS sys_role_permission (
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='角色-权限';

-- ------------------------------------------------------------
-- M3 分类（cms_category）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cms_category (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50) NOT NULL COMMENT '分类名',
    parent_id   BIGINT      NOT NULL DEFAULT 0 COMMENT '父分类ID，0为一级',
    status      TINYINT     NOT NULL DEFAULT 0 COMMENT '0-启用 1-停用',
    sort        INT         NOT NULL DEFAULT 0 COMMENT '排序',
    deleted     TINYINT     NOT NULL DEFAULT 0,
    create_time DATETIME    DEFAULT NULL,
    update_time DATETIME    DEFAULT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='文章分类';

-- ------------------------------------------------------------
-- M2 内容（cms_article）
-- version: 乐观锁版本号；deleted: 逻辑删除
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cms_article (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(200) NOT NULL COMMENT '文章标题',
    content     TEXT         NOT NULL COMMENT '文章正文',
    author_id   BIGINT       NOT NULL COMMENT '作者用户ID(system->sys_user.id)',
    category_id BIGINT       NOT NULL COMMENT '分类ID(system->cms_category.id)',
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '0-草稿 1-待审核 2-已发布 3-已下架',
    version     INT          NOT NULL DEFAULT 1 COMMENT '乐观锁版本号',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    create_time DATETIME     DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    KEY idx_author (author_id),
    KEY idx_category (category_id),
    KEY idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='文章';

-- ------------------------------------------------------------
-- M4 运维（sys_log，占位）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_log (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    module      VARCHAR(50) DEFAULT NULL COMMENT '模块',
    content     TEXT COMMENT '日志内容',
    level       VARCHAR(10) DEFAULT 'INFO' COMMENT '级别',
    create_time DATETIME    DEFAULT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='系统日志';

-- ============================================================
-- 初始数据（仅用于本地联调）
-- ============================================================

-- 用户（密码明文仅用于本地联调，正式M1应使用BCrypt加密）
INSERT INTO sys_user (id, username, password, nickname, deleted, create_time, update_time) VALUES
    (1, 'admin', 'admin123', '管理员', 0, NOW(), NOW()),
    (2, 'zhangsan', '123456', '张三', 0, NOW(), NOW());

INSERT INTO sys_role (id, code, name, deleted, create_time, update_time) VALUES
    (1, 'admin', '管理员', 0, NOW(), NOW()),
    (2, 'user', '普通用户', 0, NOW(), NOW());

INSERT INTO sys_permission (id, code, name, deleted, create_time, update_time) VALUES
    (1, 'article:create', '新建文章', 0, NOW(), NOW()),
    (2, 'article:update', '修改文章', 0, NOW(), NOW()),
    (3, 'article:delete', '删除文章', 0, NOW(), NOW()),
    (4, 'article:audit', '审核文章', 0, NOW(), NOW()),
    (5, 'article:offline', '下架文章', 0, NOW(), NOW()),
    (6, 'user:delete', '删除账号', 0, NOW(), NOW());

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1), (2, 2);
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
    (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6),
    (2, 1), (2, 2);

-- 分类
INSERT INTO cms_category (id, name, parent_id, status, sort, deleted, create_time, update_time) VALUES
    (1, '科技', 0, 0, 1, 0, NOW(), NOW()),
    (2, '生活', 0, 0, 2, 0, NOW(), NOW()),
    (3, '数码', 1, 0, 1, 0, NOW(), NOW()),
    (4, '停用分类', 0, 1, 9, 0, NOW(), NOW());

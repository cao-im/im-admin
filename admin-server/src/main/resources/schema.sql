CREATE TABLE IF NOT EXISTS admin_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50) NOT NULL DEFAULT '',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1-正常, 0-停用',
    super_admin TINYINT NOT NULL DEFAULT 0 COMMENT '1-超级管理员',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_admin_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员';

CREATE TABLE IF NOT EXISTS admin_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(50) NOT NULL,
    remark VARCHAR(200) DEFAULT '',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_admin_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色';

CREATE TABLE IF NOT EXISTS admin_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(50) NOT NULL,
    path VARCHAR(100) DEFAULT '',
    permission VARCHAR(80) DEFAULT '',
    icon VARCHAR(50) DEFAULT '',
    sort_order INT NOT NULL DEFAULT 0,
    menu_type TINYINT NOT NULL DEFAULT 2 COMMENT '2-菜单, 3-按钮',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单';

CREATE TABLE IF NOT EXISTS admin_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY uk_admin_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员角色';

CREATE TABLE IF NOT EXISTS admin_role_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    UNIQUE KEY uk_admin_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单';

CREATE TABLE IF NOT EXISTS admin_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT DEFAULT NULL,
    admin_name VARCHAR(50) DEFAULT '',
    action VARCHAR(50) NOT NULL,
    target_type VARCHAR(30) DEFAULT '',
    target_id VARCHAR(64) DEFAULT '',
    detail VARCHAR(500) DEFAULT '',
    ip VARCHAR(64) DEFAULT '',
    result TINYINT NOT NULL DEFAULT 1 COMMENT '1-成功, 0-失败',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_admin_audit_time (create_time),
    INDEX idx_admin_audit_name (admin_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理操作审计';

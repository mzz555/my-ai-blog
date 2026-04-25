-- Test seed data for integration tests

-- Roles
INSERT INTO roles (id, name, code, description, sort_order) VALUES
(1, '超级管理员', 'ADMIN', '拥有所有权限', 1),
(2, '普通用户',   'USER',  '注册用户',    2);

-- Menus / permissions
INSERT INTO menus (name, code, type, sort_order) VALUES
('文章列表',   'article:list',    'BUTTON', 1),
('文章创建',   'article:create',  'BUTTON', 2),
('文章编辑',   'article:update',  'BUTTON', 3),
('文章删除',   'article:delete',  'BUTTON', 4),
('文章发布',   'article:publish', 'BUTTON', 5),
('评论列表',   'comment:list',    'BUTTON', 6),
('评论审核',   'comment:approve', 'BUTTON', 7),
('评论删除',   'comment:delete',  'BUTTON', 8),
('分类管理',   'category:manage', 'BUTTON', 9),
('标签管理',   'tag:manage',      'BUTTON', 10),
('用户列表',   'user:list',       'BUTTON', 11),
('角色管理',   'role:manage',     'BUTTON', 12),
('菜单管理',   'menu:manage',     'BUTTON', 13);

-- ADMIN role gets all permissions
INSERT INTO role_menus (role_id, menu_id) SELECT 1, id FROM menus;

-- Admin user: admin / Admin@2024  (BCrypt hash)
INSERT INTO users (id, username, email, password, status) VALUES
(1, 'admin', 'admin@blog.com', '$2a$10$ZYpBB4KjKcad8tisoTzhAe9fQ5sqjHnKwNbIcFOryslOaFElW9j8e', 1);

-- Assign admin user to ADMIN role
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);

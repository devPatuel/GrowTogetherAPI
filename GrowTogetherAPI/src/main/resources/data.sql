INSERT INTO usuarios (nombre, email, password, puntos_totales, fecha_registro, rol, token_version, activo, tema, idioma)
VALUES ('Jordi Admin', 'admin@growtogether.com', '$2a$10$toHaI/U7/7c1ZT6E9WNknetHzCKQyHmsTMfUTYeOA1376Ox8oOYUC', 0, CURRENT_TIMESTAMP, 'ADMIN', 0, true, 'CLARO', 'es');

INSERT INTO usuarios (nombre, email, password, puntos_totales, fecha_registro, rol, token_version, activo, tema, idioma)
VALUES ('Usuario Estándar', 'usuario@growtogether.com', '$2a$10$toHaI/U7/7c1ZT6E9WNknetHzCKQyHmsTMfUTYeOA1376Ox8oOYUC', 0, CURRENT_TIMESTAMP, 'STANDARD', 0, true, 'CLARO', 'es');

INSERT INTO habitos (nombre, descripcion, racha_actual, racha_maxima, usuario_id, activo, frecuencia)
SELECT 'Gimnasio', 'Entrenar 1 hora', 0, 0, id, true, 'DIARIO' FROM usuarios WHERE email = 'admin@growtogether.com';

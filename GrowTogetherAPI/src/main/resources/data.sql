INSERT INTO usuarios (nombre, email, password, puntos_totales, fecha_registro, rol, token_version, activo, tema, idioma)
VALUES ('Jordi Admin', 'admin@growtogether.com', '$2a$10$toHaI/U7/7c1ZT6E9WNknetHzCKQyHmsTMfUTYeOA1376Ox8oOYUC', 0, CURRENT_TIMESTAMP, 'ADMIN', 0, true, 'CLARO', 'es');

INSERT INTO usuarios (nombre, email, password, puntos_totales, fecha_registro, rol, token_version, activo, tema, idioma)
VALUES ('Usuario Estándar', 'usuario@growtogether.com', '$2a$10$toHaI/U7/7c1ZT6E9WNknetHzCKQyHmsTMfUTYeOA1376Ox8oOYUC', 0, CURRENT_TIMESTAMP, 'STANDARD', 0, true, 'CLARO', 'es');

INSERT INTO habitos (nombre, descripcion, racha_actual, racha_maxima, usuario_id, activo, frecuencia, tipo, icono)
SELECT 'Gimnasio', 'Entrenar 1 hora', 0, 0, id, true, 'DIARIO', 'POSITIVO', 'fitness' FROM usuarios WHERE email = 'admin@growtogether.com';

INSERT INTO habitos (nombre, descripcion, racha_actual, racha_maxima, usuario_id, activo, frecuencia, tipo, icono)
SELECT 'Leer 30 minutos', 'Leer al menos 30 minutos al día', 0, 0, id, true, 'DIARIO', 'POSITIVO', 'book' FROM usuarios WHERE email = 'admin@growtogether.com';

INSERT INTO habitos (nombre, descripcion, racha_actual, racha_maxima, usuario_id, activo, frecuencia, tipo, icono)
SELECT 'Meditar', 'Meditación de 10 minutos', 0, 0, id, true, 'DIARIO', 'POSITIVO', 'meditation' FROM usuarios WHERE email = 'admin@growtogether.com';

INSERT INTO habitos (nombre, descripcion, racha_actual, racha_maxima, usuario_id, activo, frecuencia, tipo, icono)
SELECT 'Dejar de fumar', 'Día sin fumar', 0, 0, id, true, 'DIARIO', 'NEGATIVO', 'smoke' FROM usuarios WHERE email = 'admin@growtogether.com';

INSERT INTO habitos (nombre, descripcion, racha_actual, racha_maxima, usuario_id, activo, frecuencia, tipo, icono)
SELECT 'Beber agua', 'Beber al menos 2 litros de agua', 0, 0, id, true, 'DIARIO', 'POSITIVO', 'water' FROM usuarios WHERE email = 'usuario@growtogether.com';

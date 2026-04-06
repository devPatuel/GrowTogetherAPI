-- ===================== USUARIOS =====================
INSERT INTO usuarios (nombre, email, password, puntos_totales, fecha_registro, rol, token_version, activo, tema, idioma)
VALUES ('Jordi Admin', 'admin@growtogether.com', '$2a$10$toHaI/U7/7c1ZT6E9WNknetHzCKQyHmsTMfUTYeOA1376Ox8oOYUC', 50, CURRENT_TIMESTAMP, 'ADMIN', 0, true, 'CLARO', 'es');

INSERT INTO usuarios (nombre, email, password, puntos_totales, fecha_registro, rol, token_version, activo, tema, idioma)
VALUES ('Usuario Estándar', 'usuario@growtogether.com', '$2a$10$toHaI/U7/7c1ZT6E9WNknetHzCKQyHmsTMfUTYeOA1376Ox8oOYUC', 20, CURRENT_TIMESTAMP, 'STANDARD', 0, true, 'CLARO', 'es');

-- ===================== HABITOS ADMIN =====================
INSERT INTO habitos (nombre, descripcion, racha_actual, racha_maxima, usuario_id, activo, frecuencia, tipo, icono)
SELECT 'Gimnasio', 'Entrenar 1 hora', 3, 5, id, true, 'DIARIO', 'POSITIVO', 'fitness' FROM usuarios WHERE email = 'admin@growtogether.com';

INSERT INTO habitos (nombre, descripcion, racha_actual, racha_maxima, usuario_id, activo, frecuencia, tipo, icono)
SELECT 'Leer 30 minutos', 'Leer al menos 30 minutos al día', 2, 4, id, true, 'DIARIO', 'POSITIVO', 'book' FROM usuarios WHERE email = 'admin@growtogether.com';

INSERT INTO habitos (nombre, descripcion, racha_actual, racha_maxima, usuario_id, activo, frecuencia, tipo, icono)
SELECT 'Meditar', 'Meditación de 10 minutos', 1, 3, id, true, 'DIARIO', 'POSITIVO', 'meditation' FROM usuarios WHERE email = 'admin@growtogether.com';

INSERT INTO habitos (nombre, descripcion, racha_actual, racha_maxima, usuario_id, activo, frecuencia, tipo, icono)
SELECT 'Dejar de fumar', 'Día sin fumar', 5, 5, id, true, 'DIARIO', 'NEGATIVO', 'smoke' FROM usuarios WHERE email = 'admin@growtogether.com';

-- ===================== HABITOS USUARIO =====================
INSERT INTO habitos (nombre, descripcion, racha_actual, racha_maxima, usuario_id, activo, frecuencia, tipo, icono)
SELECT 'Beber agua', 'Beber al menos 2 litros de agua', 1, 2, id, true, 'DIARIO', 'POSITIVO', 'water' FROM usuarios WHERE email = 'usuario@growtogether.com';

-- ===================== HISTORIAL (registro_habitos) =====================
-- Gimnasio (habito 1, admin id 1): completado ultimos 3 dias
INSERT INTO registro_habitos (fecha, estado, usuario_id, habito_id) VALUES (CURRENT_DATE, 'PENDIENTE', 1, 1);
INSERT INTO registro_habitos (fecha, estado, usuario_id, habito_id) VALUES (CURRENT_DATE - INTERVAL '1 day', 'COMPLETADO', 1, 1);
INSERT INTO registro_habitos (fecha, estado, usuario_id, habito_id) VALUES (CURRENT_DATE - INTERVAL '2 days', 'COMPLETADO', 1, 1);
INSERT INTO registro_habitos (fecha, estado, usuario_id, habito_id) VALUES (CURRENT_DATE - INTERVAL '3 days', 'COMPLETADO', 1, 1);
INSERT INTO registro_habitos (fecha, estado, usuario_id, habito_id) VALUES (CURRENT_DATE - INTERVAL '4 days', 'NO_COMPLETADO', 1, 1);
INSERT INTO registro_habitos (fecha, estado, usuario_id, habito_id) VALUES (CURRENT_DATE - INTERVAL '5 days', 'COMPLETADO', 1, 1);

-- Leer (habito 2, admin id 1): completado 2 dias seguidos
INSERT INTO registro_habitos (fecha, estado, usuario_id, habito_id) VALUES (CURRENT_DATE, 'PENDIENTE', 1, 2);
INSERT INTO registro_habitos (fecha, estado, usuario_id, habito_id) VALUES (CURRENT_DATE - INTERVAL '1 day', 'COMPLETADO', 1, 2);
INSERT INTO registro_habitos (fecha, estado, usuario_id, habito_id) VALUES (CURRENT_DATE - INTERVAL '2 days', 'COMPLETADO', 1, 2);
INSERT INTO registro_habitos (fecha, estado, usuario_id, habito_id) VALUES (CURRENT_DATE - INTERVAL '3 days', 'NO_COMPLETADO', 1, 2);
INSERT INTO registro_habitos (fecha, estado, usuario_id, habito_id) VALUES (CURRENT_DATE - INTERVAL '4 days', 'COMPLETADO', 1, 2);

-- Meditar (habito 3, admin id 1): completado ayer
INSERT INTO registro_habitos (fecha, estado, usuario_id, habito_id) VALUES (CURRENT_DATE, 'PENDIENTE', 1, 3);
INSERT INTO registro_habitos (fecha, estado, usuario_id, habito_id) VALUES (CURRENT_DATE - INTERVAL '1 day', 'COMPLETADO', 1, 3);
INSERT INTO registro_habitos (fecha, estado, usuario_id, habito_id) VALUES (CURRENT_DATE - INTERVAL '2 days', 'NO_COMPLETADO', 1, 3);

-- Dejar de fumar (habito 4, admin id 1): 5 dias sin fumar
INSERT INTO registro_habitos (fecha, estado, usuario_id, habito_id) VALUES (CURRENT_DATE, 'PENDIENTE', 1, 4);
INSERT INTO registro_habitos (fecha, estado, usuario_id, habito_id) VALUES (CURRENT_DATE - INTERVAL '1 day', 'COMPLETADO', 1, 4);
INSERT INTO registro_habitos (fecha, estado, usuario_id, habito_id) VALUES (CURRENT_DATE - INTERVAL '2 days', 'COMPLETADO', 1, 4);
INSERT INTO registro_habitos (fecha, estado, usuario_id, habito_id) VALUES (CURRENT_DATE - INTERVAL '3 days', 'COMPLETADO', 1, 4);
INSERT INTO registro_habitos (fecha, estado, usuario_id, habito_id) VALUES (CURRENT_DATE - INTERVAL '4 days', 'COMPLETADO', 1, 4);
INSERT INTO registro_habitos (fecha, estado, usuario_id, habito_id) VALUES (CURRENT_DATE - INTERVAL '5 days', 'COMPLETADO', 1, 4);

-- Beber agua (habito 5, usuario id 2): completado ayer
INSERT INTO registro_habitos (fecha, estado, usuario_id, habito_id) VALUES (CURRENT_DATE, 'PENDIENTE', 2, 5);
INSERT INTO registro_habitos (fecha, estado, usuario_id, habito_id) VALUES (CURRENT_DATE - INTERVAL '1 day', 'COMPLETADO', 2, 5);

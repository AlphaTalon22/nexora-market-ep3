-- Bases de datos de Nexora Market para desarrollo local.
-- Se ejecuta una sola vez, al primer arranque del contenedor.
CREATE DATABASE IF NOT EXISTS nexora_productos;
CREATE DATABASE IF NOT EXISTS nexora_pedidos;
CREATE DATABASE IF NOT EXISTS nexora_pagos;
CREATE DATABASE IF NOT EXISTS nexora_notificaciones;
CREATE DATABASE IF NOT EXISTS nexora_analitica;

GRANT ALL PRIVILEGES ON nexora_productos.* TO 'nexora'@'%';
GRANT ALL PRIVILEGES ON nexora_pedidos.* TO 'nexora'@'%';
GRANT ALL PRIVILEGES ON nexora_pagos.* TO 'nexora'@'%';
GRANT ALL PRIVILEGES ON nexora_notificaciones.* TO 'nexora'@'%';
GRANT ALL PRIVILEGES ON nexora_analitica.* TO 'nexora'@'%';

FLUSH PRIVILEGES;

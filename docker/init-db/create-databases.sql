-- ─────────────────────────────────────────────────────────────────────────────
-- Script de inicialización de bases de datos
--
-- PostgreSQL ejecuta este script automáticamente la PRIMERA VEZ que arranca
-- el contenedor (cuando el volumen está vacío).
--
-- La base de datos "moviedb" ya fue creada por la variable POSTGRES_DB
-- del docker-compose.
--
-- Para aplicar cambios aquí: make infra-clean && make infra-up
CREATE DATABASE bookingdb;        -- booking-service


-- Crear antes el rol LOGIN ensename_app con una contraseña propia y segura,
-- mediante la cuenta de preparación de Neon. No escribir esa contraseña en Git.
-- Ejecutar este archivo como propietario del esquema.
BEGIN;
GRANT USAGE ON SCHEMA dbo TO ensename_app;
GRANT SELECT,INSERT,UPDATE ON dbo.usuario,dbo.orientador TO ensename_app;
GRANT SELECT ON dbo.especialidad TO ensename_app;
GRANT SELECT,INSERT,DELETE ON dbo.orientadorespecialidad TO ensename_app;
GRANT SELECT,INSERT ON dbo.suscripcion TO ensename_app;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA dbo TO ensename_app;
COMMIT;

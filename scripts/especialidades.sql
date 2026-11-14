-- Ejecutar una sola vez con la cuenta de preparación de base de datos.
-- Idempotente: conserva todas las especialidades y los datos existentes.
SET XACT_ABORT ON;
BEGIN TRANSACTION;
IF NOT EXISTS (SELECT 1 FROM dbo.Especialidad WHERE nombre=N'Programación')
 INSERT INTO dbo.Especialidad(nombre) VALUES(N'Programación');
IF NOT EXISTS (SELECT 1 FROM dbo.Especialidad WHERE nombre=N'Matemáticas')
 INSERT INTO dbo.Especialidad(nombre) VALUES(N'Matemáticas');
IF NOT EXISTS (SELECT 1 FROM dbo.Especialidad WHERE nombre=N'Bases de datos')
 INSERT INTO dbo.Especialidad(nombre) VALUES(N'Bases de datos');
COMMIT;

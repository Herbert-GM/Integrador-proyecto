
-- 01_crear_base.sql
IF DB_ID(N'Ensename') IS NULL
    EXEC(N'CREATE DATABASE Ensename');
GO
USE Ensename;
GO

-- 02_esquema.sql
USE Ensename;
GO
-- Esquema inicial
SET XACT_ABORT ON;
BEGIN TRY
 BEGIN TRANSACTION;
 IF OBJECT_ID(N'dbo.Usuario', N'U') IS NOT NULL
  THROW 50001, N'El esquema ya existe. No se modificaron datos.', 1;
 CREATE TABLE dbo.Usuario (
  usuario_id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Usuario PRIMARY KEY,
  nombre NVARCHAR(120) NOT NULL,
  correo NVARCHAR(254) COLLATE Latin1_General_100_CI_AS NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  habilitado BIT NOT NULL CONSTRAINT DF_Usuario_Habilitado DEFAULT 1,
  creado_en DATETIMEOFFSET(0) NOT NULL CONSTRAINT DF_Usuario_Creado DEFAULT SYSDATETIMEOFFSET(),
  CONSTRAINT UQ_Usuario_Correo UNIQUE (correo),
  CONSTRAINT CK_Usuario_Nombre CHECK (LEN(LTRIM(RTRIM(nombre)))>0),
  CONSTRAINT CK_Usuario_Hash CHECK (LEN(password_hash)>0)
 );
 CREATE TABLE dbo.Orientador (
  usuario_id BIGINT NOT NULL CONSTRAINT PK_Orientador PRIMARY KEY,
  descripcion NVARCHAR(1000) NOT NULL CONSTRAINT DF_Orientador_Descripcion DEFAULT N'',
  whatsapp VARCHAR(15) NULL,
  publicar BIT NOT NULL CONSTRAINT DF_Orientador_Publicar DEFAULT 0,
  actualizado_en DATETIMEOFFSET(0) NOT NULL CONSTRAINT DF_Orientador_Actualizado DEFAULT SYSDATETIMEOFFSET(),
  CONSTRAINT FK_Orientador_Usuario FOREIGN KEY (usuario_id) REFERENCES dbo.Usuario(usuario_id),
  CONSTRAINT CK_Orientador_WhatsApp CHECK (whatsapp IS NULL OR
   (LEN(whatsapp) BETWEEN 8 AND 15 AND whatsapp COLLATE Latin1_General_100_BIN2 NOT LIKE '%[^0-9]%'
    AND LEFT(whatsapp,1)<>'0'))
 );
 CREATE TABLE dbo.Especialidad (
  especialidad_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Especialidad PRIMARY KEY,
  nombre NVARCHAR(80) NOT NULL,
  CONSTRAINT UQ_Especialidad_Nombre UNIQUE (nombre),
  CONSTRAINT CK_Especialidad_Nombre CHECK (LEN(LTRIM(RTRIM(nombre)))>0)
 );
 CREATE TABLE dbo.OrientadorEspecialidad (
  usuario_id BIGINT NOT NULL,
  especialidad_id INT NOT NULL,
  CONSTRAINT PK_OrientadorEspecialidad PRIMARY KEY (usuario_id,especialidad_id),
  CONSTRAINT FK_OE_Orientador FOREIGN KEY (usuario_id) REFERENCES dbo.Orientador(usuario_id),
  CONSTRAINT FK_OE_Especialidad FOREIGN KEY (especialidad_id) REFERENCES dbo.Especialidad(especialidad_id)
 );
 CREATE TABLE dbo.Suscripcion (
  suscripcion_id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Suscripcion PRIMARY KEY,
  usuario_id BIGINT NOT NULL,
  inicio DATETIMEOFFSET(0) NOT NULL,
  fin DATETIMEOFFSET(0) NOT NULL,
  es_simulada BIT NOT NULL CONSTRAINT DF_Suscripcion_Simulada DEFAULT 1,
  CONSTRAINT FK_Suscripcion_Orientador FOREIGN KEY (usuario_id) REFERENCES dbo.Orientador(usuario_id),
  CONSTRAINT CK_Suscripcion_Mes CHECK (fin=DATEADD(MONTH,1,inicio) AND fin>inicio),
  CONSTRAINT CK_Suscripcion_Simulada CHECK (es_simulada=1)
 );
 CREATE INDEX IX_OE_Especialidad ON dbo.OrientadorEspecialidad(especialidad_id,usuario_id);
 CREATE INDEX IX_Suscripcion_Vigencia ON dbo.Suscripcion(usuario_id,inicio,fin);
 COMMIT;
END TRY
BEGIN CATCH
 IF XACT_STATE()<>0 ROLLBACK;
 THROW;
END CATCH;
GO

-- 03_perfiles_publicados.sql
USE Ensename;
GO
CREATE OR ALTER VIEW dbo.vw_OrientadoresPublicados
AS
 SELECT u.usuario_id,u.nombre,o.descripcion,o.whatsapp
 FROM dbo.Usuario u
 JOIN dbo.Orientador o ON o.usuario_id=u.usuario_id
 WHERE u.habilitado=1 AND o.publicar=1
 AND LEN(LTRIM(RTRIM(o.descripcion)))>0 AND o.whatsapp IS NOT NULL
 AND EXISTS (SELECT 1 FROM dbo.OrientadorEspecialidad oe WHERE oe.usuario_id=o.usuario_id)
 AND EXISTS (SELECT 1 FROM dbo.Suscripcion s WHERE s.usuario_id=o.usuario_id
  AND s.inicio<=SYSDATETIMEOFFSET() AND SYSDATETIMEOFFSET()<s.fin);
GO
-- Consulta de catálogo. NULL muestra todas las especialidades.
DECLARE @especialidad_id INT=NULL;
SELECT p.usuario_id,p.nombre,p.descripcion,p.whatsapp
FROM dbo.vw_OrientadoresPublicados p
WHERE @especialidad_id IS NULL OR EXISTS
 (SELECT 1 FROM dbo.OrientadorEspecialidad oe
  WHERE oe.usuario_id=p.usuario_id AND oe.especialidad_id=@especialidad_id)
ORDER BY p.nombre,p.usuario_id;

GO

-- 04_activar_simulacion.sql
USE Ensename;
GO
-- El servidor obtiene @usuario_id de la sesión autenticada, nunca de un formulario ajeno.
-- No hay pagos ni datos de tarjeta. Las altas pasan por este procedimiento.
CREATE OR ALTER PROCEDURE dbo.ActivarSuscripcionSimulada
 @usuario_id BIGINT
AS
BEGIN
 SET NOCOUNT ON;
 SET XACT_ABORT ON;
 DECLARE @ahora DATETIMEOFFSET(0)=SWITCHOFFSET(SYSDATETIMEOFFSET(),'-05:00');
 DECLARE @propietario BIGINT;
 BEGIN TRY
  BEGIN TRANSACTION;
  -- Serializa activaciones concurrentes de este orientador.
  SELECT @propietario=o.usuario_id FROM dbo.Orientador o WITH (UPDLOCK,HOLDLOCK)
   JOIN dbo.Usuario u ON u.usuario_id=o.usuario_id
   WHERE o.usuario_id=@usuario_id AND u.habilitado=1;
  IF @propietario IS NULL THROW 50002,N'Perfil de orientador no disponible.',1;
  IF NOT EXISTS (SELECT 1 FROM dbo.Suscripcion WHERE usuario_id=@usuario_id
   AND inicio<=@ahora AND @ahora<fin)
   INSERT dbo.Suscripcion(usuario_id,inicio,fin,es_simulada)
    VALUES (@usuario_id,@ahora,DATEADD(MONTH,1,@ahora),1);
  SELECT TOP(1) suscripcion_id,inicio,fin,es_simulada
   FROM dbo.Suscripcion WHERE usuario_id=@usuario_id AND inicio<=@ahora AND @ahora<fin
   ORDER BY inicio DESC,suscripcion_id DESC;
  COMMIT;
 END TRY
 BEGIN CATCH
  IF XACT_STATE()<>0 ROLLBACK;
  THROW;
 END CATCH;
END;
GO


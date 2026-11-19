-- Ejecutar conectado a una base nueva y vacía de Azure SQL o SQL Server. No crea bases ni borra tablas.
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

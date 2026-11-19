package pe.edu.utp.ensename;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

/** Repite el recorrido web y las pruebas de propiedad contra PostgreSQL real. */
@ActiveProfiles(value="postgres", inheritProfiles=false)
@TestPropertySource(inheritProperties=false, inheritLocations=false, properties={
    "spring.datasource.url=jdbc:postgresql://127.0.0.1:55432/ensename",
    "spring.datasource.driver-class-name=org.postgresql.Driver",
    "spring.datasource.username=ensename_test", "spring.datasource.password="})
@EnabledIfEnvironmentVariable(named="RUN_POSTGRES_TESTS", matches="true")
@Sql(statements="SELECT 1")
@Transactional
class PostgresWebTests extends CuentasSeguridadTests {}

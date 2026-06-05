# Conexion Java con MySQL

El programa ya usa JDBC para guardar y leer datos desde MySQL.

## Requisitos

1. Iniciar MySQL desde XAMPP.
2. Importar `database/schema.sql` en phpMyAdmin.
3. Agregar el conector JDBC de MySQL al proyecto Java.

Si la base ya estaba creada antes de agregar obras sociales, ejecutar tambien
`database/migracion-obras-sociales.sql` desde phpMyAdmin.

## Datos de conexion usados

```text
Base de datos: asistencia_medica
Host: localhost
Puerto: 3306
Usuario: root
Clave: vacia
```

## Cobertura de obra social

La tabla `obras_sociales` guarda el porcentaje de cobertura. Cada paciente se
relaciona con una obra social mediante `pacientes.obra_social_id`.

El calculo se realiza en Java:

```text
monto obra social = valor consulta * porcentaje cobertura / 100
monto paciente = valor consulta - monto obra social
```

Valores de consulta usados:

```text
Consulta general: 12000
Emergencia: 30000
Telemedicina: 9000
```

Estos datos estan en `src/LSP/ConexionBD.java`.

## Agregar el conector en IntelliJ

1. Descargar MySQL Connector/J.
2. Guardar el archivo `.jar` en una carpeta del proyecto, por ejemplo `lib`.
3. En IntelliJ: File > Project Structure > Modules > Dependencies.
4. Presionar `+` > JARs or directories.
5. Seleccionar el `.jar` del conector.
6. Aplicar los cambios y ejecutar `Main`.

Si el programa muestra `No se encontro el conector JDBC de MySQL`, significa que falta este `.jar` en el proyecto.

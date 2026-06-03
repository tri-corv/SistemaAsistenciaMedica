package LSP;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    private static final String URL = "jdbc:mysql://localhost:3306/asistencia_medica?serverTimezone=America/Argentina/Buenos_Aires";
    private static final String USUARIO = "root";
    private static final String CLAVE = "";

    private ConexionBD() {
    }

    public static Connection obtenerConexion() throws SQLException {
        cargarDriver();
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }

    public static void probarConexion() throws SQLException {
        try (Connection conexion = obtenerConexion()) {
            if (!conexion.isValid(2)) {
                throw new SQLException("La conexion no es valida.");
            }
        }
    }

    private static void cargarDriver() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException error) {
            throw new SQLException("No se encontro el conector JDBC de MySQL. Agregue mysql-connector-j al proyecto.", error);
        }
    }
}

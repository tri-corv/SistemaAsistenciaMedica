package asistenciaMedica.dao;

import asistenciaMedica.modelo.ObraSocial;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ObraSocialDAO {
    public ObraSocial guardar(String nombre, double porcentajeCobertura) throws SQLException {
        String sql = "INSERT INTO obras_sociales (nombre, porcentaje_cobertura) VALUES (?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, nombre);
            sentencia.setDouble(2, porcentajeCobertura);
            sentencia.executeUpdate();

            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    return new ObraSocial(claves.getInt(1), nombre, porcentajeCobertura);
                }
            }
        }

        throw new SQLException("No se pudo obtener el ID generado para la obra social.");
    }

    public List<ObraSocial> listarTodas() throws SQLException {
        String sql = "SELECT id, nombre, porcentaje_cobertura FROM obras_sociales ORDER BY id";
        List<ObraSocial> obrasSociales = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                obrasSociales.add(mapear(resultado));
            }
        }

        return obrasSociales;
    }

    public Optional<ObraSocial> buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre, porcentaje_cobertura FROM obras_sociales WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return Optional.of(mapear(resultado));
                }
            }
        }

        return Optional.empty();
    }

    private ObraSocial mapear(ResultSet resultado) throws SQLException {
        return new ObraSocial(
                resultado.getInt("id"),
                resultado.getString("nombre"),
                resultado.getDouble("porcentaje_cobertura")
        );
    }
}

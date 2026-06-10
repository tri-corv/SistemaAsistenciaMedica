package LSP;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class VentanaPrincipal extends JFrame {
    private static final DateTimeFormatter FORMATO_ENTRADA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ObraSocialDAO obraSocialDAO = new ObraSocialDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final ProfesionalDAO profesionalDAO = new ProfesionalDAO();
    private final AsistenciaMedicaDAO asistenciaDAO = new AsistenciaMedicaDAO();

    private final JComboBox<ObraSocial> pacienteObraSocialCombo = new JComboBox<>();
    private final JComboBox<Paciente> asistenciaPacienteCombo = new JComboBox<>();
    private final JComboBox<Profesional> asistenciaProfesionalCombo = new JComboBox<>();
    private final JComboBox<String> asistenciaTipoCombo = new JComboBox<>(new String[]{"Consulta general", "Emergencia", "Telemedicina"});
    private final JComboBox<NivelUrgencia> urgenciaCombo = new JComboBox<>(NivelUrgencia.values());

    private final JTextField obraSocialNombreField = new JTextField(22);
    private final JSpinner obraSocialPorcentajeSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 5.0));
    private final JTextField pacienteNombreField = new JTextField(22);
    private final JTextField pacienteDniField = new JTextField(22);
    private final JTextField profesionalNombreField = new JTextField(22);
    private final JTextField profesionalEspecialidadField = new JTextField(22);
    private final JTextField profesionalMatriculaField = new JTextField(22);
    private final JTextField asistenciaFechaField = new JTextField("2026-06-10 10:30", 22);
    private final JTextField asistenciaDetalleField = new JTextField(22);

    private final DefaultTableModel asistenciasModel = new DefaultTableModel(
            new String[]{"ID", "Tipo", "Paciente", "Profesional", "Estado", "Total", "Paciente paga", "Obra social cubre"},
            0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable asistenciasTable = new JTable(asistenciasModel);

    public VentanaPrincipal() {
        super("Sistema de asistencias medicas");
        configurarVentana();
        verificarConexion();
        recargarCombos();
        recargarAsistencias();
    }

    public static void mostrar() {
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }

    private void configurarVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Obras sociales", crearPanelObraSocial());
        tabs.addTab("Pacientes", crearPanelPaciente());
        tabs.addTab("Profesionales", crearPanelProfesional());
        tabs.addTab("Asistencias", crearPanelAsistencia());
        tabs.addTab("Listado", crearPanelListado());

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel crearPanelObraSocial() {
        JPanel panel = crearFormulario();
        agregarCampo(panel, 0, "Nombre", obraSocialNombreField);
        agregarCampo(panel, 1, "Porcentaje de cobertura", obraSocialPorcentajeSpinner);

        JButton guardar = new JButton("Guardar obra social");
        guardar.addActionListener(event -> guardarObraSocial());
        agregarBoton(panel, 2, guardar);
        return panel;
    }

    private JPanel crearPanelPaciente() {
        JPanel panel = crearFormulario();
        agregarCampo(panel, 0, "Nombre", pacienteNombreField);
        agregarCampo(panel, 1, "DNI", pacienteDniField);
        agregarCampo(panel, 2, "Obra social", pacienteObraSocialCombo);

        JButton guardar = new JButton("Guardar paciente");
        guardar.addActionListener(event -> guardarPaciente());
        agregarBoton(panel, 3, guardar);
        return panel;
    }

    private JPanel crearPanelProfesional() {
        JPanel panel = crearFormulario();
        agregarCampo(panel, 0, "Nombre", profesionalNombreField);
        agregarCampo(panel, 1, "Especialidad", profesionalEspecialidadField);
        agregarCampo(panel, 2, "Matricula", profesionalMatriculaField);

        JButton guardar = new JButton("Guardar profesional");
        guardar.addActionListener(event -> guardarProfesional());
        agregarBoton(panel, 3, guardar);
        return panel;
    }

    private JPanel crearPanelAsistencia() {
        JPanel panel = crearFormulario();
        agregarCampo(panel, 0, "Paciente", asistenciaPacienteCombo);
        agregarCampo(panel, 1, "Profesional", asistenciaProfesionalCombo);
        agregarCampo(panel, 2, "Fecha y hora", asistenciaFechaField);
        agregarCampo(panel, 3, "Tipo", asistenciaTipoCombo);
        agregarCampo(panel, 4, "Nivel urgencia", urgenciaCombo);
        agregarCampo(panel, 5, "Consultorio o enlace", asistenciaDetalleField);

        asistenciaTipoCombo.addActionListener(event -> actualizarCamposTipo());
        actualizarCamposTipo();

        JButton guardar = new JButton("Guardar asistencia");
        guardar.addActionListener(event -> guardarAsistencia());
        agregarBoton(panel, 6, guardar);
        return panel;
    }

    private JPanel crearPanelListado() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        asistenciasTable.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(asistenciasTable), BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refrescar = new JButton("Actualizar listado");
        refrescar.addActionListener(event -> recargarAsistencias());
        JButton atender = new JButton("Atender seleccionada");
        atender.addActionListener(event -> atenderSeleccionada());
        JButton atenderPendientes = new JButton("Atender pendientes");
        atenderPendientes.addActionListener(event -> atenderPendientes());
        acciones.add(refrescar);
        acciones.add(atender);
        acciones.add(atenderPendientes);
        panel.add(acciones, BorderLayout.NORTH);
        return panel;
    }

    private JPanel crearFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        return panel;
    }

    private void agregarCampo(JPanel panel, int fila, String etiqueta, java.awt.Component campo) {
        GridBagConstraints label = new GridBagConstraints();
        label.gridx = 0;
        label.gridy = fila;
        label.anchor = GridBagConstraints.LINE_END;
        label.insets = new Insets(8, 8, 8, 12);
        panel.add(new JLabel(etiqueta + ":"), label);

        GridBagConstraints input = new GridBagConstraints();
        input.gridx = 1;
        input.gridy = fila;
        input.fill = GridBagConstraints.HORIZONTAL;
        input.weightx = 1;
        input.insets = new Insets(8, 8, 8, 8);
        panel.add(campo, input);
    }

    private void agregarBoton(JPanel panel, int fila, JButton boton) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = fila;
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.insets = new Insets(16, 8, 8, 8);
        panel.add(boton, constraints);
    }

    private void guardarObraSocial() {
        try {
            String nombre = leerTexto(obraSocialNombreField, "Ingrese el nombre de la obra social.");
            double porcentaje = ((Number) obraSocialPorcentajeSpinner.getValue()).doubleValue();
            obraSocialDAO.guardar(nombre, porcentaje);
            obraSocialNombreField.setText("");
            obraSocialPorcentajeSpinner.setValue(0.0);
            recargarCombos();
            informar("Obra social guardada.");
        } catch (IllegalArgumentException | SQLException error) {
            mostrarError(error);
        }
    }

    private void guardarPaciente() {
        try {
            String nombre = leerTexto(pacienteNombreField, "Ingrese el nombre del paciente.");
            String dni = leerTexto(pacienteDniField, "Ingrese el DNI del paciente.");
            ObraSocial obraSocial = (ObraSocial) pacienteObraSocialCombo.getSelectedItem();
            if (obraSocial == null) {
                throw new IllegalArgumentException("Debe seleccionar una obra social.");
            }
            pacienteDAO.guardar(nombre, dni, obraSocial);
            pacienteNombreField.setText("");
            pacienteDniField.setText("");
            recargarCombos();
            informar("Paciente guardado.");
        } catch (IllegalArgumentException | SQLException error) {
            mostrarError(error);
        }
    }

    private void guardarProfesional() {
        try {
            String nombre = leerTexto(profesionalNombreField, "Ingrese el nombre del profesional.");
            String especialidad = leerTexto(profesionalEspecialidadField, "Ingrese la especialidad.");
            String matricula = leerTexto(profesionalMatriculaField, "Ingrese la matricula.");
            profesionalDAO.guardar(nombre, especialidad, matricula);
            profesionalNombreField.setText("");
            profesionalEspecialidadField.setText("");
            profesionalMatriculaField.setText("");
            recargarCombos();
            informar("Profesional guardado.");
        } catch (IllegalArgumentException | SQLException error) {
            mostrarError(error);
        }
    }

    private void guardarAsistencia() {
        try {
            Paciente paciente = (Paciente) asistenciaPacienteCombo.getSelectedItem();
            Profesional profesional = (Profesional) asistenciaProfesionalCombo.getSelectedItem();
            if (paciente == null || profesional == null) {
                throw new IllegalArgumentException("Debe seleccionar paciente y profesional.");
            }

            LocalDateTime fechaHora = LocalDateTime.parse(asistenciaFechaField.getText().trim(), FORMATO_ENTRADA);
            String tipo = (String) asistenciaTipoCombo.getSelectedItem();
            String detalle = asistenciaDetalleField.getText().trim();

            if ("Consulta general".equals(tipo)) {
                if (detalle.isEmpty()) {
                    throw new IllegalArgumentException("Ingrese el consultorio.");
                }
                asistenciaDAO.guardarConsultaGeneral(paciente, profesional, fechaHora, detalle);
            } else if ("Emergencia".equals(tipo)) {
                NivelUrgencia nivel = (NivelUrgencia) urgenciaCombo.getSelectedItem();
                asistenciaDAO.guardarEmergencia(paciente, profesional, fechaHora, nivel);
            } else {
                if (detalle.isEmpty()) {
                    throw new IllegalArgumentException("Ingrese el enlace de videollamada.");
                }
                asistenciaDAO.guardarTelemedicina(paciente, profesional, fechaHora, detalle);
            }

            asistenciaDetalleField.setText("");
            recargarAsistencias();
            informar("Asistencia guardada.");
        } catch (DateTimeParseException error) {
            mostrarError(new IllegalArgumentException("Formato de fecha invalido. Use yyyy-MM-dd HH:mm."));
        } catch (IllegalArgumentException | SQLException error) {
            mostrarError(error);
        }
    }

    private void atenderSeleccionada() {
        int fila = asistenciasTable.getSelectedRow();
        if (fila < 0) {
            informar("Seleccione una asistencia de la tabla.");
            return;
        }

        int modeloFila = asistenciasTable.convertRowIndexToModel(fila);
        int id = (Integer) asistenciasModel.getValueAt(modeloFila, 0);

        try {
            asistenciaDAO.marcarAtendida(id);
            recargarAsistencias();
            informar("Asistencia marcada como atendida.");
        } catch (SQLException error) {
            mostrarError(error);
        }
    }

    private void atenderPendientes() {
        try {
            List<AsistenciaMedica> pendientes = asistenciaDAO.listarPendientes();
            for (AsistenciaMedica asistencia : pendientes) {
                asistenciaDAO.marcarAtendida(asistencia.getId());
            }
            recargarAsistencias();
            informar("Asistencias pendientes atendidas.");
        } catch (SQLException error) {
            mostrarError(error);
        }
    }

    private void recargarCombos() {
        try {
            recargarCombo(pacienteObraSocialCombo, obraSocialDAO.listarTodas());
            recargarCombo(asistenciaPacienteCombo, pacienteDAO.listarTodos());
            recargarCombo(asistenciaProfesionalCombo, profesionalDAO.listarTodos());
        } catch (SQLException error) {
            mostrarError(error);
        }
    }

    private <T> void recargarCombo(JComboBox<T> combo, List<T> elementos) {
        combo.removeAllItems();
        for (T elemento : elementos) {
            combo.addItem(elemento);
        }
    }

    private void recargarAsistencias() {
        try {
            asistenciasModel.setRowCount(0);
            for (AsistenciaMedica asistencia : asistenciaDAO.listarTodas()) {
                asistenciasModel.addRow(new Object[]{
                        asistencia.getId(),
                        asistencia.obtenerTipo(),
                        asistencia.getPaciente().getNombre(),
                        asistencia.getProfesional().getNombre(),
                        asistencia.getEstado(),
                        importe(asistencia.obtenerValorConsulta()),
                        importe(asistencia.calcularMontoPaciente()),
                        importe(asistencia.calcularMontoObraSocial())
                });
            }
        } catch (SQLException error) {
            mostrarError(error);
        }
    }

    private void actualizarCamposTipo() {
        String tipo = (String) asistenciaTipoCombo.getSelectedItem();
        boolean emergencia = "Emergencia".equals(tipo);
        urgenciaCombo.setEnabled(emergencia);
        asistenciaDetalleField.setEnabled(!emergencia);
        if (emergencia) {
            asistenciaDetalleField.setText("");
        }
    }

    private void verificarConexion() {
        try {
            ConexionBD.probarConexion();
        } catch (SQLException error) {
            mostrarError(new SQLException("No se pudo conectar a MySQL. Revise XAMPP, la base asistencia_medica y el conector JDBC.\n" + error.getMessage()));
        }
    }

    private String leerTexto(JTextField campo, String mensajeError) {
        String valor = campo.getText().trim();
        if (valor.isEmpty()) {
            throw new IllegalArgumentException(mensajeError);
        }
        return valor;
    }

    private String importe(double valor) {
        return "$" + String.format("%.2f", valor);
    }

    private void informar(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Sistema de asistencias medicas", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(Exception error) {
        JOptionPane.showMessageDialog(this, error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

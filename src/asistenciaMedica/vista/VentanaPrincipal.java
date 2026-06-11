package asistenciaMedica.vista;

import asistenciaMedica.dao.AsistenciaMedicaDAO;
import asistenciaMedica.dao.ConexionBD;
import asistenciaMedica.dao.ObraSocialDAO;
import asistenciaMedica.dao.PacienteDAO;
import asistenciaMedica.dao.ProfesionalDAO;
import asistenciaMedica.modelo.AsistenciaMedica;
import asistenciaMedica.modelo.NivelUrgencia;
import asistenciaMedica.modelo.ObraSocial;
import asistenciaMedica.modelo.Paciente;
import asistenciaMedica.modelo.Profesional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.Component;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class VentanaPrincipal extends JFrame {
    private static final DateTimeFormatter FORMATO_ENTRADA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final Color AZUL_PRINCIPAL = new Color(25, 118, 210);
    private static final Color FONDO_CLARO = new Color(245, 247, 250);
    private static final Color BORDE_SUAVE = new Color(220, 220, 220);
    private static final Font FUENTE_NORMAL = new Font("Arial", Font.PLAIN, 14);

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

    private TableRowSorter<DefaultTableModel> sorter;
    private final JTextField buscarAsistenciaField = new JTextField(25);
    private JPanel contenidoPrincipal;

    private JLabel totalPacientesLabel;
    private JLabel totalProfesionalesLabel;
    private JLabel totalAsistenciasLabel;
    private JLabel totalPendientesLabel;

    private JButton botonMenuActivo;

    private final JComboBox<String> filtroEstadoCombo = new JComboBox<>(
            new String[]{"Todos", "Pendiente", "Atendida"}
    );
    private boolean filtroEstadoConfigurado = false;

    private final JComboBox<String> filtroTipoCombo = new JComboBox<>(
            new String[]{"Todos", "Consulta general", "Emergencia", "Telemedicina"}
    );
    private boolean filtroTipoConfigurado = false;

    public VentanaPrincipal() {
        super("Sistema de asistencias medicas");
        configurarVentana();
        estilizarComponentes();
        verificarConexion();
        recargarCombos();
        recargarAsistencias();
        actualizarDashboard();
    }

    public static void mostrar() {
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }

    private void configurarVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel menuLateral = crearMenuLateral();
        contenidoPrincipal = new JPanel(new BorderLayout());
        contenidoPrincipal.setBackground(FONDO_CLARO);

        mostrarPanel(crearPanelInicio());

        add(crearEncabezado(), BorderLayout.NORTH);
        add(menuLateral, BorderLayout.WEST);
        add(contenidoPrincipal, BorderLayout.CENTER);
    }

    private void mostrarPanel(JPanel panel) {
        contenidoPrincipal.removeAll();
        contenidoPrincipal.add(panel, BorderLayout.CENTER);
        contenidoPrincipal.revalidate();
        contenidoPrincipal.repaint();
    }

    private JPanel crearMenuLateral() {
        JPanel menu = new JPanel(new BorderLayout());
        menu.setBackground(AZUL_PRINCIPAL);
        menu.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        menu.setPreferredSize(new Dimension(220, 0));

        JLabel tituloMenu = new JLabel("MENÚ");
        tituloMenu.setForeground(Color.WHITE);
        tituloMenu.setFont(new Font("Arial", Font.BOLD, 18));
        tituloMenu.setHorizontalAlignment(JLabel.CENTER);

        JPanel botonesMenu = new JPanel(new GridLayout(6, 1, 0, 5));
        botonesMenu.setBackground(AZUL_PRINCIPAL);

        JButton inicio = crearBotonMenu("Inicio");
        inicio.addActionListener(e -> {
            seleccionarBotonMenu(inicio);
            mostrarPanel(crearPanelInicio());
        });

        JButton obras = crearBotonMenu("Obras sociales");
        obras.addActionListener(e -> {
            seleccionarBotonMenu(obras);
            mostrarPanel(crearPanelObraSocial());
        });

        JButton pacientes = crearBotonMenu("Pacientes");
        pacientes.addActionListener(e -> {
            seleccionarBotonMenu(pacientes);
            mostrarPanel(crearPanelPaciente());
        });

        JButton profesionales = crearBotonMenu("Profesionales");
        profesionales.addActionListener(e -> {
            seleccionarBotonMenu(profesionales);
            mostrarPanel(crearPanelProfesional());
        });

        JButton asistencias = crearBotonMenu("Asistencias");
        asistencias.addActionListener(e -> {
            seleccionarBotonMenu(asistencias);
            mostrarPanel(crearPanelAsistencia());
        });

        JButton listado = crearBotonMenu("Listado");
        listado.addActionListener(e -> {
            seleccionarBotonMenu(listado);
            mostrarPanel(crearPanelListado());
            recargarAsistencias();
        });

        botonesMenu.add(inicio);
        botonesMenu.add(obras);
        botonesMenu.add(pacientes);
        botonesMenu.add(profesionales);
        botonesMenu.add(asistencias);
        botonesMenu.add(listado);

        menu.add(tituloMenu, BorderLayout.NORTH);
        menu.add(botonesMenu, BorderLayout.CENTER);

        seleccionarBotonMenu(inicio);

        return menu;
    }

    private JButton crearBotonMenu(String texto) {
        JButton boton = new JButton(texto);
        boton.setForeground(Color.WHITE);
        boton.setBackground(AZUL_PRINCIPAL);
        boton.setFont(new Font("Arial", Font.BOLD, 15));
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        boton.setHorizontalAlignment(JButton.LEFT);

        return boton;
    }

    private void seleccionarBotonMenu(JButton boton) {
        if (botonMenuActivo != null) {
            botonMenuActivo.setBackground(AZUL_PRINCIPAL);
        }

        boton.setBackground(new Color(13, 71, 161));
        botonMenuActivo = boton;
    }

    private JPanel crearPanelInicio() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(FONDO_CLARO);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titulo = new JLabel("Sistema de Asistencias Médicas", JLabel.CENTER);
        titulo.setFont(new java.awt.Font("Arial", Font.BOLD, 26));

        JLabel subtitulo = new JLabel("Panel General", JLabel.CENTER);
        subtitulo.setFont(new java.awt.Font("Arial", Font.PLAIN, 16));

        JPanel encabezado = new JPanel();
        encabezado.setBackground(FONDO_CLARO);
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));

        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        encabezado.add(titulo);
        encabezado.add(Box.createVerticalStrut(8));
        encabezado.add(subtitulo);

        JPanel tarjetas = new JPanel(new java.awt.GridLayout(2, 2, 20, 20));
        tarjetas.setBackground(FONDO_CLARO);

        totalPacientesLabel = new JLabel("0");
        totalProfesionalesLabel = new JLabel("0");
        totalAsistenciasLabel = new JLabel("0");
        totalPendientesLabel = new JLabel("0");

        tarjetas.add(crearTarjetaDashboard("Pacientes", totalPacientesLabel, "Registrados", AZUL_PRINCIPAL));
        tarjetas.add(crearTarjetaDashboard("Profesionales", totalProfesionalesLabel, "Activos", new Color(46, 125, 50)));
        tarjetas.add(crearTarjetaDashboard("Asistencias", totalAsistenciasLabel, "Totales", new Color(94, 53, 177)));
        tarjetas.add(crearTarjetaDashboard("Pendientes", totalPendientesLabel, "Sin atender", new Color(245, 124, 0)));

        panel.add(encabezado, BorderLayout.NORTH);
        panel.add(tarjetas, BorderLayout.CENTER);

        actualizarDashboard();

        return panel;
    }

    private JPanel crearTarjetaDashboard(String titulo, JLabel numeroLabel, String descripcion, Color color) {
        JPanel tarjeta = new JPanel(new BorderLayout(10, 10));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE_SUAVE),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel tituloLabel = new JLabel(titulo);
        tituloLabel.setFont(new Font("Arial", Font.BOLD, 18));

        numeroLabel.setFont(new Font("Arial", Font.BOLD, 34));
        numeroLabel.setForeground(color);

        JLabel descripcionLabel = new JLabel(descripcion);
        descripcionLabel.setFont(FUENTE_NORMAL);

        tarjeta.add(tituloLabel, BorderLayout.NORTH);
        tarjeta.add(numeroLabel, BorderLayout.CENTER);
        tarjeta.add(descripcionLabel, BorderLayout.SOUTH);

        return tarjeta;
    }

    private void actualizarDashboard() {
        try {
            if (totalPacientesLabel != null) {
                totalPacientesLabel.setText(String.valueOf(pacienteDAO.listarTodos().size()));
            }

            if (totalProfesionalesLabel != null) {
                totalProfesionalesLabel.setText(String.valueOf(profesionalDAO.listarTodos().size()));
            }

            if (totalAsistenciasLabel != null) {
                totalAsistenciasLabel.setText(String.valueOf(asistenciaDAO.listarTodas().size()));
            }

            if (totalPendientesLabel != null) {
                totalPendientesLabel.setText(String.valueOf(asistenciaDAO.listarPendientes().size()));
            }

        } catch (SQLException error) {
            mostrarError(error);
        }
    }

    private JPanel crearPanelObraSocial() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(FONDO_CLARO);
        contenedor.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel panel = crearTarjeta("Datos de la Obra Social");
        agregarCampo(panel, 0, "Nombre", obraSocialNombreField);
        agregarCampo(panel, 1, "Porcentaje de cobertura", obraSocialPorcentajeSpinner);

        JButton guardar = new JButton("Guardar obra social");
        estilizarBotonPrincipal(guardar);
        guardar.addActionListener(event -> guardarObraSocial());
        agregarBoton(panel, 2, guardar);
        contenedor.add(panel, BorderLayout.NORTH);

        return contenedor;
    }

    private JPanel crearPanelPaciente() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(FONDO_CLARO);
        contenedor.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel panel = crearTarjeta("Datos del Paciente");
        agregarCampo(panel, 0, "Nombre", pacienteNombreField);
        agregarCampo(panel, 1, "DNI", pacienteDniField);
        agregarCampo(panel, 2, "Obra social", pacienteObraSocialCombo);

        JButton guardar = new JButton("Guardar paciente");
        estilizarBotonPrincipal(guardar);
        guardar.addActionListener(event -> guardarPaciente());
        agregarBoton(panel, 3, guardar);
        contenedor.add(panel, BorderLayout.NORTH);

        return contenedor;
    }


    private JPanel crearPanelProfesional() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(FONDO_CLARO);
        contenedor.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel panel = crearTarjeta("Datos del Profesional");
        agregarCampo(panel, 0, "Nombre", profesionalNombreField);
        agregarCampo(panel, 1, "Especialidad", profesionalEspecialidadField);
        agregarCampo(panel, 2, "Matricula", profesionalMatriculaField);

        JButton guardar = new JButton("Guardar profesional");
        estilizarBotonPrincipal(guardar);
        guardar.addActionListener(event -> guardarProfesional());
        agregarBoton(panel, 3, guardar);
        contenedor.add(panel, BorderLayout.NORTH);

        return contenedor;
    }

    private JPanel crearPanelAsistencia() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(FONDO_CLARO);
        contenedor.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel panel = crearTarjeta("Datos de la Asistencia");
        agregarCampo(panel, 0, "Paciente", asistenciaPacienteCombo);
        agregarCampo(panel, 1, "Profesional", asistenciaProfesionalCombo);
        agregarCampo(panel, 2, "Fecha y hora", asistenciaFechaField);
        agregarCampo(panel, 3, "Tipo", asistenciaTipoCombo);
        agregarCampo(panel, 4, "Nivel urgencia", urgenciaCombo);
        agregarCampo(panel, 5, "Consultorio o enlace", asistenciaDetalleField);

        asistenciaTipoCombo.addActionListener(event -> actualizarCamposTipo());
        actualizarCamposTipo();

        JButton guardar = new JButton("Guardar asistencia");
        estilizarBotonPrincipal(guardar);
        guardar.addActionListener(event -> guardarAsistencia());
        agregarBoton(panel, 6, guardar);
        contenedor.add(panel, BorderLayout.NORTH);

        return contenedor;
    }

    private boolean buscadorConfigurado = false;

    private JPanel crearPanelListado() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        sorter = new TableRowSorter<>(asistenciasModel);
        asistenciasTable.setRowSorter(sorter);
        estilizarTablaAsistencias();
        panel.add(new JScrollPane(asistenciasTable), BorderLayout.CENTER);

        JPanel acciones = new JPanel(new BorderLayout(10, 10));
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel buscador = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton refrescar = new JButton("Actualizar listado");
        estilizarBotonPrincipal(refrescar);
        refrescar.addActionListener(event -> recargarAsistencias());
        JButton atender = new JButton("Atender seleccionada");
        estilizarBotonPrincipal(atender);
        atender.addActionListener(event -> atenderSeleccionada());
        JButton atenderPendientes = new JButton("Atender pendientes");
        estilizarBotonPrincipal(atenderPendientes);
        atenderPendientes.addActionListener(event -> atenderPendientes());
        JButton detalle = new JButton("Ver detalle");
        estilizarBotonSecundario(detalle);
        detalle.addActionListener(e -> verDetalleAsistencia());

        botones.add(refrescar);
        botones.add(atender);
        botones.add(atenderPendientes);
        botones.add(detalle);

        buscador.add(new JLabel("🔍 Buscar:"));
        buscador.add(buscarAsistenciaField);

        buscador.add(new JLabel("Estado:"));
        buscador.add(filtroEstadoCombo);
        buscador.add(new JLabel("Tipo:"));
        buscador.add(filtroTipoCombo);

        acciones.add(botones, BorderLayout.NORTH);
        acciones.add(buscador, BorderLayout.SOUTH);

        if (!buscadorConfigurado) {
            buscarAsistenciaField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    filtrarAsistencias();
                }

                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    filtrarAsistencias();
                }

                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    filtrarAsistencias();
                }
            });

            buscadorConfigurado = true;
        }

        if (!filtroEstadoConfigurado) {
            filtroEstadoCombo.addActionListener(e -> filtrarAsistencias());
            filtroEstadoConfigurado = true;
        }

        if (!filtroTipoConfigurado) {
            filtroTipoCombo.addActionListener(e -> filtrarAsistencias());
            filtroTipoConfigurado = true;
        }

        panel.add(acciones, BorderLayout.NORTH);
        return panel;
    }

    private void verDetalleAsistencia() {
        int fila = asistenciasTable.getSelectedRow();

        if (fila < 0) {
            informar("Seleccione una asistencia de la tabla.");
            return;
        }

        int filaModelo = asistenciasTable.convertRowIndexToModel(fila);

        StringBuilder detalle = new StringBuilder();

        for (int columna = 0; columna < asistenciasModel.getColumnCount(); columna++) {
            detalle.append(asistenciasModel.getColumnName(columna))
                    .append(": ")
                    .append(asistenciasModel.getValueAt(filaModelo, columna))
                    .append("\n");
        }

        JOptionPane.showMessageDialog(
                this,
                detalle.toString(),
                "Detalle de asistencia",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void estilizarBotonSecundario(JButton boton) {
        boton.setBackground(Color.WHITE);
        boton.setForeground(AZUL_PRINCIPAL);
        boton.setFont(new Font("Arial", Font.BOLD, 14));
        boton.setFocusPainted(false);

        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AZUL_PRINCIPAL),
                BorderFactory.createEmptyBorder(9, 16, 9, 16)
        ));
    }

    private JPanel crearEncabezado() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titulo = new JLabel("Sistema de Asistencias Médicas");
        titulo.setFont(new Font("Arial", Font.BOLD, 22));

        JLabel usuario = new JLabel("Administrador");
        usuario.setFont(new Font("Arial", Font.PLAIN, 14));

        header.add(titulo, BorderLayout.WEST);
        header.add(usuario, BorderLayout.EAST);

        return header;
    }

    private JPanel crearTarjeta(String titulo) {
        JPanel tarjeta = new JPanel(new GridBagLayout());
        tarjeta.setBackground(Color.WHITE);

        javax.swing.border.TitledBorder borde =
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(BORDE_SUAVE),
                        titulo
                );

        borde.setTitleFont(new Font("Arial", Font.BOLD, 20));
        borde.setTitleColor(Color.BLACK);

        tarjeta.setBorder(
                BorderFactory.createCompoundBorder(
                        borde,
                        BorderFactory.createEmptyBorder(15, 15, 15, 15)
                )
        );

        return tarjeta;
    }

    private void filtrarAsistencias() {
        if (sorter == null) {
            return;
        }

        String texto = buscarAsistenciaField.getText().trim();
        String estado = String.valueOf(filtroEstadoCombo.getSelectedItem());
        String tipo = String.valueOf(filtroTipoCombo.getSelectedItem());

        java.util.List<RowFilter<Object, Object>> filtros = new java.util.ArrayList<>();

        if (!texto.isEmpty()) {
            filtros.add(RowFilter.regexFilter("(?i)" + texto));
        }

        if (!"Todos".equals(estado)) {
            filtros.add(RowFilter.regexFilter("(?i)^" + estado + "$", 4));
        }

        if (!"Todos".equals(tipo)) {
            filtros.add(RowFilter.regexFilter("(?i)" + tipo, 1));
        }

        if (filtros.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filtros));
        }
    }

    private void estilizarTablaAsistencias() {
        asistenciasTable.setRowHeight(28);
        asistenciasTable.setFont(FUENTE_NORMAL);
        asistenciasTable.setGridColor(BORDE_SUAVE);
        asistenciasTable.setShowVerticalLines(false);

        JTableHeader header = asistenciasTable.getTableHeader();
        header.setBackground(AZUL_PRINCIPAL);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 14));

        asistenciasTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column
            ) {
                Component componente = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column
                );

                if (!isSelected) {
                    componente.setBackground(row % 2 == 0 ? Color.WHITE : FONDO_CLARO);
                    componente.setForeground(Color.BLACK);

                    int columnaModelo = table.convertColumnIndexToModel(column);

                    if (columnaModelo == 1) { // Tipo
                        String tipo = String.valueOf(value).toUpperCase();

                        if (tipo.contains("EMERGENCIA")) {
                            componente.setForeground(new Color(198, 40, 40));
                        } else if (tipo.contains("TELEMEDICINA")) {
                            componente.setForeground(new Color(25, 118, 210));
                        } else if (tipo.contains("CONSULTA")) {
                            componente.setForeground(new Color(46, 125, 50));
                        }
                    }

                    if (columnaModelo == 4) { // Estado
                        String estado = String.valueOf(value);

                        if (estado.equalsIgnoreCase("PENDIENTE")) {
                            componente.setForeground(new Color(245, 124, 0));
                        } else if (estado.equalsIgnoreCase("ATENDIDA")) {
                            componente.setForeground(new Color(46, 125, 50));
                        }
                    }
                }

                setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

                return componente;
            }
        });
    }

    private void estilizarBotonPrincipal(JButton boton) {
        boton.setBackground(AZUL_PRINCIPAL);
        boton.setForeground(Color.WHITE);
        boton.setFont(new Font("Arial", Font.BOLD, 14));
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
    }

    private void estilizarCampo(JTextField campo) {
        campo.setFont(FUENTE_NORMAL);
        campo.setPreferredSize(new Dimension(250, 35));
    }

    private void estilizarCombo(JComboBox<?> combo) {
        combo.setFont(FUENTE_NORMAL);
        combo.setPreferredSize(new Dimension(250, 35));
    }

    private void estilizarSpinner(JSpinner spinner) {
        spinner.setPreferredSize(new Dimension(250, 35));
    }

    private void estilizarComponentes() {

        estilizarCampo(obraSocialNombreField);

        estilizarCampo(pacienteNombreField);
        estilizarCampo(pacienteDniField);

        estilizarCampo(profesionalNombreField);
        estilizarCampo(profesionalEspecialidadField);
        estilizarCampo(profesionalMatriculaField);

        estilizarCampo(asistenciaFechaField);
        estilizarCampo(asistenciaDetalleField);

        estilizarCombo(pacienteObraSocialCombo);
        estilizarCombo(asistenciaPacienteCombo);
        estilizarCombo(asistenciaProfesionalCombo);
        estilizarCombo(asistenciaTipoCombo);
        estilizarCombo(urgenciaCombo);

        estilizarSpinner(obraSocialPorcentajeSpinner);

        agregarPlaceholder(pacienteNombreField, "Ingrese nombre completo");
        agregarPlaceholder(pacienteDniField, "Ej: 40123456");

        agregarPlaceholder(profesionalNombreField, "Nombre del profesional");
        agregarPlaceholder(profesionalEspecialidadField, "Ej: Cardiología");
        agregarPlaceholder(profesionalMatriculaField, "Ej: MP-1234");

        agregarPlaceholder(obraSocialNombreField, "Nombre de la obra social");

        agregarPlaceholder(asistenciaDetalleField, "Consultorio o enlace");
    }

    private void agregarCampo(JPanel panel, int fila, String etiqueta, java.awt.Component campo) {
        GridBagConstraints label = new GridBagConstraints();
        label.gridx = 0;
        label.gridy = fila;
        label.anchor = GridBagConstraints.LINE_END;
        label.insets = new Insets(8, 8, 8, 12);
        JLabel texto = new JLabel(etiqueta + ":");
        texto.setFont(new Font("Arial", Font.BOLD, 14));

        panel.add(texto, label);

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

    private void agregarPlaceholder(JTextField campo, String texto) {
        campo.setText(texto);
        campo.setForeground(Color.GRAY);

        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (campo.getText().equals(texto)) {
                    campo.setText("");
                    campo.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (campo.getText().trim().isEmpty()) {
                    campo.setText(texto);
                    campo.setForeground(Color.GRAY);
                }
            }
        });
    }

    private void guardarObraSocial() {
        try {
            String nombre = leerTexto(obraSocialNombreField, "Ingrese el nombre de la obra social.");
            double porcentaje = ((Number) obraSocialPorcentajeSpinner.getValue()).doubleValue();
            obraSocialDAO.guardar(nombre, porcentaje);
            obraSocialNombreField.setText("");
            obraSocialPorcentajeSpinner.setValue(0.0);
            recargarCombos();
            actualizarDashboard();
            informar("Obra social guardada correctamente.");
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
            actualizarDashboard();
            informar("Paciente guardado correctamente.");
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
            actualizarDashboard();
            informar("Profesional guardado correctamente.");
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
            actualizarDashboard();
            informar("Asistencia registrada correctamente.");
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
            if (!confirmar("¿Desea marcar la asistencia como atendida?")) {
                return;
            }
            asistenciaDAO.marcarAtendida(id);
            recargarAsistencias();
            actualizarDashboard();
            informar("Asistencia marcada como atendida.");
        } catch (SQLException error) {
            mostrarError(error);
        }
    }

    private void atenderPendientes() {
        try {
            List<AsistenciaMedica> pendientes = asistenciaDAO.listarPendientes();
            if (!confirmar("¿Desea marcar todas las asistencias pendientes como atendidas?")) {
                return;
            }
            for (AsistenciaMedica asistencia : pendientes) {
                asistenciaDAO.marcarAtendida(asistencia.getId());
            }
            recargarAsistencias();
            actualizarDashboard();
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

            if (sorter != null) {
                filtrarAsistencias();
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
        JOptionPane.showMessageDialog(
                this,
                "✅ " + mensaje,
                "Operación exitosa",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void mostrarError(Exception error) {
        JOptionPane.showMessageDialog(
                this,
                "⚠️ " + error.getMessage(),
                "Atención",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private boolean confirmar(String mensaje) {
        int opcion = JOptionPane.showConfirmDialog(
                this,
                mensaje,
                "Confirmar acción",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        return opcion == JOptionPane.YES_OPTION;
    }
}

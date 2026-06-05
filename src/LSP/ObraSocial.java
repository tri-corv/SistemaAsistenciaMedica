package LSP;

public class ObraSocial {
    private final int id;
    private final String nombre;
    private final double porcentajeCobertura;

    public ObraSocial(int id, String nombre, double porcentajeCobertura) {
        if (porcentajeCobertura < 0 || porcentajeCobertura > 100) {
            throw new IllegalArgumentException("El porcentaje de cobertura debe estar entre 0 y 100.");
        }

        this.id = id;
        this.nombre = nombre;
        this.porcentajeCobertura = porcentajeCobertura;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPorcentajeCobertura() {
        return porcentajeCobertura;
    }
}

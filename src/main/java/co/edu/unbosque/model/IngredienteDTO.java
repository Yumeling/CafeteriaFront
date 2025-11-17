package co.edu.unbosque.model;

public class IngredienteDTO {
    private Integer codigo;
    private String nombre;
    private Integer cantidad; // cantidad solicitada
    private String estado;
    private Integer costoUnitario; // agregado como pediste

    public IngredienteDTO() { }

    public IngredienteDTO(Integer codigo, String nombre, Integer cantidad, String estado, Integer costoUnitario) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.estado = estado;
        this.costoUnitario = costoUnitario;
    }

    public Integer getCodigo() { return codigo; }
    public void setCodigo(Integer codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Integer getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(Integer costoUnitario) { this.costoUnitario = costoUnitario; }

    @Override
    public String toString() {
        return "IngredienteDTO [codigo=" + codigo + ", nombre=" + nombre + ", cantidad=" + cantidad +
                ", estado=" + estado + ", costoUnitario=" + costoUnitario + "]";
    }
}

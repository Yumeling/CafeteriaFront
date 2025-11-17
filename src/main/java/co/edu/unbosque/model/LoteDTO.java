package co.edu.unbosque.model;

import java.time.LocalDate;

public class LoteDTO {
    private Integer loteId;
    private LocalDate fechaRecepcion;
    private LocalDate fechaCaducidad;   // nuevo
    private Integer cantidad;
    private Integer ordenId;
    private Integer codigoIngrediente;
    private Integer proveedorId;
    private Integer precio;            // nuevo: precio total del lote

    public LoteDTO() { }

   

    public LoteDTO(Integer loteId, LocalDate fechaRecepcion, LocalDate fechaCaducidad, Integer cantidad,
			Integer ordenId, Integer codigoIngrediente, Integer proveedorId, Integer precio) {
		super();
		this.loteId = loteId;
		this.fechaRecepcion = fechaRecepcion;
		this.fechaCaducidad = fechaCaducidad;
		this.cantidad = cantidad;
		this.ordenId = ordenId;
		this.codigoIngrediente = codigoIngrediente;
		this.proveedorId = proveedorId;
		this.precio = precio;
	}



	public Integer getLoteId() { return loteId; }
    public void setLoteId(Integer loteId) { this.loteId = loteId; }

    public LocalDate getFechaRecepcion() { return fechaRecepcion; }
    public void setFechaRecepcion(LocalDate fechaRecepcion) { this.fechaRecepcion = fechaRecepcion; }

    public LocalDate getFechaCaducidad() { return fechaCaducidad; }
    public void setFechaCaducidad(LocalDate fechaCaducidad) { this.fechaCaducidad = fechaCaducidad; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public Integer getOrdenId() { return ordenId; }
    public void setOrdenId(Integer ordenId) { this.ordenId = ordenId; }

    public Integer getCodigoIngrediente() { return codigoIngrediente; }
    public void setCodigoIngrediente(Integer codigoIngrediente) { this.codigoIngrediente = codigoIngrediente; }

    public Integer getProveedorId() { return proveedorId; }
    public void setProveedorId(Integer proveedorId) { this.proveedorId = proveedorId; }

    public Integer getPrecio() { return precio; }
    public void setPrecio(Integer precio) { this.precio = precio; }

    @Override
    public String toString() {
        return "LoteDTO [loteId=" + loteId + ", fechaRecepcion=" + fechaRecepcion + ", fechaCaducidad=" + fechaCaducidad
                + ", cantidad=" + cantidad + ", ordenId=" + ordenId + ", codigoIngrediente=" + codigoIngrediente
                + ", proveedorId=" + proveedorId + ", precio=" + precio + "]";
    }
}

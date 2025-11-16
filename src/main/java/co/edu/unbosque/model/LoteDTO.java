package co.edu.unbosque.model;

import java.time.LocalDate;

public class LoteDTO {
	private Integer loteId;
	private LocalDate fechaRecepcion;
	private Integer cantidad;
	private Integer ordenId;
	private Integer codigoIngrediente;
	private Integer proveedorId;

	public LoteDTO() {
		super();
	}

	

	public LoteDTO(LocalDate fechaRecepcion, Integer cantidad, Integer ordenId, Integer codigoIngrediente,
			Integer proveedorId) {
		super();
		this.fechaRecepcion = fechaRecepcion;
		this.cantidad = cantidad;
		this.ordenId = ordenId;
		this.codigoIngrediente = codigoIngrediente;
		this.proveedorId = proveedorId;
	}



	

	public LocalDate getFechaRecepcion() {
		return fechaRecepcion;
	}

	public void setFechaRecepcion(LocalDate fechaRecepcion) {
		this.fechaRecepcion = fechaRecepcion;
	}

	public Integer getCantidad() {
		return cantidad;
	}

	public void setCantidad(Integer cantidad) {
		this.cantidad = cantidad;
	}

	public Integer getOrdenId() {
		return ordenId;
	}

	public void setOrdenId(Integer ordenId) {
		this.ordenId = ordenId;
	}

	public Integer getCodigoIngrediente() {
		return codigoIngrediente;
	}

	public void setCodigoIngrediente(Integer codigoIngrediente) {
		this.codigoIngrediente = codigoIngrediente;
	}

	public Integer getProveedorId() {
		return proveedorId;
	}

	public void setProveedorId(Integer proveedorId) {
		this.proveedorId = proveedorId;
	}

	@Override
	public String toString() {
		return "LoteDTO [loteId=" + loteId + ", fechaRecepcion=" + fechaRecepcion + ", cantidad=" + cantidad
				+ ", ordenId=" + ordenId + ", codigoIngrediente=" + codigoIngrediente + ", proveedorId=" + proveedorId
				+ "]";
	}

}

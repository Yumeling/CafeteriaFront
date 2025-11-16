package co.edu.unbosque.model;

import java.time.LocalDate;

public class OrdenCompraDTO {
	private Integer ordenId;
	private LocalDate fechaEmision;
	private LocalDate fechaRecepcion;
	private String estado;
	private Integer total;
	private Integer nitEmpresa;
	private Integer administradorId;

	public OrdenCompraDTO() {
		super();
	}

	

	public OrdenCompraDTO(LocalDate fechaEmision, LocalDate fechaRecepcion, String estado, Integer total,
			Integer nitEmpresa, Integer administradorId) {
		super();
		this.fechaEmision = fechaEmision;
		this.fechaRecepcion = fechaRecepcion;
		this.estado = estado;
		this.total = total;
		this.nitEmpresa = nitEmpresa;
		this.administradorId = administradorId;
	}



	

	public Integer getOrdenId() {
		return ordenId;
	}



	public void setOrdenId(Integer ordenId) {
		this.ordenId = ordenId;
	}



	public LocalDate getFechaEmision() {
		return fechaEmision;
	}

	public void setFechaEmision(LocalDate fechaEmision) {
		this.fechaEmision = fechaEmision;
	}

	public LocalDate getFechaRecepcion() {
		return fechaRecepcion;
	}

	public void setFechaRecepcion(LocalDate fechaRecepcion) {
		this.fechaRecepcion = fechaRecepcion;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public Integer getNitEmpresa() {
		return nitEmpresa;
	}

	public void setNitEmpresa(Integer nitEmpresa) {
		this.nitEmpresa = nitEmpresa;
	}

	public Integer getAdministradorId() {
		return administradorId;
	}

	public void setAdministradorId(Integer administradorId) {
		this.administradorId = administradorId;
	}

	@Override
	public String toString() {
		return "OrdenCompraDTO [ordenId=" + ordenId + ", fechaEmision=" + fechaEmision + ", fechaRecepcion="
				+ fechaRecepcion + ", estado=" + estado + ", total=" + total + ", nitEmpresa=" + nitEmpresa
				+ ", administradorId=" + administradorId + "]";
	}

}

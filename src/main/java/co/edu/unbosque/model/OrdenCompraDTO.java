package co.edu.unbosque.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrdenCompraDTO {
	private Integer ordenId;
	private LocalDate fechaEmision;
	private String estado;
	private Integer administradorId;
	private String administradorNombre;
	private Integer nitEmpresa;
	private List<IngredienteDTO> ingredientes = new ArrayList<>();

	public OrdenCompraDTO() {
	}

	public OrdenCompraDTO(Integer ordenId, LocalDate fechaEmision, String estado, Integer administradorId,
			String administradorNombre, Integer nitEmpresa, List<IngredienteDTO> ingredientes) {
		this.ordenId = ordenId;
		this.fechaEmision = fechaEmision;
		this.estado = estado;
		this.administradorId = administradorId;
		this.administradorNombre = administradorNombre;
		this.nitEmpresa = nitEmpresa;
		this.ingredientes = ingredientes != null ? ingredientes : new ArrayList<>();
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

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public Integer getAdministradorId() {
		return administradorId;
	}

	public void setAdministradorId(Integer administradorId) {
		this.administradorId = administradorId;
	}

	public String getAdministradorNombre() {
		return administradorNombre;
	}

	public void setAdministradorNombre(String administradorNombre) {
		this.administradorNombre = administradorNombre;
	}

	public Integer getNitEmpresa() {
		return nitEmpresa;
	}

	public void setNitEmpresa(Integer nitEmpresa) {
		this.nitEmpresa = nitEmpresa;
	}

	public List<IngredienteDTO> getIngredientes() {
		return ingredientes;
	}

	public void setIngredientes(List<IngredienteDTO> ingredientes) {
		this.ingredientes = ingredientes;
	}

	@Override
	public String toString() {
		return "OrdenCompraDTO [ordenId=" + ordenId + ", fechaEmision=" + fechaEmision + ", estado=" + estado
				+ ", administradorId=" + administradorId + ", administradorNombre=" + administradorNombre
				+ ", nitEmpresa=" + nitEmpresa + ", ingredientes=" + ingredientes + "]";
	}
}

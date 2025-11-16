package co.edu.unbosque.model;

import java.time.LocalDate;

public class IngredienteDTO {
	private Integer codigo;
	private String nombre;
	private Integer costoUnitario;
	private LocalDate fechaCaducidad;
	private String estado;

	public IngredienteDTO() {
	}

	public IngredienteDTO(String nombre, Integer costoUnitario, LocalDate fechaCaducidad, String estado) {
		super();
		this.nombre = nombre;
		this.costoUnitario = costoUnitario;
		this.fechaCaducidad = fechaCaducidad;
		this.estado = estado;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Integer getCostoUnitario() {
		return costoUnitario;
	}

	public void setCostoUnitario(Integer costoUnitario) {
		this.costoUnitario = costoUnitario;
	}

	public LocalDate getFechaCaducidad() {
		return fechaCaducidad;
	}

	public void setFechaCaducidad(LocalDate fechaCaducidad) {
		this.fechaCaducidad = fechaCaducidad;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	@Override
	public String toString() {
		return "IngredienteDTO [codigo=" + codigo + ", nombre=" + nombre + ", costoUnitario=" + costoUnitario
				+ ", fechaCaducidad=" + fechaCaducidad + ", estado=" + estado + "]";
	}

}

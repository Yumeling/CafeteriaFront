package co.edu.unbosque.model;

import java.util.List;

public class RecetaDTO {
	private Integer recetaId;
	private String nombre;
	private String descripcion;
	private Integer tiempoPreparacion;
	private String imagen;
	private List<IngredienteDTO> ingredientes;

	public RecetaDTO() {
	}

	public RecetaDTO(Integer recetaId, String nombre, String descripcion, Integer tiempoPreparacion, String imagen,
			List<IngredienteDTO> ingredientes) {
		super();
		this.recetaId = recetaId;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.tiempoPreparacion = tiempoPreparacion;
		this.imagen = imagen;
		this.ingredientes = ingredientes;
	}

	public Integer getRecetaId() {
		return recetaId;
	}

	public void setRecetaId(Integer recetaId) {
		this.recetaId = recetaId;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Integer getTiempoPreparacion() {
		return tiempoPreparacion;
	}

	public void setTiempoPreparacion(Integer tiempoPreparacion) {
		this.tiempoPreparacion = tiempoPreparacion;
	}

	public String getImagen() {
		return imagen;
	}

	public void setImagen(String imagen) {
		this.imagen = imagen;
	}

	public List<IngredienteDTO> getIngredientes() {
		return ingredientes;
	}

	public void setIngredientes(List<IngredienteDTO> ingredientes) {
		this.ingredientes = ingredientes;
	}

	@Override
	public String toString() {
		return "RecetaDTO [recetaId=" + recetaId + ", nombre=" + nombre + ", descripcion=" + descripcion
				+ ", tiempoPreparacion=" + tiempoPreparacion + ", imagen=" + imagen + ", ingredientes=" + ingredientes
				+ "]";
	}
}

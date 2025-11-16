package co.edu.unbosque.model;



public class PlatoDTO {
	private Integer platoId;
	private String nombre;
	private Integer precioVenta;
	private Integer margenGanancia;
	private Integer costo;
	private String imagen;

	public PlatoDTO() {
		super();
	}

	

	public PlatoDTO(String nombre, Integer precioVenta, Integer margenGanancia, Integer costo, String imagen) {
		super();
		this.nombre = nombre;
		this.precioVenta = precioVenta;
		this.margenGanancia = margenGanancia;
		this.costo = costo;
		this.imagen = imagen;
	}



	

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Integer getPrecioVenta() {
		return precioVenta;
	}

	public void setPrecioVenta(Integer precioVenta) {
		this.precioVenta = precioVenta;
	}

	public Integer getMargenGanancia() {
		return margenGanancia;
	}

	public void setMargenGanancia(Integer margenGanancia) {
		this.margenGanancia = margenGanancia;
	}

	public Integer getCosto() {
		return costo;
	}

	public void setCosto(Integer costo) {
		this.costo = costo;
	}

	public String getImagen() {
		return imagen;
	}

	public void setImagen(String imagen) {
		this.imagen = imagen;
	}

	@Override
	public String toString() {
		return "PlatoDTO [platoId=" + platoId + ", nombre=" + nombre + ", precioVenta=" + precioVenta
				+ ", margenGanancia=" + margenGanancia + ", costo=" + costo + ", imagen=" + imagen + "]";
	}

}

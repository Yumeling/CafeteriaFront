package co.edu.unbosque.model;

public class EmpresaDTO {
	private Integer nit;
	private String nombre;
	private String email;
	private Long telefono;
	private Integer direccionId;
	private String estado;

	public EmpresaDTO() {
		super();
	}

	public EmpresaDTO(Integer nit, String nombre, String email, Long telefono, Integer direccionId, String estado) {
		super();
		this.nit = nit;
		this.nombre = nombre;
		this.email = email;
		this.telefono = telefono;
		this.direccionId = direccionId;
		this.estado = estado;
	}

	public Long getTelefono() {
		return telefono;
	}

	public void setTelefono(Long telefono) {
		this.telefono = telefono;
	}

	public Integer getNit() {
		return nit;
	}

	public void setNit(Integer nit) {
		this.nit = nit;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Integer getDireccionId() {
		return direccionId;
	}

	public void setDireccionId(Integer direccionId) {
		this.direccionId = direccionId;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	@Override
	public String toString() {
		return "EmpresaDTO [nit=" + nit + ", nombre=" + nombre + ", email=" + email + ", telefono=" + telefono
				+ ", direccionId=" + direccionId + ", estado=" + estado + "]";
	}

}

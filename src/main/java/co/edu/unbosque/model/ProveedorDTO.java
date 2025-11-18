package co.edu.unbosque.model;

public class ProveedorDTO {
	private Integer proveedorId;
	private Integer nitEmpresa;
	private String primerNombre;
	private String segundoNombre;
	private String primerApellido;
	private String segundoApellido;
	private String email;
	private Long telefono;
	private String cargo;

	public ProveedorDTO() {
		super();
	}

	public ProveedorDTO(Integer proveedorId, Integer nitEmpresa, String primerNombre, String segundoNombre,
			String primerApellido, String segundoApellido, String email, Long telefono, String cargo) {
		super();
		this.proveedorId = proveedorId;
		this.nitEmpresa = nitEmpresa;
		this.primerNombre = primerNombre;
		this.segundoNombre = segundoNombre;
		this.primerApellido = primerApellido;
		this.segundoApellido = segundoApellido;
		this.email = email;
		this.telefono = telefono;
		this.cargo = cargo;
	}

	public Integer getProveedorId() {
		return proveedorId;
	}

	public void setProveedorId(Integer proveedorId) {
		this.proveedorId = proveedorId;
	}

	public Integer getNitEmpresa() {
		return nitEmpresa;
	}

	public void setNitEmpresa(Integer nitEmpresa) {
		this.nitEmpresa = nitEmpresa;
	}

	public String getPrimerNombre() {
		return primerNombre;
	}

	public void setPrimerNombre(String primerNombre) {
		this.primerNombre = primerNombre;
	}

	public String getSegundoNombre() {
		return segundoNombre;
	}

	public void setSegundoNombre(String segundoNombre) {
		this.segundoNombre = segundoNombre;
	}

	public String getPrimerApellido() {
		return primerApellido;
	}

	public void setPrimerApellido(String primerApellido) {
		this.primerApellido = primerApellido;
	}

	public String getSegundoApellido() {
		return segundoApellido;
	}

	public void setSegundoApellido(String segundoApellido) {
		this.segundoApellido = segundoApellido;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Long getTelefono() {
		return telefono;
	}

	public void setTelefono(Long telefono) {
		this.telefono = telefono;
	}

	public String getCargo() {
		return cargo;
	}

	public void setCargo(String cargo) {
		this.cargo = cargo;
	}

	@Override
	public String toString() {
		return "ProveedorDTO [proveedorId=" + proveedorId + ", nitEmpresa=" + nitEmpresa + ", primerNombre="
				+ primerNombre + ", segundoNombre=" + segundoNombre + ", primerApellido=" + primerApellido
				+ ", segundoApellido=" + segundoApellido + ", email=" + email + ", telefono=" + telefono + ", cargo="
				+ cargo + "]";
	}

}

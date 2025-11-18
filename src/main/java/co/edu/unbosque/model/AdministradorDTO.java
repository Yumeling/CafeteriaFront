package co.edu.unbosque.model;

public class AdministradorDTO {
	private Integer administradorId;
	private String nombreAdministrador;
	private String clave;

	public AdministradorDTO() {
		super();
	}

	public AdministradorDTO(Integer administradorId, String nombreAdministrador, String clave) {
		super();
		this.administradorId = administradorId;
		this.nombreAdministrador = nombreAdministrador;
		this.clave = clave;
	}

	public Integer getAdministradorId() {
		return administradorId;
	}

	public void setAdministradorId(Integer administradorId) {
		this.administradorId = administradorId;
	}

	public String getNombreAdministrador() {
		return nombreAdministrador;
	}

	public void setNombreAdministrador(String nombreAdministrador) {
		this.nombreAdministrador = nombreAdministrador;
	}

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	@Override
	public String toString() {
		return "AdministradorDTO [administradorId=" + administradorId + ", nombreAdministrador=" + nombreAdministrador
				+ ", clave=" + clave + "]";
	}

}

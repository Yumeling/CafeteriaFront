package co.edu.unbosque.model;

public class AdministradorDTO {
    private Integer administradorId;
    private String nombreAdministrador;
    private String clave;
	public AdministradorDTO() {
		super();
	}
	
	public AdministradorDTO(String nombreAdministrador, String clave) {
		super();
		this.nombreAdministrador = nombreAdministrador;
		this.clave = clave;
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

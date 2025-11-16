package co.edu.unbosque.model;

public class DireccionEmpresaDTO {
	private Integer direccionId;
	private String direccionText;
	private String ciudad;
	private String departamento;
	private String pais;
	private Integer empresaNit;

	public DireccionEmpresaDTO() {
		super();
	}

	public DireccionEmpresaDTO(Integer direccionId, String direccionText, String ciudad, String departamento,
			String pais, Integer empresaNit) {
		super();
		this.direccionId = direccionId;
		this.direccionText = direccionText;
		this.ciudad = ciudad;
		this.departamento = departamento;
		this.pais = pais;
		this.empresaNit = empresaNit;
	}

	public Integer getEmpresaNit() {
		return empresaNit;
	}

	public void setEmpresaNit(Integer empresaNit) {
		this.empresaNit = empresaNit;
	}

	public Integer getDireccionId() {
		return direccionId;
	}

	public void setDireccionId(Integer direccionId) {
		this.direccionId = direccionId;
	}

	public String getDireccionText() {
		return direccionText;
	}

	public void setDireccionText(String direccionText) {
		this.direccionText = direccionText;
	}

	public String getCiudad() {
		return ciudad;
	}

	public void setCiudad(String ciudad) {
		this.ciudad = ciudad;
	}

	public String getDepartamento() {
		return departamento;
	}

	public void setDepartamento(String departamento) {
		this.departamento = departamento;
	}

	public String getPais() {
		return pais;
	}

	public void setPais(String pais) {
		this.pais = pais;
	}

	@Override
	public String toString() {
		return "DireccionEmpresaDTO [direccionId=" + direccionId + ", direccionText=" + direccionText + ", ciudad="
				+ ciudad + ", departamento=" + departamento + ", pais=" + pais + "]";
	}

}
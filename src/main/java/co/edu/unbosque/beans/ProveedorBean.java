package co.edu.unbosque.beans;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import co.edu.unbosque.model.ProveedorDTO;
import co.edu.unbosque.model.EmpresaDTO;
import co.edu.unbosque.util.LocalDateAdapter;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named("proveedorBean")
@SessionScoped
public class ProveedorBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private List<ProveedorDTO> proveedores = new ArrayList<>();
	private ProveedorDTO nuevoProveedor = new ProveedorDTO();
	private ProveedorDTO proveedorSeleccionado = new ProveedorDTO();
	private List<EmpresaDTO> empresas = new ArrayList<>();
	private Integer selectedEmpresaNit;
	private final String BASE_URL = "http://localhost:8083/api/proveedores";
	private final String EMP_URL = "http://localhost:8083/api/empresas";
	private final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2)
			.connectTimeout(Duration.ofSeconds(10)).build();
	private final Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).create();

	@PostConstruct
	public void init() {
		cargarEmpresas();
		cargarProveedores();
	}

	public void cargarProveedores() {
		try {
			HttpRequest req = HttpRequest.newBuilder().GET().uri(URI.create(BASE_URL))
					.header("Content-Type", "application/json").build();

			HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
			System.out.println("[ProveedorBean] GET " + BASE_URL + " -> " + resp.statusCode());
			String json = resp.body() == null ? "[]" : resp.body().trim();

			if (json.startsWith("[")) {
				Type t = new TypeToken<List<ProveedorDTO>>() {
				}.getType();
				List<ProveedorDTO> list = gson.fromJson(json, t);
				proveedores = list != null ? list : new ArrayList<>();
			} else if (json.startsWith("{")) {
				JsonElement je = JsonParser.parseString(json);
				if (je.getAsJsonObject().has("data")) {
					String inner = je.getAsJsonObject().get("data").toString();
					Type t = new TypeToken<List<ProveedorDTO>>() {
					}.getType();
					proveedores = gson.fromJson(inner, t);
				} else {
					ProveedorDTO single = gson.fromJson(json, ProveedorDTO.class);
					proveedores = new ArrayList<>();
					proveedores.add(single);
				}
			} else {
				proveedores = new ArrayList<>();
			}
		} catch (Exception e) {
			e.printStackTrace();
			proveedores = new ArrayList<>();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los proveedores."));
		}
	}

	public void cargarEmpresas() {
		try {
			HttpRequest req = HttpRequest.newBuilder().GET().uri(URI.create(EMP_URL))
					.header("Content-Type", "application/json").build();

			HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
			System.out.println("[ProveedorBean] GET " + EMP_URL + " -> " + resp.statusCode());
			String json = resp.body() == null ? "[]" : resp.body().trim();

			if (json.startsWith("[")) {
				Type t = new TypeToken<List<EmpresaDTO>>() {
				}.getType();
				List<EmpresaDTO> list = gson.fromJson(json, t);
				empresas = list != null ? list : new ArrayList<>();
			} else if (json.startsWith("{")) {
				JsonElement je = JsonParser.parseString(json);
				if (je.getAsJsonObject().has("data")) {
					String inner = je.getAsJsonObject().get("data").toString();
					Type t = new TypeToken<List<EmpresaDTO>>() {
					}.getType();
					empresas = gson.fromJson(inner, t);
				} else {
					EmpresaDTO single = gson.fromJson(json, EmpresaDTO.class);
					empresas = new ArrayList<>();
					empresas.add(single);
				}
			} else {
				empresas = new ArrayList<>();
			}
		} catch (Exception e) {
			e.printStackTrace();
			empresas = new ArrayList<>();
		}
	}

	public void prepararNuevo() {
		nuevoProveedor = new ProveedorDTO();
		selectedEmpresaNit = null;
	}

	public void crearProveedor() {
		try {
			if (nuevoProveedor.getPrimerNombre() == null || nuevoProveedor.getPrimerNombre().isBlank()
					|| nuevoProveedor.getPrimerApellido() == null || nuevoProveedor.getPrimerApellido().isBlank()) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Error", "Primer nombre y primer apellido son obligatorios."));
				return;
			}
			if (selectedEmpresaNit != null) {
				nuevoProveedor.setNitEmpresa(selectedEmpresaNit);
			}

			String json = gson.toJson(nuevoProveedor);
			System.out.println("[ProveedorBean] POST payload: " + json);

			HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE_URL))
					.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build();

			HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
			System.out.println("[ProveedorBean] POST " + BASE_URL + " -> " + resp.statusCode());
			System.out.println("[ProveedorBean] POST response body: " + (resp.body() == null ? "" : resp.body()));

			if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Proveedor creado."));
			} else {
				String detalle = resp.body() == null ? "Código: " + resp.statusCode() : resp.body();
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error creando proveedor", detalle));
			}

			prepararNuevo();
			cargarProveedores();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", e.getMessage()));
		}
	}

	public void prepararEditar(ProveedorDTO p) {
		if (p == null)
			return;
		this.proveedorSeleccionado = p;
		this.selectedEmpresaNit = p.getNitEmpresa();
	}

	public void actualizarProveedor() {
		try {
			if (proveedorSeleccionado == null || proveedorSeleccionado.getProveedorId() == null) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Seleccione un proveedor para editar."));
				return;
			}

			if (proveedorSeleccionado.getPrimerNombre() == null || proveedorSeleccionado.getPrimerNombre().isBlank()
					|| proveedorSeleccionado.getPrimerApellido() == null
					|| proveedorSeleccionado.getPrimerApellido().isBlank()) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Error", "Primer nombre y primer apellido son obligatorios."));
				return;
			}

			if (selectedEmpresaNit != null)
				proveedorSeleccionado.setNitEmpresa(selectedEmpresaNit);

			String json = gson.toJson(proveedorSeleccionado);
			System.out.println("[ProveedorBean] PUT payload: " + json);

			HttpRequest req = HttpRequest.newBuilder()
					.uri(URI.create(BASE_URL + "/" + proveedorSeleccionado.getProveedorId()))
					.header("Content-Type", "application/json").PUT(HttpRequest.BodyPublishers.ofString(json)).build();

			HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
			System.out.println("[ProveedorBean] PUT " + BASE_URL + "/" + proveedorSeleccionado.getProveedorId() + " -> "
					+ resp.statusCode());
			System.out.println("[ProveedorBean] PUT response body: " + (resp.body() == null ? "" : resp.body()));

			if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Proveedor actualizado."));
			} else {
				String detalle = resp.body() == null ? "Código: " + resp.statusCode() : resp.body();
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error actualizando proveedor", detalle));
			}

			cargarProveedores();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", e.getMessage()));
		}
	}

	public List<ProveedorDTO> getProveedores() {
		return proveedores;
	}

	public void setProveedores(List<ProveedorDTO> proveedores) {
		this.proveedores = proveedores;
	}

	public ProveedorDTO getNuevoProveedor() {
		return nuevoProveedor;
	}

	public void setNuevoProveedor(ProveedorDTO nuevoProveedor) {
		this.nuevoProveedor = nuevoProveedor;
	}

	public ProveedorDTO getProveedorSeleccionado() {
		return proveedorSeleccionado;
	}

	public void setProveedorSeleccionado(ProveedorDTO proveedorSeleccionado) {
		this.proveedorSeleccionado = proveedorSeleccionado;
	}

	public List<EmpresaDTO> getEmpresas() {
		return empresas;
	}

	public void setEmpresas(List<EmpresaDTO> empresas) {
		this.empresas = empresas;
	}

	public Integer getSelectedEmpresaNit() {
		return selectedEmpresaNit;
	}

	public void setSelectedEmpresaNit(Integer selectedEmpresaNit) {
		this.selectedEmpresaNit = selectedEmpresaNit;
	}

	public String getBASE_URL() {
		return BASE_URL;
	}

	public String getEMP_URL() {
		return EMP_URL;
	}

	public Gson getGson() {
		return gson;
	}
}

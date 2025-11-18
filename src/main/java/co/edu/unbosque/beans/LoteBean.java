package co.edu.unbosque.beans;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import co.edu.unbosque.model.LoteDTO;
import co.edu.unbosque.model.OrdenCompraDTO;
import co.edu.unbosque.model.IngredienteDTO;
import co.edu.unbosque.model.ProveedorDTO;
import co.edu.unbosque.model.persistence.ExternalHTTPRequestHandler;
import co.edu.unbosque.util.LocalDateAdapter;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named("loteBean")
@SessionScoped
public class LoteBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private List<LoteDTO> lotes = new ArrayList<>();
	private LoteDTO nuevoLote = new LoteDTO();
	private List<OrdenCompraDTO> ordenes = new ArrayList<>();
	private List<IngredienteDTO> ingredientes = new ArrayList<>();
	private List<ProveedorDTO> proveedores = new ArrayList<>();
	private ProveedorView proveedorView = new ProveedorView();
	private final String BASE_URL = "http://localhost:8083/api/lotes";
	private final String ORDEN_URL = "http://localhost:8083/api/ordenes";
	private final String ING_URL = "http://localhost:8083/api/ingredientes";
	private final String PROV_URL = "http://localhost:8083/api/proveedores";
	private final Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).create();

	@PostConstruct
	public void init() {
		prepararNuevoLote();
		cargarRecursos();
		cargarLotes();
	}

	public void cargarRecursos() {
		cargarOrdenes();
		cargarIngredientes();
		cargarProveedores();
	}

	public void cargarOrdenes() {
		try {
			String body = ExternalHTTPRequestHandler.doGet(ORDEN_URL);
			List<OrdenCompraDTO> list = parseListFromBody(body, OrdenCompraDTO.class);
			ordenes = list != null ? list : new ArrayList<>();
		} catch (Exception e) {
			e.printStackTrace();
			ordenes = new ArrayList<>();
		}
	}

	public void cargarIngredientes() {
		try {
			String body = ExternalHTTPRequestHandler.doGet(ING_URL);
			List<IngredienteDTO> list = parseListFromBody(body, IngredienteDTO.class);
			ingredientes = list != null ? list : new ArrayList<>();
		} catch (Exception e) {
			e.printStackTrace();
			ingredientes = new ArrayList<>();
		}
	}

	public void cargarProveedores() {
		try {
			String body = ExternalHTTPRequestHandler.doGet(PROV_URL);
			List<ProveedorDTO> list = parseListFromBody(body, ProveedorDTO.class);
			proveedores = list != null ? list : new ArrayList<>();
		} catch (Exception e) {
			e.printStackTrace();
			proveedores = new ArrayList<>();
		}
	}

	public void cargarLotes() {
		try {
			String body = ExternalHTTPRequestHandler.doGet(BASE_URL);
			List<LoteDTO> list = parseListFromBody(body, LoteDTO.class);
			lotes = list != null ? list : new ArrayList<>();
		} catch (Exception e) {
			e.printStackTrace();
			lotes = new ArrayList<>();
		}
	}

	private <T> List<T> parseListFromBody(String body, Class<T> clazz) {
		try {
			if (body == null)
				return new ArrayList<>();
			String trimmed = body.trim();
			if (trimmed.isEmpty())
				return new ArrayList<>();

			if (trimmed.startsWith("[")) {
				java.lang.reflect.Type tt = com.google.gson.reflect.TypeToken.getParameterized(List.class, clazz)
						.getType();
				List<T> parsed = gson.fromJson(trimmed, tt);
				return parsed != null ? parsed : new ArrayList<>();
			}

			if (trimmed.startsWith("{")) {
				JsonElement je = JsonParser.parseString(trimmed);
				if (je.isJsonObject()) {
					JsonObject jo = je.getAsJsonObject();
					if (jo.has("data")) {
						JsonElement data = jo.get("data");
						if (data.isJsonArray()) {
							java.lang.reflect.Type tt = com.google.gson.reflect.TypeToken
									.getParameterized(List.class, clazz).getType();
							List<T> parsed = gson.fromJson(data.toString(), tt);
							return parsed != null ? parsed : new ArrayList<>();
						} else {
							String wrapped = "[" + data.toString() + "]";
							java.lang.reflect.Type tt = com.google.gson.reflect.TypeToken
									.getParameterized(List.class, clazz).getType();
							List<T> parsed = gson.fromJson(wrapped, tt);
							return parsed != null ? parsed : new ArrayList<>();
						}
					} else {
						String wrapped = "[" + trimmed + "]";
						java.lang.reflect.Type tt = com.google.gson.reflect.TypeToken
								.getParameterized(List.class, clazz).getType();
						List<T> parsed = gson.fromJson(wrapped, tt);
						return parsed != null ? parsed : new ArrayList<>();
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new ArrayList<>();
	}

	public void prepararNuevoLote() {
		nuevoLote = new LoteDTO();
		nuevoLote.setFechaRecepcion(LocalDate.now());
	}

	public void crearLote() {
		try {
			if (nuevoLote.getFechaRecepcion() == null) {
				nuevoLote.setFechaRecepcion(LocalDate.now());
			}

			if (nuevoLote.getCantidad() == null || nuevoLote.getCantidad() <= 0) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "La cantidad debe ser mayor que 0."));
				return;
			}
			if (nuevoLote.getPrecio() == null || nuevoLote.getPrecio() < 0) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Error", "El precio del lote es requerido y no puede ser negativo."));
				return;
			}
			if (nuevoLote.getFechaCaducidad() != null && nuevoLote.getFechaCaducidad().isBefore(LocalDate.now())) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Error", "La fecha de caducidad no puede ser anterior al día de hoy."));
				return;
			}
			if (nuevoLote.getCodigoIngrediente() == null) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Debe seleccionar un ingrediente."));
				return;
			}

			JsonObject j = new JsonObject();
			j.addProperty("fechaRecepcion", nuevoLote.getFechaRecepcion().toString());
			if (nuevoLote.getFechaCaducidad() != null)
				j.addProperty("fechaCaducidad", nuevoLote.getFechaCaducidad().toString());
			j.addProperty("cantidad", nuevoLote.getCantidad());
			j.addProperty("precio", nuevoLote.getPrecio());
			if (nuevoLote.getOrdenId() != null)
				j.addProperty("ordenId", nuevoLote.getOrdenId());
			j.addProperty("codigoIngrediente", nuevoLote.getCodigoIngrediente());
			if (nuevoLote.getProveedorId() != null)
				j.addProperty("proveedorId", nuevoLote.getProveedorId());

			ExternalHTTPRequestHandler.doPost(BASE_URL, gson.toJson(j));

			try {
				if (nuevoLote.getCantidad() != null && nuevoLote.getCantidad() > 0 && nuevoLote.getPrecio() != null) {
					int costoUnitario = nuevoLote.getPrecio() / nuevoLote.getCantidad();
					JsonObject ingUpd = new JsonObject();
					ingUpd.addProperty("costoUnitario", costoUnitario);
					ExternalHTTPRequestHandler.doUpdate(ING_URL + "/" + nuevoLote.getCodigoIngrediente(),
							gson.toJson(ingUpd));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			prepararNuevoLote();
			cargarLotes();
			cargarIngredientes();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Lote creado"));
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo crear el lote"));
		}
	}

	public void eliminarLote(Integer id) {
		try {
			if (id == null)
				return;
			ExternalHTTPRequestHandler.doDelete(BASE_URL + "/" + id);
			cargarLotes();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Lote eliminado"));
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar el lote"));
		}
	}

	public void prepararMostrarProveedor(Integer proveedorId) {
		proveedorView = new ProveedorView();
		if (proveedorId == null || proveedores == null)
			return;
		for (ProveedorDTO p : proveedores) {
			if (p != null && p.getProveedorId() != null && p.getProveedorId().equals(proveedorId)) {
				StringBuilder nombre = new StringBuilder();
				if (p.getPrimerNombre() != null)
					nombre.append(p.getPrimerNombre());
				if (p.getSegundoNombre() != null && !p.getSegundoNombre().isBlank()) {
					if (nombre.length() > 0)
						nombre.append(" ");
					nombre.append(p.getSegundoNombre());
				}
				if (p.getPrimerApellido() != null) {
					if (nombre.length() > 0)
						nombre.append(" ");
					nombre.append(p.getPrimerApellido());
				}
				if (p.getSegundoApellido() != null && !p.getSegundoApellido().isBlank()) {
					if (nombre.length() > 0)
						nombre.append(" ");
					nombre.append(p.getSegundoApellido());
				}
				String fullName = nombre.length() > 0 ? nombre.toString() : "(sin nombre)";

				proveedorView.setProveedorId(String.valueOf(p.getProveedorId()));
				proveedorView.setNombre(fullName);
				proveedorView.setNit(p.getNitEmpresa() != null ? String.valueOf(p.getNitEmpresa()) : "—");
				proveedorView.setTelefono(p.getTelefono() != null ? String.valueOf(p.getTelefono()) : "—");
				proveedorView.setEmail(p.getEmail() != null ? p.getEmail() : "—");
				proveedorView.setDireccion("—");
				break;
			}
		}
	}

	public String getIngredienteNombre(Integer codigo) {
		if (codigo == null || ingredientes == null)
			return "—";
		for (IngredienteDTO it : ingredientes) {
			if (it != null && it.getCodigo() != null && it.getCodigo().equals(codigo)) {
				return it.getNombre() != null ? it.getNombre() : "—";
			}
		}
		return "—";
	}

	public List<LoteDTO> getLotes() {
		return lotes;
	}

	public void setLotes(List<LoteDTO> lotes) {
		this.lotes = lotes;
	}

	public LoteDTO getNuevoLote() {
		return nuevoLote;
	}

	public void setNuevoLote(LoteDTO nuevoLote) {
		this.nuevoLote = nuevoLote;
	}

	public List<OrdenCompraDTO> getOrdenes() {
		return ordenes;
	}

	public void setOrdenes(List<OrdenCompraDTO> ordenes) {
		this.ordenes = ordenes;
	}

	public List<IngredienteDTO> getIngredientes() {
		return ingredientes;
	}

	public void setIngredientes(List<IngredienteDTO> ingredientes) {
		this.ingredientes = ingredientes;
	}

	public List<ProveedorDTO> getProveedores() {
		return proveedores;
	}

	public void setProveedores(List<ProveedorDTO> proveedores) {
		this.proveedores = proveedores;
	}

	public ProveedorView getProveedorView() {
		return proveedorView;
	}

	public void setProveedorView(ProveedorView proveedorView) {
		this.proveedorView = proveedorView;
	}

	public String getBASE_URL() {
		return BASE_URL;
	}

	public String getORDEN_URL() {
		return ORDEN_URL;
	}

	public String getING_URL() {
		return ING_URL;
	}

	public String getPROV_URL() {
		return PROV_URL;
	}

	public Gson getGson() {
		return gson;
	}

	public static class ProveedorView {
		private String proveedorId;
		private String nombre;
		private String nit;
		private String telefono;
		private String email;
		private String direccion;

		public String getProveedorId() {
			return proveedorId;
		}

		public void setProveedorId(String proveedorId) {
			this.proveedorId = proveedorId;
		}

		public String getNombre() {
			return nombre;
		}

		public void setNombre(String nombre) {
			this.nombre = nombre;
		}

		public String getNit() {
			return nit;
		}

		public void setNit(String nit) {
			this.nit = nit;
		}

		public String getTelefono() {
			return telefono;
		}

		public void setTelefono(String telefono) {
			this.telefono = telefono;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getDireccion() {
			return direccion;
		}

		public void setDireccion(String direccion) {
			this.direccion = direccion;
		}
	}
}

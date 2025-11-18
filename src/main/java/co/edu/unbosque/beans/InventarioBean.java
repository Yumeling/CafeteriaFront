package co.edu.unbosque.beans;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.http.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import co.edu.unbosque.model.InventarioDTO;
import co.edu.unbosque.model.IngredienteDTO;
import co.edu.unbosque.model.AdministradorDTO;
import co.edu.unbosque.model.LoteDTO;
import co.edu.unbosque.util.LocalDateAdapter;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Named;

@Named("inventarioBean")
@SessionScoped
public class InventarioBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private List<InventarioDTO> inventarios = new ArrayList<>();
	private List<AdministradorDTO> administradores = new ArrayList<>();
	private List<IngredienteDTO> ingredientes = new ArrayList<>();
	private List<LoteDTO> lotes = new ArrayList<>();
	private List<SelectItem> administradoresSelectItems = new ArrayList<>();
	private List<SelectItem> ingredientesSelectItems = new ArrayList<>();
	private final String BASE_URL = "http://localhost:8083/api/inventarios";
	private final String ADMIN_URL = "http://localhost:8083/api/administradores";
	private final String ING_URL = "http://localhost:8083/api/ingredientes";
	private final String LOTES_URL = "http://localhost:8083/api/lotes";
	private final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
	private final Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).create();

	@PostConstruct
	public void init() {
		cargarRecursos();
		cargarInventarios();
	}

	public void cargarRecursos() {
		cargarAdministradores();
		cargarIngredientes();
		cargarLotes();
	}

	public void cargarAdministradores() {
		try {
			HttpRequest req = HttpRequest.newBuilder().GET().uri(URI.create(ADMIN_URL))
					.header("Content-Type", "application/json").build();
			HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
			String body = resp.body() == null ? "[]" : resp.body().trim();
			administradores = parseListFromBody(body, AdministradorDTO.class);
			buildAdministradoresSelectItems();
		} catch (Exception e) {
			e.printStackTrace();
			administradores = new ArrayList<>();
			administradoresSelectItems = new ArrayList<>();
		}
	}

	private void buildAdministradoresSelectItems() {
		administradoresSelectItems = new ArrayList<>();
		administradoresSelectItems.add(new SelectItem(null, "-- Ninguno --"));
		if (administradores == null)
			return;
		for (AdministradorDTO a : administradores) {
			Integer id = a == null ? null : a.getAdministradorId();
			String label = a == null ? "—"
					: (a.getNombreAdministrador() == null ? ("id:" + id) : a.getNombreAdministrador());
			administradoresSelectItems.add(new SelectItem(id, label));
		}
	}

	public void cargarIngredientes() {
		try {
			HttpRequest req = HttpRequest.newBuilder().GET().uri(URI.create(ING_URL))
					.header("Content-Type", "application/json").build();
			HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
			String body = resp.body() == null ? "[]" : resp.body().trim();
			ingredientes = parseListFromBody(body, IngredienteDTO.class);
			buildIngredientesSelectItems();
		} catch (Exception e) {
			e.printStackTrace();
			ingredientes = new ArrayList<>();
			ingredientesSelectItems = new ArrayList<>();
		}
	}

	private void buildIngredientesSelectItems() {
		ingredientesSelectItems = new ArrayList<>();
		ingredientesSelectItems.add(new SelectItem(null, "-- Seleccione --"));
		if (ingredientes == null)
			return;
		for (IngredienteDTO i : ingredientes) {
			Integer codigo = i == null ? null : i.getCodigo();
			String label = i == null ? "—" : (i.getNombre() == null ? ("cod:" + codigo) : i.getNombre());
			ingredientesSelectItems.add(new SelectItem(codigo, label));
		}
	}

	public void cargarLotes() {
		try {
			HttpRequest req = HttpRequest.newBuilder().GET().uri(URI.create(LOTES_URL))
					.header("Content-Type", "application/json").build();
			HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
			String body = resp.body() == null ? "[]" : resp.body().trim();
			lotes = parseListFromBody(body, LoteDTO.class);
		} catch (Exception e) {
			e.printStackTrace();
			lotes = new ArrayList<>();
		}
	}

	public void cargarInventarios() {
		try {
			cargarLotes();
			cargarAdministradores();
			cargarIngredientes();

			HttpRequest req = HttpRequest.newBuilder().GET().uri(URI.create(BASE_URL))
					.header("Content-Type", "application/json").build();
			HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
			String json = resp.body() == null ? "[]" : resp.body().trim();

			List<InventarioDTO> list = parseListFromBody(json, InventarioDTO.class);
			if (list == null)
				list = new ArrayList<>();

			for (InventarioDTO inv : list) {
				inv.setCantidad(calcularCantidadTotalIngrediente(inv.getIngredienteId()));
			}
			inventarios = list;
		} catch (Exception e) {
			e.printStackTrace();
			inventarios = new ArrayList<>();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudieron cargar los inventarios."));
		}
	}

	public void reconstruirInventariosDesdeIngredientes() {
		try {
			cargarLotes();
			cargarAdministradores();
			cargarIngredientes();
			cargarInventarios();

			int created = 0;
			int skipped = 0;
			for (IngredienteDTO ing : ingredientes) {
				if (ing == null || ing.getCodigo() == null)
					continue;
				Integer codigo = ing.getCodigo();

				Integer cantidad = calcularCantidadTotalIngrediente(codigo);

				if (cantidad == null || cantidad <= 0) {
					skipped++;
					continue;
				}

				boolean existe = false;
				for (InventarioDTO inv : inventarios) {
					if (inv != null && inv.getIngredienteId() != null && inv.getIngredienteId().equals(codigo)) {
						existe = true;
						break;
					}
				}
				if (existe) {
					skipped++;
					continue;
				}

				JsonObject j = new JsonObject();
				j.add("administradorId", JsonNull.INSTANCE);
				j.addProperty("ingredienteId", codigo);
				j.add("stockMinimo", JsonNull.INSTANCE);

				HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE_URL))
						.header("Content-Type", "application/json")
						.POST(HttpRequest.BodyPublishers.ofString(gson.toJson(j))).build();

				HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
				if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
					created++;
				} else {

					FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso",
									"No se pudo crear inventario para ingrediente " + codigo + " (code: "
											+ resp.statusCode() + ")"));
				}
			}

			cargarInventarios();

			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Proceso terminado", "Inventarios creados: " + created + " | omisiones/skipped: " + skipped));
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
					"Fallo durante reconstrucción: " + e.getMessage()));
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", e.getMessage()));
		}
	}

	private Integer calcularCantidadTotalIngrediente(Integer ingredienteId) {
		if (ingredienteId == null || lotes == null)
			return 0;
		int sum = 0;
		LocalDate hoy = LocalDate.now();
		for (LoteDTO lote : lotes) {
			if (lote == null)
				continue;
			try {
				Integer codigo = lote.getCodigoIngrediente();
				if (codigo != null && codigo.equals(ingredienteId)) {
					LocalDate cad = lote.getFechaCaducidad();
					if (cad != null && cad.isBefore(hoy))
						continue;
					Integer c = lote.getCantidad() != null ? lote.getCantidad() : 0;
					sum += c;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return sum;
	}

	public String getAdministradorNombreById(Integer id) {
		if (id == null || administradores == null)
			return "—";
		for (AdministradorDTO a : administradores) {
			if (a != null && a.getAdministradorId() != null && a.getAdministradorId().equals(id)) {
				return a.getNombreAdministrador() != null ? a.getNombreAdministrador() : "—";
			}
		}
		return "—";
	}

	public String getIngredienteNombreById(Integer id) {
		if (id == null || ingredientes == null)
			return "—";
		for (IngredienteDTO i : ingredientes) {
			if (i != null && i.getCodigo() != null && i.getCodigo().equals(id)) {
				return i.getNombre() != null ? i.getNombre() : "—";
			}
		}
		return "—";
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

	public List<InventarioDTO> getInventarios() {
		return inventarios;
	}

	public void setInventarios(List<InventarioDTO> inventarios) {
		this.inventarios = inventarios;
	}

	public List<AdministradorDTO> getAdministradores() {
		return administradores;
	}

	public List<IngredienteDTO> getIngredientes() {
		return ingredientes;
	}

	public List<LoteDTO> getLotes() {
		return lotes;
	}

	public List<SelectItem> getAdministradoresSelectItems() {
		return administradoresSelectItems;
	}

	public List<SelectItem> getIngredientesSelectItems() {
		return ingredientesSelectItems;
	}

	public void reconstruirInventarios() {
		reconstruirInventariosDesdeIngredientes();
	}
}

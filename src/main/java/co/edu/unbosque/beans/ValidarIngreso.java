package co.edu.unbosque.beans;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import co.edu.unbosque.model.AdministradorDTO;
import co.edu.unbosque.model.persistence.ExternalHTTPRequestHandler;
import co.edu.unbosque.util.LocalDateAdapter;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named("validarIngreso")
@SessionScoped
public class ValidarIngreso implements Serializable {
	private static final long serialVersionUID = 1L;
	private String username;
	private String password;
	private AdministradorDTO administradorLogeado;
	private final String BASE_URL = "http://localhost:8083/api/administradores";
	private final Gson gson = new GsonBuilder().registerTypeAdapter(java.time.LocalDate.class, new LocalDateAdapter())
			.create();

	public String login() {
		try {
			String body = ExternalHTTPRequestHandler.doGet(BASE_URL);

			if (body == null)
				body = "";

			List<AdministradorDTO> admins = new ArrayList<>();

			try {
				Type t = new TypeToken<List<AdministradorDTO>>() {
				}.getType();
				admins = gson.fromJson(body, t);
			} catch (Exception ex) {
				try {
					AdministradorDTO single = gson.fromJson(body, AdministradorDTO.class);
					if (single != null) {
						admins = new ArrayList<>();
						admins.add(single);
					}
				} catch (Exception ex2) {
				}
			}

			if (admins == null)
				admins = new ArrayList<>();

			String userTrim = username == null ? "" : username.trim();
			String pass = password == null ? "" : password;

			for (AdministradorDTO a : admins) {
				if (a == null)
					continue;
				String nombre = a.getNombreAdministrador() == null ? "" : a.getNombreAdministrador().trim();
				String clave = a.getClave() == null ? "" : a.getClave();

				if (nombre.equals(userTrim) && clave.equals(pass)) {
					this.administradorLogeado = a;
					FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("admin", a);
					FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage(FacesMessage.SEVERITY_INFO, "Bienvenido " + nombre, null));
					return "/principal.xhtml?faces-redirect=true";
				}
			}

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Usuario o contraseña incorrectos", null));
			return null;

		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error al autenticarse: " + e.getMessage(), null));
			return null;
		}
	}

	public String logout() {
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		this.administradorLogeado = null;
		return "/login.xhtml?faces-redirect=true";
	}

	public boolean isLoggedIn() {
		if (administradorLogeado != null)
			return true;
		Object s = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("admin");
		return s != null;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public AdministradorDTO getAdministradorLogeado() {
		return administradorLogeado;
	}

	public void setAdministradorLogeado(AdministradorDTO administradorLogeado) {
		this.administradorLogeado = administradorLogeado;
	}
}

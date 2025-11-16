package co.edu.unbosque.beans;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import co.edu.unbosque.model.ProveedorDTO;
import co.edu.unbosque.model.EmpresaDTO;
import co.edu.unbosque.model.persistence.ExternalHTTPRequestHandler;
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

    private List<EmpresaDTO> empresas = new ArrayList<>();

    private final String BASE_URL = "http://localhost:8083/api/proveedores";
    private final String EMP_URL = "http://localhost:8083/api/empresas";

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(java.time.LocalDate.class, new LocalDateAdapter())
            .create();

    @PostConstruct
    public void init() {
        cargarEmpresas();
        cargarProveedores();
    }

    public void cargarProveedores() {
        try {
            String body = ExternalHTTPRequestHandler.doGet(BASE_URL + "/all");
            Type t = new TypeToken<List<ProveedorDTO>>() {}.getType();
            proveedores = gson.fromJson(body, t);
            if (proveedores == null) proveedores = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            proveedores = new ArrayList<>();
        }
    }

    public void cargarEmpresas() {
        try {
            String body = ExternalHTTPRequestHandler.doGet(EMP_URL + "/all");
            Type t = new TypeToken<List<EmpresaDTO>>() {}.getType();
            empresas = gson.fromJson(body, t);
            if (empresas == null) empresas = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            empresas = new ArrayList<>();
        }
    }

    public void crearProveedor() {
        try {
            JsonObject j = new JsonObject();
            j.addProperty("primerNombre", nuevoProveedor.getPrimerNombre());
            j.addProperty("segundoNombre", nuevoProveedor.getSegundoNombre());
            j.addProperty("primerApellido", nuevoProveedor.getPrimerApellido());
            j.addProperty("segundoApellido", nuevoProveedor.getSegundoApellido());
            j.addProperty("email", nuevoProveedor.getEmail());
            if (nuevoProveedor.getTelefono() != null) j.addProperty("telefono", nuevoProveedor.getTelefono());
            j.addProperty("cargo", nuevoProveedor.getCargo());
            if (nuevoProveedor.getNitEmpresa() != null) j.addProperty("nitEmpresa", nuevoProveedor.getNitEmpresa());

            ExternalHTTPRequestHandler.doPost(BASE_URL + "/create", gson.toJson(j));
            nuevoProveedor = new ProveedorDTO();
            cargarProveedores();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Proveedor creado"));
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo crear el proveedor"));
        }
    }

    public void eliminarProveedor(Integer id) {
        try {
            ExternalHTTPRequestHandler.doDelete(BASE_URL + "/delete/" + id);
            cargarProveedores();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Proveedor eliminado"));
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar el proveedor"));
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

	public List<EmpresaDTO> getEmpresas() {
		return empresas;
	}

	public void setEmpresas(List<EmpresaDTO> empresas) {
		this.empresas = empresas;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
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

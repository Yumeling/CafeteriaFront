package co.edu.unbosque.model.persistence;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import co.edu.unbosque.model.AdministradorDTO;
import co.edu.unbosque.model.DireccionEmpresaDTO;
import co.edu.unbosque.model.EmpresaDTO;
import co.edu.unbosque.model.IngredienteDTO;
import co.edu.unbosque.model.InventarioDTO;
import co.edu.unbosque.model.LoteDTO;
import co.edu.unbosque.model.OrdenCompraDTO;
import co.edu.unbosque.model.PlatoDTO;
import co.edu.unbosque.model.ProveedorDTO;
import co.edu.unbosque.model.RecetaDTO;

import co.edu.unbosque.util.LocalDateAdapter;


public class ExternalHTTPRequestHandler {

    protected static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .setLenient()
            .create();


    public static String doGet(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> resp = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("GET " + url + " -> " + resp.statusCode());
            return resp.body() == null ? "" : resp.body();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String doGetDetallado(String url, String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .build();

            HttpResponse<String> resp = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("GET (auth) " + url + " -> " + resp.statusCode());
            return resp.body() == null ? "" : resp.body();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String doPost(String url, String json) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json == null ? "" : json))
                    .build();

            HttpResponse<String> resp = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("POST " + url + " -> " + resp.statusCode());
            return resp.statusCode() + "\n" + (resp.body() == null ? "" : resp.body());
        } catch (Exception e) {
            e.printStackTrace();
            return "500\n";
        }
    }

    public static String doPostRegister(String url, String json) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json == null ? "" : json))
                    .build();

            HttpResponse<String> resp = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            String body = resp.body() == null ? "" : resp.body();
            System.out.println("doPostRegister -> " + resp.statusCode());
            return resp.statusCode() + "   \n   " + body;
        } catch (Exception e) {
            e.printStackTrace();
            return "500   \n   ";
        }
    }

    public static String doPostLogin(String url, String json) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json == null ? "" : json))
                    .build();

            HttpResponse<String> resp = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            String body = resp.body() == null ? "" : resp.body();
            System.out.println("doPostLogin -> " + resp.statusCode());
            return resp.statusCode() + "\n" + body;
        } catch (Exception e) {
            e.printStackTrace();
            return "500\n";
        }
    }

    public static String doPostLoginVendedor(String url, String json) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json == null ? "" : json))
                    .build();

            HttpResponse<String> resp = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("doPostLoginVendedor -> " + resp.statusCode());
            if (resp.statusCode() == 200) {
                return resp.body() == null ? "" : resp.body();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String doUpdate(String url, String json) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json == null ? "" : json))
                    .build();

            HttpResponse<String> resp = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("PUT " + url + " -> " + resp.statusCode());
            return resp.statusCode() + "\n" + (resp.body() == null ? "" : resp.body());
        } catch (Exception e) {
            e.printStackTrace();
            return "500\n";
        }
    }

    public static String doDelete(String url) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .DELETE()
                    .build();

            HttpResponse<String> resp = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println("DELETE " + url + " -> " + resp.statusCode());
            return resp.body() == null ? "" : resp.body();
        } catch (Exception e) {
            try {
                URL u = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(false);
                int responseCode = conn.getResponseCode();

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();
                conn.disconnect();
                System.out.println("DELETE (fallback) " + url + " -> " + responseCode);
                return sb.toString();
            } catch (Exception ex) {
                ex.printStackTrace();
                return "";
            }
        }
    }


    public static String prettyPrintUsingGson(String uglyJson) {
        try {
            Gson pretty = new GsonBuilder().setLenient().setPrettyPrinting().create();
            JsonElement je = JsonParser.parseString(uglyJson);
            return pretty.toJson(je);
        } catch (Exception e) {
            return uglyJson == null ? "" : uglyJson;
        }
    }

    private static <T> List<T> parseJsonToList(String json, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        if (json == null) return result;
        String trimmed = json.trim();
        try {
            if (trimmed.startsWith("[")) {
                Type listType = TypeToken.getParameterized(List.class, clazz).getType();
                result = GSON.fromJson(trimmed, listType);
                if (result == null) result = new ArrayList<>();
                return result;
            } else if (trimmed.startsWith("{")) {
                JsonElement je = JsonParser.parseString(trimmed);
                if (je.isJsonObject() && je.getAsJsonObject().has("data")) {
                    JsonElement data = je.getAsJsonObject().get("data");
                    if (data.isJsonArray()) {
                        Type listType = TypeToken.getParameterized(List.class, clazz).getType();
                        result = GSON.fromJson(data, listType);
                        if (result == null) result = new ArrayList<>();
                        return result;
                    }
                }
                T single = GSON.fromJson(trimmed, clazz);
                if (single != null) result.add(single);
                return result;
            } else {
                return result;
            }
        } catch (JsonSyntaxException ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

   

    public static List<AdministradorDTO> doGetAndConvertToDTOListAdministrador(String url) {
        try {
            String j = doGet(url);
            return parseJsonToList(j, AdministradorDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static List<DireccionEmpresaDTO> doGetAndConvertToDTOListDireccionEmpresa(String url) {
        try {
            String j = doGet(url);
            return parseJsonToList(j, DireccionEmpresaDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static List<EmpresaDTO> doGetAndConvertToDTOListEmpresa(String url) {
        try {
            String j = doGet(url);
            return parseJsonToList(j, EmpresaDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static List<IngredienteDTO> doGetAndConvertToDTOListIngrediente(String url) {
        try {
            String j = doGet(url);
            return parseJsonToList(j, IngredienteDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static List<InventarioDTO> doGetAndConvertToDTOListInventario(String url) {
        try {
            String j = doGet(url);
            return parseJsonToList(j, InventarioDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static List<LoteDTO> doGetAndConvertToDTOListLote(String url) {
        try {
            String j = doGet(url);
            return parseJsonToList(j, LoteDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static List<OrdenCompraDTO> doGetAndConvertToDTOListOrdenCompra(String url) {
        try {
            String j = doGet(url);
            return parseJsonToList(j, OrdenCompraDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static List<PlatoDTO> doGetAndConvertToDTOListPlato(String url) {
        try {
            String j = doGet(url);
            return parseJsonToList(j, PlatoDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static List<RecetaDTO> doGetAndConvertToDTOListReceta(String url) {
        try {
            String j = doGet(url);
            return parseJsonToList(j, RecetaDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static <T> List<T> doGetAndConvertToDTOListGeneric(String url, Class<T> clazz) {
        try {
            String j = doGet(url);
            return parseJsonToList(j, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}

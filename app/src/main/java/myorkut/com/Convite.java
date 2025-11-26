package myorkut.com;

public class Convite {
    private String id;
    private String usuId;
    private String texto;
    private String tipo;
    private String destId;
    private String data;

    public Convite(String id, String usuId, String texto, String tipo, String destId, String data) {
        this.id = id;
        this.usuId = usuId;
        this.texto = texto;
        this.tipo = tipo;
        this.destId = destId;
        this.data = data;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getUsuId() {
        return usuId;
    }

    public String getTexto() {
        return texto;
    }

    public String getTipo() {
        return tipo;
    }

    public String getDestId() {
        return destId;
    }

    public String getData() {
        return data;
    }

    // Setters (opcionais, mas úteis)
    public void setId(String id) {
        this.id = id;
    }

    public void setUsuId(String usuId) {
        this.usuId = usuId;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setDestId(String destId) {
        this.destId = destId;
    }

    public void setData(String data) {
        this.data = data;
    }

    // Método toString para debug
    @Override
    public String toString() {
        return "Convite{" +
                "id='" + id + '\'' +
                ", usuId='" + usuId + '\'' +
                ", texto='" + texto + '\'' +
                ", tipo='" + tipo + '\'' +
                ", destId='" + destId + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
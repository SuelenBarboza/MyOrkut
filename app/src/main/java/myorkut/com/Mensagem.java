package myorkut.com;

public class Mensagem {
    private String texto;
    private String remetente;
    private long timestamp;
    private boolean enviadaPorMim;

    public Mensagem(String texto, String remetente, long timestamp, boolean enviadaPorMim) {
        this.texto = texto;
        this.remetente = remetente;
        this.timestamp = timestamp;
        this.enviadaPorMim = enviadaPorMim;
    }

    // Getters
    public String getTexto() { return texto; }
    public String getRemetente() { return remetente; }
    public long getTimestamp() { return timestamp; }
    public boolean isEnviadaPorMim() { return enviadaPorMim; }

    public void setId(String id) {

    }
}


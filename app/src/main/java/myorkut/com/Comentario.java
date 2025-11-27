package myorkut.com;

public class Comentario {
    private String pubId;
    private String usuId;
    private String texto;
    private String data;
    private String nomeUsuComment;


    public Comentario(String pubId, String usuId, String texto, String data, String nomeUsuComment) {
        this.pubId = pubId;
        this.usuId = usuId;
        this.texto = texto;
        this.data = data;
        this.nomeUsuComment = nomeUsuComment;
    }
    public String getUsu_nome() {
        return nomeUsuComment;
    }

    public String getCom_data() {
        return data;
    }

    public String getCom_texto() {
        return texto;
    }

    // Outros getters se necessário
    public String getPubId() { return pubId; }
    public String getUsuId() { return usuId; }
    public String getData() { return data; }
    public String getNomeUsuComment() { return nomeUsuComment; }
    public String getTexto() { return texto; }
}

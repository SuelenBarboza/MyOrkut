package myorkut.com;

import java.util.Objects;

public class PublicacaoComUsuario {
    private String pub_id;
    private String usu_id;
    private String pub_texto;
    private String pub_data;
    private String pub_status;
    private String usu_nome;

    private String idPubli;

    public PublicacaoComUsuario(String pub_id, String usu_id, String pub_texto,
                                String pub_data, String pub_status, String usu_nome) {
        this.pub_id = pub_id;
        this.usu_id = usu_id;
        this.pub_texto = pub_texto;
        this.pub_data = pub_data;
        this.pub_status = pub_status;
        this.usu_nome = usu_nome;
    }



    // Getters
    public String getPub_id() { return pub_id; }
    public String getUsu_id() { return usu_id; }
    public String getPub_texto() { return pub_texto; }
    public String getPub_data() { return pub_data; }
    public String getPub_status() { return pub_status; }
    public String getUsu_nome() { return usu_nome; }

    // Setters
    public void setPub_id(String pub_id) { this.pub_id = pub_id; }
    public void setUsu_id(String usu_id) { this.usu_id = usu_id; }
    public void setPub_texto(String pub_texto) { this.pub_texto = pub_texto; }
    public void setPub_data(String pub_data) { this.pub_data = pub_data; }
    public void setPub_status(String pub_status) { this.pub_status = pub_status; }
    public void setUsu_nome(String usu_nome) { this.usu_nome = usu_nome; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicacaoComUsuario that = (PublicacaoComUsuario) o;
        return pub_id == that.pub_id &&
                Objects.equals(usu_id, that.usu_id) &&
                Objects.equals(pub_texto, that.pub_texto) &&
                Objects.equals(pub_data, that.pub_data) &&
                Objects.equals(pub_status, that.pub_status) &&
                Objects.equals(usu_nome, that.usu_nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pub_id, usu_id, pub_texto, pub_data, pub_status, usu_nome);
    }
}

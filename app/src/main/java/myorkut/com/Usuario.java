package myorkut.com;

public class Usuario {
    private String usu_id;    // Apelido como ID
    private String usu_nome;  // Nome completo
    private String usu_email; // Email

    public Usuario(String id, String nome, String apelido) {
        this.usu_id = id;
        this.usu_nome = nome;
        this.usu_email = apelido;
    }

    // Getters
    public String getUsu_id() { return usu_id; }
    public String getUsu_nome() { return usu_nome; }
    public String getUsu_email() { return usu_email; }

    // Setters (se necessário)
    public void setUsu_id(String usu_id) { this.usu_id = usu_id; }
    public void setUsu_nome(String usu_nome) { this.usu_nome = usu_nome; }
    public void setUsu_email(String usu_email) { this.usu_email = usu_email; }

}

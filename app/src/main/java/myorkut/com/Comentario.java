package myorkut.com;

public class Comentario {
    private int com_id;
    private int pub_id;
    private String usu_id;
    private String usu_nome;
    private String com_texto;
    private String com_data;

    public Comentario(int com_id, int pub_id, String usu_id, String usu_nome, String com_texto, String com_data) {
        this.com_id = com_id;
        this.pub_id = pub_id;
        this.usu_id = usu_id;
        this.usu_nome = usu_nome;
        this.com_texto = com_texto;
        this.com_data = com_data;
    }

    // Getters
    public int getCom_id() { return com_id; }
    public int getPub_id() { return pub_id; }
    public String getUsu_id() { return usu_id; }
    public String getUsu_nome() { return usu_nome; }
    public String getCom_texto() { return com_texto; }
    public String getCom_data() { return com_data; }
}

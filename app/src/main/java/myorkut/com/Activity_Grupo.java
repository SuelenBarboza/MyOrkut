package myorkut.com;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;

public class Activity_Grupo extends AppCompatActivity {

    LinearLayout linearLayoutContatos;
    ImageView home, perfil, publicacao;
    private String usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupo);

        initViews();
        setupClickListeners();
        carregarContatos();
    }

    private void initViews() {
        linearLayoutContatos = findViewById(R.id.linearLayoutContatos);
        home = findViewById(R.id.imageViewHome);
        perfil = findViewById(R.id.imageViewPerfil);
        publicacao = findViewById(R.id.imageViewNovaPublicacao);
    }

    private void setupClickListeners() {
        perfil.setOnClickListener(v -> {
            startActivity(new Intent(Activity_Grupo.this, activity_perfil.class));
            finish();
        });

        home.setOnClickListener(v -> {
            startActivity(new Intent(Activity_Grupo.this, activity_feed.class));
            finish();
        });

        publicacao.setOnClickListener(v -> {
            startActivity(new Intent(Activity_Grupo.this, NovaPublicacaoActivity.class));
            finish();
        });
    }

    private void carregarContatos() {
        linearLayoutContatos.removeAllViews();

        new Thread(() -> {
            try {
                SharedPreferences sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE);
                String usuarioLogadoId = sharedPref.getString("usuario_logado", "");

                if (usuarioLogadoId == null || usuarioLogadoId.isEmpty()) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Usuário não logado", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                usuarioId = usuarioLogadoId.replace("\"", "");

                String response = HttpConnection.get("contacts/" + usuarioId, this);

                System.out.println("RESPOSTA BRUTA: " + response);

                if (response == null || response.isEmpty()) {
                    runOnUiThread(this::exibirMensagemSemContatos);
                    return;
                }

                JSONArray jsonArray = new JSONArray(response);

                runOnUiThread(() -> {
                    linearLayoutContatos.removeAllViews();

                    if (jsonArray.length() == 0) {
                        exibirMensagemSemContatos();
                        return;
                    }

                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            JSONObject contatoObj = jsonArray.getJSONObject(i);

                            // Tente diferentes chaves possíveis
                            String contatoId = contatoObj.optString("contatoId",
                                    contatoObj.optString("CONTATOID",
                                            contatoObj.optString("id", "")));

                            String nomeContato = contatoObj.optString("nomeContato",
                                    contatoObj.optString("NOMECONTATO",
                                            contatoObj.optString("nome", "Desconhecido")));

                            // Se ainda não encontrou, use as chaves originais
                            if (contatoId.isEmpty()) {
                                contatoId = contatoObj.optString("usu1Id", "");
                            }

                            if (nomeContato.equals("Desconhecido")) {
                                nomeContato = contatoObj.optString("nomeUsuario", "Contato");
                            }

                            // Pular se não tem ID válido ou é o próprio usuário
                            if (contatoId.isEmpty() || contatoId.equals(usuarioId)) {
                                continue;
                            }

                            String nomeReduzido = nomeContato.length() > 6
                                    ? nomeContato.substring(0, 6) + "..."
                                    : nomeContato;

                            adicionarContatoComBotaoGrupo(contatoId, nomeReduzido, "contato", "");

                            System.out.println("Contato adicionado: " + nomeContato);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    private void exibirMensagemSemContatos() {
        TextView txt = new TextView(this);
        txt.setText("Nenhuma conversa encontrada");
        txt.setTextSize(16);
        txt.setPadding(50, 50, 50, 50);
        txt.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        linearLayoutContatos.addView(txt);
    }
    private void adicionarContatoComBotaoGrupo(String contatoId, String nome, String status, String ultimaMensagem) {
        try {
            View view = getLayoutInflater().inflate(R.layout.item_contato_grupo, null);

            TextView txtNome = view.findViewById(R.id.txtNomeContato);
            Button btnCriarGrupo = view.findViewById(R.id.btnCriarGrupo);

            txtNome.setText(nome);

            view.setOnClickListener(v -> {
                onContatoClick(contatoId, nome);
            });

            btnCriarGrupo.setOnClickListener(v -> {
                criarGrupoComContato(contatoId, nome);
            });

            linearLayoutContatos.addView(view);

            System.out.println("SUCESSO: Contato '" + nome + "' adicionado à tela");

        } catch (Exception e) {
            System.out.println("ERRO ao inflar layout: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void criarGrupoComContato(String contatoId, String contatoNome) {
        new Thread(() -> {
            try {
                // 1. Criar o grupo
                JSONObject jsonGrupo = new JSONObject();
                jsonGrupo.put("nome", "Grupo com " + contatoNome);
                jsonGrupo.put("descricao", "Grupo criado pelo app");
                jsonGrupo.put("status", "ATIVO");

                String grupoResponse = HttpConnection.post("groups", jsonGrupo.toString(), this);

                System.out.println("RESPOSTA CRIAÇÃO GRUPO: " + grupoResponse);

                if (grupoResponse == null || grupoResponse.isEmpty()) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Erro ao criar grupo - resposta vazia", Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                JSONObject grupoJson = new JSONObject(grupoResponse);
                String grupoId = grupoJson.optString("id", "");

                // Tenta diferentes possibilidades de chave para o ID
                if (grupoId.isEmpty()) {
                    grupoId = grupoJson.optString("ID", "");
                }
                if (grupoId.isEmpty()) {
                    grupoId = grupoJson.optString("grupoId", "");
                }

                System.out.println("GRUPO ID OBTIDO: " + grupoId);

                if (grupoId.isEmpty()) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Erro: grupo sem ID retornado", Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                // 2. Adicionar usuário logado ao grupo
                SharedPreferences sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE);
                String usuarioLogadoId = sharedPref.getString("usuario_logado", "").replace("\"", "");

                JSONObject bodyUser = new JSONObject();
                bodyUser.put("grupoId", grupoId);
                bodyUser.put("usuarioId", usuarioLogadoId);

                String responseUser = HttpConnection.post("groups/members", bodyUser.toString(), this);
                System.out.println("RESPOSTA ADD USUÁRIO: " + responseUser);

                // 3. Adicionar contato ao grupo
                JSONObject bodyContato = new JSONObject();
                bodyContato.put("grupoId", grupoId);
                bodyContato.put("usuarioId", contatoId);

                String responseContato = HttpConnection.post("groups/members", bodyContato.toString(), this);
                System.out.println("RESPOSTA ADD CONTATO: " + responseContato);

                runOnUiThread(() -> {
                    Toast.makeText(this,
                            "Grupo criado e membros adicionados com sucesso!",
                            Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this,
                            "Erro ao criar grupo: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }


    private void onContatoClick(String contatoId, String contatoNome) {
        Intent intent = new Intent(Activity_Grupo.this, activity_mensagem.class);
        intent.putExtra("CONTATO_ID", contatoId);
        intent.putExtra("CONTATO_NOME", contatoNome);
        startActivity(intent);
    }
}
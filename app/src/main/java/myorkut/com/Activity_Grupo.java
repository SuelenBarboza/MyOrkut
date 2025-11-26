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
        setContentView(R.layout.activity_chat); // Verifique se este layout é o correto

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

                String response = HttpConnection.get("messages/" + usuarioId, this);

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

                    HashSet<String> contatosUnicos = new HashSet<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            JSONObject msgObj = jsonArray.getJSONObject(i);

                            String usuarioQueEnviou = msgObj.optString("USUID", "");
                            String usuarioQueRecebeu = msgObj.optString("CONTATOID", "");

                            String nomeUsuario = msgObj.optString("NOMEUSUARIO", "Desconhecido");
                            String descricao = msgObj.optString("DESCRICAO", "");
                            String status = msgObj.optString("STATUS", "");

                            String contatoId;

                            if (usuarioQueEnviou.equals(usuarioId)) {
                                contatoId = usuarioQueRecebeu;
                            } else {
                                contatoId = usuarioQueEnviou;
                            }

                            if (contatosUnicos.contains(contatoId)) continue;
                            contatosUnicos.add(contatoId);

                            String nomeReduzido = nomeUsuario.length() > 6
                                    ? nomeUsuario.substring(0, 6) + "..."
                                    : nomeUsuario;

                            adicionarContatoComBotaoGrupo(contatoId, nomeReduzido, status, descricao);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Erro ao carregar mensagens", Toast.LENGTH_SHORT).show()
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
    }

    private void criarGrupoComContato(String contatoId, String contatoNome) {
        // TODO: Implementar criação real de grupo
        Toast.makeText(this,
                "Criar grupo com: " + contatoNome + " (ID: " + contatoId + ")",
                Toast.LENGTH_LONG).show();

    }

    private void onContatoClick(String contatoId, String contatoNome) {
        Intent intent = new Intent(Activity_Grupo.this, activity_mensagem.class);
        intent.putExtra("CONTATO_ID", contatoId);
        intent.putExtra("CONTATO_NOME", contatoNome);
        startActivity(intent);
    }
}
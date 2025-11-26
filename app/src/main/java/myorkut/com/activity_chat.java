package myorkut.com;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;

public class activity_chat extends AppCompatActivity {

    LinearLayout linearLayoutContatos;

    ImageView home, perfil, publicacao;

    private  String usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        linearLayoutContatos = findViewById(R.id.linearLayoutContatos);
        home = findViewById(R.id.imageViewHome);
        perfil = findViewById(R.id.imageViewPerfil);
        publicacao = findViewById(R.id.imageViewNovaPublicacao);

        setupClickListeners();
        carregarContatos();
    }

    private void  setupClickListeners(){
        perfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(activity_chat.this, activity_perfil.class));
                finish();
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(activity_chat.this, activity_feed.class));
                finish();
            }
        });

        publicacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(activity_chat.this, NovaPublicacaoActivity.class));
                finish();
            }
        });
    }

    private void carregarContatos() {
        linearLayoutContatos.removeAllViews();

        new Thread(() -> {
            try {

                SharedPreferences sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE);
                String usuarioLogadoId = sharedPref.getString("usuario_logado", "");

                usuarioId = usuarioLogadoId.replace("\"", "");

                String response = HttpConnection.get("messages/" + usuarioId, this);
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

                            String usuarioQueEnviou = msgObj.optString("USUID");
                            String usuarioQueRecebeu = msgObj.optString("CONTATOID");

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
                                    ? nomeUsuario.substring(0, 6)
                                    : nomeUsuario;

                            adicionarContato(contatoId, nomeReduzido, status, descricao);

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
        txt.setPadding(20, 20, 20, 20);
        linearLayoutContatos.addView(txt);
    }

    private void adicionarContato(String contatoId, String nome, String status, String ultimaMensagem) {

        View view = getLayoutInflater().inflate(R.layout.item_contato, null);

        TextView txtNome = view.findViewById(R.id.txtNomeContato);
        TextView txtUltimaMsg = view.findViewById(R.id.txtUltimaMensagem);

        txtNome.setText(nome);
        txtUltimaMsg.setText(ultimaMensagem);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(16, 16, 16, 24); // esquerda, topo, direita, inferior
        view.setLayoutParams(params);

        view.setOnClickListener(v -> onContatoClick(contatoId, nome));

        linearLayoutContatos.addView(view);
    }


    public void onContatoClick(String contatoId, String contatoNome) {
        Intent intent = new Intent(activity_chat.this, activity_mensagem.class);
        intent.putExtra("CONTATO_ID", contatoId);
        intent.putExtra("CONTATO_NOME", contatoNome);
        startActivity(intent);
    }
}
package myorkut.com;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

public class activity_chat extends AppCompatActivity {

    LinearLayout linearLayoutContatos;

    ImageView home, perfil, publicacao, imageViewLupa;
    EditText editTextPesquisa;
    private PopupWindow popupWindow;

    private  String usuarioId;
    private ListView listViewResultados;

    private final ArrayList<String> nomesResultados = new ArrayList<>();
    private final ArrayList<String> idsResultados = new ArrayList<>();

    private final ArrayList<ContatoBusca> todosOsContatos = new ArrayList<>();

    private static class ContatoBusca {
        String id;
        String nome;
        String nomeLower;

        ContatoBusca(String id, String nome) {
            this.id = id;
            this.nome = nome;
            this.nomeLower = nome.toLowerCase(Locale.getDefault());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        linearLayoutContatos = findViewById(R.id.linearLayoutContatos);
        home = findViewById(R.id.imageViewHome);
        perfil = findViewById(R.id.imageViewPerfil);
        publicacao = findViewById(R.id.imageViewNovaPublicacao);
        editTextPesquisa = findViewById(R.id.editTextPesquisa);
        imageViewLupa = findViewById(R.id.imageViewLupa);


        findViewById(R.id.imageViewHome).setOnClickListener(v -> startAndFinish(activity_feed.class));
        findViewById(R.id.imageViewPerfil).setOnClickListener(v -> startAndFinish(activity_perfil.class));
        findViewById(R.id.imageViewNovaPublicacao).setOnClickListener(v -> startAndFinish(NovaPublicacaoActivity.class));
        findViewById(R.id.imageViewNotificacao).setOnClickListener(v -> startAndFinish(activity_notificacao.class));

        imageViewLupa.setOnClickListener(v -> buscarContatosDoUsuarioLogado());

        carregarContatos();
    }
    private void startAndFinish(Class<?> cls) {
        startActivity(new Intent(this, cls));
        finish();
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

    private void buscarContatosDoUsuarioLogado() {
        String termo = editTextPesquisa.getText().toString().trim();
        if (termo.isEmpty()) {
            Toast.makeText(this, "Digite um nome", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                usuarioId = prefs.getString("usuario_logado", "").replace("\"", "");

                String response = HttpConnection.get("contacts/" + usuarioId, this);
                JSONArray jsonArray = new JSONArray(response);

                nomesResultados.clear();
                idsResultados.clear();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String usu1Id = obj.optString("usu1Id");
                    String contatoId = obj.optString("contatoId");
                    String nomeContato = obj.optString("nomeContato");

                    String idOutro = usu1Id.equals(usuarioId) ? contatoId : usu1Id;

                    if (nomeContato.toLowerCase().contains(termo.toLowerCase())) {
                        nomesResultados.add(nomeContato);
                        idsResultados.add(idOutro);
                    }
                }

                runOnUiThread(() -> {
                    if (nomesResultados.isEmpty()) {
                        Toast.makeText(this, "Nenhum contato encontrado", Toast.LENGTH_SHORT).show();
                        if (popupWindow != null) popupWindow.dismiss();
                    } else {
                        mostrarPopup();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Erro ao buscar contatos", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void mostrarPopup() {
        if (popupWindow == null) {
            View view = LayoutInflater.from(this).inflate(R.layout.popup_busca, null);
            listViewResultados = view.findViewById(R.id.listViewResultados);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1, nomesResultados);
            listViewResultados.setAdapter(adapter);

            listViewResultados.setOnItemClickListener((parent, view1, position, id) -> {
                String contatoId = idsResultados.get(position);
                String contatoNome = nomesResultados.get(position);

                Intent intent = new Intent(this, activity_mensagem.class);
                intent.putExtra("CONTATO_ID", contatoId);
                intent.putExtra("CONTATO_NOME", contatoNome);
                startActivity(intent);

                editTextPesquisa.setText("");
                popupWindow.dismiss();
            });

            popupWindow = new PopupWindow(view,
                    editTextPesquisa.getWidth(),
                    500, true);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setBackgroundDrawable(getDrawable(android.R.color.white));
        } else {
            ((ArrayAdapter<?>) listViewResultados.getAdapter()).notifyDataSetChanged();
        }

        if (!popupWindow.isShowing()) {
            popupWindow.showAsDropDown(editTextPesquisa, 0, 10);
        }
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
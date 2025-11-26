package myorkut.com;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

public class activity_perfil extends AppCompatActivity {

    private EditText editTextPesquisa;
    private ImageView imageViewLupa;
    private ImageView imageViewNotificacao;
    private ImageView imageViewHome;
    private ImageView imageViewNovaPublicacao;
    private ImageView imageViewPerfil;
    private RecyclerView recyclerViewPublicacoes;
    private TextView textViewNomeUsuario;
    private TextView textViewApelido;
    private TextView textViewPublicacoesCount;

    private Button btnLoggout;

    private PublicacaoAdapter publicacaoAdapter;
    private List<PublicacaoComUsuario> listaPublicacoesUsuario;
    private String usuarioLogadoId;
    private String usuarioLogadoNome;
    private String usuarioLogadoApelido;
    private Button buttonConvidarAmigos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        initViews();
        setupRecyclerView();
        setupClickListeners();
        carregarPublicacoesUsuario();

        obterUsuarioLogado();
    }

    private void obterUsuarioLogado() {
        new Thread(() -> {
            try {
                SharedPreferences sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE);
                usuarioLogadoId = sharedPref.getString("usuario_logado", "usuario_teste");

                usuarioLogadoId = usuarioLogadoId.replace("\"", "");

                String response = HttpConnection.get("users/" + usuarioLogadoId, activity_perfil.this);
                Log.d("API_DADOSPERFIL", "DADOS-PERFIL: " + response + " ID ENVIADO: " + usuarioLogadoId);

                Object json = new JSONTokener(response).nextValue();

                if (json instanceof JSONObject) {
                    JSONObject obj = (JSONObject) json;

                    usuarioLogadoApelido = obj.getString("apelido");
                    usuarioLogadoNome = obj.getString("nome");

                    runOnUiThread(() -> {
                        textViewApelido.setText("@" + usuarioLogadoApelido);
                        textViewNomeUsuario.setText(usuarioLogadoNome);
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void initViews() {
        editTextPesquisa = findViewById(R.id.editTextPesquisa);
        imageViewLupa = findViewById(R.id.imageViewLupa);
        imageViewNotificacao = findViewById(R.id.imageViewNotificacao);
        imageViewHome = findViewById(R.id.imageViewHome);
        imageViewNovaPublicacao = findViewById(R.id.imageViewNovaPublicacao);
        imageViewPerfil = findViewById(R.id.imageViewPerfil);
        recyclerViewPublicacoes = findViewById(R.id.recyclerViewPublicacoes);
        textViewApelido = findViewById(R.id.textViewApelido);
        textViewPublicacoesCount = findViewById(R.id.textViewPublicacoesCount);
        buttonConvidarAmigos = findViewById(R.id.buttonConvidarAmigos);
        btnLoggout = (Button) findViewById(R.id.btnLoggout);
        textViewNomeUsuario = findViewById(R.id.textViewNomeUsuario);


        textViewApelido.setText("@" + usuarioLogadoApelido);
        textViewNomeUsuario.setText(usuarioLogadoNome);
    }

    private void setupRecyclerView() {
        listaPublicacoesUsuario = new ArrayList<>();
        publicacaoAdapter = new PublicacaoAdapter(listaPublicacoesUsuario);

        recyclerViewPublicacoes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPublicacoes.setAdapter(publicacaoAdapter);
    }

    private void setupClickListeners() {
        // Lupa de Pesquisa
        imageViewLupa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pesquisa = editTextPesquisa.getText().toString().trim();
                if (pesquisa.isEmpty()) {
                    Toast.makeText(activity_perfil.this, "Digite algo para pesquisar", Toast.LENGTH_SHORT).show();
                } else {
                    pesquisarPublicacoes(pesquisa);
                }
            }
        });

        // Sino de Notificação
        imageViewNotificacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_perfil.this, activity_notificacao.class);
                startActivity(intent);
            }
        });

        // Ícone Home (Feed)
        imageViewHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_perfil.this, activity_feed.class);
                startActivity(intent);
                finish();
            }
        });

        // Ícone Nova Publicação
        imageViewNovaPublicacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_perfil.this, NovaPublicacaoActivity.class);
                startActivity(intent);
            }
        });

        // Ícone Perfil
        imageViewPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity_perfil.this, "Você já está no seu perfil", Toast.LENGTH_SHORT).show();
            }
        });

        buttonConvidarAmigos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirListaUsuarios();
            }
        });

        btnLoggout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // PEGAR TOKEN DO STORAGE
                SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
//                String token = prefs.getString("jwt_token", null);//
                String token = HttpConnection.getToken(activity_perfil.this);

                if (token == null) {
                    Toast.makeText(activity_perfil.this, "Nenhum token encontrado. Faça login novamente.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // EXECUTAR REQUISIÇÃO EM THREAD
                new Thread(() -> {
                    try {
                        // CHAMA A ROTA POST /logout
                        String response = HttpConnection.post("logout", "",activity_perfil.this);

                        // LIMPAR DADOS LOCALMENTE APÓS RESPOSTA
                        runOnUiThread(() -> {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.remove("jwt_token");
                            editor.remove("userId");
                            editor.remove("apelido");
                            editor.apply();

                            Toast.makeText(activity_perfil.this, "Logout realizado!", Toast.LENGTH_SHORT).show();

                            // REDIRECIONA PARA LOGIN
                            Intent intent = new Intent(activity_perfil.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            Toast.makeText(activity_perfil.this, "Erro ao fazer logout", Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();
            }
        });

    }

    private void abrirListaUsuarios() {
        Intent intent = new Intent(activity_perfil.this, activity_lista_usuarios.class);
        startActivity(intent);
    }


    private void carregarPublicacoesUsuario() {

        listaPublicacoesUsuario.clear();
        publicacaoAdapter.notifyDataSetChanged();

        if (usuarioLogadoId == null) {
            Toast.makeText(this, "Usuário não identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        //correcao da api enviando id com aspas e barra
        if(usuarioLogadoId != null){
            usuarioLogadoId = usuarioLogadoId.replace("\"", "");
        }


        Log.d("API_PUBLICATIONS", "ID enviado para API: " + usuarioLogadoId);

        new Thread(() -> {
            try {

                String response = HttpConnection.get("publications/"+ usuarioLogadoId, activity_perfil.this);

                Log.d("API_PUBLICATIONS", "RESPOSTA: " + response);

                // Se API retornou erro, não tente transformar em JSONArray
                if (response.trim().startsWith("{\"error\"")) {
                    JSONObject err = new JSONObject(response);
                    String msg = err.optString("error", "Erro desconhecido");

                    runOnUiThread(() ->
                            Toast.makeText(this, "Erro API: " + msg, Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                JSONArray jsonArray = new JSONArray(response);
                List<PublicacaoComUsuario> listaTemp = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject json = jsonArray.getJSONObject(i);

                    PublicacaoComUsuario pub = new PublicacaoComUsuario(
                            json.getString("id"),
                            json.getString("usuId"),
                            json.getString("texto"),
                            json.getString("data"),
                            json.getString("status"),
                            json.getString("nome")
                    );

                    listaTemp.add(pub);
                }

                runOnUiThread(() -> {
                    listaPublicacoesUsuario.clear();
                    listaPublicacoesUsuario.addAll(listaTemp);
                    publicacaoAdapter.notifyDataSetChanged();

                    textViewPublicacoesCount.setText(String.valueOf(listaPublicacoesUsuario.size()));

                    if (listaPublicacoesUsuario.isEmpty()) {
                        Toast.makeText(this, "Você ainda não tem publicações. Crie a primeira!", Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                Log.e("API_PUBLICATIONS", "Erro ao carregar publicações: ", e);

                runOnUiThread(() ->
                        Toast.makeText(this, "Erro ao carregar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }




    private void pesquisarPublicacoes(String termo) {
        List<PublicacaoComUsuario> resultados = new ArrayList<>();

        for (PublicacaoComUsuario publicacao : listaPublicacoesUsuario) {
            if (publicacao.getPub_texto().toLowerCase().contains(termo.toLowerCase())) {
                resultados.add(publicacao);
            }
        }

        if (resultados.isEmpty()) {
            Toast.makeText(this, "Nenhuma publicação encontrada para: " + termo, Toast.LENGTH_SHORT).show();
        } else {
            publicacaoAdapter.atualizarLista(resultados);
            Toast.makeText(this, "Encontradas " + resultados.size() + " publicações", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        carregarPublicacoesUsuario();
    }



}
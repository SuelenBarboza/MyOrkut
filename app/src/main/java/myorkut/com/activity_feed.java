package myorkut.com;

import android.os.Bundle;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

public class activity_feed extends AppCompatActivity {

    private EditText editTextPesquisa;
    private ImageView imageViewLupa;
    private ImageView imageViewNotificacao;
    private ImageView imageViewContatos;
    private ImageView imageViewHome;
    private ImageView imageViewNovaPublicacao;
    private ImageView imageViewPerfil;
    private RecyclerView recyclerViewPublicacoes;
    private PublicacaoAdapter publicacaoAdapter;
    private List<PublicacaoComUsuario> listaPublicacoes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        initViews();
        setupRecyclerView();
        setupClickListeners();
        carregarPublicacoesDoBanco();
    }

    private void initViews() {
        editTextPesquisa = findViewById(R.id.editTextPesquisa);
        imageViewLupa = findViewById(R.id.imageViewLupa);
        imageViewNotificacao = findViewById(R.id.imageViewNotificacao);
        imageViewContatos = findViewById(R.id.imageViewContatos);
        imageViewHome = findViewById(R.id.imageViewHome);
        imageViewNovaPublicacao = findViewById(R.id.imageViewNovaPublicacao);
        imageViewPerfil = findViewById(R.id.imageViewPerfil);
        recyclerViewPublicacoes = findViewById(R.id.recyclerViewPublicacoes);

    }

    private void setupRecyclerView() {
        listaPublicacoes = new ArrayList<>();
        publicacaoAdapter = new PublicacaoAdapter(listaPublicacoes);

        recyclerViewPublicacoes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPublicacoes.setAdapter(publicacaoAdapter);
    }

    private void carregarPublicacoesDoBanco() {
        new Thread(() -> {
            try {
                String response = HttpConnection.get("publications", activity_feed.this);

                runOnUiThread(() -> {
                    try {
                        System.out.println("📌 Resposta da API: " + response);

                        listaPublicacoes.clear();


                        Object json = new JSONTokener(response).nextValue();

                        if (json instanceof JSONArray) {

                            JSONArray jsonArray = (JSONArray) json;

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject obj = jsonArray.getJSONObject(i);

                                String idPubli = obj.getString("id");
                                String usuId = obj.getString("usuId");
                                String texto = obj.getString("texto");
                                String data = obj.optString("data", "Sem data");
                                String status = obj.optString("status", "A");
                                String nome = obj.optString("nome", "Usuário");

                                listaPublicacoes.add(new PublicacaoComUsuario(
                                        idPubli,
                                        usuId,
                                        texto,
                                        data,
                                        status,
                                        nome
                                ));
                            }

                            publicacaoAdapter.notifyDataSetChanged();

                            if (listaPublicacoes.isEmpty()) {
                                Toast.makeText(activity_feed.this, "Nenhuma publicação encontrada", Toast.LENGTH_SHORT).show();
                            }

                        } else if (json instanceof JSONObject) {


                            JSONObject obj = (JSONObject) json;

                            String erro = obj.optString("error", "Resposta inesperada do servidor");
                            System.out.println("⚠ ERRO DA API: " + erro);

                            Toast.makeText(activity_feed.this, erro, Toast.LENGTH_LONG).show();

                        }

                    } catch (Exception e) {
                        System.out.println("⚠ Erro ao interpretar JSON:");
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                System.out.println("⚠ Erro ao chamar a API:");
                e.printStackTrace();

                runOnUiThread(() ->
                        Toast.makeText(activity_feed.this, "Erro ao conectar ao servidor", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }


    private void setupClickListeners() {
        // Lupa de Pesquisa
        imageViewLupa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pesquisa = editTextPesquisa.getText().toString().trim();
                if (pesquisa.isEmpty()) {
                    Toast.makeText(activity_feed.this, "Digite algo para pesquisar", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity_feed.this, "Pesquisando: " + pesquisa, Toast.LENGTH_SHORT).show();
                    pesquisarPublicacoes(pesquisa);
                }
            }
        });

        // Sino de Notificação
        imageViewNotificacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_feed.this, activity_notificacao.class);
                startActivity(intent);
            }
        });

        // Ícone de Contatos
        imageViewContatos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity_feed.this, "Contatos/Amigos", Toast.LENGTH_SHORT).show();
                // Intent para tela de contatos
                startActivity(new Intent(activity_feed.this, activity_chat.class));
            }
        });

        // Ícone Home
        imageViewHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity_feed.this, "Recarregando feed...", Toast.LENGTH_SHORT).show();
                carregarPublicacoesDoBanco(); // Recarregar o feed do banco
            }
        });

        // Ícone Nova Publicação
        imageViewNovaPublicacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity_feed.this, "Nova Publicação", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(activity_feed.this, NovaPublicacaoActivity.class);
                startActivity(intent);
            }
        });

        // Ícone Perfil
        imageViewPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity_feed.this, "Perfil", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(activity_feed.this, activity_perfil.class);
                startActivity(intent);
            }
        });
    }

    private void pesquisarPublicacoes(String termoPesquisa) {

        List<PublicacaoComUsuario> resultados = new ArrayList<>();

        for (PublicacaoComUsuario publicacao : listaPublicacoes) {
            if (publicacao.getPub_texto().toLowerCase().contains(termoPesquisa.toLowerCase()) ||
                    publicacao.getUsu_nome().toLowerCase().contains(termoPesquisa.toLowerCase())) {
                resultados.add(publicacao);
            }
        }

        if (resultados.isEmpty()) {
            Toast.makeText(this, "Nenhuma publicação encontrada para: " + termoPesquisa, Toast.LENGTH_SHORT).show();
        } else {
            publicacaoAdapter.atualizarLista(resultados);
            Toast.makeText(this, "Encontradas " + resultados.size() + " publicações", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        carregarPublicacoesDoBanco();
    }
}
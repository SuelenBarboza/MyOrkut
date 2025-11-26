package myorkut.com;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class activity_comentarios extends AppCompatActivity {

    private ImageView imageViewVoltar;
    private ImageView imageViewEnviarComentario;
    private EditText editTextComentario;
    private RecyclerView recyclerViewComentarios;
    private TextView textViewAutorPublicacao;
    private TextView textViewDataPublicacao;
    private TextView textViewTextoPublicacao;

    private ComentarioAdapter comentarioAdapter;
    private List<Comentario> listaComentarios;
    private PublicacaoComUsuario publicacao;

    private String usuarioLogadoId;
    private String usuarioLogadoNome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comentarios);

        obterUsuarioLogado();
        obterPublicacaoIntent();

        initViews();
        setupRecyclerView();
        setupClickListeners();
        carregarComentarios();
        exibirPublicacaoOriginal();
    }

    private void obterUsuarioLogado() {
        SharedPreferences sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE);
        usuarioLogadoId = sharedPref.getString("usuario_logado", "usuario_teste");
        usuarioLogadoNome = sharedPref.getString("usuario_nome", "Usuário Teste");
    }

    private void obterPublicacaoIntent() {
        Intent intent = getIntent();
        publicacao = new PublicacaoComUsuario(
                intent.getStringExtra("PUB_ID"),
                intent.getStringExtra("PUB_USU_ID"),
                intent.getStringExtra("PUB_TEXTO"),
                intent.getStringExtra("PUB_DATA"),
                intent.getStringExtra("PUB_STATUS"),
                intent.getStringExtra("PUB_USU_NOME")
        );
    }

    private void initViews() {
        imageViewVoltar = findViewById(R.id.imageViewVoltar);
        imageViewEnviarComentario = findViewById(R.id.imageViewEnviarComentario);
        editTextComentario = findViewById(R.id.editTextComentario);
        recyclerViewComentarios = findViewById(R.id.recyclerViewComentarios);
        textViewAutorPublicacao = findViewById(R.id.textViewAutorPublicacao);
        textViewDataPublicacao = findViewById(R.id.textViewDataPublicacao);
        textViewTextoPublicacao = findViewById(R.id.textViewTextoPublicacao);
    }

    private void setupRecyclerView() {
        listaComentarios = new ArrayList<>();
        comentarioAdapter = new ComentarioAdapter(listaComentarios);
        recyclerViewComentarios.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewComentarios.setAdapter(comentarioAdapter);
    }

    private void setupClickListeners() {
        imageViewVoltar.setOnClickListener(v -> finish());

        imageViewEnviarComentario.setOnClickListener(v -> enviarComentario());
    }

    private void exibirPublicacaoOriginal() {
        if (publicacao != null) {
            textViewAutorPublicacao.setText(publicacao.getUsu_nome());
            textViewDataPublicacao.setText(publicacao.getPub_data());
            textViewTextoPublicacao.setText(publicacao.getPub_texto());
        }
    }

    private void carregarComentarios() {
        // TODO: Fazer GET na rota /comments?pubId=...
        // Temporário vazio
        comentarioAdapter.notifyDataSetChanged();
    }

    private void enviarComentario() {
        String textoComentario = editTextComentario.getText().toString().trim();

        if (textoComentario.isEmpty()) {
            Toast.makeText(this, "Digite um comentário", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔥 JSON enviado à API
        JSONObject body = new JSONObject();
        try {
            body.put("pubId", publicacao.getPub_id());
            body.put("usuId", usuarioLogadoId);
            body.put("texto", textoComentario);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("API_COMMENTS", "Enviando: " + body.toString());

        // 🔥 Fazendo requisição real
        new Thread(() -> {

            String response = null;
            try {
                response = HttpConnection.post("comments", body.toString(), activity_comentarios.this);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String finalResponse = response;

            runOnUiThread(() -> {
                if (finalResponse == null) {
                    Toast.makeText(this, "Erro ao conectar com servidor", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    JSONObject resp = new JSONObject(finalResponse);

                    // Criar objeto com retorno real
                    Comentario comentarioSalvo = new Comentario(
                            resp.getInt("id"),
                            resp.getInt("pubId"),
                            resp.getString("usuId"),
                            usuarioLogadoNome,
                            resp.getString("texto"),
                            resp.getString("createdAt")
                    );

                    comentarioAdapter.adicionarComentario(comentarioSalvo);
                    recyclerViewComentarios.scrollToPosition(comentarioAdapter.getItemCount() - 1);
                    editTextComentario.setText("");

                    Toast.makeText(this, "Comentário enviado!", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    Toast.makeText(this, "Erro ao processar resposta", Toast.LENGTH_SHORT).show();
                    Log.e("API_COMMENTS", "Erro: " + e.getMessage());
                }
            });
        }).start();
    }

}

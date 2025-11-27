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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        exibirPublicacaoOriginal();

        carregarComentarios();
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

        imageViewEnviarComentario.setOnClickListener(v -> {
            Log.d("CLICK", "Botão enviar comentário clicado");
            enviarComentario();
        });
    }

    private void exibirPublicacaoOriginal() {
        if (publicacao != null) {
            textViewAutorPublicacao.setText(publicacao.getUsu_nome());
            textViewDataPublicacao.setText(publicacao.getPub_data());
            textViewTextoPublicacao.setText(publicacao.getPub_texto());
        }
    }

    private void carregarComentarios() {
        new Thread(() -> {
            try {
                if (publicacao == null || publicacao.getPub_id() == null) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Publicação não encontrada", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                String pubId = publicacao.getPub_id();
                Log.d("API_COMMENTS", "Buscando comentários para pubId: " + pubId);

                String response = HttpConnection.get("comments/" + pubId, this);
                Log.d("API_COMMENTS", "Resposta GET: " + response);

                if (response == null || response.isEmpty()) {
                    runOnUiThread(() -> {
                        listaComentarios.clear();
                        comentarioAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Nenhum comentário encontrado", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                org.json.JSONArray jsonArray = new org.json.JSONArray(response);
                List<Comentario> comentariosCarregados = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        org.json.JSONObject comentarioObj = jsonArray.getJSONObject(i);

                        String pubIdFromResponse = comentarioObj.optString("pubId", "");
                        String usuId = comentarioObj.optString("usuId", "");
                        String texto = comentarioObj.optString("texto", "");
                        String data = comentarioObj.optString("data", "");
                        String nomeUsuComment = comentarioObj.optString("nomeUsuComment", "Usuário");

                        Log.d("API_COMMENTS", "Comentário " + i + ": " + nomeUsuComment + " - " + texto);

                        Comentario comentario = new Comentario(
                                pubIdFromResponse,
                                usuId,
                                texto,
                                data,
                                nomeUsuComment
                        );

                        comentariosCarregados.add(comentario);

                    } catch (Exception e) {
                        Log.e("API_COMMENTS", "Erro ao processar comentário " + i, e);
                    }
                }

                runOnUiThread(() -> {
                    listaComentarios.clear();
                    listaComentarios.addAll(comentariosCarregados);
                    comentarioAdapter.notifyDataSetChanged();

                    if (!listaComentarios.isEmpty()) {
                        recyclerViewComentarios.scrollToPosition(listaComentarios.size() - 1);
                    }

                    Log.d("API_COMMENTS", "Carregados: " + listaComentarios.size() + " comentários");

                    if (listaComentarios.isEmpty()) {
                        Toast.makeText(this, "Nenhum comentário encontrado", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e("API_COMMENTS", "Erro ao carregar comentários", e);
                runOnUiThread(() -> {
                    listaComentarios.clear();
                    comentarioAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Erro ao carregar comentários: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void enviarComentario() {

        imageViewEnviarComentario.setAlpha(0.5f);
        imageViewEnviarComentario.setClickable(false);

        String textoComentario = editTextComentario.getText().toString().trim();

        if (textoComentario.isEmpty()) {
            Toast.makeText(this, "Digite um comentário", Toast.LENGTH_SHORT).show();

            imageViewEnviarComentario.setAlpha(1.0f);
            imageViewEnviarComentario.setClickable(true);
            return;
        }

        if (publicacao == null || publicacao.getPub_id() == null) {
            Toast.makeText(this, "Publicação não encontrada", Toast.LENGTH_SHORT).show();

            imageViewEnviarComentario.setAlpha(1.0f);
            imageViewEnviarComentario.setClickable(true);
            return;
        }
        usuarioLogadoId = usuarioLogadoId.replace("\"","");
        JSONObject body = new JSONObject();
        try {
            body.put("pubId", publicacao.getPub_id());
            body.put("usuId", usuarioLogadoId);
            body.put("texto", textoComentario);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("API_COMMENTS", "Enviando comentário: " + body.toString());

        new Thread(() -> {
            try {
                String response = HttpConnection.post("comments", body.toString(), activity_comentarios.this);
                Log.d("API_COMMENTS", "Resposta do POST: " + response);

                runOnUiThread(() -> {

                    imageViewEnviarComentario.setAlpha(1.0f);
                    imageViewEnviarComentario.setClickable(true);

                    if (response == null || response.isEmpty()) {
                        Toast.makeText(this, "Erro ao enviar comentário", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        JSONObject resp = new JSONObject(response);

                        Log.d("API_COMMENTS", "RESPOSTA CREATE:");
                        java.util.Iterator<String> keys = resp.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            Log.d("API_COMMENTS", key + ": " + resp.opt(key));
                        }

                        boolean status = resp.optBoolean("status", true);

                        if (status) {

                            editTextComentario.setText("");
                            carregarComentarios();

                            Toast.makeText(this, "Comentário enviado!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Erro ao salvar comentário", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Log.e("API_COMMENTS", "Erro ao processar resposta: " + e.getMessage(), e);
                        Toast.makeText(this, "Erro ao processar resposta do servidor", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e("API_COMMENTS", "Erro na requisição: " + e.getMessage(), e);
                runOnUiThread(() -> {

                    imageViewEnviarComentario.setAlpha(1.0f);
                    imageViewEnviarComentario.setClickable(true);
                    Toast.makeText(this, "Erro de conexão com o servidor", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}

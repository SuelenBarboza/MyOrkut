package myorkut.com;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;
import androidx.activity.OnBackPressedCallback;


public class NovaPublicacaoActivity extends AppCompatActivity {

    private EditText editTextPublicacao;
    private EditText editTextPesquisa;
    private Button btnPublicar;
    private TextView textContador;
    private ImageView imageViewLupa;
    private ImageView imageViewNotificacao;
    private ImageView imageViewContatos;
    private ImageView imageViewHome;
    private ImageView imageViewNovaPublicacao;
    private ImageView imageViewPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nova_publicacao);

        initViews();
        setupClickListeners();
        setupTextWatcher();

        // 🔥 Novo callback para substituir onBackPressed() depreciado
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }


    private void initViews() {
        editTextPublicacao = findViewById(R.id.editTextPublicacao);
        editTextPesquisa = findViewById(R.id.editTextPesquisa);
        btnPublicar = findViewById(R.id.btnPublicar);
        textContador = findViewById(R.id.textContador);
        imageViewLupa = findViewById(R.id.imageViewLupa);
        imageViewNotificacao = findViewById(R.id.imageViewNotificacao);
        imageViewContatos = findViewById(R.id.imageViewContatos);
        imageViewHome = findViewById(R.id.imageViewHome);
        imageViewNovaPublicacao = findViewById(R.id.imageViewNovaPublicacao);
        imageViewPerfil = findViewById(R.id.imageViewPerfil);
    }

    private void setupTextWatcher() {
        editTextPublicacao.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int caracteresRestantes = 255 - s.length();
                textContador.setText(s.length() + "/255 caracteres");

                if (caracteresRestantes < 50) {
                    textContador.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                } else {
                    textContador.setTextColor(getResources().getColor(R.color.black));
                }
            }
        });
    }

    private void setupClickListeners() {
        btnPublicar.setOnClickListener(v -> publicarConteudo());

        imageViewLupa.setOnClickListener(v -> {
            String pesquisa = editTextPesquisa.getText().toString().trim();
            if (pesquisa.isEmpty()) {
                Toast.makeText(this, "Digite algo para pesquisar", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Pesquisando: " + pesquisa, Toast.LENGTH_SHORT).show();
            }
        });

        imageViewNotificacao.setOnClickListener(v -> {
            Intent intent = new Intent(NovaPublicacaoActivity.this, activity_notificacao.class);
            startActivity(intent);
        });

        imageViewContatos.setOnClickListener(v ->
                Toast.makeText(this, "Contatos/Amigos", Toast.LENGTH_SHORT).show());

        imageViewHome.setOnClickListener(v -> finish());

        imageViewNovaPublicacao.setOnClickListener(v ->
                Toast.makeText(this, "Você já está criando uma publicação", Toast.LENGTH_SHORT).show());

        imageViewPerfil.setOnClickListener(v ->
                Toast.makeText(this, "Perfil", Toast.LENGTH_SHORT).show());
    }

    private void publicarConteudo() {
        String textoPublicacao = editTextPublicacao.getText().toString().trim();

        if (textoPublicacao.isEmpty()) {
            Toast.makeText(this, "Digite algo para publicar", Toast.LENGTH_SHORT).show();
            return;
        }

        if (textoPublicacao.length() > 255) {
            Toast.makeText(this, "A publicação deve ter no máximo 255 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        enviarPublicacao(textoPublicacao);
    }

    private void enviarPublicacao(String textoPublicacao) {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String usuarioId = prefs.getString("usuario_logado", null);

        if (usuarioId == null) {
            Toast.makeText(this, "Usuário não identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        //correcao da api enviando id com aspas e barra
        if (usuarioId != null) {
            usuarioId = usuarioId.replace("\"", "");
        }

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("usuId", usuarioId);
            jsonObject.put("texto", textoPublicacao);

            String jsonBody = jsonObject.toString();
            Log.d("API_PUBLICATIONS", "JSON ENVIADO: " + jsonBody);

            new Thread(() -> {
                try {
                    String response = HttpConnection.post(
                            "publications",  // já inclui token automaticamente
                            jsonBody,
                            NovaPublicacaoActivity.this
                    );

                    Log.d("API_PUBLICATIONS", "RESPOSTA: " + response);

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Publicação enviada!", Toast.LENGTH_SHORT).show();
                        finish();
                    });

                } catch (Exception e) {
                    Log.e("API_PUBLICATIONS", "Erro ao enviar publicação: ", e);
                    runOnUiThread(() ->
                            Toast.makeText(this, "Falha ao conectar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }
            }).start();

        } catch (Exception e) {
            Log.e("API_PUBLICATIONS", "Erro ao montar JSON: ", e);
        }
    }


}


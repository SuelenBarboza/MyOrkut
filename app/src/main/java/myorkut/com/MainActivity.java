package myorkut.com;

import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.net.HttpURLConnection;

import myorkut.com.R;

public class MainActivity extends AppCompatActivity {
    private EditText editTextApelido;
    private Button buttonEntrar;
    private TextView textCadastreSe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //verificarUsuarioLogado();
        initViews();
        setupClickListeners();
    }

    private void verificarUsuarioLogado() {
        SharedPreferences sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String usuarioLogado = sharedPref.getString("usuario_logado", null);

        if (usuarioLogado != null) {
            Intent intent = new Intent(MainActivity.this, activity_feed.class);
            startActivity(intent);
            finish();
        }
    }

    private void initViews() {
        editTextApelido = findViewById(R.id.editTextApelido);
        buttonEntrar = findViewById(R.id.buttonEntrar);
        textCadastreSe = findViewById(R.id.textCadastreSe);
    }

    private void setupClickListeners() {
        buttonEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entrar();
            }
        });

        textCadastreSe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cadastrar();
            }
        });
    }

    private void entrar() {
        String apelido = editTextApelido.getText().toString().trim();

        if (apelido.isEmpty()) {
            Toast.makeText(this, "Por favor, digite seu apelido", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                String body = "{ \"apelido\": \"" + apelido + "\" }";

                String response = HttpConnection.post("auth/login", body, MainActivity.this);

                // Procura o token e o id no JSON (básico sem libs)
                String token = response.split("\"token\":\"")[1].split("\"")[0];
                String userId = response.split("\"id\":")[1].split(",")[0];
                String userApelido = response.split("\"apelido\":\"")[1].split("\"")[0];

                // Salva token para usar depois
                HttpConnection.saveToken(MainActivity.this, token);

                runOnUiThread(() -> {
                    salvarUsuarioLogado(userId, userApelido);

                    Toast.makeText(MainActivity.this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, activity_feed.class));
                    finish();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Erro ao fazer login", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }


    private void salvarUsuarioLogado(String usuarioId, String usuarioNome) {
        SharedPreferences sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("usuario_logado", usuarioId);
        editor.putString("usuario_nome", usuarioNome);
        editor.apply();
    }

    private void cadastrar() {
        Toast.makeText(this, "Abrindo tela de cadastro", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, CadActivity.class);
        startActivity(intent);
    }
}
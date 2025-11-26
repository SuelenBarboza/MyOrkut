package myorkut.com;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class CadActivity extends AppCompatActivity {
    private EditText editTextName;
    private EditText editTextApelido;
    private EditText editTextDataNascimento;
    private Spinner spinnerGenero;
    private Button buttonCadastrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cad);

        initViews();


        setupSpinner();


        buttonCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cadastrar();
            }
        });
    }

    private void initViews() {
        editTextName = findViewById(R.id.editTextNome);
        editTextApelido = findViewById(R.id.editTextApelido);
        editTextDataNascimento = findViewById(R.id.editTextDataNascimento);
        spinnerGenero = findViewById(R.id.spinnerGenero);
        buttonCadastrar = findViewById(R.id.buttonCadastrar);
    }

    private void setupSpinner() {

        String[] generos = {
                "Selecione...",
                "Masculino",
                "Feminino",
                "Outro",
                "Prefiro não informar"
        };

        // adapter para o spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                generos
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGenero.setAdapter(adapter);
    }

    private void cadastrar() {
        String nome = editTextName.getText().toString().trim();
        String apelido = editTextApelido.getText().toString().trim();
        String dataNasc = editTextDataNascimento.getText().toString().trim();
        String genero = spinnerGenero.getSelectedItem().toString();

        if(genero == "Masculino"){
            genero = "Mascu";
        } else if (genero == "Feminino") {
            genero = "Femin";
        }else {
            genero = "Undef";
        }

        // validações
        if (nome.isEmpty()) {
            Toast.makeText(this, "Digite o nome", Toast.LENGTH_SHORT).show();
            editTextName.requestFocus();
            return;
        }

        if (apelido.isEmpty()) {
            Toast.makeText(this, "Digite o apelido", Toast.LENGTH_SHORT).show();
            editTextApelido.requestFocus();
            return;
        }

        if (dataNasc.isEmpty()) {
            Toast.makeText(this, "Digite a data de nascimento", Toast.LENGTH_SHORT).show();
            editTextDataNascimento.requestFocus();
            return;
        }

        if (genero.equals("Selecione...")) {
            Toast.makeText(this, "Selecione um gênero", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔥 JSON EXATO PARA API
        String jsonBody = "{"
                + "\"nome\":\"" + nome + "\","
                + "\"apelido\":\"" + apelido + "\","
                + "\"dataNasc\":\"" + dataNasc + "\","
                + "\"genero\":\"" + genero + "\""
                + "}";

        new Thread(() -> {
            try {
                // 🔥 Usa sua configuração interna de conexão
                String response = HttpConnection.post("register", jsonBody, CadActivity.this);

                runOnUiThread(() -> {
                    Toast.makeText(CadActivity.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();

                    // volta para tela de login
                    Intent intent = new Intent(CadActivity.this, MainActivity.class);
                    startActivity(intent);

                    limparCampos();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(CadActivity.this, "Erro ao cadastrar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }


    private void limparCampos() {
        editTextName.setText("");
        editTextApelido.setText("");
        editTextDataNascimento.setText("");
        spinnerGenero.setSelection(0);
    }
}
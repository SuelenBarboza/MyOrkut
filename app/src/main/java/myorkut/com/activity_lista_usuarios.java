package myorkut.com;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.ArrayList;
import java.util.List;

public class activity_lista_usuarios extends AppCompatActivity {

    private ImageView imageViewVoltar;
    private EditText editTextPesquisa;
    private ImageView imageViewLupa;
    private RecyclerView recyclerViewUsuarios;

    private UsuarioAdapter usuarioAdapter;
    private List<Usuario> listaUsuarios;
    private List<Usuario> listaUsuariosCompleta;
    private List<Convite> listaConvites;

    private String usuarioLogadoId;
    private String usuarioLogadoNome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_usuarios);

        obterUsuarioLogado();
        initViews();
        setupRecyclerView();
        setupClickListeners();
        setupTextWatcher();
        carregarUsuarios();
        carregarConvites();
    }

    private void obterUsuarioLogado() {
        SharedPreferences sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE);
        usuarioLogadoId = sharedPref.getString("usuario_logado", "usuario_teste");
        usuarioLogadoNome = sharedPref.getString("usuario_nome", "Usuário Teste");
    }

    private void initViews() {
        imageViewVoltar = findViewById(R.id.imageViewVoltar);
        editTextPesquisa = findViewById(R.id.editTextPesquisa);
        imageViewLupa = findViewById(R.id.imageViewLupa);
        recyclerViewUsuarios = findViewById(R.id.recyclerViewUsuarios);
    }

    private void setupRecyclerView() {
        listaUsuarios = new ArrayList<>();
        listaUsuariosCompleta = new ArrayList<>();
        listaConvites = new ArrayList<>();

        usuarioAdapter = new UsuarioAdapter(listaUsuarios, new UsuarioAdapter.OnUsuarioClickListener() {
            @Override
            public void onUsuarioClick(Usuario usuario) {
                enviarConvite(usuario);
            }
        });

        recyclerViewUsuarios.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsuarios.setAdapter(usuarioAdapter);
    }

    private void setupClickListeners() {
        imageViewVoltar.setOnClickListener(v -> finish());

        imageViewLupa.setOnClickListener(v -> {
            String pesquisa = editTextPesquisa.getText().toString().trim();
            pesquisarUsuarios(pesquisa);
        });
    }

    private void setupTextWatcher() {
        editTextPesquisa.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                pesquisarUsuarios(s.toString().trim());
            }
        });
    }


// nao ta fznd nada essa funcao
    private void carregarConvites() {
        new Thread(() -> {
            try {
                String response = HttpConnection.get("invitations", this);

                JSONArray jsonArray = new JSONArray(response);
                listaConvites.clear();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);

                    Convite convite = new Convite(
                            obj.getString("id").trim(),
                            obj.getString("usuId"),
                            obj.getString("texto"),
                            obj.getString("tipo"),
                            obj.getString("destId"),
                            obj.getString("data")
                    );

                    listaConvites.add(convite);
                }

                runOnUiThread(() ->
                        Toast.makeText(this, "Convites carregados: " + listaConvites.size(), Toast.LENGTH_SHORT).show()
                );

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Erro ao carregar convites", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }



    private void carregarUsuarios() {
        new Thread(() -> {
            try {
                String response = HttpConnection.get("users", this);

                JSONArray jsonArray = new JSONArray(response);
                listaUsuariosCompleta.clear();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);

                    String id = obj.getString("id");
                    String nome = obj.getString("nome");
                    String apelido = obj.getString("apelido");

                    listaUsuariosCompleta.add(new Usuario(id, nome, apelido));
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    listaUsuariosCompleta.removeIf(usuario -> usuario.getUsu_id().equals(usuarioLogadoId));
                }

                runOnUiThread(() -> {
                    listaUsuarios.clear();
                    listaUsuarios.addAll(listaUsuariosCompleta);
                    usuarioAdapter.notifyDataSetChanged();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Erro ao carregar usuários", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void enviarConvite(Usuario usuario) {
        new Thread(() -> {
            try {
                String destId = usuario.getUsu_id();
                usuarioLogadoId = usuarioLogadoId.replace("\"", "");

                JSONObject json = new JSONObject();
                json.put("usuId", usuarioLogadoId);
                json.put("texto", "Convite");
                json.put("tipo", "CN");
                json.put("destId", destId);

                String body = json.toString();

                Log.d("CONVITE_JSON_ENV", "JSON enviado: " + body);

                String response = HttpConnection.post("invitations", body, this);

                Log.d("CONVITE", usuarioLogadoId+" IDContato "+ destId);

                runOnUiThread(() ->
                        Toast.makeText(this, "Convite enviado para "+usuario.getUsu_nome(), Toast.LENGTH_SHORT).show()
                );

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Erro ao enviar convite", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void pesquisarUsuarios(String termo) {
        if (termo.isEmpty()) {
            listaUsuarios.clear();
            listaUsuarios.addAll(listaUsuariosCompleta);
        } else {
            listaUsuarios.clear();
            for (Usuario usuario : listaUsuariosCompleta) {
                if (usuario.getUsu_nome().toLowerCase().contains(termo.toLowerCase()) ||
                        usuario.getUsu_id().toLowerCase().contains(termo.toLowerCase())) {
                    listaUsuarios.add(usuario);
                }
            }
        }

        usuarioAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        editTextPesquisa.setText("");
    }
}

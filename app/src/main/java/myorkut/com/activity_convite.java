package myorkut.com;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class activity_convite extends AppCompatActivity {

    private EditText editTextPesquisa;
    private ImageView imageViewLupa;
    private ImageView imageViewHome;
    private ImageView imageViewNovaPublicacao;
    private ImageView imageViewPerfil;
    private LinearLayout linearLayoutConvites;
    private String usuarioId;
    private List<Convite> listaConvites = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convite);

        initViews();
        setupClickListeners();

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        usuarioId = prefs.getString("usuario_logado", null);

        if (usuarioId != null) {
            usuarioId = usuarioId.replace("\"", "");
        }

        carregarConvitesPendentes();
    }

    private void initViews() {
        editTextPesquisa = findViewById(R.id.editTextPesquisa);
        imageViewLupa = findViewById(R.id.imageViewLupa);
        imageViewHome = findViewById(R.id.imageViewHome);
        imageViewNovaPublicacao = findViewById(R.id.imageViewNovaPublicacao);
        imageViewPerfil = findViewById(R.id.imageViewPerfil);
        linearLayoutConvites = findViewById(R.id.linearLayoutConvites);
    }

    private void setupClickListeners() {
        imageViewLupa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pesquisa = editTextPesquisa.getText().toString().trim();
                if (pesquisa.isEmpty()) {
                    Toast.makeText(activity_convite.this, "Digite algo para pesquisar", Toast.LENGTH_SHORT).show();
                } else {
                    pesquisarConvites(pesquisa);
                }
            }
        });

        imageViewHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_convite.this, activity_feed.class);
                startActivity(intent);
                finish();
            }
        });

        imageViewNovaPublicacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_convite.this, NovaPublicacaoActivity.class);
                startActivity(intent);
            }
        });

        imageViewPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity_convite.this, "Perfil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void carregarConvitesPendentes() {
        linearLayoutConvites.removeAllViews();
        listaConvites.clear();

        if (usuarioId == null) {
            Toast.makeText(this, "Usuário não encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        usuarioId = usuarioId.replace("\"", "");

        new Thread(() -> {
            try {
                String contatosJson = HttpConnection.get("contacts/" + usuarioId, activity_convite.this);
                JSONArray contatosArray = new JSONArray(contatosJson);

                HashSet<String> meusContatos = new HashSet<>();
                for (int i = 0; i < contatosArray.length(); i++) {
                    JSONObject contato = contatosArray.getJSONObject(i);
                    meusContatos.add(contato.getString("contatoId"));
                }

                String response = HttpConnection.get("invitations", activity_convite.this);

                if (response == null || response.isEmpty()) {
                    runOnUiThread(() -> mostrarMensagemVazia("Nenhum convite pendente"));
                    return;
                }

                JSONArray jsonArray = new JSONArray(response);

                runOnUiThread(() -> {
                    try {
                        boolean encontrouConvites = false;

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject conviteJson = jsonArray.getJSONObject(i);

                            String destId = conviteJson.optString("destId", "");
                            String status = conviteJson.optString("status", "PENDENTE");
                            String remetenteId = conviteJson.optString("usuId", "");

                            if (destId.equals(usuarioId)
                                    && "PENDENTE".equals(status)
                                    && !meusContatos.contains(remetenteId)) {

                                encontrouConvites = true;

                                Convite convite = new Convite(
                                        conviteJson.getString("id"),
                                        remetenteId,
                                        conviteJson.optString("texto", "Convite"),
                                        conviteJson.optString("tipo", "CONVITE"),
                                        destId,
                                        conviteJson.getString("data")
                                );

                                listaConvites.add(convite);
                                adicionarConviteUI(convite);
                            }
                        }

                        if (!encontrouConvites) {
                            mostrarMensagemVazia("Nenhum convite pendente");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erro ao processar convites", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> mostrarMensagemVazia("Erro ao carregar convites 2"));
            }
        }).start();
    }


    private void adicionarConviteUI(Convite convite) {
        LinearLayout layoutConvite = new LinearLayout(this);
        layoutConvite.setOrientation(LinearLayout.VERTICAL);
        layoutConvite.setPadding(16, 16, 16, 16);
        layoutConvite.setBackground(ContextCompat.getDrawable(this, R.drawable.edittext_background));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        layoutConvite.setLayoutParams(params);

        TextView textUsuario = new TextView(this);
        textUsuario.setText(convite.getUsuId() + " enviou um convite de amizade");
        textUsuario.setTextSize(16);
        textUsuario.setTextColor(ContextCompat.getColor(this, R.color.black));
        textUsuario.setPadding(0, 0, 0, 8);

        TextView textData = new TextView(this);
        textData.setText("Enviado em: " + formatarData(convite.getData()));
        textData.setTextSize(12);
        textData.setTextColor(ContextCompat.getColor(this, R.color.black));
        textData.setPadding(0, 0, 0, 16);

        LinearLayout layoutBotoes = new LinearLayout(this);
        layoutBotoes.setOrientation(LinearLayout.HORIZONTAL);
        layoutBotoes.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView btnAceitar = new TextView(this);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        btnParams.setMargins(0, 0, 8, 0);
        btnAceitar.setLayoutParams(btnParams);
        btnAceitar.setText("Aceitar");
        btnAceitar.setTextSize(14);
        btnAceitar.setTextColor(ContextCompat.getColor(this, R.color.white));
        btnAceitar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        btnAceitar.setPadding(16, 12, 16, 12);
        btnAceitar.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        btnAceitar.setClickable(true);
        btnAceitar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aceitarConvite(convite);
            }
        });

        TextView btnRecusar = new TextView(this);
        btnRecusar.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        btnRecusar.setText("Recusar");
        btnRecusar.setTextSize(14);
        btnRecusar.setTextColor(ContextCompat.getColor(this, R.color.white));
        btnRecusar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        btnRecusar.setPadding(16, 12, 16, 12);
        btnRecusar.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        btnRecusar.setClickable(true);
        btnRecusar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recusarConvite(convite);
            }
        });

        layoutBotoes.addView(btnAceitar);
        layoutBotoes.addView(btnRecusar);

        layoutConvite.addView(textUsuario);
        layoutConvite.addView(textData);
        layoutConvite.addView(layoutBotoes);

        linearLayoutConvites.addView(layoutConvite);
    }

    private void aceitarConvite(Convite convite) {
        new Thread(() -> {
            try {
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("usuId", convite.getUsuId());
                jsonBody.put("contatoId", convite.getDestId());

                String response = HttpConnection.post("contacts", jsonBody.toString(), activity_convite.this);

                jsonBody.put("usuId", convite.getDestId());
                jsonBody.put("contatoId", convite.getUsuId());

                String response2 = HttpConnection.post("contacts", jsonBody.toString(), activity_convite.this);

                runOnUiThread(() -> {
                    if (response != null && (response.contains("Contato adicionado") || response.contains("aceito"))) {
                        Toast.makeText(this, "Convite aceito! Agora você é amigo de " + convite.getUsuId(), Toast.LENGTH_SHORT).show();
                        listaConvites.remove(convite);
                        carregarConvitesPendentes();
                    } else {
                        Toast.makeText(this, "Erro ao aceitar convite", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Erro ao processar convite", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void recusarConvite(Convite convite) {
        new Thread(() -> {
            try {
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("invitationId", convite.getId());
                jsonBody.put("action", "REJECT");

                String response = HttpConnection.post("invitations/reject", jsonBody.toString(), activity_convite.this);

                runOnUiThread(() -> {
                    if (response != null && (response.contains("sucesso") || response.contains("recusado"))) {
                        Toast.makeText(this, "Convite recusado", Toast.LENGTH_SHORT).show();
                        listaConvites.remove(convite);
                        carregarConvitesPendentes();
                    } else {
                        Toast.makeText(this, "Erro ao recusar convite", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Erro ao processar convite", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void pesquisarConvites(String termo) {
        for (int i = 0; i < linearLayoutConvites.getChildCount(); i++) {
            View conviteView = linearLayoutConvites.getChildAt(i);
            if (conviteView instanceof LinearLayout) {
                LinearLayout layoutConvite = (LinearLayout) conviteView;
                TextView textUsuario = (TextView) layoutConvite.getChildAt(0);

                String textoConvite = textUsuario.getText().toString().toLowerCase();
                if (textoConvite.contains(termo.toLowerCase())) {
                    conviteView.setVisibility(View.VISIBLE);
                } else {
                    conviteView.setVisibility(View.GONE);
                }
            }
        }
    }

    private void mostrarMensagemVazia(String mensagem) {
        linearLayoutConvites.removeAllViews();

        TextView textVazio = new TextView(this);
        textVazio.setText(mensagem);
        textVazio.setTextSize(16);
        textVazio.setTextColor(ContextCompat.getColor(this, R.color.black));
        textVazio.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textVazio.setPadding(0, 50, 0, 0);

        linearLayoutConvites.addView(textVazio);
    }

    private String formatarData(String data) {
        try {
            if (data.contains("T")) {
                return data.split("T")[0];
            }
            return data;
        } catch (Exception e) {
            return data;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (int i = 0; i < linearLayoutConvites.getChildCount(); i++) {
            linearLayoutConvites.getChildAt(i).setVisibility(View.VISIBLE);
        }
        editTextPesquisa.setText("");
    }
}

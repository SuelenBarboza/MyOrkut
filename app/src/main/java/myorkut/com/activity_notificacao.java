package myorkut.com;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class activity_notificacao extends AppCompatActivity {

    private EditText editTextPesquisa;
    private ImageView imageViewLupa;
    private ImageView imageViewHome;
    private ImageView imageViewNovaPublicacao;
    private ImageView imageViewPerfil;
    private LinearLayout linearLayoutNotificacoes;
    private String usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificacao);

        initViews();
        setupClickListeners();

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        usuarioId = prefs.getString("usuario_logado", null);

        carregarNotificacoesDaAPI();
       //CarregarNotificacaoMensagem();
        conectarRealtime();
    }

    private void initViews() {
        editTextPesquisa = findViewById(R.id.editTextPesquisa);
        imageViewLupa = findViewById(R.id.imageViewLupa);
        imageViewHome = findViewById(R.id.imageViewHome);
        imageViewNovaPublicacao = findViewById(R.id.imageViewNovaPublicacao);
        imageViewPerfil = findViewById(R.id.imageViewPerfil);
        linearLayoutNotificacoes = findViewById(R.id.linearLayoutNotificacoes);
    }

    private void setupClickListeners() {
        imageViewLupa.setOnClickListener(v -> {
            String pesquisa = editTextPesquisa.getText().toString().trim();
            if (pesquisa.isEmpty()) {
                Toast.makeText(activity_notificacao.this, "Digite algo para pesquisar", Toast.LENGTH_SHORT).show();
            } else {
                pesquisarNotificacoes(pesquisa);
            }
        });

        imageViewHome.setOnClickListener(v -> {
            startActivity(new Intent(activity_notificacao.this, activity_feed.class));
            finish();
        });

        imageViewNovaPublicacao.setOnClickListener(v ->
                startActivity(new Intent(activity_notificacao.this, NovaPublicacaoActivity.class)));

        imageViewPerfil.setOnClickListener(v ->
                Toast.makeText(activity_notificacao.this, "Perfil", Toast.LENGTH_SHORT).show());
    }

    private void carregarNotificacoesDaAPI() {
        linearLayoutNotificacoes.removeAllViews();

        if (usuarioId == null) {
            runOnUiThread(() -> Toast.makeText(this, "Usuário não encontrado", Toast.LENGTH_SHORT).show());
            return;
        }

        new Thread(() -> {
            try {
                String response = HttpConnection.get("invitations/" + usuarioId, activity_notificacao.this);

                if (response == null || response.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(this, "Nenhuma notificação encontrada", Toast.LENGTH_SHORT).show());
                    return;
                }

                JSONArray jsonArray = new JSONArray(response);

                runOnUiThread(() -> {
                    try {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);

                            String tipo = obj.optString("tipo", "CN");
                            String remetenteNome = obj.optString("remetenteNome", "Usuário");
                            String data = obj.getString("data");

                            ZonedDateTime zdt = ZonedDateTime.parse(data);
                            ZonedDateTime zdtBrazil = zdt.withZoneSameInstant(ZoneId.of("America/Sao_Paulo"));
                            String horaBrasil = zdtBrazil.format(DateTimeFormatter.ofPattern("HH:mm"));

                            String texto = gerarTexto(tipo, remetenteNome);

                            adicionarNotificacao(remetenteNome, texto, horaBrasil, tipo);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }

    private void CarregarNotificacaoMensagem(){
        new Thread(() -> {
            try {
                String response = HttpConnection.get("notifications/" + usuarioId, activity_notificacao.this);

                if (response == null || response.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(this, "Nenhuma notificação encontrada", Toast.LENGTH_SHORT).show());
                    return;
                }

                JSONArray jsonArray = new JSONArray(response);

                runOnUiThread(() -> {
                    try {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);

                            String tipo = obj.optString("notifications", "");
                            String remetenteNome = obj.optString("nomeUsuario", "Usuário");
                            String data = obj.getString("data");

                            ZonedDateTime zdt = ZonedDateTime.parse(data);
                            ZonedDateTime zdtBrazil = zdt.withZoneSameInstant(ZoneId.of("America/Sao_Paulo"));
                            String horaBrasil = zdtBrazil.format(DateTimeFormatter.ofPattern("HH:mm"));

                            String texto = gerarTexto(tipo, remetenteNome);

                            adicionarNotificacao(remetenteNome, texto, horaBrasil, tipo);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }

    private String gerarTexto(String tipo, String remetente) {
        switch (tipo) {
            case "CN":
                return remetente + " te enviou um convite";
            case "COMENTARIO":
                return remetente + " comentou na sua publicação";
            case "MENSAGEM":
                return "Nova mensagem" + remetente;
            default:
                return "Nova notificação";
        }
    }

    private void conectarRealtime() {
        try {
            usuarioId = usuarioId.replace("\"", "");

            WebSocket ws = new WebSocketFactory()
                    .createSocket("ws://192.168.0.25:3000/" + usuarioId)
                    .connect();

            ws.addListener(new WebSocketAdapter() {
                @Override
                public void onTextMessage(WebSocket websocket, String message) {
                    try {
                        JSONObject obj = new JSONObject(message);

                        if (obj.getString("type").equals("nova_notificacao")) {
                            JSONObject data = obj.getJSONObject("data");

                            String nome = data.optString("remetenteNome", "Usuário");
                            String texto = data.optString("mensagem", "");
                            String tipo = data.optString("tipo", "CN");
                            String tempo = data.optString("data", "");

                            runOnUiThread(() -> adicionarNotificacao(nome, texto, tempo, tipo));
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void adicionarNotificacao(String usuarioId, String texto, String tempo, String tipo) {
        LinearLayout layoutNotificacao = new LinearLayout(this);
        layoutNotificacao.setOrientation(LinearLayout.HORIZONTAL);
        layoutNotificacao.setPadding(16, 16, 16, 16);
        layoutNotificacao.setBackground(ContextCompat.getDrawable(this, R.drawable.edittext_background));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 8);
        layoutNotificacao.setLayoutParams(params);

        ImageView imageViewIcone = new ImageView(this);
        imageViewIcone.setLayoutParams(new LinearLayout.LayoutParams(40, 40));
        imageViewIcone.setPadding(0, 0, 12, 0);

        switch (tipo) {
            case "COMENTARIO":
                imageViewIcone.setImageResource(android.R.drawable.ic_menu_edit);
                break;
            case "CN":
                imageViewIcone.setImageResource(android.R.drawable.ic_menu_myplaces);
                break;
            case "MENSAGEM":
                imageViewIcone.setImageResource(android.R.drawable.ic_dialog_email);
                break;
        }

        LinearLayout layoutTexto = new LinearLayout(this);
        layoutTexto.setOrientation(LinearLayout.VERTICAL);
        layoutTexto.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));

        TextView textNotificacao = new TextView(this);
        textNotificacao.setText(texto);
        textNotificacao.setTextSize(14);
        textNotificacao.setTextColor(ContextCompat.getColor(this, R.color.black));
        textNotificacao.setTypeface(null, Typeface.NORMAL);

        TextView textTempo = new TextView(this);
        textTempo.setText(tempo);
        textTempo.setTextSize(10);
        textTempo.setTextColor(ContextCompat.getColor(this, R.color.black));

        layoutTexto.addView(textNotificacao);
        layoutTexto.addView(textTempo);

        layoutNotificacao.addView(imageViewIcone);
        layoutNotificacao.addView(layoutTexto);

        layoutNotificacao.setOnClickListener(v -> onNotificacaoClick(usuarioId, tipo));

        linearLayoutNotificacoes.addView(layoutNotificacao);
    }

    private void onNotificacaoClick(String usuarioId, String tipo) {
        switch (tipo) {
            case "COMENTARIO":
                startActivity(new Intent(this, activity_feed.class));
                break;
            case "CN":
                startActivity(new Intent(this, activity_convite.class));
                break;
            case "MENSAGEM":
                Intent intentChat = new Intent(this, activity_mensagem.class);
                intentChat.putExtra("CONTATO_ID", usuarioId);
                startActivity(intentChat);
                break;
        }
    }

    private void pesquisarNotificacoes(String termo) {
        for (int i = 0; i < linearLayoutNotificacoes.getChildCount(); i++) {
            View notificacaoView = linearLayoutNotificacoes.getChildAt(i);
            LinearLayout layoutTexto = (LinearLayout) ((LinearLayout) notificacaoView).getChildAt(1);
            TextView textNotificacao = (TextView) layoutTexto.getChildAt(0);

            String textoNotificacao = textNotificacao.getText().toString().toLowerCase();
            notificacaoView.setVisibility(textoNotificacao.contains(termo.toLowerCase())
                    ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (int i = 0; i < linearLayoutNotificacoes.getChildCount(); i++) {
            linearLayoutNotificacoes.getChildAt(i).setVisibility(View.VISIBLE);
        }
        editTextPesquisa.setText("");
    }
}

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

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class activity_mensagem extends AppCompatActivity {

    private static final String TAG = "ChatRealTime";
    // base do websocket — o ID do usuário será acrescentado dinamicamente
    private static final String WS_BASE = "ws://192.168.0.102:3000/messages/";

    private TextView textViewNomeContato;
    private EditText editTextMensagem;
    private ImageView imageViewEnviar, imageViewVoltar, imageViewHome, imageViewPerfil, imageViewAdicionarAmigos;
    private RecyclerView recyclerViewMensagens;
    private MensagemAdapter mensagemAdapter;
    private final List<Mensagem> listaMensagens = new ArrayList<>();

    private String meuId;
    private String contatoId;
    private String contatoNome;
    private WebSocket webSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensagem);

        Intent intent = getIntent();
        contatoId = intent.getStringExtra("CONTATO_ID");
        contatoNome = intent.getStringExtra("CONTATO_NOME");

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        meuId = prefs.getString("usuario_logado", null);

        if (meuId == null || meuId.isEmpty()) {
            Toast.makeText(this, "Erro: Usuário não logado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (contatoId == null || contatoNome == null) {
            Toast.makeText(this, "Erro ao abrir conversa", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        meuId = meuId.replace("\"", "");
        contatoId = contatoId.replace("\"", "");

        initViews();
        setupRecyclerView();
        setupClickListeners();

        carregarMensagensAntigas();
        conectarWebSocket();
    }

    private void initViews() {
        textViewNomeContato = findViewById(R.id.textViewNomeContato);
        editTextMensagem = findViewById(R.id.editTextMensagem);
        imageViewEnviar = findViewById(R.id.imageViewEnviar);
        imageViewVoltar = findViewById(R.id.imageViewVoltar);
        imageViewAdicionarAmigos = findViewById(R.id.imageViewAdicionarAmigos);
        imageViewHome = findViewById(R.id.imageViewHome);
        imageViewPerfil = findViewById(R.id.imageViewPerfil);
        recyclerViewMensagens = findViewById(R.id.recyclerViewMensagens);

        textViewNomeContato.setText(contatoNome);
    }

    private void setupRecyclerView() {
        mensagemAdapter = new MensagemAdapter(listaMensagens);
        recyclerViewMensagens.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMensagens.setAdapter(mensagemAdapter);
    }

    private void setupClickListeners() {
        imageViewEnviar.setOnClickListener(v -> enviarMensagem());
        imageViewVoltar.setOnClickListener(v -> finish());
        imageViewHome.setOnClickListener(v -> {
            startActivity(new Intent(this, activity_feed.class));
            finish();
        });
        imageViewPerfil.setOnClickListener(v -> Toast.makeText(this, "Perfil", Toast.LENGTH_SHORT).show());
        imageViewAdicionarAmigos.setOnClickListener(v -> {
            startActivity(new Intent(this, Activity_Grupo.class));
        });
    }

    private void carregarMensagensAntigas() {
        new Thread(() -> {
            try {
                String json = HttpConnection.get("messages/" + meuId + "/" + contatoId, activity_mensagem.this);

                org.json.JSONArray array = new org.json.JSONArray(json);

                runOnUiThread(() -> {
                    listaMensagens.clear();
                    for (int i = 0; i < array.length(); i++) {
                        try {
                            org.json.JSONObject obj = array.getJSONObject(i);

                            String texto = obj.optString("DESCRICAO", "");
                            String deQuem = obj.optString("USUID", "");
                            String nomeUsuario = obj.optString("NOMEUSUARIO", deQuem.equals(meuId) ? "Você" : contatoNome);
                            boolean enviadaPorMim = deQuem.equals(meuId);
                            long timestamp = parseDate(obj.optString("DATA", ""));

                            Mensagem msg = new Mensagem(
                                    texto,
                                    nomeUsuario,
                                    timestamp,
                                    enviadaPorMim
                            );
                            listaMensagens.add(msg);

                        } catch (Exception e) {
                            Log.e(TAG, "Erro ao parsear mensagem " + i, e);
                        }
                    }
                    mensagemAdapter.notifyDataSetChanged();
                    scrollToBottom();
                });

            } catch (Exception e) {
                Log.e(TAG, "Erro ao carregar mensagens antigas", e);
                runOnUiThread(() -> Toast.makeText(this, "Erro ao carregar mensagens", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webSocket != null && webSocket.isOpen()) {
            try {
                webSocket.disconnect();
            } catch (Exception ignored) {}
            webSocket = null;
        }
    }

    private void conectarWebSocket() {
        try {

            if (webSocket != null) {
                try { webSocket.disconnect(); } catch (Exception ignored) {}
                webSocket = null;
            }

            String wsUrl = WS_BASE + meuId;
            Log.d(TAG, "Conectando WS em: " + wsUrl);

            webSocket = new WebSocketFactory()
                    .setConnectionTimeout(8000)
                    .createSocket(wsUrl)
                    .addListener(new WebSocketAdapter() {

                        @Override
                        public void onConnected(WebSocket websocket, java.util.Map<String, List<String>> headers) {
                            Log.d(TAG, "WebSocket conectado!");
                        }

                        @Override
                        public void onTextMessage(WebSocket ws, String text) {
                            Log.d(TAG, "Mensagem recebida WS: " + text);
                            try {
                                JSONObject json = new JSONObject(text);

                                String type = json.optString("type", "");
                                if ("connected".equals(type)) {
                                    // mensagem de handshake — ignora ou loga
                                    Log.d(TAG, "Handshake recebido do servidor.");
                                    return;
                                }

                                String de = json.has("from") ? json.optString("from") : json.optString("USUID");
                                String para = json.has("to") ? json.optString("to") : json.optString("CONTATOID");
                                String msgTexto = json.has("descricao") ? json.optString("descricao") : json.optString("DESCRICAO");
                                String dataStr = json.has("data") ? json.optString("data") : json.optString("DATA");

                                boolean daConversa = (de != null && para != null) && (
                                        (de.equals(meuId) && para.equals(contatoId)) ||
                                                (de.equals(contatoId) && para.equals(meuId))
                                );

                                if (daConversa) {
                                    boolean enviadaPorMim = de.equals(meuId);
                                    long timestamp = parseDate(dataStr);

                                    runOnUiThread(() -> {
                                        Mensagem nova = new Mensagem(
                                                msgTexto != null ? msgTexto : "",
                                                enviadaPorMim ? "Você" : contatoNome,
                                                timestamp,
                                                enviadaPorMim
                                        );
                                        mensagemAdapter.adicionarMensagem(nova);
                                        scrollToBottom();
                                    });
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Erro ao processar mensagem do WebSocket", e);
                            }
                        }

                        @Override
                        public void onDisconnected(WebSocket websocket,
                                                   com.neovisionaries.ws.client.WebSocketFrame serverCloseFrame,
                                                   com.neovisionaries.ws.client.WebSocketFrame clientCloseFrame,
                                                   boolean closedByServer) {
                            Log.d(TAG, "WebSocket desconectado. Tentando reconectar...");
                            reconectarWebSocket();
                        }

                        @Override
                        public void onError(WebSocket websocket, com.neovisionaries.ws.client.WebSocketException cause) {
                            Log.e(TAG, "Erro WebSocket: " + cause.getMessage());
                            reconectarWebSocket();
                        }

                    })
                    .connectAsynchronously();

        } catch (Exception e) {
            Log.e(TAG, "Falha ao conectar WebSocket", e);
        }
    }

    private void reconectarWebSocket() {
        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {}
            if (webSocket == null || !webSocket.isOpen()) {
                conectarWebSocket();
            }
        }).start();
    }

    private void enviarMensagem() {
        String texto = editTextMensagem.getText().toString().trim();
        if (texto.isEmpty()) {
            Toast.makeText(this, "Digite uma mensagem", Toast.LENGTH_SHORT).show();
            return;
        }

        Mensagem msgLocal = new Mensagem(texto, "Você", System.currentTimeMillis(), true);
        mensagemAdapter.adicionarMensagem(msgLocal);
        scrollToBottom();
        editTextMensagem.setText("");

        meuId = meuId.replace("\"", "");
        contatoId = contatoId.replace("\"", "");

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("usuId", meuId);
            jsonObject.put("contatoId", contatoId);
            jsonObject.put("descricao", texto);
            jsonObject.put("status", "E");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao montar JSON", e);
            return;
        }

        new Thread(() -> {
            try {
                String resposta = HttpConnection.post("messages", jsonObject.toString(), activity_mensagem.this);
                JSONObject respJson = new JSONObject(resposta);

                if (respJson.has("result")) {
                    JSONObject result = respJson.optJSONObject("result");
                    if (result != null && result.has("id")) {
                        msgLocal.setId(result.optString("id"));
                    }
                } else if (respJson.has("id")) {
                    msgLocal.setId(respJson.optString("id"));
                }
            } catch (Exception e) {
                Log.e(TAG, "Erro ao enviar mensagem", e);
                runOnUiThread(() -> Toast.makeText(activity_mensagem.this, "Erro ao enviar mensagem", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private long parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return System.currentTimeMillis();
        try {

            String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
            SimpleDateFormat sdfUTC = new SimpleDateFormat(pattern, Locale.US);
            sdfUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sdfUTC.parse(dateStr);
            if (date == null) return System.currentTimeMillis();
            return date.getTime();
        } catch (Exception e) {
            try {

                String pattern2 = "yyyy-MM-dd'T'HH:mm:ss'Z'";
                SimpleDateFormat sdfUTC2 = new SimpleDateFormat(pattern2, Locale.US);
                sdfUTC2.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = sdfUTC2.parse(dateStr);
                if (date == null) return System.currentTimeMillis();
                return date.getTime();
            } catch (Exception ex) {
                return System.currentTimeMillis();
            }
        }
    }

    private void scrollToBottom() {
        if (mensagemAdapter.getItemCount() > 0) {
            recyclerViewMensagens.post(() ->
                    recyclerViewMensagens.smoothScrollToPosition(mensagemAdapter.getItemCount() - 1)
            );
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocket != null && webSocket.isOpen()) {
            try { webSocket.disconnect(); } catch (Exception ignored) {}
        }
        webSocket = null;
    }
}

package myorkut.com;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MensagemAdapter extends RecyclerView.Adapter<MensagemAdapter.ViewHolder> {

    private final List<Mensagem> listaMensagens;

    public MensagemAdapter(List<Mensagem> listaMensagens) {
        this.listaMensagens = listaMensagens;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mensagem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Mensagem atual = listaMensagens.get(position);
        Mensagem anterior = position > 0 ? listaMensagens.get(position - 1) : null;

        holder.bind(atual, anterior);
    }


    @Override
    public int getItemCount() {
        return listaMensagens.size();
    }

    // Adiciona mensagem e atualiza RecyclerView
    public void adicionarMensagem(Mensagem mensagem) {
        listaMensagens.add(mensagem);
        notifyItemInserted(listaMensagens.size() - 1);
    }

    // ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout layoutMensagem;
        private final LinearLayout layoutBalao;
        private final TextView textMensagem;
        private final TextView textHora;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutMensagem = itemView.findViewById(R.id.layoutMensagem);
            layoutBalao = itemView.findViewById(R.id.layoutBalao);
            textMensagem = itemView.findViewById(R.id.textMensagem);
            textHora = itemView.findViewById(R.id.textHora);
        }

        public void bind(Mensagem mensagem, Mensagem mensagemAnterior) {

            textMensagem.setText(mensagem.getTexto());
            textHora.setText(formatarHora(mensagem.getTimestamp()));

            if (mensagem.isEnviadaPorMim()) {
                layoutMensagem.setGravity(Gravity.END);
                layoutBalao.setBackgroundResource(R.drawable.balao_mensagem_enviada);
            } else {
                layoutMensagem.setGravity(Gravity.START);
                layoutBalao.setBackgroundResource(R.drawable.balao_mensagem_recebida);
            }

            LinearLayout.LayoutParams params =
                    (LinearLayout.LayoutParams) layoutBalao.getLayoutParams();

            if (mensagemAnterior == null) {
                params.topMargin = 20;
            } else {
                if (mensagem.isEnviadaPorMim() != mensagemAnterior.isEnviadaPorMim()) {
                    params.topMargin = 40;
                } else {
                    params.topMargin = 10;
                }
            }

            layoutBalao.setLayoutParams(params);
        }


        private String formatarHora(long timestamp) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                return sdf.format(new Date(timestamp));
            } catch (Exception e) {
                return "--:--";
            }
        }
    }
}

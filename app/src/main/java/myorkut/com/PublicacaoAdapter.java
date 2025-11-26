package myorkut.com;

import android.content.Intent;
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

public class PublicacaoAdapter extends RecyclerView.Adapter<PublicacaoAdapter.ViewHolder> {

    private List<PublicacaoComUsuario> listaPublicacoes;

    public PublicacaoAdapter(List<PublicacaoComUsuario> listaPublicacoes) {
        this.listaPublicacoes = listaPublicacoes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_item_publicacao, parent, false);
        return new ViewHolder(view);
    }
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PublicacaoComUsuario publicacao = listaPublicacoes.get(position);
        holder.bind(publicacao);
    }

    @Override
    public int getItemCount() {
        return listaPublicacoes.size();
    }

    public void atualizarLista(List<PublicacaoComUsuario> novaLista) {
        this.listaPublicacoes = novaLista;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textApelido;
        private final TextView textData;
        private final TextView textPublicacao;
        private final TextView textComentariosCount;
        private final LinearLayout layoutComentario;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textApelido = itemView.findViewById(R.id.textApelido);
            textData = itemView.findViewById(R.id.textData);
            textPublicacao = itemView.findViewById(R.id.textPublicacao);
            textComentariosCount = itemView.findViewById(R.id.textComentariosCount);
            layoutComentario = itemView.findViewById(R.id.layoutComentario);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Ação quando clicar na publicação
                }
            });

            layoutComentario.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PublicacaoComUsuario publicacao = (PublicacaoComUsuario) v.getTag();
                    if (publicacao != null) {
                        abrirComentarios(publicacao);
                    }
                }
            });
        }

        public void bind(PublicacaoComUsuario publicacao) {
            textApelido.setText(publicacao.getUsu_nome());
            textData.setText(formatarData(publicacao.getPub_data()));
            textPublicacao.setText(publicacao.getPub_texto());
            //contador do banco fixo com exemplo
            textComentariosCount.setText("2"); // TODO: Buscar do banco

            layoutComentario.setTag(publicacao);
        }

        private String formatarData(String data) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault());
                Date date = inputFormat.parse(data);
                return outputFormat.format(date);
            } catch (Exception e) {
                return data;
            }
        }

        private void abrirComentarios(PublicacaoComUsuario publicacao) {
            Intent intent = new Intent(itemView.getContext(), activity_comentarios.class);
            intent.putExtra("PUB_ID", publicacao.getPub_id());
            intent.putExtra("PUB_USU_ID", publicacao.getUsu_id());
            intent.putExtra("PUB_TEXTO", publicacao.getPub_texto());
            intent.putExtra("PUB_DATA", publicacao.getPub_data());
            intent.putExtra("PUB_STATUS", publicacao.getPub_status());
            intent.putExtra("PUB_USU_NOME", publicacao.getUsu_nome());
            itemView.getContext().startActivity(intent);
        }
    }
}

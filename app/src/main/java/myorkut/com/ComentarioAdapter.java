package myorkut.com;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ComentarioAdapter extends RecyclerView.Adapter<ComentarioAdapter.ViewHolder> {

    private List<Comentario> listaComentarios;

    public ComentarioAdapter(List<Comentario> listaComentarios) {
        this.listaComentarios = listaComentarios;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comentario, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comentario comentario = listaComentarios.get(position);
        holder.bind(comentario);
    }

    @Override
    public int getItemCount() {
        return listaComentarios.size();
    }

    public void adicionarComentario(Comentario comentario) {
        listaComentarios.add(comentario);
        notifyItemInserted(listaComentarios.size() - 1);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textAutor;
        private final TextView textData;
        private final TextView textComentario;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textAutor = itemView.findViewById(R.id.textAutorComentario);
            textData = itemView.findViewById(R.id.textDataComentario);
            textComentario = itemView.findViewById(R.id.textComentario);
        }

        public void bind(Comentario comentario) {
            textAutor.setText(comentario.getUsu_nome());
            textData.setText(formatarData(comentario.getCom_data()));
            textComentario.setText(comentario.getCom_texto());
        }

        private String formatarData(String data) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
                Date date = inputFormat.parse(data);
                return outputFormat.format(date);
            } catch (Exception e) {
                return data;
            }
        }
    }
}

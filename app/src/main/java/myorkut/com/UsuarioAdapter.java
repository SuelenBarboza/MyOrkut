package myorkut.com;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.ViewHolder> {

    private List<Usuario> listaUsuarios;
    private OnUsuarioClickListener listener;

    public interface OnUsuarioClickListener {
        void onUsuarioClick(Usuario usuario);
    }

    public UsuarioAdapter(List<Usuario> listaUsuarios, OnUsuarioClickListener listener) {
        this.listaUsuarios = listaUsuarios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Usuario usuario = listaUsuarios.get(position);
        holder.bind(usuario);
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public void atualizarLista(List<Usuario> novaLista) {
        this.listaUsuarios = novaLista;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textApelido;
//        private TextView textNome;

        public ViewHolder(@NonNull View itemView, TextView textApelido) {
            super(itemView);
            this.textApelido = textApelido;
        }

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textApelido = itemView.findViewById(R.id.textApelidoUsuario);
//            textNome = itemView.findViewById(R.id.editTextNome);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onUsuarioClick(listaUsuarios.get(position));
                    }
                }
            });
        }

        public void bind(Usuario usuario)
        {
//            textNome.setText(usuario.getUsu_nome());
            textApelido.setText("@" + usuario.getUsu_email());
        }
    }
}

package mobile.passworld.activity.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import mobile.passworld.R;
import mobile.passworld.data.PasswordDTO;

public class PasswordAdapter extends ListAdapter<PasswordDTO, PasswordAdapter.ViewHolder> {

    // Listener para clicks en items
    public interface OnItemClickListener {
        void onItemClick(PasswordDTO password);
    }

    private OnItemClickListener listener;

    // Constructor que recibe el DiffCallback
    public PasswordAdapter() {
        super(new PasswordDiffCallback());
    }

    // Método para asignar listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_password, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PasswordDTO dto = getItem(position);
        holder.desc.setText(dto.getDescription());
        if (dto.getUsername() == null || dto.getUsername().isEmpty()) {
            holder.username.setText(R.string.no_username);
        } else {
            holder.username.setText(dto.getUsername());
        }

        // Controla la visibilidad del icono de advertencia
        if (dto.isCompromised() || dto.isWeak() || dto.isDuplicate() || dto.isUrlUnsafe()) {
            holder.securityWarningIcon.setVisibility(View.VISIBLE);
        } else {
            holder.securityWarningIcon.setVisibility(View.GONE);
        }

        // Asignamos el click listener al item completo
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(dto);
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView desc, username;
        ImageView securityWarningIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            desc = itemView.findViewById(R.id.password_desc);
            username = itemView.findViewById(R.id.password_username);
            securityWarningIcon = itemView.findViewById(R.id.security_warning_icon);
        }
    }

    // DiffCallback como clase estática
    private static class PasswordDiffCallback extends DiffUtil.ItemCallback<PasswordDTO> {
        @Override
        public boolean areItemsTheSame(@NonNull PasswordDTO oldItem, @NonNull PasswordDTO newItem) {
            // Comprueba si son el mismo elemento por identidad
            // Usa el identificador único de tus PasswordDTO
            return oldItem.getIdFb().equals(newItem.getIdFb());
        }

        @Override
        public boolean areContentsTheSame(@NonNull PasswordDTO oldItem, @NonNull PasswordDTO newItem) {
            // Comprueba si el contenido es igual
            return Objects.equals(oldItem.getDescription(), newItem.getDescription()) &&
                    Objects.equals(oldItem.getUsername(), newItem.getUsername()) &&
                    Objects.equals(oldItem.getUrl(), newItem.getUrl()) &&
                    Objects.equals(oldItem.getPassword(), newItem.getPassword()) &&
                    oldItem.getIdFb().equals(newItem.getIdFb()) &&
                    oldItem.isCompromised() == newItem.isCompromised() &&
                    oldItem.isWeak() == newItem.isWeak() &&
                    oldItem.isDuplicate() == newItem.isDuplicate() &&
                    oldItem.isUrlUnsafe() == newItem.isUrlUnsafe();
        }
    }
}
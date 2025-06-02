package mobile.passworld.activity.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import mobile.passworld.R;
import mobile.passworld.data.PasswordDTO;

public class PasswordAdapter extends RecyclerView.Adapter<PasswordAdapter.ViewHolder> {
    private List<PasswordDTO> passwords = new ArrayList<>();

    // Listener para clicks en items
    public interface OnItemClickListener {
        void onItemClick(PasswordDTO password);
    }

    private OnItemClickListener listener;

    // Método para asignar listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setPasswords(List<PasswordDTO> list) {
        passwords = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return passwords.size();
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
        PasswordDTO dto = passwords.get(position);
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
}
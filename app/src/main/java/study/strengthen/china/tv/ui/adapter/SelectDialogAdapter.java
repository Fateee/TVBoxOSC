package study.strengthen.china.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import study.strengthen.china.tv.R;
import study.strengthen.china.tv.ui.tv.widget.SimpleCornerTextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SelectDialogAdapter<T> extends ListAdapter<T, SelectDialogAdapter.SelectViewHolder> {

    class SelectViewHolder extends RecyclerView.ViewHolder {

        public SelectViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
        }
    }

    public interface SelectDialogInterface<T> {
        void click(T value, int pos);

        String getDisplay(T val);
    }


    public static DiffUtil.ItemCallback<String> stringDiff = new DiffUtil.ItemCallback<String>() {

        @Override
        public boolean areItemsTheSame(@NonNull @NotNull String oldItem, @NonNull @NotNull String newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull @NotNull String oldItem, @NonNull @NotNull String newItem) {
            return oldItem.equals(newItem);
        }
    };


    private ArrayList<T> data = new ArrayList<>();

    private int select = 0;

    private SelectDialogInterface dialogInterface = null;

    public SelectDialogAdapter(SelectDialogInterface dialogInterface, DiffUtil.ItemCallback diffCallback) {
        super(diffCallback);
        this.dialogInterface = dialogInterface;
    }

    public void setData(List<T> newData, int defaultSelect) {
        data.clear();
        data.addAll(newData);
        select = defaultSelect;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    @Override
    public SelectDialogAdapter.SelectViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new SelectDialogAdapter.SelectViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dialog_select, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull SelectDialogAdapter.SelectViewHolder holder, int position) {
        T value = data.get(position);
        String name = dialogInterface.getDisplay(value);
        if (holder.itemView instanceof SimpleCornerTextView) {
            if (position == select) {
//            name = "√ " + name;
                ((SimpleCornerTextView) holder.itemView.findViewById(R.id.tvName)).setBackgroundResColor(R.color.main_color);
            } else {
                ((SimpleCornerTextView) holder.itemView.findViewById(R.id.tvName)).setBackgroundResColor(R.color.main_normal_color);
            }
        }
        ((TextView) holder.itemView.findViewById(R.id.tvName)).setText(name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == select)
                    return;
                notifyItemChanged(select);
                select = position;
                notifyItemChanged(select);
                dialogInterface.click(value, position);
            }
        });
    }
}

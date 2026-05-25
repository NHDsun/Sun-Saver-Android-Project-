package com.example.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.R;
import com.example.models.Transaction;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

/**
 * Adapter quản lý việc hiển thị danh sách các Giao dịch trong RecyclerView cho Sun Saver.
 * Tương thích 100% với MainActivity và item_transaction.xml.
 */
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private Context context;
    private List<Transaction> transactionList;
    private OnTransactionLongClickListener longClickListener;

    // Giao diện callback khi người dùng nhấn giữ item (Sửa/Xoá)
    public interface OnTransactionLongClickListener {
        void onTransactionLongClick(Transaction transaction, int position);
    }

    public TransactionAdapter(Context context, List<Transaction> transactionList, OnTransactionLongClickListener longClickListener) {
        this.context = context;
        this.transactionList = transactionList;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        // 1. Tên giao dịch
        holder.tvTitle.setText(transaction.getTitle());

        // 2. Danh mục & Ngày tháng
        holder.tvCategory.setText(transaction.getCategory());
        holder.tvDate.setText(transaction.getDate());

        // 3. Định dạng tiền tệ VND (ví dụ: 150.000 ₫)
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("#,###", symbols);
        String formattedAmount = decimalFormat.format(transaction.getAmount()) + " ₫";

        // 4. Phân biệt màu sắc & Icon vòng tròn giữa Thu nhập (Income) và Chi tiêu (Expense)
        if ("Income".equalsIgnoreCase(transaction.getType())) {
            holder.tvAmount.setText("+" + formattedAmount);
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.color_income));
            holder.layoutIconHolder.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.color_income_light)
            ));
        } else {
            holder.tvAmount.setText("-" + formattedAmount);
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.color_expense));
            holder.layoutIconHolder.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.color_expense_light)
            ));
        }

        // 5. Gán Emoji thương hiệu cho các nhóm danh mục
        String category = transaction.getCategory();
        String emoji = "💰"; // Mặc định
        if ("Ăn uống".equalsIgnoreCase(category)) {
            emoji = "🍔";
        } else if ("Học tập".equalsIgnoreCase(category)) {
            emoji = "📚";
        } else if ("Giải trí".equalsIgnoreCase(category)) {
            emoji = "🎮";
        } else if ("Di chuyển".equalsIgnoreCase(category)) {
            emoji = "🚗";
        } else if ("Khác".equalsIgnoreCase(category)) {
            emoji = "🪙";
        }
        holder.tvEmoji.setText(emoji);

        // 6. Nhấn giữ lâu chuyển đổi Sửa/Xoá
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (longClickListener != null) {
                    longClickListener.onTransactionLongClick(transaction, holder.getAdapterPosition());
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactionList != null ? transactionList.size() : 0;
    }

    /**
     * Đồng bộ nâng cao danh sách khi có cập nhật SQL
     */
    public void updateList(List<Transaction> newList) {
        this.transactionList = newList;
        notifyDataSetChanged();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvDate, tvAmount, tvEmoji;
        LinearLayout layoutIconHolder;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_item_title);
            tvCategory = itemView.findViewById(R.id.tv_item_category);
            tvDate = itemView.findViewById(R.id.tv_item_date);
            tvAmount = itemView.findViewById(R.id.tv_item_amount);
            tvEmoji = itemView.findViewById(R.id.tv_category_emoji);
            layoutIconHolder = itemView.findViewById(R.id.layout_category_icon_holder);
        }
    }
}

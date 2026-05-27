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
        holder.tvDate.setText(getReadableDate(transaction.getDate()));

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

        // 5. Gán Emoji thương hiệu tương ứng với bộ nhiều lựa chọn danh mục mới
        String category = transaction.getCategory();
        String emoji = "💰"; // Mặc định
        if (category != null) {
            if (category.contains("Ăn uống")) {
                emoji = "🍔";
            } else if (category.contains("Học tập")) {
                emoji = "📚";
            } else if (category.contains("Giải trí")) {
                emoji = "🎮";
            } else if (category.contains("Di chuyển")) {
                emoji = "🚗";
            } else if (category.contains("Mua sắm")) {
                emoji = "🛍️";
            } else if (category.contains("Điện nước")) {
                emoji = "⚡";
            } else if (category.contains("Sức khỏe")) {
                emoji = "💊";
            } else if (category.contains("Nhà cửa")) {
                emoji = "🏠";
            } else if (category.contains("Lương")) {
                emoji = "💵";
            } else if (category.contains("Quà")) {
                emoji = "🎁";
            } else if (category.contains("Kinh doanh")) {
                emoji = "📈";
            } else if (category.contains("Đầu tư")) {
                emoji = "📊";
            } else if (category.contains("Bán đồ")) {
                emoji = "📦";
            } else if (category.contains("Khác")) {
                emoji = "🪙";
            }
        }
        holder.tvEmoji.setText(emoji);

        // 6. Nhấp ngắn hoặc giữ lâu đều mở Sửa/Xoá rất thân thiện
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (longClickListener != null) {
                    longClickListener.onTransactionLongClick(transaction, holder.getAdapterPosition());
                }
            }
        });

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

    private String getReadableDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return "";
        }
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            java.util.Date date = sdf.parse(dateStr);
            if (date == null) return dateStr;

            java.util.Calendar inputCal = java.util.Calendar.getInstance();
            inputCal.setTime(date);

            java.util.Calendar todayCal = java.util.Calendar.getInstance();
            java.util.Calendar yesterdayCal = java.util.Calendar.getInstance();
            yesterdayCal.add(java.util.Calendar.DAY_OF_YEAR, -1);

            boolean isToday = inputCal.get(java.util.Calendar.YEAR) == todayCal.get(java.util.Calendar.YEAR) &&
                    inputCal.get(java.util.Calendar.DAY_OF_YEAR) == todayCal.get(java.util.Calendar.DAY_OF_YEAR);

            boolean isYesterday = inputCal.get(java.util.Calendar.YEAR) == yesterdayCal.get(java.util.Calendar.YEAR) &&
                    inputCal.get(java.util.Calendar.DAY_OF_YEAR) == yesterdayCal.get(java.util.Calendar.DAY_OF_YEAR);

            if (isToday) {
                return "Hôm nay (" + dateStr + ")";
            } else if (isYesterday) {
                return "Hôm qua (" + dateStr + ")";
            } else {
                // Lấy thứ trong tuần
                int dayOfWeek = inputCal.get(java.util.Calendar.DAY_OF_WEEK);
                String dayName = "Chủ Nhật";
                switch (dayOfWeek) {
                    case java.util.Calendar.MONDAY: dayName = "Thứ Hai"; break;
                    case java.util.Calendar.TUESDAY: dayName = "Thứ Ba"; break;
                    case java.util.Calendar.WEDNESDAY: dayName = "Thứ Tư"; break;
                    case java.util.Calendar.THURSDAY: dayName = "Thứ Năm"; break;
                    case java.util.Calendar.FRIDAY: dayName = "Thứ Sáu"; break;
                    case java.util.Calendar.SATURDAY: dayName = "Thứ Bảy"; break;
                }
                return dayName + " (" + dateStr + ")";
            }
        } catch (Exception e) {
            return dateStr;
        }
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

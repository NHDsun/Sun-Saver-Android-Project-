package com.example.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.models.Transaction;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp SQLiteOpenHelper quản lý việc tạo bảng và thực hiện các thao tác CRUD (Thêm, Đọc, Sửa, Xoá)
 * cho ứng dụng Sun Saver. Thiết kế cực kỳ dễ hiểu, clean và có đầy đủ comment tiếng Việt.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Khai báo thông tin database
    private static final String DATABASE_NAME = "sun_saver.db";
    private static final int DATABASE_VERSION = 1;

    // Tên bảng và các cột
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_TYPE = "type"; // "Income" hoặc "Expense"
    public static final String COLUMN_CATEGORY = "category"; // Ăn uống, Học tập, Giải trí...
    public static final String COLUMN_DATE = "date"; // Định dạng dd/MM/yyyy

    // Câu lệnh tạo bảng
    private static final String CREATE_TABLE_TRANSACTIONS = "CREATE TABLE " + TABLE_TRANSACTIONS + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_TITLE + " TEXT, "
            + COLUMN_AMOUNT + " REAL, "
            + COLUMN_TYPE + " TEXT, "
            + COLUMN_CATEGORY + " TEXT, "
            + COLUMN_DATE + " TEXT"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng giao dịch
        db.execSQL(CREATE_TABLE_TRANSACTIONS);
        
        // Thêm một số dữ liệu mẫu ban đầu để giao diện không bị trống
        insertSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xoá bảng cũ nếu tồn tại và tạo lại bảng mới
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        onCreate(db);
    }

    /**
     * Hàm thêm dữ liệu mẫu ban đầu giúp app khi mở ra có sẵn một số giao dịch mẫu trực quan.
     */
    private void insertSampleData(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + TABLE_TRANSACTIONS + " (title, amount, type, category, date) VALUES "
                + "('Mua sách giáo trình', 85000, 'Expense', 'Học tập', '25/05/2026'), "
                + "('Ăn tối Domino Pizza', 120000, 'Expense', 'Ăn uống', '24/05/2026'), "
                + "('Nhận lương bán thời gian', 1500000, 'Income', 'Khác', '23/05/2026'), "
                + "('Vé xem phim CGV', 95000, 'Expense', 'Giải trí', '22/05/2026'), "
                + "('Đổ xăng xe máy', 50000, 'Expense', 'Di chuyển', '21/05/2026')");
    }

    // ==========================================
    // CÁC PHƯƠNG THỨC CRUD (THÊM, ĐỌC, SỬA, XOÁ)
    // ==========================================

    /**
     * Thêm một giao dịch mới
     */
    public long insertTransaction(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, transaction.getTitle());
        values.put(COLUMN_AMOUNT, transaction.getAmount());
        values.put(COLUMN_TYPE, transaction.getType());
        values.put(COLUMN_CATEGORY, transaction.getCategory());
        values.put(COLUMN_DATE, transaction.getDate());

        long id = db.insert(TABLE_TRANSACTIONS, null, values);
        db.close();
        return id;
    }

    /**
     * Lấy toàn bộ danh sách giao dịch (mới nhất xếp lên đầu)
     */
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TRANSACTIONS + " ORDER BY id DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Transaction transaction = new Transaction();
                transaction.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                transaction.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                transaction.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)));
                transaction.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
                transaction.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
                transaction.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));

                transactions.add(transaction);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return transactions;
    }

    /**
     * Tìm kiếm giao dịch theo tên (không phân biệt chữ hoa, chữ thường)
     */
    public List<Transaction> searchTransactions(String query) {
        List<Transaction> transactions = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TRANSACTIONS + " WHERE " + COLUMN_TITLE + " LIKE ? ORDER BY id DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{"%" + query + "%"});

        if (cursor.moveToFirst()) {
            do {
                Transaction transaction = new Transaction();
                transaction.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                transaction.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                transaction.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)));
                transaction.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
                transaction.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
                transaction.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));

                transactions.add(transaction);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return transactions;
    }

    /**
     * Cập nhật một giao dịch hiện tại
     */
    public int updateTransaction(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, transaction.getTitle());
        values.put(COLUMN_AMOUNT, transaction.getAmount());
        values.put(COLUMN_TYPE, transaction.getType());
        values.put(COLUMN_CATEGORY, transaction.getCategory());
        values.put(COLUMN_DATE, transaction.getDate());

        int count = db.update(TABLE_TRANSACTIONS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(transaction.getId())});
        db.close();
        return count;
    }

    /**
     * Xoá một giao dịch
     */
    public void deleteTransaction(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRANSACTIONS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // ==========================================
    // CÁC PHƯƠNG THỨC THỐNG KÊ (QUY ĐỔI SỐ LIỆU)
    // ==========================================

    /**
     * Tính tổng thu nhập
     */
    public double getTotalIncome() {
        double total = 0;
        String query = "SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_TRANSACTIONS + " WHERE " + COLUMN_TYPE + " = 'Income'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return total;
    }

    /**
     * Tính tổng chi tiêu
     */
    public double getTotalExpense() {
        double total = 0;
        String query = "SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_TRANSACTIONS + " WHERE " + COLUMN_TYPE + " = 'Expense'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return total;
    }
}

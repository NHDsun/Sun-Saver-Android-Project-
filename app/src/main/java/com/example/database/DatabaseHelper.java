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
 * Lớp DatabaseHelper hỗ trợ lưu trữ thông tin giao dịch và tài khoản người dùng cho Sun Saver.
 * Đã nâng cấp lên Version 2 để tự động khởi tạo bảng đăng ký/đăng nhập.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sun_saver.db";
    private static final int DATABASE_VERSION = 2; // Nâng cấp từ 1 lên 2 để kích hoạt tính năng Users

    // Tên bảng và các cột của Giao dịch
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_TYPE = "type"; // "Income" hoặc "Expense"
    public static final String COLUMN_CATEGORY = "category"; // Ăn uống, Học tập, Giải trí...
    public static final String COLUMN_DATE = "date"; // Định dạng dd/MM/yyyy
    public static final String COLUMN_USER_REF = "user_ref"; // Username đang đăng nhập sở hữu

    // Tên bảng và các cột của Người dùng (Đăng nhập / Đăng ký)
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_FULLNAME = "fullname";

    // Câu lệnh tạo bảng Transactions
    private static final String CREATE_TABLE_TRANSACTIONS = "CREATE TABLE " + TABLE_TRANSACTIONS + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_TITLE + " TEXT, "
            + COLUMN_AMOUNT + " REAL, "
            + COLUMN_TYPE + " TEXT, "
            + COLUMN_CATEGORY + " TEXT, "
            + COLUMN_DATE + " TEXT, "
            + COLUMN_USER_REF + " TEXT DEFAULT 'guest'"
            + ")";

    // Câu lệnh tạo bảng Users
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + " ("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_USERNAME + " TEXT UNIQUE, "
            + COLUMN_PASSWORD + " TEXT, "
            + COLUMN_FULLNAME + " TEXT"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng Người dùng và bảng Giao dịch
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_TRANSACTIONS);
        
        // Nhập mẫu dữ liệu mặc định ban đầu cho khách (guest)
        insertSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nâng cấp DB xoá sạch dữ liệu cũ để tránh xung đột cấu trúc cột
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    /**
     * Chèn sẵn dữ liệu mẫu cho phiên bản demo
     */
    private void insertSampleData(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + TABLE_TRANSACTIONS + " (title, amount, type, category, date, user_ref) VALUES "
                + "('Mua sách giáo trình', 85000, 'Expense', 'Học tập', '25/05/2026', 'guest'), "
                + "('Ăn tối Domino Pizza', 120000, 'Expense', 'Ăn uống', '24/05/2026', 'guest'), "
                + "('Nhận lương bán thời gian', 1500000, 'Income', 'Khác', '23/05/2026', 'guest'), "
                + "('Vé xem phim CGV', 95000, 'Expense', 'Giải trí', '22/05/2026', 'guest'), "
                + "('Đổ xăng xe máy', 50000, 'Expense', 'Di chuyển', '21/05/2026', 'guest')");
    }

    // ==========================================
    // QUẢN LÝ TÀI KHOẢN (REGISTRATION & LOGIN)
    // ==========================================

    /**
     * Đăng ký người dùng mới
     */
    public boolean registerUser(String username, String password, String fullname) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_USERNAME, username.trim().toLowerCase());
            values.put(COLUMN_PASSWORD, password);
            values.put(COLUMN_FULLNAME, fullname.trim());

            long id = db.insert(TABLE_USERS, null, values);
            db.close();
            return id != -1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Kiểm tra xem username đã tồn tại chưa
     */
    public boolean checkUserExists(String username) {
        boolean exists = false;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_USER_ID},
                    COLUMN_USERNAME + " = ?",
                    new String[]{username.trim().toLowerCase()},
                    null, null, null);
            if (cursor != null) {
                exists = cursor.getCount() > 0;
                cursor.close();
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exists;
    }

    /**
     * Xác thực thông tin đăng nhập thành viên
     */
    public boolean checkLogin(String username, String password) {
        boolean success = false;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_USER_ID},
                    COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?",
                    new String[]{username.trim().toLowerCase(), password},
                    null, null, null);
            if (cursor != null) {
                success = cursor.getCount() > 0;
                cursor.close();
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    /**
     * Truy xuất họ tên đầy đủ hiển thị lên màn hình chính
     */
    public String getUserFullName(String username) {
        String fullName = "Thành viên";
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_FULLNAME},
                    COLUMN_USERNAME + " = ?",
                    new String[]{username.trim().toLowerCase()},
                    null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                fullName = cursor.getString(0);
                cursor.close();
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fullName;
    }

    // ==========================================
    // CÁC PHƯƠNG THỨC CRUD (HỖ TRỢ USER_REF)
    // ==========================================

    /**
     * Thêm một giao dịch mới chỉ định cho một tài khoản cụ thể
     */
    public long insertTransaction(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, transaction.getTitle());
        values.put(COLUMN_AMOUNT, transaction.getAmount());
        values.put(COLUMN_TYPE, transaction.getType());
        values.put(COLUMN_CATEGORY, transaction.getCategory());
        values.put(COLUMN_DATE, transaction.getDate());
        values.put(COLUMN_USER_REF, transaction.getUserRef());

        long id = db.insert(TABLE_TRANSACTIONS, null, values);
        db.close();
        return id;
    }

    /**
     * Lấy danh sách giao dịch cho người dùng chỉ định
     */
    public List<Transaction> getAllTransactions(String userRef) {
        List<Transaction> transactions = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TRANSACTIONS + " WHERE " + COLUMN_USER_REF + " = ? ORDER BY id DESC";

        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, new String[]{userRef});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Transaction transaction = new Transaction();
                    transaction.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    transaction.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                    transaction.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)));
                    transaction.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
                    transaction.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
                    transaction.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
                    transaction.setUserRef(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_REF)));

                    transactions.add(transaction);
                } while (cursor.moveToNext());
                cursor.close();
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return transactions;
    }

    /**
     * Hàm lấy danh sách giao dịch không tham số tương thích ngược phiên bản cũ (mặc định lấy cho guest)
     */
    public List<Transaction> getAllTransactions() {
        return getAllTransactions("guest");
    }

    /**
     * Tìm kiếm giao dịch trong nội bộ tài khoản người dùng đăng nhập
     */
    public List<Transaction> searchTransactions(String query, String userRef) {
        List<Transaction> transactions = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TRANSACTIONS + " WHERE " + COLUMN_TITLE + " LIKE ? AND " + COLUMN_USER_REF + " = ? ORDER BY id DESC";

        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, new String[]{"%" + query + "%", userRef});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Transaction transaction = new Transaction();
                    transaction.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    transaction.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                    transaction.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)));
                    transaction.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
                    transaction.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
                    transaction.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
                    transaction.setUserRef(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_REF)));

                    transactions.add(transaction);
                } while (cursor.moveToNext());
                cursor.close();
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return transactions;
    }

    /**
     * Hàm tìm kiếm giao dịch tương thích ngược phiên bản cũ (mặc định lấy cho guest)
     */
    public List<Transaction> searchTransactions(String query) {
        return searchTransactions(query, "guest");
    }

    /**
     * Cập nhật một giao dịch hiện tại
     */
    public int updateTransaction(Transaction transaction) {
        int count = 0;
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, transaction.getTitle());
            values.put(COLUMN_AMOUNT, transaction.getAmount());
            values.put(COLUMN_TYPE, transaction.getType());
            values.put(COLUMN_CATEGORY, transaction.getCategory());
            values.put(COLUMN_DATE, transaction.getDate());
            values.put(COLUMN_USER_REF, transaction.getUserRef());

            count = db.update(TABLE_TRANSACTIONS, values, COLUMN_ID + " = ?",
                    new String[]{String.valueOf(transaction.getId())});
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Xoá một giao dịch
     */
    public void deleteTransaction(int id) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_TRANSACTIONS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // CÁC PHƯƠNG THỨC THỐNG KÊ (HỖ TRỢ USER_REF)
    // ==========================================

    /**
     * Tính tổng thu nhập của một người dùng đăng nhập
     */
    public double getTotalIncome(String userRef) {
        double total = 0;
        try {
            String query = "SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_TRANSACTIONS + " WHERE " + COLUMN_TYPE + " = 'Income' AND " + COLUMN_USER_REF + " = ?";
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(query, new String[]{userRef});
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    total = cursor.getDouble(0);
                }
                cursor.close();
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    /**
     * Tính tổng thu nhập tương thích ngược phiên bản cũ (guest)
     */
    public double getTotalIncome() {
        return getTotalIncome("guest");
    }

    /**
     * Tính tổng chi tiêu của một người dùng đăng nhập
     */
    public double getTotalExpense(String userRef) {
        double total = 0;
        try {
            String query = "SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_TRANSACTIONS + " WHERE " + COLUMN_TYPE + " = 'Expense' AND " + COLUMN_USER_REF + " = ?";
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(query, new String[]{userRef});
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    total = cursor.getDouble(0);
                }
                cursor.close();
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    /**
     * Tính tổng chi tiêu tương thích ngược phiên bản cũ (guest)
     */
    public double getTotalExpense() {
        return getTotalExpense("guest");
    }
}

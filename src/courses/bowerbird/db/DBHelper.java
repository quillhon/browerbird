package courses.bowerbird.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "Bowerbird.db";

	private static final String TEXT_TYPE = " TEXT";
	private static final String INT_TYPE = " INTEGER";
	private static final String BOOL_TYPE = " INTEGER";
	private static final String COMMA_SEP = ",";
	private static final String SQL_CREATE_ITEM = 
			"CREATE TABLE " + DBEntry.Item.TABLE_NAME + " ("
			+ DBEntry.Item._ID + INT_TYPE + " PRIMARY KEY,"
			+ DBEntry.Item.COLUMN_NAME + TEXT_TYPE + COMMA_SEP
			+ DBEntry.Item.COLUMN_QUOTA + INT_TYPE + COMMA_SEP
			+ DBEntry.Item.COLUMN_IS_FINISHED + BOOL_TYPE
			+ ");";

	private static final String SQL_DELETE_ITEM = "DROP TABLE IF EXISTS "
			+ DBEntry.Item.TABLE_NAME;

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ITEM);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// This database is only a cache for online data, so its upgrade policy
		// is
		// to simply to discard the data and start over
		db.execSQL(SQL_DELETE_ITEM);
		onCreate(db);
	}

	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}

}

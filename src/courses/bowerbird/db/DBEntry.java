package courses.bowerbird.db;

import android.provider.BaseColumns;

public abstract class DBEntry {

	public abstract class Item implements BaseColumns {
		public static final String TABLE_NAME = "item";
		public static final String COLUMN_NAME = "name";
		public static final String COLUMN_QUOTA = "quota";
		public static final String COLUMN_IS_FINISHED = "is_finsihed";

		private Item() {
		}; // prevents class being instantiated
	}
}

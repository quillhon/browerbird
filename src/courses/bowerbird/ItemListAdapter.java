package courses.bowerbird;

import java.util.ArrayList;

import courses.bowerbird.db.DBEntry;
import courses.bowerbird.db.DBHelper;
import courses.bowerbird.models.Item;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

public class ItemListAdapter extends ArrayAdapter<Item> {

	private MainActivity mActivity;
	private ArrayList<Item> mItems;
	private DBHelper mDBHelper;

	public ItemListAdapter(MainActivity context, ArrayList<Item> objects) {
		super(context, R.layout.layout_item_list, objects);
		mActivity = context;
		mItems = objects;
		mDBHelper = new DBHelper(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Item item = mItems.get(position);
		if (convertView == null) {
			LayoutInflater vi = (LayoutInflater) mActivity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.layout_item_list, null);
		}
		final TextView name = (TextView) convertView.findViewById(R.id.name_text);
		TextView quota = (TextView) convertView.findViewById(R.id.quota_text);
		CheckBox finishBox = (CheckBox) convertView
				.findViewById(R.id.finish_checkbox);
		name.setText(item.getName());
		if (item.isFinsihed())
			name.setPaintFlags(name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		quota.setText(Integer.toString(item.getQuota()));
		finishBox.setTag(item);
		finishBox.setChecked(item.isFinsihed());
		finishBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				Item i = (Item) buttonView.getTag();
				SQLiteDatabase sqlconnection = mDBHelper.getWritableDatabase();
				ContentValues values = new ContentValues();
				String whereClause = DBEntry.Item._ID + "=?";
				String[] whereArgs = new String[] { Integer.toString(i.getId()) };
				if (isChecked) {
					values.put(DBEntry.Item.COLUMN_IS_FINISHED, 1);
					name.setPaintFlags(name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				} else {
					values.put(DBEntry.Item.COLUMN_IS_FINISHED, 0);
					name.setPaintFlags( name.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
				}
				sqlconnection.update(DBEntry.Item.TABLE_NAME, values,
						whereClause, whereArgs);
				sqlconnection.close();
				
				mActivity.syncItems();
			}
		});
		return convertView;
	}
}

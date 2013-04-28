package courses.bowerbird;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.style.UpdateAppearance;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import courses.bowerbird.DeviceDetailFragment.FileServerAsyncTask;
import courses.bowerbird.db.DBEntry;
import courses.bowerbird.db.DBHelper;
import courses.bowerbird.models.Item;
import courses.bowerbird.sync.SyncServerThread;

public class MainActivity extends ListActivity {

	public final static int REQUEST_SYNC = 1;
	public final static int SEND_LIST = 2;

	private DBHelper mDBHelper;
	private ArrayList<Item> mItems;
	private ItemListAdapter mItemListAdapter;

	private MainActivityHandler mHandler;
	private SyncServerThread mItemSyncThread;

	private WifiP2pInfo mWifiP2pInfo;

	// Client input and output
	Socket mSocket;
	ObjectOutputStream mObjectOutput;
	ObjectInputStream mObjectInput;

	private boolean isSyncing;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mDBHelper = new DBHelper(this);
		mItems = new ArrayList<Item>();
		mItemListAdapter = new ItemListAdapter(this, mItems);
		setListAdapter(mItemListAdapter);
		initList();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add_item:
			showCreateItemDialog();
			break;
		case R.id.action_sync:
			Intent intent = new Intent(this, WiFiDirectActivity.class);
			startActivityForResult(intent, REQUEST_SYNC);
			startActivity(intent);
			break;
		default:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	public void initList() {
		SQLiteDatabase sqlConnection = null;
		mItems.clear();
		try {
			sqlConnection = mDBHelper.getReadableDatabase();
			String[] columns = new String[] { DBEntry.Item._ID,
					DBEntry.Item.COLUMN_NAME, DBEntry.Item.COLUMN_QUOTA,
					DBEntry.Item.COLUMN_IS_FINISHED };
			String selection = DBEntry.Item.COLUMN_IS_FINISHED + "=0";
			Cursor cursor = sqlConnection.query(DBEntry.Item.TABLE_NAME,
					columns, selection, null, null, null, null);
			if (cursor.moveToFirst()) {
				do {
					Item item = new Item();
					item.setId(cursor.getInt(0));
					item.setName(cursor.getString(1));
					item.setQuota(cursor.getInt(2));
					item.setFinsihed(cursor.getInt(3) == 1);
					mItems.add(item);
				} while (cursor.moveToNext());
			}
			mItemListAdapter.notifyDataSetChanged();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public void showCreateItemDialog() {
		LayoutInflater inflater = LayoutInflater.from(this);
		View layout = inflater.inflate(R.layout.layout_new_item, null);
		final EditText nameEdit = (EditText) layout
				.findViewById(R.id.name_input);
		final EditText quotaEdit = (EditText) layout
				.findViewById(R.id.quota_input);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Add new item");
		builder.setView(layout);
		builder.setPositiveButton("Create",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						SQLiteDatabase sqlConnection = null;

						try {
							sqlConnection = mDBHelper.getWritableDatabase();
							ContentValues values = new ContentValues();
							values.put(DBEntry.Item.COLUMN_NAME, nameEdit
									.getText().toString());
							values.put(DBEntry.Item.COLUMN_QUOTA, Integer
									.parseInt(quotaEdit.getText().toString()));
							values.put(DBEntry.Item.COLUMN_IS_FINISHED, 0);
							sqlConnection.insert(DBEntry.Item.TABLE_NAME, null,
									values);
						} catch (Exception e) {
							// TODO: handle exception
						} finally {
							if (sqlConnection != null) {
								sqlConnection.close();
							}
						}
						initList();
						syncItems();
					}
				});
		builder.setNegativeButton("Cancel", null);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_SYNC:
			if (resultCode == RESULT_OK) {
				int port = 1234;
				if (mWifiP2pInfo.isGroupOwner) {
					mHandler = new MainActivityHandler(this);
					mItemSyncThread = new SyncServerThread(this, port);
					mItemSyncThread.start();
				} else {
					try {
						mSocket = new Socket(mWifiP2pInfo.groupOwnerAddress,
								port);
						mObjectOutput = new ObjectOutputStream(
								mSocket.getOutputStream());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				isSyncing = true;
			}
			break;

		default:
			break;
		}
	}

	public void syncItems() {
		if (isSyncing) {
			try {
				mObjectOutput.writeObject(mItems);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void setItems(ArrayList<Item> items) {
		mItems = items;
		mItemListAdapter.notifyDataSetChanged();
	}

	public Handler getHandler() {
		return mHandler;
	}
}

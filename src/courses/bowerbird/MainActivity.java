package courses.bowerbird;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;


import courses.bowerbird.db.DBEntry;
import courses.bowerbird.db.DBHelper;
import courses.bowerbird.models.Item;
import courses.bowerbird.sync.SyncServiceClientHandler;
import courses.bowerbird.sync.SyncServiceServerHandler;

public class MainActivity extends SwipeListViewActivity {

	public final static String TAG = "simpleList";
	
	public final static int REQUEST_SYNC = 1;
	public final static int SEND_LIST = 2;

	private DBHelper mDBHelper;
	private ArrayList<Item> mItems;
	private ListView mItemList;
	private ItemListAdapter mItemListAdapter;

	private MainActivityHandler mHandler;
	private SimpleChannelUpstreamHandler mSyncHandler;
	private int mPort = 8988;

	private ServerBootstrap mServerBootstrap;
	private ClientBootstrap mClientBootstrap;
	private ChannelFuture mChannelFuture;
	private Channel mChannel;

	private boolean mIsSyncing;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mItemList = (ListView) findViewById(R.id.item_list);
		mDBHelper = new DBHelper(this);
		mItems = new ArrayList<Item>();
		mItemListAdapter = new ItemListAdapter(this, mItems);
		mItemList.setAdapter(mItemListAdapter);
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
			break;
		default:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	public void deleteItem(int id) {
		SQLiteDatabase sqlConnection = null;

		try {
			sqlConnection = mDBHelper.getWritableDatabase();
			String whereClause = DBEntry.Item._ID + "=?";
			String[] whereArgs = new String[] {
					Integer.toString(id)
			};
			
			sqlConnection.delete(DBEntry.Item.TABLE_NAME, whereClause, whereArgs);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (sqlConnection != null) {
				sqlConnection.close();
			}
		}
	}

	public void initList() {
		SQLiteDatabase sqlConnection = null;
		mItems.clear();
		try {
			sqlConnection = mDBHelper.getReadableDatabase();
			String[] columns = new String[] { DBEntry.Item._ID,
					DBEntry.Item.COLUMN_NAME, DBEntry.Item.COLUMN_QUOTA,
					DBEntry.Item.COLUMN_IS_FINISHED };
			Cursor cursor = sqlConnection.query(DBEntry.Item.TABLE_NAME,
					columns, null, null, null, null, null);
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
				Bundle info = data.getExtras();
				
				mHandler = new MainActivityHandler(this);
				if (info.getBoolean("is_owner")) {
					String infoStr = "Starting server...";
					Toast.makeText(this, infoStr, Toast.LENGTH_LONG).show();
					mSyncHandler = new SyncServiceServerHandler(this);
					new initServerTask().execute(mPort);
				} else {
					String infoStr = "Starting client...";
					Toast.makeText(this, infoStr, Toast.LENGTH_LONG).show();
					mSyncHandler = new SyncServiceClientHandler(this);
					new initClientTask().execute(info.getString("owner_addr"),
							Integer.toString(mPort));
				}
			}

			break;

		default:
			break;
		}
	}

	private class initServerTask extends AsyncTask<Integer, Void, Void> {

		@Override
		protected Void doInBackground(Integer... port) {
			// Configure the server.
			mServerBootstrap = new ServerBootstrap(
					new NioServerSocketChannelFactory(
							Executors.newCachedThreadPool(),
							Executors.newCachedThreadPool()));

			// Set up the pipeline factory.
			mServerBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
				public ChannelPipeline getPipeline() throws Exception {
					return Channels
							.pipeline(
									new ObjectEncoder(),
									new ObjectDecoder(ClassResolvers
											.cacheDisabled(getClass()
													.getClassLoader())),
									mSyncHandler);
				}
			});

			// Bind and start to accept incoming connections.
			mServerBootstrap.bind(new InetSocketAddress(port[0]));

			return null;
		}

	}

	private class initClientTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {

			mClientBootstrap = new ClientBootstrap(
					new NioClientSocketChannelFactory(
							Executors.newCachedThreadPool(),
							Executors.newCachedThreadPool()));

			// Set up the pipeline factory.
			mClientBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
				public ChannelPipeline getPipeline() throws Exception {
					return Channels
							.pipeline(
									new ObjectEncoder(),
									new ObjectDecoder(ClassResolvers
											.cacheDisabled(getClass()
													.getClassLoader())),
									mSyncHandler);
				}
			});
			mClientBootstrap.setOption("tcpNoDelay", true);
			mClientBootstrap.setOption("keepAlive", true);

			// Start the connection attempt.
			mChannelFuture = mClientBootstrap.connect(new InetSocketAddress(
					params[0], Integer.parseInt(params[1])));
			mChannelFuture.awaitUninterruptibly();
			mChannel = mChannelFuture.awaitUninterruptibly().getChannel();

			mIsSyncing = true;
			
			syncItems();

			return null;
		}

	}

	public void syncItems() {
		if (mIsSyncing) {
			initList();
			mChannel.write(mItems);
		}
	}

	public void setItems(ArrayList<Item> items) {
		Log.i(TAG, "Update list");
		mItems.clear();
		for (int i = 0; i < items.size(); ++i) {
			mItems.add(items.get(i));
		}
		clearDatabase();
		writeItemsToDatabase(items);
		mItemListAdapter.notifyDataSetChanged();
	}
	
	private void clearDatabase() {
		SQLiteDatabase sqlConnection = null;

		try {
			sqlConnection = mDBHelper.getWritableDatabase();
			sqlConnection.execSQL("DELETE FROM " + DBEntry.Item.TABLE_NAME);
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (sqlConnection != null) {
				sqlConnection.close();
			}
		}
	}

	private void writeItemsToDatabase(ArrayList<Item> items) {

		SQLiteDatabase sqlConnection = null;

		try {
			sqlConnection = mDBHelper.getWritableDatabase();
			for (int i = 0; i < items.size(); ++i) {
				ContentValues values = new ContentValues();
				values.put(DBEntry.Item._ID, items.get(i).getId());
				values.put(DBEntry.Item.COLUMN_NAME, items.get(i).getName());
				values.put(DBEntry.Item.COLUMN_QUOTA, items.get(i).getQuota());
				int isFinished = items.get(i).isFinsihed() == true ? 1 : 0;  
				values.put(DBEntry.Item.COLUMN_IS_FINISHED, isFinished);
				sqlConnection.insert(DBEntry.Item.TABLE_NAME, null, values);
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (sqlConnection != null) {
				sqlConnection.close();
			}
		}
	}

	public Handler getHandler() {
		return mHandler;
	}

	public void setChannel(Channel channel) {
		mChannel = channel;
		mIsSyncing = true;
	}

	@Override
	public ListView getListView() {
		return mItemList;
	}


	@Override
	public void getSwipeItem(boolean isRight, int position) {
		Item item = mItems.get(position);
		deleteItem(item.getId());
		mItems.remove(position);
		syncItems();
		mItemListAdapter.notifyDataSetChanged();
	}


	@Override
	public void onItemClickListener(ListAdapter adapter, int position) {
		//Toast.makeText(this, "Single tap on item position " + position, Toast.LENGTH_SHORT).show();
	}
}

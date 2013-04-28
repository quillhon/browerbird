package courses.bowerbird;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import courses.bowerbird.models.Item;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class MainActivityHandler extends Handler {

	public final static int UPDATE_ITEMS = 1;

	private MainActivity mActivity;

	public MainActivityHandler(MainActivity activity) {
		mActivity = activity;
	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case UPDATE_ITEMS:
			Bundle data = msg.getData();
			mActivity.setItems((ArrayList<Item>) data.getSerializable("item"));
			break;
		default:
			break;
		}
	}

}

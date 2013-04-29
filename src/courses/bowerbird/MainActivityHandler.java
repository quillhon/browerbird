package courses.bowerbird;

import java.util.ArrayList;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import courses.bowerbird.models.Item;

public class MainActivityHandler extends Handler {

	public static final String TAG = "SimpleChannel";

	public static final int UPDATE_LIST = 1;
	public static final int SET_CHANNEL = 2;

	private MainActivity mActivity;

	public MainActivityHandler(MainActivity activity) {
		mActivity = activity;
	}

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
		case UPDATE_LIST:
			ArrayList<Item> items = (ArrayList<Item>) msg.obj;
			mActivity.setItems(items);
			break;
		case SET_CHANNEL:
			Channel channel = (Channel) msg.obj;
			mActivity.setChannel(channel);
			break;
		default:
			break;
		}

	}

}
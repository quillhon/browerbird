package courses.bowerbird.sync;

import java.util.ArrayList;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import courses.bowerbird.MainActivity;
import courses.bowerbird.MainActivityHandler;
import courses.bowerbird.models.Item;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SyncServiceServerHandler extends SimpleChannelUpstreamHandler {

	private static final String TAG = "SimpleChannel";

	private MainActivity mActivity;
	
	private Channel mChannel;

	public SyncServiceServerHandler(MainActivity activity) {
		mActivity = activity;
	}

	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
			throws Exception {
		if (e instanceof ChannelStateEvent
				&& ((ChannelStateEvent) e).getState() != ChannelState.INTEREST_OPS) {
			Log.i(TAG, e.toString());
		}
		super.handleUpstream(ctx, e);
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		Log.i(TAG, "Connected to server");
		mChannel = e.getChannel();
		Handler handler = mActivity.getHandler();
		Message msg = handler.obtainMessage(MainActivityHandler.SET_CHANNEL,
				mChannel);
		handler.sendMessage(msg);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		// Echo back the received object to the server.
		Log.i(TAG, "Recevived message");
		ArrayList<Item> items = (ArrayList<Item>) e.getMessage();
		Handler handler = mActivity.getHandler();
		Message msg = handler.obtainMessage(MainActivityHandler.UPDATE_LIST,
				items);
		handler.sendMessage(msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		Log.w(TAG, "Unexpected exception from downstream.", e.getCause());
		e.getChannel().close();
	}
}

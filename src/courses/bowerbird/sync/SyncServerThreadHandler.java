package courses.bowerbird.sync;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import courses.bowerbird.MainActivity;
import courses.bowerbird.models.Item;

import android.util.Log;

public class SyncServerThreadHandler extends SimpleChannelUpstreamHandler {
	private static final String TAG = "SimpleChannel";

	private MainActivity mActivity;

	public SyncServerThreadHandler(MainActivity activity) {
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
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		// Echo back the received object to the client.
		ArrayList<Item> items = (ArrayList<Item>) e.getMessage();
		mActivity.setItems(items);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		Log.w(TAG, "Unexpected exception from downstream.", e.getCause());
		e.getChannel().close();
	}
}

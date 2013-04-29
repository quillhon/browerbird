package courses.bowerbird;

import java.util.ArrayList;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelState;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import courses.bowerbird.models.Item;

import android.util.Log;

public class MainActivityHandler extends SimpleChannelUpstreamHandler {

	private static final String TAG = "SimpleChannel";

	private MainActivity mActivity;

	public MainActivityHandler(MainActivity activity) {
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
		// Echo back the received object to the server.
		ArrayList<Item> items = (ArrayList<Item>) e.getMessage();
		mActivity.setItems(items);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		Log.w(TAG, "Unexpected exception from downstream.", e.getCause());
		e.getChannel().close();
	}
}

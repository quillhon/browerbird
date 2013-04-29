package courses.bowerbird.sync;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import android.os.Handler;
import courses.bowerbird.MainActivity;

public class SyncServerThread extends Thread {

	private MainActivity mActivity;
	private Handler mHandler;

	private int mPort;
	private Channel mChannel;

	public SyncServerThread(MainActivity activity, int port) {
		mActivity = activity;
		mPort = port;
	}

	public Handler getHandler() {
		return mHandler;
	}

	public void run() {

	}
}

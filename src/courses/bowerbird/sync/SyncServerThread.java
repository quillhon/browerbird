package courses.bowerbird.sync;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.GetChars;
import courses.bowerbird.MainActivity;
import courses.bowerbird.MainActivityHandler;
import courses.bowerbird.models.Item;

public class SyncServerThread extends Thread {

	private MainActivity mActivity;
	private Handler mHandler;

	private int mPort;
	private Socket mSocket;
	ObjectInputStream mObjectInput;
	ObjectOutputStream mObjectOutput;

	private boolean isConnected = false;

	public SyncServerThread(MainActivity activity, int port) {
		mActivity = activity;
		mPort = port;
		mHandler = new ItemSyncThreadHandler();
	}

	public Handler getHandler() {
		return mHandler;
	}

	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			if (isConnected) {
				try {
					Object object = mObjectInput.readObject();
					ArrayList<Item> items = (ArrayList<Item>) object;
					Message message = mActivity.getHandler().obtainMessage(
							MainActivityHandler.UPDATE_ITEMS);
					message.obj = items;
					mActivity.getHandler().sendMessage(message);
				} catch (OptionalDataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					ServerSocket serverSocket = new ServerSocket(mPort);
					mSocket = serverSocket.accept();
					mObjectInput = new ObjectInputStream(
							mSocket.getInputStream());
					mObjectOutput = new ObjectOutputStream(
							mSocket.getOutputStream());
					isConnected = true;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}

package com.example.geocaching;

/*
 * This class is only used to simplify concurrent thread attempts at 
 * changing the GUI and reducing clutter in the CacheList class
 */
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class HelperFunctions {
	private Handler handler;
	
	/*
	 * Handler is set
	 */
	public HelperFunctions(CacheList context)
	{
		handler = context.getHandler();
	}
	
	/*
	 * send Intent to Handler to get taken care of
	 */
	public void sendIntentToHandler(String message) {
		messageToHandler("intent", message);
	}

	/*
	 * Message is sent to handler
	 */
	public void sendMessageToHandler(String message) {
		messageToHandler("message", message);
	}

	/*
	 * Test Message is sent to Handler
	 */
	public void sendTestMessageToHandler(String message) {
		messageToHandler("test", message);
	}
	
	/*
	 * Toast message is sent to Handler
	 */
	public void sendToastToHandler(String message)
	{
		messageToHandler("toast", message);
	}
	
	/*
	 * Simplifies other methods
	 */
	private void messageToHandler(String key, String message){
		Message msgObj = handler.obtainMessage();
		Bundle b = new Bundle();
		b.putString(key, message);
		msgObj.setData(b);
		handler.sendMessage(msgObj);
	}

}

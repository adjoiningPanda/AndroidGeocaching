package com.example.geocaching;

/*
 * This is a normal Runnable class that allows me to
 * also reference the context inside the inner class
 */
public class CustomRunnable implements Runnable{

	CacheList context;
	
	/*
	 * Context is passed in when initialized
	 */
	public CustomRunnable(CacheList context){
		this.context = context;
	}
	
	/*
	 * Returns the context
	 */
	public CacheList getParentContext()
	{
		return context;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 * 
	 * This is overwritten in the CacheList class
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}

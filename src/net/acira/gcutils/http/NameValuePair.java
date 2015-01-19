package net.acira.gcutils.http;

public class NameValuePair<T1, T2> {
	
	public NameValuePair(T1 name, T2 value) {
		this.name = name;
		this.value = value;
	}
	
	public T1 name;
	public T2 value;
	
}

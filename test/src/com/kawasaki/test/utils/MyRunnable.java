package com.kawasaki.test.utils;

public abstract class MyRunnable<T> implements Runnable {
	
	protected T mObj;
	
	public MyRunnable(T obj){
		super();
		mObj = obj;
	}
	
	public T getAndFillNullToMember(){
		T res = mObj;
		mObj=null;
		return res;
	}
}

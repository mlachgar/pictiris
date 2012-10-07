package com.afp.pictiris.data;

public interface Callback<T> {
	public void success(T t);
	public void faillure(Throwable th);
}

package com.abc.product.app.bo;

public class BaseResponse <T>{
	private boolean success;
	private ServiceError error;
	private T response;
	
	public BaseResponse() {
	}
	public BaseResponse(String code, String type, String text) {
		this.success = false;
		setServiceError(code, type, text);
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public ServiceError getError() {
		return error;
	}
	public void setError(ServiceError error) {
		this.error = error;
	}
	public void setServiceError(String code, String type, String text) {
		this.error = new ServiceError(code, type, text);
	}
	public T getResponse() {
		return response;
	}
	public void setResponse(T response) {
		this.response = response;
	}

	@Override
	public String toString() {
		return "BaseResponse{" +
				"success=" + success +
				", error=" + error+
				", response=" + response +
				'}';
	}
}

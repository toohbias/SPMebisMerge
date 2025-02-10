package com.codename1.io.websocket;


/**
 * 
 *  @author shannah
 */
public class SimpleWebSocket extends WebSocket {

	public SimpleWebSocket(String url, String protocols) {
	}

	public SimpleWebSocket(String url) {
	}

	public void addWebSocketListener(WebSocketListener l) {
	}

	public void removeWebSocketListener(WebSocketListener l) {
	}

	public void addOpenListener(Runnable r) {
	}

	public void removeOpenListener(Runnable r) {
	}

	public void addCloseListener(com.codename1.ui.events.ActionListener l) {
	}

	public void removeCloseListener(com.codename1.ui.events.ActionListener li) {
	}

	public void addErrorListener(com.codename1.ui.events.ActionListener l) {
	}

	public void removeErrorListener(com.codename1.ui.events.ActionListener li) {
	}

	public void addMessageListener(com.codename1.ui.events.ActionListener l) {
	}

	public void removeMessageListener(com.codename1.ui.events.ActionListener li) {
	}

	@java.lang.Override
	protected void onOpen() {
	}

	@java.lang.Override
	protected void onClose(int statusCode, String reason) {
	}

	@java.lang.Override
	protected void onMessage(String message) {
	}

	@java.lang.Override
	protected void onMessage(byte[] message) {
	}

	@java.lang.Override
	protected void onError(Exception ex) {
	}

	public class CloseEvent {


		public int getCode() {
		}

		public String getReason() {
		}

		public WebSocket getSource() {
		}
	}

	public class ErrorEvent {


		public Exception getError() {
		}
	}

	public class MessageEvent {


		public String getString() {
		}

		public byte[] getBytes() {
		}
	}
}

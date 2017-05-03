package com.google.sample.cast.refplayer.chatting;

import java.util.Date;

public class ChatMessage {

	private String username;
	private String message;
	//private Date date;
	private String date;
	private boolean incomingMessage;
	private String index;
	private String identity;

	public ChatMessage() {
		super();
	}

	public ChatMessage(String message) {
		super();
		this.message = message;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isIncomingMessage() {
		return incomingMessage;
	}

	public void setIncomingMessage(boolean incomingMessage) {
		this.incomingMessage = incomingMessage;
	}
	
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	/*
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	*/
	public boolean isSystemMessage(){
		//return getUsername()==null;
		return false;
	}

	public void setIndex(String index){
		this.index = index;
	}

	public String getIndex() {
		return index;
	}

	public void setIdentity(String identity){
		this.identity = identity;
	}

	public String getIdentity() {
		return identity;
	}

}

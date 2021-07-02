package com.mayfarm.elastic.DTO;

public class ElasticDTO {
	
	private String title;
	private String content;
	private String date;
	private String id;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	
	public String getAll() {
		return "title => " + title + " content => " + content + " date => " + date;
	}
}

package com.mkyong.core;
 
import java.util.ArrayList;
import java.util.List;
 
public class User {
 
	private int age = 29;
	private String name = "mkyong";
	private List<String> messages = new ArrayList<String>() {
		{
			add("msg 1");
			add("msg 2");
			add("msg 3");
		}
	};
 
	public void setAge(int age){
		this.age=age;
	}
	
	public void setName(String name){
		this.name=name;
	}
	
	public int getAge(){
		return this.age;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setMessages(List messages){
		this.messages=messages;
	}
	
	
 
	@Override
	public String toString() {
		return "User [age=" + age + ", name=" + name + ", " +
				"messages=" + messages + "]";
	}
}
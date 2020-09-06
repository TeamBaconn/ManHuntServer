package com.Tuong.Core;

public class BotInfo {
	public String name;
	public float x,y;
	public int health = 100,energy = 100;
	public BotInfo(float x, float y, String name) {
		this.x = x;
		this.y = y;
		this.name = name;
	}
	public void updateATT(Room room) {
		room.broadCast("ATTRIBUTE:"+name+":"+health+":"+energy);
		if(health <= 0) health = 100; 
	}
}

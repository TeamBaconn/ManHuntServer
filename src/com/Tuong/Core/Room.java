package com.Tuong.Core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Room {
	public String roomName;
	public ArrayList<Player> playerList;
	public boolean started;
	public ArrayList<Item> item = new ArrayList<Item>();
	public HashMap<String,BotInfo> bot = new HashMap<String,BotInfo>();
	public Room(String roomName) {
		this.roomName = roomName;
		this.playerList = new ArrayList<Player>();
		this.started = false;
	}
	Random random = new Random();
	public Character returnRandomCharacter() {
		ArrayList<Character> char_list = new ArrayList<Character>();
		for(Character character : Character.values()) char_list.add(character);
		for(Player p : playerList) char_list.remove(p.character);
		Character charact = char_list.get(random.nextInt(char_list.size()-1));
		return charact;
	}
	
	public void broadCastExcept(String cmd,Player p) {
		for (Player player : playerList)  if(p != player){
			player.sendCommand("3:"+p.playerName+":"+cmd);
		}
	}
	boolean roll = false;
	public void start() {
		started = true;
		if(roll) {
			for(Player p : playerList) if(p.isTerror) p.sendCommand("TERROR");
			return;
		}
		roll = true;
		int k = random.nextInt((playerList.size()-1)/2 + 1);
		Player p = playerList.get(k);
		p.isTerror = true;
		p.sendCommand("TERROR");
		if(playerList.size() >= 3) {
			Player p2 = playerList.get(k+random.nextInt(playerList.size() - k-1)+1);
			p2.isTerror=true;
			p2.sendCommand("TERROR");
		}
		
	}
	public void roomRemove() {
		for(Player player : playerList) {
			player.room = null;
		}
		Main.roomList.remove(this);
		broadCast("OUTROOM");
	}
	
	public String ter = "TERROR";
	public void broadCast(String cmd) {
		for (Player player : playerList) {
			player.sendCommand(cmd);
		}
	}
	public int time = 40;
	public void updatePlayerList() {
		String s = "2:"+time+":";
		for(Player player:playerList) {
			s+=player.playerName+"|"+player.character.toString()+",";
		}
		broadCast(s);
		if(ter.split(":").length > 2) broadCast(ter);
		else playerList.get(0).sendCommand("TERRORUPDATE:");
		for(Item it : item) broadCast("SI:"+it.id+","+it.x+","+it.y);
	}
}

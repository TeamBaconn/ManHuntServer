package com.Tuong.Core;

import java.net.InetAddress;

public class Player {
	public String playerName = "DEFAULT";
	public Player player;
	public Character character = Character.FARMER;
	public boolean isTerror;
	public InetAddress identity;
	public Room room;
	public PacketHandler handler;
	public int port;
	public float x,y;
	public int health=100,energy=100;
	public Player lastDamage;
	public boolean terror = false;

	public Long checkConnection = System.currentTimeMillis();
	
	public int uniqueID;

	// Attribute

	public Player(PacketHandler handler, InetAddress identity, int port, int uniqueID) {
		this.uniqueID = uniqueID;
		this.handler = handler;
		this.identity = identity;
		this.player = this;
		this.port = port;
		sendCommand("1:1:"+uniqueID);
	}

	public void leave() {
		System.out.println("[" + playerName + "]" + "Disconnected");
		if (room != null) {
			if (room.playerList.indexOf(player) == 0)
				room.roomRemove();
			else {
				sendCommand("OUTROOM");
				room.playerList.remove(player);
				room.updatePlayerList();
				room = null;
			}
		}
		handler.playerList.remove(player);
		System.out.println("[PLAYER COUNT] " + handler.playerList.size());
	}

	public void sendCommand(String cmd) {
		handler.sendCommand(cmd, identity, port);
	}
	public double distance(Player p) {
		return (p.x - player.x)*(p.x-player.x) + (p.y - player.y)*(p.y - player.y);
	}
	public double distance(BotInfo p) {
		return (p.x - player.x)*(p.x-player.x) + (p.y - player.y)*(p.y - player.y);
	}
	public void updateATT() {
		room.broadCast("ATTRIBUTE:"+playerName+":"+health+":"+energy);
		if(health<=0) health = 100;
	}
	public double next(double x1, double y1, double x2, double y2, double x3, double y3) {
		
		double xp = y2 - y1;
		double yp = -(x2 - x1);
		
		double c = -xp*x1 - yp*y1;
		
		return Math.abs(xp*x3 + yp*y3 + c)/Math.sqrt(xp*xp+yp*yp);
	}
	final double range = 0.7;
	public boolean damage(double x1, double y1, double x2, double y2) {
		if(Math.max(x1, x2) + range < this.x || Math.max(y1, y2)+range < this.y || Math.min(x1, x2)-range > this.x || Math.min(y1, y2)-range > this.y) 
			return false;
		if(next(x1, y1, x2, y2, this.x, this.y) > range) return false;
		return true;
	}
	public void takeDamage(double damage, Player damager) {
		health -= damage;
		if(health < 0) {
			//Death;
			room.broadCast("DEATH:"+player.playerName);
		}else
		room.broadCast("DAMAGE:"+player.playerName);
		updateATT();
	}
	public void checkCommand(String s) throws Exception {
		checkConnection = System.currentTimeMillis();
		String[] ss = s.split(":");
		switch (ss[0]) {
		case "ITEM":
			if (room == null && !room.started)
				break;
			String[] si = ss[1].split(",");
			room.item.add(new Item(Float.valueOf(si[1]), Float.valueOf(si[2]), Integer.valueOf(si[0])));
			break;
		case "PITEM":
			if (room == null && !room.started)
				break;
			String[] sp = ss[1].split(",");
			Item ik = null;
			for (Item i : room.item)
				if (i.id == Integer.valueOf(sp[0]) && i.x == Float.valueOf(sp[1]) && i.y == Float.valueOf(sp[2])) {
					ik = i;
					room.item.remove(i);
					break;
				}
			if (ik != null)
				room.broadCast("DI:" + player.playerName + "," + ik.id + "," + ik.x + "," + ik.y);
			break;
		case "TERROR":
			if (room != null) {
				player.terror = Boolean.valueOf(ss[1]);
				room.broadCast("TURNTERROR:" + player.playerName + ":" + player.terror);
			}
			break;
		case "ISTERROR":
			if(room.ter.split(":").length > 2) break;
			System.out.println(room.roomName+" monster is "+ ss[1]);
			room.ter+=':'+ss[1];
			room.updatePlayerList();
			break;
		case "PUNCH":
			energy -= Double.valueOf(ss[3]);
			updateATT();
			for(Player p : room.playerList) if(p != this.player) {
				Double d = distance(p);
				if(d <= Double.valueOf(ss[1])) {
					p.health -= Double.valueOf(ss[2]);
					room.broadCast("ATTACK:"+p.playerName+":"+this.playerName);
					p.updateATT();
				}
			}
			for(BotInfo b : room.bot.values()) {
				Double d = distance(b);
				if(d <= Double.valueOf(ss[1])) {
					b.health -= Double.valueOf(ss[2]);
					room.broadCast("ATTACK:"+b.name+":"+this.playerName);
					b.updateATT(room);
				}
			}
			break;
		case "ATTACK":
			room.broadCast("ATTACK:"+ss[2]+":"+ss[1]);
			for(Player p : room.playerList) if(p.playerName.equals(ss[2])) {
				p.health -= Double.valueOf(ss[3]);
				p.updateATT();
				break;
			}
			if(room.bot.containsKey(ss[2])) {
			room.bot.get(ss[2]).health -= Double.valueOf(ss[3]);
			room.bot.get(ss[2]).updateATT(room);
			}
			if(room.bot.containsKey(ss[1])) {
			room.bot.get(ss[1]).energy -= Double.valueOf(ss[4]);
			room.bot.get(ss[1]).updateATT(room);
			}
			break;
		case "SHOT":
			//SHOT x1 y1 x2 y2 damage 
			double x1 = Double.valueOf(ss[1]), y1 = Double.valueOf(ss[2]),
			x2 = Double.valueOf(ss[3]), y2 = Double.valueOf(ss[4]);
			for(Player p : room.playerList) if(p != this.player){
				if(p.damage(x1, y1, x2, y2)) {
					p.takeDamage(Double.valueOf(ss[5]), player);
					break;
				}
			}
			break;
		case "QUIT":
			leave();
			break;
		case "PLAYERNAME":
			boolean pickaboo = false;
			for (Player player : handler.playerList)
				if (player.playerName.contentEquals(ss[1])) {
					sendCommand("1:10");
					pickaboo = true;
					break;
				}
			if (pickaboo)
				break;
			this.playerName = ss[1];
			sendCommand("1:9");
			break;
		case "CREATEROOM":
			if (room == null) {
				boolean b = false;
				for (Room room : Main.roomList)
					if (room.roomName.equals(ss[1])) {
						b = true;
						break;
					}
				if (!b) {
					this.room = new Room(ss[1]);
					this.room.playerList.add(player);
					Main.roomList.add(this.room);
					System.out.println("[" + playerName + "] Has created room " + ss[1]);
					sendCommand("1:8");
					character = room.returnRandomCharacter();
				} else {
					sendCommand("1:7");
				}
			}
			break;
		case "GETROOM":
			if (room == null)
				for (Room room : Main.roomList)
					if (room.roomName.equals(ss[1]) && !room.playerList.contains(player) && room.playerList.size() < Character.values().length) {
						sendCommand("1:6");
						this.room = room;
						this.room.playerList.add(player);
						character = room.returnRandomCharacter();
						break;
					} else
						sendCommand("1:5");
			break;
		case "2":
			if (room != null) {
				if(ss.length>1) room.time = Integer.valueOf(ss[1]);
				room.updatePlayerList();
			}
			break;
		case "STARTROOM":
			if (room != null && room.playerList.get(0).equals(player)) {
				room.updatePlayerList();
				room.broadCast("STARTGAME");
				room.start();
				System.out.println("[" + room.roomName + "] started");
			} else {
				sendCommand("1:0");
			}
			break;
		case "3":
			if (room != null) {
				room.broadCastExcept(s.replace("3:", ""), player);
				if(ss[1].equals("5")) {
					String[] sf = ss[2].split(",");
					x = Float.valueOf(sf[0]);
					y = Float.valueOf(sf[1]);
				}else if(ss[1].equals("4")) {
					String[] sf = ss[2].split(",");
					if(!room.bot.containsKey(sf[0]))room.bot.put(sf[0], new BotInfo(Float.valueOf(sf[1]),Float.valueOf(sf[2]),sf[0]));
					else {
						room.bot.get(sf[0]).x = Float.valueOf(sf[1]);
						room.bot.get(sf[0]).y = Float.valueOf(sf[2]);
					}
				}
			} else
				sendCommand("1:0");
			break;
		case "6": //REGEN
			for(Player p : room.playerList) if(p.playerName.equals(ss[1])) {
				p.health = 100;
				p.updateATT();
				break;
			}
			if(room.bot.containsKey(ss[1])) {
				room.bot.get(ss[1]).health =100;
				room.bot.get(ss[1]).updateATT(room);
			}
			break;
		case "SAYALL":
			if (room != null) {
				room.broadCast(s.replace("SAYALL:", ""));
				break;
			} else
				sendCommand("1:0");
		case "LEAVEROOM":
			if (room != null) {
				if (room.playerList.indexOf(player) == 0)
					room.roomRemove();
				else {
					sendCommand("OUTROOM");
					room.playerList.remove(player);
					room.updatePlayerList();
					room = null;
				}
			} else
				sendCommand("1:0");
			break;
		default:
			System.out.println("[FATAL] Unknown paramenter "+s);
			break;
		}
	}
}

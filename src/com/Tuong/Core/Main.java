package com.Tuong.Core;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
	public static ArrayList<Room> roomList = new ArrayList<Room>();
	public static DatagramSocket server;
	private static Scanner scanner;
	public static final int checkConnectedDelay = 12;
	
	public static int uniqueID = 0;
	
	public static void main(String[] args) throws IOException {
		System.out.println("START RECEIVING PLAYERS");
		server = new DatagramSocket(1604);
		PacketHandler handler = new PacketHandler(server);
		new Thread() {
			public void run() {
				while (true) {
					try {
						//UDP listening
						byte[] buf = new byte[1024];
						DatagramPacket packet = new DatagramPacket(buf, buf.length);
						server.receive(packet);
						handler.packets.add(packet);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
		//Pinging the player to check disconnect and remove
		new Thread() {
			public void run() {
				while(true) {
					ArrayList<Player> playerList = new ArrayList<Player>(handler.playerList);
					if(playerList.size() == 0) uniqueID = 0;
					for(Player player : playerList) if(System.currentTimeMillis() - player.checkConnection > checkConnectedDelay*1000) player.leave();
					try {Thread.sleep(checkConnectedDelay*1000);}catch(Exception e) {e.printStackTrace();}
				}
			}
		}.start();
		scanner = new Scanner(System.in);
		try {
			while (true) {
				String s1 = scanner.nextLine();
				String[] cmd = s1.split(":");
				switch (cmd[0]) {
				case "SENDCMD":
					for (Player player : handler.playerList) player.sendCommand(s1.replace("SENDCMD:", ""));
					break;
				case "PLAYERLIST":
					String s = "Player list: ";
					for (Player player : handler.playerList)
						s += player.playerName + ",";
					System.out.println(handler.playerList.size() + " players");
					System.out.println(s);
					break;
				case "ROOMLIST":
					System.out.println(roomList.size() + " rooms");
					break;
				case "CLEAR":
					handler.playerList.clear();
					handler.packets.clear();
				default:
					System.out.println("UNKNOW COMMAND");
				}
			}
		} catch (NoSuchElementException e) {
		}
	}
}

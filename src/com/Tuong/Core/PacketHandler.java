package com.Tuong.Core;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class PacketHandler {
	public ArrayList<Player> playerList = new ArrayList<Player>();
	public ArrayList<DatagramPacket> packets = new ArrayList<DatagramPacket>();
	public PacketHandler handler;
	public DatagramSocket socket;

	public PacketHandler(DatagramSocket socket) {
		this.handler = this;
		this.socket = socket;
		new Thread() {
			public void run() {
				while (true) {
					try {
						ArrayList<DatagramPacket> packett = new ArrayList<DatagramPacket>(packets);
						if (packett.size() == 0)
							continue;
						DatagramPacket packet = packett.get(0);
						String st = new String(packet.getData(), 0, 1024).trim();
						Player p = getPlayer(packet.getAddress(), packet.getPort());
						if (p == null) {
							if (st.equals("NEWCONNECTION")) {
								System.out.println("[NEW CONNECTION] " + packet.getAddress().toString() + " "
										+ packet.getPort() + " " + Main.uniqueID);
								playerList
										.add(new Player(handler, packet.getAddress(), packet.getPort(), Main.uniqueID));
								System.out.println("[PLAYER COUNT] " + handler.playerList.size());
							}else if(st.contains("RECONNECT:")) {
								int id = Integer.valueOf(st.split(":")[1]);
								ArrayList<Player> playerL = new ArrayList<Player>(playerList);
								for (Player player : playerL) if(player.uniqueID == id) {
									player.identity = packet.getAddress();
									player.port = packet.getPort();
									System.out.println("[RECONNECTED] " + packet.getAddress().toString() + " "
											+ packet.getPort() + " " + id);
								}
							}
						} else if (p != null) {
							if (st.contentEquals("NEWCONNECTION"))
								p.sendCommand("1:1");
							else
								p.checkCommand(st);
						}
					} catch (Exception e) {
					}
					packets.remove(0);
				}
			}
		}.start();
	}

	public Player getPlayer(InetAddress identity, int port) {
		ArrayList<Player> playerL = new ArrayList<Player>(playerList);
		for (Player player : playerL)
			if (player.identity.equals(identity) && player.port == port)
				return player;
		return null;
	}

	public void sendCommand(String command, InetAddress identity, int port) {
		// System.out.println(command + " "+identity.toString()+ " "+port);
		DatagramPacket send = new DatagramPacket(command.getBytes(), command.getBytes().length, identity, port);
		try {
			socket.send(send);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

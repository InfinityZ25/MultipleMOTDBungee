package me.infinity.plugins.multiplemotdbungee;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class TestClass extends JavaPlugin implements PluginMessageListener {

    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        if (!channel.equals("yourChannelOnBungee")) {
            return;
        }

        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        DataInputStream in = new DataInputStream(stream);
        try {
            //Get the message from bungee
            String message = in.readUTF();
            //Log it into console for debugging purposes if you want to
            getLogger().info(message);
            //Do whatever you want with your data

            String[] msg = message.split(" ");

            Player p = Bukkit.getPlayer(msg[0]);
            String server = msg[2];
            String ip = msg[4];

            switch (server.toLowerCase()){
                case "lobby-pro":{
                    switch (ip.toLowerCase()){
                        case "myserver.net":{
                            Bukkit.broadcastMessage(p.getName() + " has connected the server " + server + "thru the ip " + ip);
                            break;
                        }
                    }
                    break;
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

package com.mengcraft.account;

import com.mengcraft.account.bungee.BungeeMain;
import com.mengcraft.account.bungee.BungeeMessage;
import com.mengcraft.account.lib.ReadWriteUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.DataInput;
import java.util.HashMap;
import java.util.Map;

import static com.mengcraft.account.bungee.BungeeMain.CHANNEL;

/**
 * Created on 16-2-17.
 */
public class BungeeSupport implements PluginMessageListener {

    public static final BungeeSupport INSTANCE = new BungeeSupport();

    private BungeeSupport() {
    }

    private final Map<String, String> map = new HashMap<>();

    @Override
    public void onPluginMessageReceived(String tag, Player p, byte[] data) {
        if (Main.eq(tag, BungeeMain.CHANNEL)) {
            processMessage(data);
        }
    }

    private void processMessage(byte[] data) {
        DataInput input = ReadWriteUtil.toDataInput(data);
        BungeeMessage message = BungeeMessage.read(input);
        if (Main.eq(message.getType(), BungeeMain.ADD_LOGGED)) {
            map.put(message.getName(), message.getIp());
            OfflinePlayer j = Bukkit.getOfflinePlayer(message.getName());
            if (LockedList.INSTANCE.isLocked(j.getUniqueId()) && j.isOnline()) {
                Player p = j.getPlayer();
                if (Main.eq(message.getIp(), p.getAddress().getAddress().getHostAddress())) {
                    LockedList.INSTANCE.remove(j.getUniqueId());
                }
            }
        } else if (Main.eq(message.getType(), BungeeMain.DEL_LOGGED)) {
            map.remove(message.getName());
        }
    }

    public boolean hasLoggedIn(Player p) {
        String ip = map.get(p.getName());
        return ip != null && Main.eq(ip, p.getAddress().getAddress().getHostAddress());
    }

    public void sendLoggedIn(Plugin plugin, Player p) {
        BungeeMessage message = new BungeeMessage();
        message.setType(BungeeMain.DISTRIBUTE);
        message.setName(p.getName());
        message.setIp(p.getAddress().getAddress().getHostAddress());

        map.put(message.getName(), message.getIp());// Force set local map.
        p.sendPluginMessage(plugin, CHANNEL, message.toByteArray());
    }

}

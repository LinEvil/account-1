package com.mengcraft.account;

import com.mengcraft.account.bungee.BungeeMain;
import com.mengcraft.account.command.BindingCommand;
import com.mengcraft.account.entity.AppAccountBinding;
import com.mengcraft.account.entity.AppAccountEvent;
import com.mengcraft.account.entity.Member;
import com.mengcraft.account.lib.Messenger;
import com.mengcraft.account.lib.MetricsLite;
import com.mengcraft.simpleorm.EbeanHandler;
import com.mengcraft.simpleorm.EbeanManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private boolean log;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        EbeanHandler source = EbeanManager.DEFAULT.getHandler(this);
        if (!source.isInitialized()) {
            source.define(AppAccountBinding.class);
            source.define(AppAccountEvent.class);
            source.define(Member.class);
            try {
                source.initialize();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        source.install(true);
        source.reflect();

        Account.INSTANCE.setMain(this);
        log = getConfig().getBoolean("log");

        new ExecutorCore(this).bind();

        if (!getConfig().getBoolean("minimal")) {
            new Executor(this, new Messenger(this)).bind();
            new ExecutorEvent().bind(this);

            getServer().getMessenger().registerIncomingPluginChannel(this, BungeeMain.CHANNEL, BungeeSupport.INSTANCE);
            getServer().getMessenger().registerOutgoingPluginChannel(this, BungeeMain.CHANNEL);
        }

        if (getConfig().getBoolean("binding.command")) {
            getCommand("binding").setExecutor(new BindingCommand(this));
        }

        new MetricsLite(this).start();

        String[] j = {
                ChatColor.GREEN + "梦梦家高性能服务器出租店",
                ChatColor.GREEN + "shop105595113.taobao.com"
        };
        getServer().getConsoleSender().sendMessage(j);
    }

    public void execute(Runnable runnable) {
        getServer().getScheduler().runTaskAsynchronously(this, runnable);
    }

    public void process(Runnable task, int tick) {
        getServer().getScheduler().runTaskLater(this, task, tick);
    }

    public void process(Runnable task) {
        getServer().getScheduler().runTask(this, task);
    }

    public boolean isLog() {
        return log;
    }

    public static boolean eq(Object i, Object j) {
        return i == j || (i != null && i.equals(j));
    }

}

package me.minebuilders.clearlag.commands;

import me.minebuilders.clearlag.ClearLag;
import me.minebuilders.clearlag.RAMUtil;
import me.minebuilders.clearlag.Util;
import me.minebuilders.clearlag.annotations.AutoWire;
import me.minebuilders.clearlag.exceptions.WrongCommandArgumentException;
import me.minebuilders.clearlag.language.LanguageValue;
import me.minebuilders.clearlag.language.messages.MessageTree;
import me.minebuilders.clearlag.modules.CommandModule;
import me.minebuilders.clearlag.tasks.TPSTask;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Hopper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CheckCmd extends CommandModule {

    @AutoWire
    private TPSTask tpsTask;

    @LanguageValue(key = "command.check.")
    private MessageTree lang;

    @Override
    protected void run(CommandSender sender, String[] args) throws WrongCommandArgumentException {
        final List<World> worlds;
        if (args.length > 0) {
            worlds = new ArrayList<>(args.length);
            for (String arg : args) {
                World world = Bukkit.getWorld(arg);
                if (world == null) {
                    throw new WrongCommandArgumentException(lang.getMessage("invalidworld"), arg);
                }

                worlds.add(world);
            }
        } else {
            worlds = Bukkit.getWorlds();
        }

        AtomicInteger removed = new AtomicInteger(0), mobs = new AtomicInteger(0), animals = new AtomicInteger(0), chunks = new AtomicInteger(0), spawners = new AtomicInteger(0), activehoppers = new AtomicInteger(0), inactivehoppers = new AtomicInteger(0), players = new AtomicInteger(0);
        for (World w : worlds) {
            for (Chunk c : w.getLoadedChunks()) {
                for (BlockState bt : c.getTileEntities()) {
                    if (bt instanceof CreatureSpawner) {
                        spawners.incrementAndGet();
                    } else if (bt instanceof Hopper) {
                        if (!isHopperEmpty((Hopper) bt)) {
                            activehoppers.incrementAndGet();
                        } else {
                            inactivehoppers.incrementAndGet();
                        }
                    }
                }

                for (Entity e : c.getEntities()) {
                    switch (e) {
                        case Monster monster -> mobs.incrementAndGet();
                        case Player player -> players.incrementAndGet();
                        case Creature creature -> animals.incrementAndGet();
                        case Item item -> removed.incrementAndGet();
                        default -> {
                        }
                    }
                }

                chunks.incrementAndGet();
            }
        }

        lang.sendMessage("header", sender);

        lang.sendMessage("printed", sender,
                removed.get(),
                mobs.get(),
                animals.get(),
                players.get(),
                chunks.get(),
                activehoppers.get(),
                inactivehoppers.get(),
                spawners.get(),
                Util.getTime(System.currentTimeMillis() - ClearLag.getInstance().getInitialBootTimestamp()),
                tpsTask.getStringTPS(),
                RAMUtil.getUsedMemory(), RAMUtil.getMaxMemory(),
                (RAMUtil.getMaxMemory() - RAMUtil.getUsedMemory())
        );

        lang.sendMessage("footer", sender);
    }

    private boolean isHopperEmpty(Hopper hop) {
        for (ItemStack it : hop.getInventory().getContents()) {
            if (it != null) return false;
        }

        return true;
    }
}

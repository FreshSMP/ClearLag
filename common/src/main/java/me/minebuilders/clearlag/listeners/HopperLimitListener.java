package me.minebuilders.clearlag.listeners;

import me.minebuilders.clearlag.ChunkKey;
import me.minebuilders.clearlag.ClearLag;
import me.minebuilders.clearlag.annotations.ConfigPath;
import me.minebuilders.clearlag.annotations.ConfigValue;
import me.minebuilders.clearlag.modules.EventModule;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryMoveItemEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@ConfigPath(path = "hopper-limiter")
public class HopperLimitListener extends EventModule implements Runnable {

    @ConfigValue
    private final int transferLimit = 6;

    @ConfigValue
    private final int checkInterval = 1;

    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    // Concurrent + atomic counters per chunk
    private ConcurrentHashMap<ChunkKey, AtomicInteger> hopperDataMap = new ConcurrentHashMap<>();

    @EventHandler
    public void onHopper(InventoryMoveItemEvent event) {
        if (event.getSource().getHolder() instanceof Hopper hopper) {
            final ChunkKey chunkKey = new ChunkKey(hopper.getChunk());
            final AtomicInteger transfers = hopperDataMap.computeIfAbsent(chunkKey, k -> new AtomicInteger(0));

            if (transfers.get() >= transferLimit) {
                event.setCancelled(true);
            } else {
                transfers.incrementAndGet();
            }
        }
    }

    @Override
    public void run() {
        hopperDataMap.entrySet().removeIf(e -> e.getValue().get() == 0);
        hopperDataMap.forEach((k, v) -> v.set(0));
    }

    @Override
    public void setEnabled() {
        super.setEnabled();

        cancelled.set(false);

        final long ticks = Math.max(1L, checkInterval * 20L);
        ClearLag.scheduler().runTimer(task -> {
            if (cancelled.get()) {
                task.cancel();
                return;
            }

            this.run();
        }, ticks, ticks);
    }

    @Override
    public void setDisabled() {
        super.setDisabled();
        cancelled.set(true);
        hopperDataMap = new ConcurrentHashMap<>();
    }
}

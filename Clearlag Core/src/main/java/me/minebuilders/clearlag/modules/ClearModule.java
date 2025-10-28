package me.minebuilders.clearlag.modules;

import me.minebuilders.clearlag.ClearLag;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.LinkedList;
import java.util.List;

public abstract class ClearModule extends ClearlagModule {

	public List<Entity> getRemovables(List<Entity> list, World w) {
		List<Entity> en = new LinkedList<>();
		if (!isWorldEnabled(w) || list.isEmpty()) {
            return en;
        }

		for (Entity ent : list) {
			ClearLag.scheduler().runAtEntity(ent, task -> {
				if (isRemovable(ent)) {
                    en.add(ent);
                }
			});
		}

		return en;
	}

	public List<Entity> getAllRemovables() {
		List<Entity> en = new LinkedList<>();
		for (World w : Bukkit.getWorlds()) {
			if (!isWorldEnabled(w)) {
                continue;
            }

			for (Entity ent : w.getEntities()) {
				ClearLag.scheduler().runAtEntity(ent, task -> {
					if (isRemovable(ent)) {
                        en.add(ent);
                    }
				});
			}
		}

		return en;
	}

	public abstract boolean isRemovable(Entity e);

	public boolean isWorldEnabled(World w) {
		return true;
	}
}

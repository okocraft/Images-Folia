/*
 * MIT License
 *
 * Copyright (c) 2020 Mark
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.andavin.images.nms;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData.MapPatch;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.map.MapPalette;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.emptyList;

/**
 * @author Andavin
 * @since September 20, 2019
 */
public class MapHelper {

    static final int DEFAULT_STARTING_ID = 1_000_000;
    private static final Map<UUID, AtomicInteger> MAP_IDS = new HashMap<>(4);

    public int nextMapId(org.bukkit.World world) {
        return MAP_IDS.computeIfAbsent(world.getUID(), __ -> new AtomicInteger(DEFAULT_STARTING_ID)).getAndIncrement();
    }

    public void createMap(int frameId, int rawMapId, Player player, Location location,
                          BlockFace direction, int rotation, byte[] pixels) {

        ItemStack item = new ItemStack(Items.FILLED_MAP);
        MapId mapId = new MapId(rawMapId);
        item.set(DataComponents.MAP_ID, mapId);

        ItemFrame frame = new ItemFrame(((CraftWorld) player.getWorld()).getHandle(),
                BlockPos.containing(location.getX(), location.getY(), location.getZ()),
                CraftBlock.blockFaceToNotch(direction));
        frame.setItem(item, false, false);
        frame.setInvisible(com.andavin.images.MapHelper.invisible);
        frame.setId(frameId);
        if (rotation != 0) {
            frame.setRotation(rotation);
        }

        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;

        connection.send(new ClientboundAddEntityPacket(frame, frame.getDirection().get3DDataValue(), frame.getPos()));

        var packed = frame.getEntityData().packDirty();
        if (packed != null) {
            connection.send(new ClientboundSetEntityDataPacket(frame.getId(), packed));
        }

        connection.send(new ClientboundMapItemDataPacket(mapId, (byte) 3, false, emptyList(), new MapPatch(0, 0, 128, 128, pixels)));
    }

    public void destroyMap(Player player, int[] frameIds) {
        ((CraftPlayer) player).getHandle().connection.send(new ClientboundRemoveEntitiesPacket(frameIds));
    }

    @SuppressWarnings("removal")
    public byte[] createPixels(BufferedImage image) {
        int pixelCount = image.getWidth() * image.getHeight();
        int[] pixels = new int[pixelCount];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        byte[] colors = new byte[pixelCount];
        for (int i = 0; i < pixelCount; i++) {
            colors[i] = MapPalette.matchColor(new Color(pixels[i], true));
        }

        return colors;
    }
}

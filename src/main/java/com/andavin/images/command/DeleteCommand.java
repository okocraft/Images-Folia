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
package com.andavin.images.command;

import com.andavin.images.Images;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Locale;

/**
 * @author Andavin
 * @since February 14, 2018
 */
final class DeleteCommand extends BaseCommand implements Listener {

    DeleteCommand() {
        super("delete", "images.command.delete");
        this.setAliases("del", "remove", "unload");
        this.setUsage("/image delete");
        this.setDesc("Delete an existing image by clicking on it");
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length < 1) {
            player.sendMessage("§c/images delete <id>");
            return;
        }

        String imageName = args[0];
        var images = Images.getMatchingImages(image -> image.getImageName().equalsIgnoreCase(imageName));

        if (images.isEmpty()) {
            player.sendMessage("§cNo images found.");
            return;
        }

        for (var image : images) {
            if (Images.removeImage(image)) {
                image.destroy();
                player.sendMessage("§aImage successfully deleted: " + image.getImageName());
            } else {
                player.sendMessage("§cFailed to delete image: " + image.getImageName());
            }
        }
    }

    @Override
    public void tabComplete(CommandSender sender, String[] args, List<String> completions) {
        if (args.length != 1 || !hasPermission(sender, args)) {
            return;
        }

        var filter = args[0].toLowerCase(Locale.ENGLISH);

        for (var image : Images.getMatchingImages(i -> i.getImageName().toLowerCase(Locale.ENGLISH).startsWith(filter))) {
            completions.add(image.getImageName());
        }
    }
}

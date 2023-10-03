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

import com.andavin.images.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;

import java.util.Map;

/**
 * A manager for registering all {@link BaseCommand}s.
 */
public final class CommandRegistry {

    /**
     * Register all the commands.
     */
    public static void registerCommands() {
        register(new ImageCommand());
    }

    /**
     * Register the given BaseCommand and allow it to
     * be executed by Bukkit.
     *
     * @param command The command to register.
     */
    public static void register(BaseCommand command) {
        Logger.debug("Registering command {}.", command);
        clearCommand(command);
        Bukkit.getCommandMap().register("images", new CommandExecutor(command));
    }

    private static void clearCommand(BaseCommand command) {
        Map<String, Command> commands = Bukkit.getCommandMap().getKnownCommands();
        commands.remove(command.getName().toLowerCase());
        for (String alias : command.getAliases()) {
            commands.remove(alias.toLowerCase());
        }
    }
}

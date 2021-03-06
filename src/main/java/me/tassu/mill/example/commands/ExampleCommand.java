/*
 * MIT License
 *
 * Copyright (c) 2018 Tassu <hello@tassu.me>
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.tassu.mill.example.commands;

import com.google.inject.Singleton;
import me.tassu.mill.api.ann.Command;
import me.tassu.mill.api.ann.SubCommandFrom;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@Singleton
@Command({"great"})
@SubCommandFrom("me.tassu.mill.example.commands.ExampleSubcommands")
public class ExampleCommand {

    @Command("yes")
    public void doIt(String message) {
        Bukkit.broadcastMessage(ChatColor.AQUA + message);
    }

    public void execute(CommandSender sender) {
        sender.sendMessage(sender.getName());
    }

}

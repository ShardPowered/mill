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

package me.tassu.mill.register;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import lombok.val;
import me.tassu.easy.register.command.Command;
import me.tassu.easy.register.command.error.CommandException;
import me.tassu.easy.register.core.NotSingleton;
import me.tassu.mill.Mill;
import me.tassu.snake.util.Chat;
import org.bukkit.command.CommandSender;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@NotSingleton
public class MillCommandInstance extends Command {

    private Mill mill;

    public MillCommandInstance(Mill mill, Set<String> aliases) {
        super(aliases.stream().findFirst().orElse("wtf"));
        this.setAliases(Lists.newArrayList(aliases));
        this.mill = mill;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("aliases", getAliases())
                .toString();
    }

    @Override
    protected void run(CommandSender sender, String label, List<String> args) throws CommandException {
        val contexts = mill.match(sender,(label + " " + joinArgs(args)).trim());

        if (contexts.size() != 1) {
            sender.sendMessage(Chat.RED + "command not found"); // whoops!
            return;
        }

        val context = contexts.stream().findFirst().orElse(null);
        val command = Objects.requireNonNull(context).getCommand();

        val instance =
                mill.getPlugin().getUnsafeInjector().getInstance(command.getMethod().getDeclaringClass());

        try {
            command.getMethod().invoke(instance, context.getArgs());
        } catch (IllegalAccessException | InvocationTargetException e) {
            // TODO handle error
            throw new RuntimeException("Failure executing command " + command.getAlias() + " at " + command.getMethodName(),
                    e);
        }
    }

    private String joinArgs(List<String> args) {
        return Joiner.on(' ').join(args).trim();
    }
}

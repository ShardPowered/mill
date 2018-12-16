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

package me.tassu.mill.parser;

import com.google.common.collect.Sets;
import lombok.val;
import me.tassu.mill.api.ann.Command;
import me.tassu.mill.api.ann.SubCommandFrom;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class MillParser {

    public Set<ParsedSubCommand> parse(Set<? extends Class<?>> classes) {
        val subCommandClasses = Sets.<Class<?>>newHashSet();

        for (Class<?> aClass : classes) {
            if (aClass.isAnnotationPresent(SubCommandFrom.class)
                    && !aClass.isAnnotationPresent(Command.class)) {
                throw new RuntimeException("SubCommandFrom is present but Command is not in " + aClass.getName());
            }

            if (aClass.isAnnotationPresent(SubCommandFrom.class)) {
                Arrays.stream(aClass.getAnnotation(SubCommandFrom.class).value())
                        .map(it -> classes.stream().filter(that -> that.getName().equals(it)).findFirst().orElse(null))
                        .filter(Objects::nonNull)
                        .forEach(subCommandClasses::add);
            }
        }

        val commands = Sets.<ParsedSubCommand>newHashSet();

        for (Class<?> aClass : classes) {
            boolean subCommand = subCommandClasses.contains(aClass);

            for (Method method : aClass.getDeclaredMethods()) {
                method.setAccessible(true);

                val command = aClass.isAnnotationPresent(Command.class)
                        ? aClass.getAnnotation(Command.class)
                        : method.getAnnotation(Command.class);

                commands.addAll(parseMethod(command, aClass, method, subCommand));
            }

            if (aClass.isAnnotationPresent(Command.class) && aClass.isAnnotationPresent(SubCommandFrom.class)) {
                for (String it : aClass.getAnnotation(SubCommandFrom.class).value()) {
                    val bClass = classes.stream().filter(that -> that.getName().equals(it)).findFirst().orElse(null);
                    if (bClass == null) {
                        System.err.println(it);
                        continue;
                    }

                    for (Method method : bClass.getDeclaredMethods()) {
                        method.setAccessible(true);

                        val command = aClass.isAnnotationPresent(Command.class)
                                ? aClass.getAnnotation(Command.class)
                                : method.getAnnotation(Command.class);

                        commands.addAll(parseMethod(command, aClass, method, false));
                    }
                }
            }
        }

        return commands;
    }

    private Set<ParsedSubCommand> parseMethod(Command command, Class<?> aClass, Method method, boolean subCommand) {
        if (Modifier.isStatic(method.getModifiers())) {
            return Sets.newHashSet();
        }

        if (aClass.isAnnotationPresent(Command.class)
                && !method.isAnnotationPresent(Command.class)
                && method.getName().equals("execute")
                && !subCommand) {

            val commands = Sets.<ParsedSubCommand>newHashSet();
            for (String alias : command.value()) {
                commands.add(new ParsedSubCommand(method, alias));
            }

            return commands;
        }

        if (!method.isAnnotationPresent(Command.class)) {
            return Sets.newHashSet();
        }

        val aliases = Sets.<String>newHashSet();
        val prefixes = aClass.isAnnotationPresent(Command.class) ? aClass.getAnnotation(Command.class).value() : new String[] {""};

        Arrays.stream(method.getAnnotation(Command.class).value())
                .filter(it -> !subCommand || it.startsWith("/"))
                .map(it -> {
                    if (it.startsWith("/")) {
                        return Sets.newHashSet(removeSlashIfPresent(it));
                    }

                    val set = Sets.<String>newHashSet();
                    for (val rawPrefix : prefixes) {
                        val prefix = rawPrefix.isEmpty() ? "" : rawPrefix + " ";
                        set.add(removeSlashIfPresent(prefix + it));
                    }

                    return set;
                })
                .forEach(aliases::addAll);

        val commands = Sets.<ParsedSubCommand>newHashSet();
        for (String alias : aliases) {
            commands.add(new ParsedSubCommand(method, alias));
        }

        return commands;

    }

    private String removeSlashIfPresent(String in) {
        if (in.startsWith("/")) {
            return in.substring(1);
        }

        return in;
    }

}

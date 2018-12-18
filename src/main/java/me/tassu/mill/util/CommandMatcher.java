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

package me.tassu.mill.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.ToString;
import lombok.val;
import me.tassu.mill.Mill;
import me.tassu.mill.parser.ParsedSubCommand;
import me.tassu.util.ArrayUtil;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class CommandMatcher {

    public static Set<MatchContext> match(CommandSender sender, Set<ParsedSubCommand> possibilities, String input, Mill mill) {
        val matching = Sets.<MatchContext>newHashSet();
        for (ParsedSubCommand possibility : possibilities) {
            if (!input.toLowerCase().startsWith(possibility.getAlias().toLowerCase())) {
                continue;
            }

            val arguments = getCommandArguments(possibility.getMethod());

            if (arguments.isEmpty() && input.equalsIgnoreCase(possibility.getAlias())) {
                matching.add(MatchContext.of(possibility, getArguments(includeSender(possibility.getMethod()) ? sender : null)));
                continue;
            }

            val parameters = input.substring(possibility.getAlias().length()).trim();

            // check for one string - that's super easy
            if (arguments.size() == 1
                    && arguments.get(0).getType().isAssignableFrom(String.class)
                    && !parameters.isEmpty()) {
                matching.add(MatchContext.of(possibility, getArguments(includeSender(possibility.getMethod()) ? sender : null, parameters)));
                continue;
            }

            val other = argumentsMatch(includeSender(possibility.getMethod()) ? sender : null,
                    arguments, parameters, mill);
            other.ifPresent(objects -> matching.add(MatchContext.of(possibility, objects)));
        }

        return matching;
    }

    @Getter
    @ToString
    public static class MatchContext {

        private MatchContext(ParsedSubCommand command, Object[] args) {
            this.command = command;
            this.args = args;
        }

        private final ParsedSubCommand command;
        private final Object[] args;

        private static MatchContext of(ParsedSubCommand cmd, Object... args) {
            return new MatchContext(cmd, args);
        }

    }

    private static Object[] getArguments(CommandSender includeSender, Object... arguments) {
        val casted = new ArrayList<Object>();

        if (includeSender != null) {
            casted.add(includeSender);
        }

        casted.addAll(Arrays.asList(arguments));
        return casted.toArray();
    }

    private static boolean includeSender(Method method) {
        val list = Lists.newArrayList(method.getParameters());

        if (!list.isEmpty()) {
            return list.get(0).getType().isAssignableFrom(CommandSender.class);
        }

        return false;

    }

    private static List<Parameter> getCommandArguments(Method method) {
        val list = Lists.newArrayList(method.getParameters());

        if (!list.isEmpty()) {
            val first = list.get(0);
            if (first.getType().isAssignableFrom(CommandSender.class)) {
                return ArrayUtil.withoutFirst(list);
            }
        }

        return list;
    }

    private static Optional<Object[]> argumentsMatch(CommandSender includeSender, List<Parameter> parameters, String input, Mill mill) {
        val casted = new ArrayList<Object>();

        if (includeSender != null) {
            casted.add(includeSender);
        }

        val stringIterator = Arrays.stream(input.split(" ")).iterator();
        val parameterIterator = parameters.iterator();

        while (stringIterator.hasNext() && parameterIterator.hasNext()) {
            val string = stringIterator.next();
            val param = parameterIterator.next();

            boolean found = false;
            if (param.getType().isAssignableFrom(String.class)) {
                found = true;
                casted.add(string);
            } else if (param.getType().isAssignableFrom(Integer.TYPE)) {
                if (found = string.matches("\\d+")) {
                    casted.add(Integer.valueOf(string));
                }
            }

            val adapter = mill.getTypeAdapters().stream()
                    .filter(it -> it.getType().isAssignableFrom(param.getType()))
                    .findFirst();

            if (adapter.isPresent()) {
                if (found = adapter.get().matches(string)) {
                    casted.add(adapter.get().convert(string));
                }
            }

            if (!found) return Optional.empty();
        }

        if (stringIterator.hasNext() != parameterIterator.hasNext()) {
            return Optional.empty();
        }

        return Optional.of(casted.toArray());
    }

}

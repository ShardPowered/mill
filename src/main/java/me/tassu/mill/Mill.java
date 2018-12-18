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

package me.tassu.mill;

import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.val;
import me.tassu.easy.EasyPlugin;
import me.tassu.easy.register.core.NotSingleton;
import me.tassu.easy.register.core.RegisterProvider;
import me.tassu.mill.api.type.TypeAdapter;
import me.tassu.mill.parser.MillParser;
import me.tassu.mill.register.MillCommandInstance;
import me.tassu.mill.util.CommandMatcher;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class Mill implements RegisterProvider {

    private Set<TypeAdapter> typeAdapters = Sets.newHashSet();

    @Getter
    private EasyPlugin plugin;

    private MillParser parser;

    private Mill(EasyPlugin plugin, String[] pkgs) {
        try {
            this.plugin = plugin;

            val classes = Sets.<Class<?>>newHashSet();

            for (String pkg : pkgs) {
                classes.addAll(ClassPath.from(getClass().getClassLoader())
                        .getTopLevelClasses(pkg)
                        .stream()
                        .map(ClassPath.ClassInfo::load)
                        .map(it -> (Class<?>) it)
                        .filter(it -> it.isAnnotationPresent(Singleton.class) || it.isAnnotationPresent(NotSingleton.class))
                        .collect(Collectors.toSet()));
            }

            parser = new MillParser();
            parser.parse(classes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new Mill instance
     * @return a new instance
     */
    public static Mill create(EasyPlugin plugin, String... pkgs) {
        return new Mill(plugin, pkgs);
    }

    public Mill withTypeAdapter(TypeAdapter adapter) {
        typeAdapters.add(adapter);
        return this;
    }

    public Set<TypeAdapter> getTypeAdapters() {
        return typeAdapters;
    }

    public void register() {
        plugin.registerProvider(this);
    }

    @Override
    public Object[] getRegistrables() {
        return new Object[] {
                new MillCommandInstance(this, parser.getTopLevelCommands())
        };
    }

    public MillParser getParser() {
        return parser;
    }

    public Set<CommandMatcher.MatchContext> match(CommandSender sender, String input) {
        return CommandMatcher.match(sender, parser.getAllParsedSubCommands(), input, this);
    }

}

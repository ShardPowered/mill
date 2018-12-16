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

import lombok.Getter;
import me.tassu.mill.util.ClassUtil;

import java.lang.reflect.Method;

public class ParsedSubCommand {

    private Method method;

    @Getter
    private String alias;

    public ParsedSubCommand(Method method, String alias) {
        if (alias.startsWith("/")) {
            throw new RuntimeException("Alias starts with a slash.");
        }

        this.method = method;
        this.alias = alias;
    }

    @Override
    public String toString() {
        return "ParsedSubCommand{\n" +
                "  method='" + getMethodName() + "'\n" +
                "  alias='/" + alias + "'\n" +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ParsedSubCommand) {
            return ((ParsedSubCommand) o).getAlias().equals(this.getAlias());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return alias.hashCode();
    }

    public String getMethodName() {
        return ClassUtil.getMethodName(method);
    }

}

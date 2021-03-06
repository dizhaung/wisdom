/*
 * Copyright (c) 2018, Gobinath Loganathan (http://github.com/slgobinath) All Rights Reserved.
 *
 * Gobinath licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. In addition, if you are using
 * this file in your research work, you are required to cite
 * WISDOM as mentioned at https://github.com/slgobinath/wisdom.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.javahelps.wisdom.query.util;

import com.javahelps.wisdom.core.WisdomApp;
import com.javahelps.wisdom.query.antlr.WisdomParserException;
import com.javahelps.wisdom.query.tree.Annotation;
import com.javahelps.wisdom.query.tree.KeyValueElement;
import com.javahelps.wisdom.query.tree.VariableReference;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.text.StringEscapeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Utility {

    private Utility() {

    }

    public static void verifyAnnotation(ParserRuleContext ctx, Annotation annotation, String name, String... properties) {
        if (name.equals(annotation.getName())) {
            for (String key : properties) {
                if (!annotation.hasProperty(key)) {
                    throw new WisdomParserException(ctx, String.format("property not found @%s in @%s", key,
                            annotation.getName()));
                }
            }
        } else {
            throw new WisdomParserException(ctx, String.format("required @%s, but found @%s", name, annotation.getName()));
        }
    }

    /**
     * Parse Wisdom query String constants and unescape special characters.
     *
     * @param str String constant
     * @return unescaped string
     */
    public static String toString(String str) {
        if (str.startsWith("\"\"\"")) {
            str = str.replaceAll("^\"\"\"|\"\"\"$", "");
        } else if (str.startsWith("\"")) {
            str = str.replaceAll("^\"|\"$", "");
        } else if (str.startsWith("'")) {
            str = str.replaceAll("^'|'$", "");
        } else if (str.startsWith("'''")) {
            str = str.replaceAll("^'''|'''$", "");
        }
        str = StringEscapeUtils.unescapeJava(str);
        return str;
    }

    public static Comparable parseNumber(String number) {
        Comparable value;
        if (number.startsWith("0x") || number.startsWith("0X")) {
            number = number.toLowerCase().replaceAll("0x", "");
            value = Long.parseLong(number, 16);
        } else if (number.startsWith("0o") || number.startsWith("0O")) {
            number = number.toLowerCase().replaceAll("0o", "");
            value = Long.parseLong(number, 8);
        } else if (number.startsWith("0b") || number.startsWith("0B")) {
            number = number.toLowerCase().replaceAll("0b", "");
            value = Long.parseLong(number, 2);
        } else if (number.contains(".")) {
            value = Double.valueOf(number);
        } else {
            value = Long.parseLong(number);
        }
        return value;
    }

    public static Map<String, Comparable> toMap(Properties properties) {
        Map<String, Comparable> map = new HashMap<String, Comparable>();
        for (Object key : properties.keySet()) {
            map.put(key.toString(), (Comparable) properties.get(key));
        }
        return map;
    }

    public static Map<String, Object> toProperties(WisdomApp app, List<KeyValueElement> keyValueElements) {
        int count = 0;
        Map<String, Object> properties = new HashMap<>(keyValueElements.size());
        for (KeyValueElement element : keyValueElements) {
            String key = element.getKey();
            if (key == null) {
                key = String.format("_param_%d", count);
            }
            Object value = element.getValue();
            if (value instanceof VariableReference) {
                properties.put(key, ((VariableReference) value).build(app));
            } else {
                properties.put(key, element.getValue());
            }
            count++;
        }
        return properties;
    }
}

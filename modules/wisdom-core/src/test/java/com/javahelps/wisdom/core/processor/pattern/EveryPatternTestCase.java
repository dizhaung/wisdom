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

package com.javahelps.wisdom.core.processor.pattern;

import com.javahelps.wisdom.core.TestUtil;
import com.javahelps.wisdom.core.WisdomApp;
import com.javahelps.wisdom.core.event.Attribute;
import com.javahelps.wisdom.core.operator.Operator;
import com.javahelps.wisdom.core.pattern.Pattern;
import com.javahelps.wisdom.core.query.Query;
import com.javahelps.wisdom.core.util.EventGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.javahelps.wisdom.core.util.Commons.map;

/**
 * Test patterns with 'every' keyword.
 */
public class EveryPatternTestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(EveryPatternTestCase.class);

    @Test
    public void testPattern1() throws InterruptedException {
        LOGGER.info("Test pattern 1 - OUT 2");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // every(e1) -> e2 -> e3
        Pattern e1 = Pattern.every(query.definePattern("StockStream1", "e1")
                .filter(Operator.GREATER_THAN(Attribute.of("price"), 45.0)));
        Pattern e2 = query.definePattern("StockStream2", "e2")
                .filter(Operator.GREATER_THAN(Attribute.of("volume"), 10));
        Pattern e3 = query.definePattern("StockStream2", "e3")
                .filter(Operator.GREATER_THAN(Attribute.of("price"), 50.0));

        Pattern finalPattern = Pattern.followedBy(Pattern.followedBy(e1, e2), e3);


        query.from(finalPattern)
                .select("e1.symbol", "e2.symbol", "e3.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream",
                map("e1.symbol", "IBM", "e2.symbol", "WSO2", "e3.symbol", "ORACLE"),
                map("e1.symbol", "GOOGLE", "e2.symbol", "WSO2", "e3.symbol", "ORACLE"));

        wisdomApp.start();

        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "IBM", "price", 50.0, "volume", 10));
        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "GOOGLE", "price", 60.0, "volume", 15));
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "WSO2", "price", 50.0, "volume", 15));
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "ORACLE", "price", 60.0, "volume", 10));

        Thread.sleep(100);

        Assert.assertEquals("Incorrect number of events", 2, callback.getEventCount());
    }

    @Test
    public void testPattern2() throws InterruptedException {
        LOGGER.info("Test pattern 2 - OUT 2");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // e1 -> every(e2) -> e3
        Pattern e1 = query.definePattern("StockStream1", "e1")
                .filter(Operator.GREATER_THAN(Attribute.of("price"), 45.0));
        Pattern e2 = Pattern.every(query.definePattern("StockStream2", "e2")
                .filter(Operator.GREATER_THAN(Attribute.of("volume"), 10)));
        Pattern e3 = query.definePattern("StockStream2", "e3")
                .filter(Operator.GREATER_THAN(Attribute.of("price"), 50.0));

        Pattern finalPattern = Pattern.followedBy(Pattern.followedBy(e1, e2), e3);


        query.from(finalPattern)
                .select("e1.symbol", "e2.symbol", "e3.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream",
                map("e1.symbol", "IBM", "e2.symbol", "GOOGLE", "e3.symbol", "ORACLE"),
                map("e1.symbol", "IBM", "e2.symbol", "WSO2", "e3.symbol", "ORACLE"));

        wisdomApp.start();

        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "IBM", "price", 50.0, "volume", 10));
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "GOOGLE", "price", 55.0, "volume", 15));
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "WSO2", "price", 60.0, "volume", 20));
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "ORACLE", "price", 65.0, "volume", 5));

        Thread.sleep(100);

        Assert.assertEquals("Incorrect number of events", 2, callback.getEventCount());
    }

    @Test
    public void testPattern3() throws InterruptedException {
        LOGGER.info("Test pattern 1 - OUT 0");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // e1 -> e2 -> every(e3)
        Pattern e1 = query.definePattern("StockStream1", "e1")
                .filter(Operator.GREATER_THAN(Attribute.of("price"), 45.0));
        Pattern e2 = query.definePattern("StockStream2", "e2")
                .filter(Operator.GREATER_THAN(Attribute.of("volume"), 10));
        Pattern e3 = Pattern.every(query.definePattern("StockStream2", "e3")
                .filter(Operator.GREATER_THAN(Attribute.of("price"), 50.0)));

        Pattern finalPattern = Pattern.followedBy(Pattern.followedBy(e1, e2), e3);


        query.from(finalPattern)
                .select("e1.symbol", "e2.symbol", "e3.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream",
                map("e1.symbol", "IBM", "e2.symbol", "WSO2", "e3.symbol", "ORACLE"),
                map("e1.symbol", "IBM", "e2.symbol", "WSO2", "e3.symbol", "MICROSOFT"));

        wisdomApp.start();

        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "IBM", "price", 50.0, "volume", 10));
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "WSO2", "price", 55.0, "volume", 15));
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "ORACLE", "price", 60.0, "volume", 2));
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "MICROSOFT", "price", 65.0, "volume", 5));

        Thread.sleep(100);

        Assert.assertEquals("Incorrect number of events", 2, callback.getEventCount());
    }

    @Test
    public void testPattern4() throws InterruptedException {
        LOGGER.info("Test pattern 4 - OUT 0");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("StockStream3");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // every(e1) -> e2 -> e3
        Pattern e1 = Pattern.every(query.definePattern("StockStream1", "e1")
                .filter(event -> event.get("symbol").equals("IBM")));
        Pattern e2 = query.definePattern("StockStream2", "e2")
                .filter(event -> event.get("symbol").equals("WSO2"));
        Pattern e3 = query.definePattern("StockStream2", "e3")
                .filter(event -> event.get("symbol").equals("ORACLE"));

        Pattern finalPattern = Pattern.followedBy(Pattern.followedBy(e1, e2), e3);


        query.from(finalPattern)
                .select("e1.symbol", "e2.symbol", "e3.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream");

        wisdomApp.start();

        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "WSO2", "price", 50.0, "volume", 15));
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "ORACLE", "price", 60.0, "volume", 10));

        Thread.sleep(100);

        Assert.assertEquals("Incorrect number of events", 0, callback.getEventCount());
    }

    @Test
    public void testPattern5() throws InterruptedException {
        LOGGER.info("Test pattern 5 - OUT 2");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // every(e1 -> e2)
        Pattern e1 = query.definePattern("StockStream1", "e1")
                .filter(Operator.GREATER_THAN(Attribute.of("price"), 45.0));
        Pattern e2 = query.definePattern("StockStream2", "e2")
                .filter(Operator.GREATER_THAN(Attribute.of("volume"), 10));

        Pattern finalPattern = Pattern.every(Pattern.followedBy(e1, e2));


        query.from(finalPattern)
                .select("e1.symbol", "e2.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream",
                map("e1.symbol", "IBM", "e2.symbol", "GOOGLE"),
                map("e1.symbol", "WSO2", "e2.symbol", "ORACLE"));

        wisdomApp.start();

        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "IBM", "price", 50.0, "volume", 10));
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "GOOGLE", "price", 55.0, "volume", 15));
        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "WSO2", "price", 60.0, "volume", 20));
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "ORACLE", "price", 65.0, "volume", 25));

        Thread.sleep(100);

        Assert.assertEquals("Incorrect number of events", 2, callback.getEventCount());
    }

    @Test
    public void testPattern6() throws InterruptedException {
        LOGGER.info("Test pattern 6 - OUT 1");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // every(e1 -> e2)
        Pattern e1 = query.definePattern("StockStream1", "e1")
                .filter(Operator.GREATER_THAN(Attribute.of("price"), 45.0));
        Pattern e2 = query.definePattern("StockStream2", "e2")
                .filter(Operator.GREATER_THAN(Attribute.of("volume"), 10));

        Pattern finalPattern = Pattern.every(Pattern.followedBy(e1, e2));


        query.from(finalPattern)
                .select("e1.symbol", "e2.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream",
                map("e1.symbol", "IBM", "e2.symbol", "GOOGLE"));

        wisdomApp.start();

        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "IBM", "price", 50.0, "volume", 10));
        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "WSO2", "price", 60.0, "volume", 20));
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "GOOGLE", "price", 55.0, "volume", 15));

        Thread.sleep(100);

        Assert.assertEquals("Incorrect number of events", 1, callback.getEventCount());
    }

    @Test
    public void testPattern7() throws InterruptedException {
        LOGGER.info("Test pattern 7 - OUT 1");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // every(e1 -> e2)
        Pattern e1 = query.definePattern("StockStream1", "e1")
                .filter(Operator.GREATER_THAN(Attribute.of("price"), 45.0));
        Pattern e2 = query.definePattern("StockStream2", "e2")
                .filter(Operator.GREATER_THAN(Attribute.of("volume"), 10));

        Pattern finalPattern = Pattern.every(Pattern.followedBy(e1, e2));


        query.from(finalPattern)
                .select("e1.symbol", "e2.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream",
                map("e1.symbol", "IBM", "e2.symbol", "GOOGLE"));

        wisdomApp.start();

        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "IBM", "price", 50.0, "volume", 10));
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "GOOGLE", "price", 55.0, "volume", 15));
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "ORACLE", "price", 65.0, "volume", 25));

        Thread.sleep(100);

        Assert.assertEquals("Incorrect number of events", 1, callback.getEventCount());
    }

    @Test
    public void testPattern8() throws InterruptedException {
        LOGGER.info("Test pattern 8 - OUT 2");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // every(e1)
        Pattern e1 = Pattern.every(query.definePattern("StockStream1", "e1")
                .filter(event -> event.get("symbol").equals("IBM")));

        query.from(e1)
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream", map
                        ("e1.symbol", "IBM", "e1.price", 50.0, "e1.volume", 10)
                , map("e1.symbol", "IBM", "e1.price", 55.0, "e1.volume", 15));

        wisdomApp.start();

        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "IBM", "price", 50.0, "volume", 10));
        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "IBM", "price", 55.0, "volume", 15));

        Thread.sleep(100);

        Assert.assertEquals("Incorrect number of events", 2, callback.getEventCount());
    }

    @Test
    public void testPattern9() throws InterruptedException {
        LOGGER.info("Test pattern 9 - OUT 2");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // every(e1 -> e2) -> e3
        Pattern e1 = query.definePattern("StockStream1", "e1")
                .filter(Operator.GREATER_THAN(Attribute.of("price"), 10.0));
        Pattern e2 = query.definePattern("StockStream1", "e2")
                .filter(Operator.GREATER_THAN(Attribute.of("price"), 20.0));
        Pattern e3 = query.definePattern("StockStream2", "e3")
                .filter(Operator.GREATER_THAN(Attribute.of("price"), e2.attribute("e1.price")));

        Pattern finalPattern = Pattern.followedBy(Pattern.every(Pattern.followedBy(e1, e2)), e3);

        query.from(finalPattern)
                .select("e1.symbol", "e2.symbol", "e3.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream",
                map("e1.symbol", "WSO2", "e2.symbol", "GOOGLE", "e3.symbol", "IBM"),
                map("e1.symbol", "WSO2", "e2.symbol", "GOOGLE", "e3.symbol", "IBM"));

        wisdomApp.start();

        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "WSO2", "price", 55.6, "volume", 100));
        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "GOOGLE", "price", 54.0, "volume", 100));
        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "WSO2", "price", 53.6, "volume", 100));
        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "GOOGLE", "price", 53.0, "volume", 100));
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "IBM", "price", 57.7, "volume", 100));

        Thread.sleep(100);

        Assert.assertEquals("Incorrect number of events", 2, callback.getEventCount());
    }

    @Test
    public void testPattern10() throws InterruptedException {
        LOGGER.info("Test pattern 10 - OUT 2");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // e1 -> every(e2 -> e3) -> e4
        Pattern e1 = query.definePattern("StockStream1", "e1")
                .filter(Operator.EQUALS(Attribute.of("symbol"), "MSFT"));
        Pattern e2 = query.definePattern("StockStream1", "e2")
                .filter(Operator.GREATER_THAN(Attribute.of("price"), 20.0));
        Pattern e3 = query.definePattern("StockStream1", "e3")
                .filter(Operator.GREATER_THAN(Attribute.of("price"), 20.0));
        Pattern e4 = query.definePattern("StockStream2", "e4")
                .filter(Operator.GREATER_THAN(Attribute.of("price"), e2.attribute("e2.price")));

        Pattern finalPattern = Pattern.followedBy(e1,
                Pattern.followedBy(Pattern.every(Pattern.followedBy(e2, e3)), e4));

        query.from(finalPattern)
                .select("e1.price", "e2.price", "e3.price", "e4.price")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream",
                map("e1.price", 55.6, "e2.price", 55.7, "e3.price", 54.0, "e4.price", 57.7),
                map("e1.price", 55.6, "e2.price", 53.6, "e3.price", 53.0, "e4.price", 57.7));

        wisdomApp.start();

        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "MSFT", "price", 55.6, "volume", 100));
        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "WSO2", "price", 55.7, "volume", 100));
        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "GOOGLE", "price", 54.0, "volume", 100));
        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "WSO2", "price", 53.6, "volume", 100));
        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "GOOGLE", "price", 53.0, "volume", 100));
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "IBM", "price", 57.7, "volume", 100));

        Thread.sleep(100);

        Assert.assertEquals("Incorrect number of events", 2, callback.getEventCount());
    }

    @Test
    public void testPattern11() throws InterruptedException {
        LOGGER.info("Test pattern 11 - OUT 2");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // every(e1) -> e2
        Pattern e1 = Pattern.every(query.definePattern("StockStream1", "e1")
                .filter(Operator.LESS_THAN(Attribute.of("price"), 20.0)));
        Pattern e2 = query.definePattern("StockStream1", "e2")
                .filter(Operator.GREATER_THAN(Attribute.of("price"), e1.attribute("e1.price")));

        Pattern finalPattern = Pattern.followedBy(e1, e2);

        query.from(finalPattern)
                .select("e1.symbol", "e2.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream",
                map("e1.symbol", "WSO2", "e2.symbol", "ORACLE"),
                map("e1.symbol", "GOOGLE", "e2.symbol", "ORACLE"));

        wisdomApp.start();

        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "WSO2", "price", 15.6, "volume", 100));
        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "GOOGLE", "price", 14.0, "volume", 100));
        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "ORACLE", "price", 33.6, "volume", 100));

        Thread.sleep(100);

        Assert.assertEquals("Incorrect number of events", 2, callback.getEventCount());
    }
}

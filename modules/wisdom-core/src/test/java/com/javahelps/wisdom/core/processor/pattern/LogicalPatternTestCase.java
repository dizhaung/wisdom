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

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static com.javahelps.wisdom.core.util.Commons.map;

/**
 * Test logical patterns of Wisdom which includes AND, OR & NOT.
 */
public class LogicalPatternTestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogicalPatternTestCase.class);

    @Test
    public void testPattern1() throws InterruptedException {
        LOGGER.info("Test pattern 1 - OUT 1");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("StockStream3");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // e1 and e2 -> e3
        Pattern e1 = query.definePattern("StockStream1", "e1")
                .filter(event -> event.get("symbol").equals("IBM"));
        Pattern e2 = query.definePattern("StockStream2", "e2")
                .filter(event -> event.get("symbol").equals("WSO2"));
        Pattern e3 = query.definePattern("StockStream3", "e3")
                .filter(event -> event.get("symbol").equals("ORACLE"));

        Pattern finalPattern = Pattern.followedBy(Pattern.and(e1, e2), e3);

        query.from(finalPattern)
                .select("e1.symbol", "e2.symbol", "e3.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream", map("e1.symbol", "IBM", "e2.symbol", "WSO2", "e3.symbol", "ORACLE"));

        wisdomApp.start();

        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "IBM", "price", 50.0, "volume", 10));
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "WSO2", "price", 50.0, "volume", 15));
        wisdomApp.send("StockStream3", EventGenerator.generate("symbol", "ORACLE", "price", 60.0, "volume", 10));

        Thread.sleep(100);

        wisdomApp.shutdown();

        Assert.assertEquals("Incorrect number of events", 1, callback.getEventCount());
    }

    @Test
    public void testPattern2() throws InterruptedException {
        LOGGER.info("Test pattern 2 - OUT 0");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("StockStream3");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // e1 and e2 -> e3
        Pattern e1 = query.definePattern("StockStream1", "e1")
                .filter(event -> event.get("symbol").equals("IBM"));
        Pattern e2 = query.definePattern("StockStream2", "e2")
                .filter(event -> event.get("symbol").equals("WSO2"));
        Pattern e3 = query.definePattern("StockStream3", "e3")
                .filter(event -> event.get("symbol").equals("ORACLE"));

        Pattern finalPattern = Pattern.followedBy(Pattern.and(e1, e2), e3);

        query.from(finalPattern)
                .select("e1.symbol", "e2.symbol", "e3.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream");

        wisdomApp.start();

        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "IBM", "price", 50.0, "volume", 10));
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "ORACLE", "price", 60.0, "volume", 10));

        Thread.sleep(100);

        wisdomApp.shutdown();

        Assert.assertEquals("Incorrect number of events", 0, callback.getEventCount());
    }

    @Test
    public void testPattern3() throws InterruptedException {
        LOGGER.info("Test pattern 3 - OUT 0");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("StockStream3");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // e1 and e2 -> e3
        Pattern e1 = query.definePattern("StockStream1", "e1")
                .filter(event -> event.get("symbol").equals("IBM"));
        Pattern e2 = query.definePattern("StockStream2", "e2")
                .filter(event -> event.get("symbol").equals("WSO2"));
        Pattern e3 = query.definePattern("StockStream3", "e3")
                .filter(event -> event.get("symbol").equals("ORACLE"));

        Pattern finalPattern = Pattern.followedBy(Pattern.and(e1, e2), e3);

        query.from(finalPattern)
                .select("e1.symbol", "e2.symbol", "e3.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream");

        wisdomApp.start();

        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "IBM", "price", 50.0, "volume", 10));
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "WSO2", "price", 50.0, "volume", 15));

        Thread.sleep(100);

        wisdomApp.shutdown();

        Assert.assertEquals("Incorrect number of events", 0, callback.getEventCount());
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

        // e1 and e2 -> e3
        Pattern e1 = query.definePattern("StockStream1", "e1")
                .filter(event -> event.get("symbol").equals("IBM"));
        Pattern e2 = query.definePattern("StockStream2", "e2")
                .filter(event -> event.get("symbol").equals("WSO2"));
        Pattern e3 = query.definePattern("StockStream3", "e3")
                .filter(event -> event.get("symbol").equals("ORACLE"));

        Pattern finalPattern = Pattern.followedBy(Pattern.and(e1, e2), e3);

        query.from(finalPattern)
                .select("e1.symbol", "e2.symbol", "e3.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream");

        wisdomApp.start();

        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "WSO2", "price", 50.0, "volume", 15));
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "ORACLE", "price", 60.0, "volume", 10));

        Thread.sleep(100);

        wisdomApp.shutdown();

        Assert.assertEquals("Incorrect number of events", 0, callback.getEventCount());
    }

    @Test
    public void testPattern5() throws InterruptedException {
        LOGGER.info("Test pattern 5 - OUT 1");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("StockStream3");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // e1 or e2 -> e3
        Pattern e1 = query.definePattern("StockStream1", "e1")
                .filter(event -> event.get("symbol").equals("IBM"));
        Pattern e2 = query.definePattern("StockStream2", "e2")
                .filter(event -> event.get("symbol").equals("WSO2"));
        Pattern e3 = query.definePattern("StockStream3", "e3")
                .filter(event -> event.get("symbol").equals("ORACLE"));

        Pattern finalPattern = Pattern.followedBy(Pattern.or(e1, e2), e3);

        query.from(finalPattern)
                .select("e1.symbol", "e2.symbol", "e3.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream", map("e1.symbol", "IBM", "e2.symbol", "WSO2", "e3.symbol", "ORACLE"));

        wisdomApp.start();

        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "IBM", "price", 50.0, "volume", 10));
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "WSO2", "price", 50.0, "volume", 15));
        wisdomApp.send("StockStream3", EventGenerator.generate("symbol", "ORACLE", "price", 60.0, "volume", 10));

        Thread.sleep(100);

        wisdomApp.shutdown();

        Assert.assertEquals("Incorrect number of events", 1, callback.getEventCount());
    }

    @Test
    public void testPattern6() throws InterruptedException {
        LOGGER.info("Test pattern 6 - OUT 1");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("StockStream3");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // e1 or e2 -> e3
        Pattern e1 = query.definePattern("StockStream1", "e1")
                .filter(event -> event.get("symbol").equals("IBM"));
        Pattern e2 = query.definePattern("StockStream2", "e2")
                .filter(event -> event.get("symbol").equals("WSO2"));
        Pattern e3 = query.definePattern("StockStream3", "e3")
                .filter(event -> event.get("symbol").equals("ORACLE"));

        Pattern finalPattern = Pattern.followedBy(Pattern.or(e1, e2), e3);

        query.from(finalPattern)
                .select("e1.symbol", "e2.symbol", "e3.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream", map("e1.symbol", "IBM", "e3.symbol", "ORACLE"));

        wisdomApp.start();

        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "IBM", "price", 50.0, "volume", 10));
        wisdomApp.send("StockStream3", EventGenerator.generate("symbol", "ORACLE", "price", 60.0, "volume", 10));

        Thread.sleep(100);

        wisdomApp.shutdown();

        Assert.assertEquals("Incorrect number of events", 1, callback.getEventCount());
    }

    @Test
    public void testPattern7() throws InterruptedException {
        LOGGER.info("Test pattern 7 - OUT 1");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("StockStream3");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // e1 or e2 -> e3
        Pattern e1 = query.definePattern("StockStream1", "e1")
                .filter(event -> event.get("symbol").equals("IBM"));
        Pattern e2 = query.definePattern("StockStream2", "e2")
                .filter(event -> event.get("symbol").equals("WSO2"));
        Pattern e3 = query.definePattern("StockStream3", "e3")
                .filter(event -> event.get("symbol").equals("ORACLE"));

        Pattern finalPattern = Pattern.followedBy(Pattern.or(e1, e2), e3);

        query.from(finalPattern)
                .select("e1.symbol", "e2.symbol", "e3.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream", map("e2.symbol", "WSO2", "e3.symbol", "ORACLE"));

        wisdomApp.start();

        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "WSO2", "price", 50.0, "volume", 15));
        wisdomApp.send("StockStream3", EventGenerator.generate("symbol", "ORACLE", "price", 60.0, "volume", 10));

        Thread.sleep(100);

        wisdomApp.shutdown();

        Assert.assertEquals("Incorrect number of events", 1, callback.getEventCount());
    }

    @Test
    public void testPattern8() throws InterruptedException {
        LOGGER.info("Test pattern 8 - OUT 0");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("StockStream3");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // e1 or e2 -> e3
        Pattern e1 = query.definePattern("StockStream1", "e1")
                .filter(event -> event.get("symbol").equals("IBM"));
        Pattern e2 = query.definePattern("StockStream2", "e2")
                .filter(event -> event.get("symbol").equals("WSO2"));
        Pattern e3 = query.definePattern("StockStream3", "e3")
                .filter(event -> event.get("symbol").equals("ORACLE"));

        Pattern finalPattern = Pattern.followedBy(Pattern.or(e1, e2), e3);

        query.from(finalPattern)
                .select("e1.symbol", "e2.symbol", "e3.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream");

        wisdomApp.start();

        wisdomApp.send("StockStream3", EventGenerator.generate("symbol", "ORACLE", "price", 60.0, "volume", 10));

        Thread.sleep(100);

        wisdomApp.shutdown();

        Assert.assertEquals("Incorrect number of events", 0, callback.getEventCount());

    }

    @Test
    public void testPattern9() {
        LOGGER.info("Test pattern 9 - OUT 1");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("StockStream3");
        wisdomApp.defineStream("StockStream4");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // (e1 and e2) or e3 -> e4
        Pattern e1 = query.definePattern("StockStream1", "e1")
                .filter(event -> event.get("symbol").equals("IBM"));
        Pattern e2 = query.definePattern("StockStream2", "e2")
                .filter(event -> event.get("symbol").equals("WSO2"));
        Pattern e3 = query.definePattern("StockStream3", "e3")
                .filter(event -> event.get("symbol").equals("ORACLE"));
        Pattern e4 = query.definePattern("StockStream4", "e4")
                .filter(event -> event.get("symbol").equals("MICROSOFT"));

        Pattern finalPattern = Pattern.followedBy(Pattern.or(Pattern.and(e1, e2), e3), e4);

        query.from(finalPattern)
                .select("e1.symbol", "e2.symbol", "e3.symbol", "e4.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream", map("e1.symbol", "IBM", "e2.symbol", "WSO2", "e3.symbol", "ORACLE",
                "e4.symbol", "MICROSOFT"));

        wisdomApp.start();

        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "IBM", "price", 50.0, "volume", 10));
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "WSO2", "price", 50.0, "volume", 15));
        wisdomApp.send("StockStream3", EventGenerator.generate("symbol", "ORACLE", "price", 60.0, "volume", 10));
        wisdomApp.send("StockStream4", EventGenerator.generate("symbol", "MICROSOFT", "price", 60.0, "volume", 10));

        wisdomApp.shutdown();

        Assert.assertEquals("Incorrect number of events", 1, callback.getEventCount());
    }

    @Test
    public void testPattern10() {
        LOGGER.info("Test pattern 10 - OUT 1");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // not e1 -> e2
        Pattern e1 = query.definePattern("StockStream1", "e1")
                .filter(event -> event.get("symbol").equals("IBM"));
        Pattern e2 = query.definePattern("StockStream2", "e2")
                .filter(event -> event.get("symbol").equals("WSO2"));

        Pattern finalPattern = Pattern.followedBy(Pattern.not(e1), e2);

        query.from(finalPattern)
                .select("e1.symbol", "e2.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream", map("e2.symbol", "WSO2"));

        wisdomApp.start();

        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "WSO2", "price", 50.0, "volume", 15));

        wisdomApp.shutdown();

        Assert.assertEquals("Incorrect number of events", 1, callback.getEventCount());
    }

    @Test
    public void testPattern11() {
        LOGGER.info("Test pattern 11 - OUT 0");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // not e1 -> e2
        Pattern e1 = query.definePattern("StockStream1", "e1")
                .filter(event -> event.get("symbol").equals("IBM"));
        Pattern e2 = query.definePattern("StockStream2", "e2")
                .filter(event -> event.get("symbol").equals("WSO2"));

        Pattern finalPattern = Pattern.followedBy(Pattern.not(e1), e2);

        query.from(finalPattern)
                .select("e1.symbol", "e2.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream");

        wisdomApp.start();

        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "IBM", "price", 50.0, "volume", 10));
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "WSO2", "price", 50.0, "volume", 15));

        wisdomApp.shutdown();

        Assert.assertEquals("Incorrect number of events", 0, callback.getEventCount());
    }

    @Test
    public void testPattern12() throws InterruptedException {
        LOGGER.info("Test pattern 12 - OUT 1");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // e1 -> not e2
        Pattern e1 = query.definePattern("StockStream1", "e1")
                .filter(event -> event.get("symbol").equals("IBM"));
        Pattern e2 = query.definePattern("StockStream2", "e2")
                .filter(event -> event.get("symbol").equals("WSO2"));

        Pattern finalPattern = Pattern.followedBy(e1, Pattern.not(e2).within(Duration.of(1, ChronoUnit.SECONDS)));

        query.from(finalPattern)
                .select("e1.symbol", "e2.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream", map("e1.symbol", "IBM"));

        wisdomApp.start();


        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "IBM", "price", 50.0, "volume", 10));

        Thread.sleep(1100);

        wisdomApp.shutdown();

        Assert.assertEquals("Incorrect number of events", 1, callback.getEventCount());
    }

    @Test
    public void testPattern13() throws InterruptedException {
        LOGGER.info("Test pattern 13 - OUT 1");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("StockStream3");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // e1 -> not e2 -> e3
        Pattern e1 = query.definePattern("StockStream1", "e1")
                .filter(event -> event.get("symbol").equals("IBM"));
        Pattern e2 = query.definePattern("StockStream2", "e2")
                .filter(event -> event.get("symbol").equals("WSO2"));
        Pattern e3 = query.definePattern("StockStream3", "e3")
                .filter(Operator.GREATER_THAN_OR_EQUAL(Attribute.of("price"), e1.attribute("e1.price")));

        Pattern finalPattern = Pattern.followedBy(Pattern.followedBy(e1, Pattern.not(e2)), e3);

        query.from(finalPattern)
                .select("e1.symbol", "e3.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream", map("e1.symbol", "IBM", "e3.symbol", "WSO2"));

        wisdomApp.start();


        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "IBM", "price", 50.0, "volume", 10));
        wisdomApp.send("StockStream3", EventGenerator.generate("symbol", "WSO2", "price", 60.0, "volume", 15));

        Thread.sleep(100);

        wisdomApp.shutdown();

        Assert.assertEquals("Incorrect number of events", 1, callback.getEventCount());
    }

    @Test
    public void testPattern14() throws InterruptedException {
        LOGGER.info("Test pattern 14 - OUT 0");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("StockStream3");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // e1 -> not e2 -> e3
        Pattern e1 = query.definePattern("StockStream1", "e1")
                .filter(event -> event.get("symbol").equals("IBM"));
        Pattern e2 = query.definePattern("StockStream2", "e2")
                .filter(event -> event.get("symbol").equals("WSO2"));
        Pattern e3 = query.definePattern("StockStream3", "e3")
                .filter(Operator.GREATER_THAN_OR_EQUAL(Attribute.of("price"), e1.attribute("e1.price")));

        Pattern finalPattern = Pattern.followedBy(Pattern.followedBy(e1, Pattern.not(e2)), e3);

        query.from(finalPattern)
                .select("e1.symbol", "e3.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream");

        wisdomApp.start();


        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "IBM", "price", 50.0, "volume", 10));
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "WSO2", "price", 55.0, "volume", 12));
        wisdomApp.send("StockStream3", EventGenerator.generate("symbol", "WSO2", "price", 60.0, "volume", 15));

        Thread.sleep(100);

        wisdomApp.shutdown();

        Assert.assertEquals("Incorrect number of events", 0, callback.getEventCount());
    }

    @Test
    public void testPattern15() {
        LOGGER.info("Test pattern 15 - OUT 1");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("StockStream3");
        wisdomApp.defineStream("StockStream4");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // (e1 and e2) or e3 -> e4
        Pattern e1 = query.definePattern("StockStream1", "e1")
                .filter(event -> event.get("symbol").equals("IBM"));
        Pattern e2 = query.definePattern("StockStream2", "e2")
                .filter(event -> event.get("symbol").equals("WSO2"));
        Pattern e3 = query.definePattern("StockStream3", "e3")
                .filter(event -> event.get("symbol").equals("ORACLE"));
        Pattern e4 = query.definePattern("StockStream4", "e4")
                .filter(event -> event.get("symbol").equals("MICROSOFT"));

        Pattern finalPattern = Pattern.followedBy(Pattern.or(Pattern.and(e1, e2), e3), e4);

        query.from(finalPattern)
                .select("e1.symbol", "e2.symbol", "e3.symbol", "e4.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream", map("e1.symbol", "IBM", "e2.symbol", "WSO2", "e4.symbol", "MICROSOFT"));

        wisdomApp.start();

        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "IBM", "price", 50.0, "volume", 10));
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "WSO2", "price", 50.0, "volume", 15));
        wisdomApp.send("StockStream4", EventGenerator.generate("symbol", "MICROSOFT", "price", 60.0, "volume", 10));

        wisdomApp.shutdown();

        Assert.assertEquals("Incorrect number of events", 1, callback.getEventCount());
    }

    @Test
    public void testPattern16() {
        LOGGER.info("Test pattern 16 - OUT 1");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("StockStream3");
        wisdomApp.defineStream("StockStream4");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // (e1 and e2) or e3 -> e4
        Pattern e1 = query.definePattern("StockStream1", "e1")
                .filter(event -> event.get("symbol").equals("IBM"));
        Pattern e2 = query.definePattern("StockStream2", "e2")
                .filter(event -> event.get("symbol").equals("WSO2"));
        Pattern e3 = query.definePattern("StockStream3", "e3")
                .filter(event -> event.get("symbol").equals("ORACLE"));
        Pattern e4 = query.definePattern("StockStream4", "e4")
                .filter(event -> event.get("symbol").equals("MICROSOFT"));

        Pattern finalPattern = Pattern.followedBy(Pattern.or(Pattern.and(e1, e2), e3), e4);

        query.from(finalPattern)
                .select("e1.symbol", "e2.symbol", "e3.symbol", "e4.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream", map("e3.symbol", "ORACLE", "e4.symbol", "MICROSOFT"));

        wisdomApp.start();

        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "IBM", "price", 50.0, "volume", 10));
        wisdomApp.send("StockStream3", EventGenerator.generate("symbol", "ORACLE", "price", 60.0, "volume", 10));
        wisdomApp.send("StockStream4", EventGenerator.generate("symbol", "MICROSOFT", "price", 60.0, "volume", 10));

        wisdomApp.shutdown();

        Assert.assertEquals("Incorrect number of events", 1, callback.getEventCount());
    }

    @Test
    public void testPattern17() throws InterruptedException {
        LOGGER.info("Test pattern 17 - OUT 1");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("StockStream3");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // e1 and e2 and e3
        Pattern e1 = query.definePattern("StockStream1", "e1")
                .filter(event -> event.get("symbol").equals("IBM"));
        Pattern e2 = query.definePattern("StockStream2", "e2")
                .filter(event -> event.get("symbol").equals("WSO2"));
        Pattern e3 = query.definePattern("StockStream3", "e3")
                .filter(event -> event.get("symbol").equals("ORACLE"));

        Pattern finalPattern = Pattern.and(Pattern.and(e1, e2), e3);

        query.from(finalPattern)
                .select("e1.symbol", "e2.symbol", "e3.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream", map("e1.symbol", "IBM", "e2.symbol", "WSO2", "e3.symbol", "ORACLE"));

        wisdomApp.start();

        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "WSO2", "price", 50.0, "volume", 15));
        wisdomApp.send("StockStream3", EventGenerator.generate("symbol", "ORACLE", "price", 60.0, "volume", 10));
        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "IBM", "price", 50.0, "volume", 10));

        Thread.sleep(100);

        wisdomApp.shutdown();

        Assert.assertEquals("Incorrect number of events", 1, callback.getEventCount());
    }

    @Test
    public void testPattern18() throws InterruptedException {
        LOGGER.info("Test pattern 18 - OUT 1");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("StockStream3");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // (e1 and e2) or e3
        Pattern e1 = query.definePattern("StockStream1", "e1")
                .filter(event -> event.get("symbol").equals("IBM"));
        Pattern e2 = query.definePattern("StockStream2", "e2")
                .filter(event -> event.get("symbol").equals("WSO2"));
        Pattern e3 = query.definePattern("StockStream3", "e3")
                .filter(event -> event.get("symbol").equals("ORACLE"));

        Pattern finalPattern = Pattern.or(Pattern.and(e1, e2), e3);

        query.from(finalPattern)
                .select("e1.symbol", "e2.symbol", "e3.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream", map("e3.symbol", "ORACLE"));

        wisdomApp.start();

        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "WSO2", "price", 50.0, "volume", 15));
        wisdomApp.send("StockStream3", EventGenerator.generate("symbol", "ORACLE", "price", 60.0, "volume", 10));

        Thread.sleep(100);

        wisdomApp.shutdown();

        Assert.assertEquals("Incorrect number of events", 1, callback.getEventCount());
    }

    @Test
    public void testPattern19() throws InterruptedException {
        LOGGER.info("Test pattern 19 - OUT 1");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("StockStream3");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // e1 -> not e2 -> e3
        Pattern e1 = query.definePattern("StockStream1", "e1")
                .filter(event -> event.get("symbol").equals("IBM"));
        Pattern e2 = query.definePattern("StockStream2", "e2")
                .filter(event -> event.get("symbol").equals("WSO2"));
        Pattern e3 = query.definePattern("StockStream3", "e3")
                .filter(Operator.GREATER_THAN_OR_EQUAL(Attribute.of("price"), e1.attribute("e1.price")));

        Pattern finalPattern = Pattern.followedBy(e1, Pattern.followedBy(Pattern.not(e2), e3));
//        Pattern finalPattern = Pattern.followedBy(Pattern.followedBy(e1, Pattern.not(e2)), e3);

        query.from(finalPattern)
                .select("e1.price", "e3.price")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream",
                map("e1.price", 51.0, "e3.price", 61.0));

        wisdomApp.start();


        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "IBM", "price", 50.0, "volume", 10));
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "WSO2", "price", 55.0, "volume", 12));
        wisdomApp.send("StockStream3", EventGenerator.generate("symbol", "WSO2", "price", 60.0, "volume", 15));

        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "IBM", "price", 51.0, "volume", 10));
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "ORACLE", "price", 56.0, "volume", 12));
        wisdomApp.send("StockStream3", EventGenerator.generate("symbol", "MICROSOFT", "price", 61.0, "volume", 15));

        Thread.sleep(100);

        wisdomApp.shutdown();

        Assert.assertEquals("Incorrect number of events", 1, callback.getEventCount());
    }
}

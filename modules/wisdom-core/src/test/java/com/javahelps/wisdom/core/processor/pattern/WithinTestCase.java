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
import com.javahelps.wisdom.core.pattern.Pattern;
import com.javahelps.wisdom.core.query.Query;
import com.javahelps.wisdom.core.util.EventGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.javahelps.wisdom.core.util.Commons.map;

/**
 * Test general patterns of Wisdom.
 */
public class WithinTestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(WithinTestCase.class);

    @Test
    public void testWithin1() throws InterruptedException {
        LOGGER.info("Test within 1 - OUT 1");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // e1 -> e2 within 100 milliseconds
        Pattern e1 = query.definePattern("StockStream1", "e1")
                .filter(event -> event.get("symbol").equals("IBM"));
        Pattern e2 = query.definePattern("StockStream2", "e2")
                .filter(event -> event.get("symbol").equals("WSO2"));

        Pattern finalPattern = Pattern.followedBy(e1, e2).within(Duration.ofMillis(100));


        query.from(finalPattern)
                .select("e1.symbol", "e2.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream", map
                ("e1.symbol", "IBM", "e2.symbol", "WSO2"));

        wisdomApp.start();

        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "IBM", "price", 50.0, "volume", 10));
        Thread.sleep(90);
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "WSO2", "price", 50.0, "volume", 15));

        Thread.sleep(100);

        Assert.assertEquals("Incorrect number of events", 1, callback.getEventCount());
    }

    @Test
    public void testWithin2() throws InterruptedException {
        LOGGER.info("Test within 2 - OUT 0");

        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream1");
        wisdomApp.defineStream("StockStream2");
        wisdomApp.defineStream("OutputStream");

        Query query = wisdomApp.defineQuery("query1");

        // e1 -> e2 within 100 milliseconds
        Pattern e1 = query.definePattern("StockStream1", "e1")
                .filter(event -> event.get("symbol").equals("IBM"));
        Pattern e2 = query.definePattern("StockStream2", "e2")
                .filter(event -> event.get("symbol").equals("WSO2"));

        Pattern finalPattern = Pattern.followedBy(e1, e2).within(Duration.ofMillis(100));

        query.from(finalPattern)
                .select("e1.symbol", "e2.symbol")
                .insertInto("OutputStream");

        TestUtil.TestCallback callback = TestUtil.addStreamCallback(LOGGER, wisdomApp, "OutputStream");

        wisdomApp.start();

        wisdomApp.send("StockStream1", EventGenerator.generate("symbol", "IBM", "price", 50.0, "volume", 10));
        Thread.sleep(200);
        wisdomApp.send("StockStream2", EventGenerator.generate("symbol", "WSO2", "price", 50.0, "volume", 15));

        Thread.sleep(100);

        Assert.assertEquals("Incorrect number of events", 0, callback.getEventCount());
    }
}

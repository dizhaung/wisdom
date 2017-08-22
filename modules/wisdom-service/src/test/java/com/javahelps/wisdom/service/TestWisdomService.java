package com.javahelps.wisdom.service;

import com.javahelps.wisdom.core.WisdomApp;
import com.javahelps.wisdom.service.client.WisdomClient;
import com.javahelps.wisdom.service.util.TestUtil;
import org.apache.http.HttpResponse;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.javahelps.wisdom.service.util.TestUtil.map;

public class TestWisdomService {

    @Test
    public void test() throws IOException, InterruptedException {

        long testServerWaitingTime = 5_000L;

        // Create the output file
        Files.deleteIfExists(Paths.get("test_server_output.log"));
        Files.createFile(Paths.get("test_server_output.log"));

        TestUtil.execTestServer(testServerWaitingTime);

        Thread.sleep(100);

        // Create a WisdomApp
        WisdomApp wisdomApp = new WisdomApp();
        wisdomApp.defineStream("StockStream");
        wisdomApp.defineStream("OutputStream");

        wisdomApp.defineQuery("query1")
                .from("StockStream")
                .select("symbol", "price")
                .insertInto("OutputStream");

        // Create a WisdomService
        WisdomService wisdomService = new WisdomService(wisdomApp, 8080);
        wisdomService.addSource("StockStream");
        wisdomService.addSink("OutputStream", "http://localhost:9999/streamReceiver");
        wisdomService.start();

        // Let the server to start
        Thread.sleep(100);

        WisdomClient client = new WisdomClient("localhost", 8080);

        HttpResponse response = client.send("StockStream", map("symbol", "IBM", "price", 50.0, "volume", 10));
        Assert.assertEquals("Failed to send input", 202, response.getStatusLine().getStatusCode());

        response = client.send("StockStream", map("symbol", "WSO2", "price", 60.0, "volume", 15));
        Assert.assertEquals("Failed to send input", 202, response.getStatusLine().getStatusCode());

        Thread.sleep(testServerWaitingTime);

        List<String> lines = Files.readAllLines(Paths.get("test_server_output.log"));
        lines.removeIf(line -> line.startsWith("INFO"));

        Assert.assertTrue(lines.get(0).contains("IBM"));
        Assert.assertTrue(lines.get(1).contains("WSO2"));

        wisdomService.shutdown();
    }

}
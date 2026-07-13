package org.wso2.carbon.apimgt.impl.wsdl;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class PolicyGatedSchemaFetcherTest {

    // allowed fetch returns body
    @Test
    public void fetchesAllowedUrl() throws Exception {
        HttpServer s = server(ctx -> respond(ctx, 200, "HELLO"));
        try {
            String body = read(new PolicyGatedSchemaFetcher("carbon.super", (u, t) -> { /* allow all */ })
                    .fetch(base(s) + "/x.xsd"));
            assertEquals("HELLO", body);
        } finally {
            s.stop(0);
        }
    }

    // untrusted host: validator throws -> propagates, host never fetched
    @Test(expected = APIManagementException.class)
    public void blocksUntrustedHost() throws Exception {
        new PolicyGatedSchemaFetcher("carbon.super",
                (u, t) -> { throw new APIManagementException(ExceptionCodes.UNTRUSTED_URL); })
                .fetch("http://169.254.169.254/x.xsd");
    }

    // size cap actually rejects oversized bodies (inject a small cap; do not loosen prod default)
    @Test
    public void rejectsOversizedBody() throws Exception {
        HttpServer s = server(ctx -> respond(ctx, 200, "X".repeat(10_000)));
        try {
            InputStream in = new PolicyGatedSchemaFetcher("carbon.super", (u, t) -> {}, 100L)
                    .fetch(base(s) + "/big.xsd");
            try {
                read(in);
                fail("expected size-limit failure");
            } catch (Exception expected) {
                // SizeLimitedInputStream aborts past the cap
            }
        } finally {
            s.stop(0);
        }
    }

    // ---- test helpers ----

    private static HttpServer server(HttpHandler handler) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", handler);
        server.start();
        return server;
    }

    private static String base(HttpServer s) {
        return "http://127.0.0.1:" + s.getAddress().getPort();
    }

    private static void respond(HttpExchange ctx, int status, String body) {
        try {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            ctx.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = ctx.getResponseBody()) {
                os.write(bytes);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            ctx.close();
        }
    }

    private static String read(InputStream in) throws IOException {
        try {
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}

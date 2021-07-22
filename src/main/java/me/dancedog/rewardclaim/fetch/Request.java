package me.dancedog.rewardclaim.fetch;

import me.dancedog.rewardclaim.RewardClaim;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by DanceDog / Ben on 3/29/20 @ 8:38 AM
 */
public class Request {

    private static final Map<String, String> DEFAULT_HEADERS = new ConcurrentHashMap<>();

    static {
        DEFAULT_HEADERS.put("Accept", "*/*");
        DEFAULT_HEADERS.put("Content-Length", "0");
        DEFAULT_HEADERS.put("User-Agent", RewardClaim.MODID + "/" + RewardClaim.VERSION + " (Minecraft Forge Modification)");
    }

    private final URL url;
    private final Method method;
    private final String cookies;

    public Request(URL url, Method method, @Nullable String cookies) {
        this.url = url;
        this.method = method;
        this.cookies = cookies;
    }

    public Response execute() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) this.url.openConnection();
        connection.setRequestMethod(this.method.name());
        connection.setConnectTimeout(10000);

        // Headers
        connection.setRequestProperty("Host", url.getHost());
        for (Entry<String, String> header : DEFAULT_HEADERS.entrySet()) {
            connection.setRequestProperty(header.getKey(), header.getValue());
        }
        if (this.cookies != null) {
            connection.setRequestProperty("Cookie", this.cookies);
        }

        // Response
        int statusCode = connection.getResponseCode();
        String responseCookies;
        // When using getHeaderField it gets the last occurrence when we want the first of set-cookie
        for (int i = 0; ; i++) {
            String headerName = connection.getHeaderFieldKey(i);
            String headerValue = connection.getHeaderField(i);
            if ("set-cookie".equals(headerName)) {
                responseCookies = headerValue;
                break;
            }
        }
        if (!(statusCode >= 200 && statusCode < 300)) {
            return new Response(statusCode, responseCookies, connection.getErrorStream());
        } else {
            return new Response(statusCode, responseCookies, connection.getInputStream());
        }
    }

    public enum Method {
        GET, POST
    }
}

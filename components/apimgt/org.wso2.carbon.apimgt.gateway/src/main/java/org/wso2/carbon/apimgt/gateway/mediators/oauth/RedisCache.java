package org.wso2.carbon.apimgt.gateway.mediators.oauth;

import com.fasterxml.jackson.databind.MapperFeature;
import org.apache.commons.lang.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.wso2.carbon.apimgt.gateway.mediators.oauth.client.TokenResponse;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;

public class RedisCache {
    private static JedisPool jedisPool;
    private static Jedis jedis;

    // To be replaced with values from deployment.toml config
    private static String host = "localhost";
    private static Integer port = 6379;
    private static String password = "";

    public RedisCache(String host, Integer port, String password) {
        // TODO
        if (StringUtils.isNotBlank(password)) {
            jedisPool = new JedisPool(new JedisPoolConfig(), host, port);
        } else {
            jedisPool = new JedisPool(host, port);
        }

        jedis = new Jedis(host, port);

        if (StringUtils.isNotBlank(password)) {
            jedis.auth(password);
        }
    }

    public void addTokenResponse(String uuid, TokenResponse tokenResponse) {
        // TODO
        try (Jedis jedis = jedisPool.getResource()) {
            // TODO - Check if the UUID is not unique
            // TODO - If not then append another string to the uuid to make it unique
            jedis.hmset(uuid, tokenResponse.toMap());
        }
    }

    public TokenResponse getTokenResponseById(String uuid) {
        // TODO
        TokenResponse tokenResponse = null;
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> tokenMap = jedis.hgetAll(uuid);
//            ObjectMapper mapper = new ObjectMapper();
//            mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
            if (tokenMap.size() != 0) {
//                tokenResponse = mapper.convertValue(tokenMap, TokenResponse.class);
                tokenResponse = new TokenResponse(tokenMap);
            }
        }
        return tokenResponse;
    }

    public void stopRedisCacheSession() {
        // TODO
        jedisPool.destroy();
        jedis.close();
    }
}
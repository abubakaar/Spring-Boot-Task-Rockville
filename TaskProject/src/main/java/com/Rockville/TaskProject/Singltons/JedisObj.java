/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.Rockville.TaskProject.Singltons;

import com.Rockville.TaskProject.Model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;

import redis.clients.jedis.Jedis;

/**
 *
 * @author abubakar
 */
public class JedisObj {

    private static Jedis jedis_instance = null;

    private static ObjectMapper mapper = new ObjectMapper();
    
    
   
    public static Long dataCahceexpireTime = 120L;

    private JedisObj() {
    }

    public static Jedis getJedis() {
        if (jedis_instance == null) {
            jedis_instance = new Jedis("localhost", 6379, 1);
        }

        return jedis_instance;

    }

    public static void maintainChache(String key, User user) {
        JedisObj.getJedis().hset(key,user.getId()+"",new Gson().toJson(new User(user.getId(), user.getName(), user.getPassword())));
        JedisObj.getJedis().expire(key,  dataCahceexpireTime);

    }

    public static List<User> getFromCache(String Key) throws JsonProcessingException {

        Map<String, String> map = JedisObj.getJedis().hgetAll(Key);
        return mapper.readValue(map.values().toString(),
                new TypeReference<List<User>>() {
        });
    }

    public static void deleteFromCache(String key, String id) {
        JedisObj.getJedis().hdel(key, id);
              
    }

}

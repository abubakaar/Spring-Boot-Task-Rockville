/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.Rockville.TaskProject.Service;

import com.Rockville.TaskProject.Model.EventModel;
import com.Rockville.TaskProject.Model.User;
import com.Rockville.TaskProject.Repository.UserRepository;
import com.Rockville.TaskProject.Singltons.JedisObj;
import com.Rockville.TaskProject.response.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 *
 * @author abubakar
 */
@Service
@Slf4j
public class UserServices {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    private User result = null;

    @Value("${event.getAllUser}")
    public  String eventgetAllUser;
    @Value("${event.deleteUser}")
    public  String eventdeleteUser;
    @Value("${event.updateUser}")
    public  String eventupdateUser;
    @Value("${event.getByID}")
    public  String eventgetByID;
    @Value("${event.registerUser}")
    public  String eventregisterUser;
    @Value("${event.otp}")
    public  String eventotp;
    @Value("10")
    public  Long credentialsexpireTime;
    @Value("${cache.KeyForUser}")
    public  String cacheKeyForUser;
    @Value("${cache.KeyForOTP}")
    public  String cacheKeyForOTP;
    @Value("${cache.KeyForRegisterUser}")
    public  String cacheKeyForRegisterUser;
    @Value("${rabbit.exchange}")
    public  String exchange;
    @Value("${rabbit.routingKey}")
    public  String routingkey;

    public Response registerUser(User user, String otp) throws JsonProcessingException, InterruptedException {

        if (JedisObj.getJedis().hget(user.getName(), cacheKeyForOTP) != null) {
            if (JedisObj.getJedis().hget(user.getName(), cacheKeyForOTP).equals(otp)) {
                result = userRepository.save(user);
                JedisObj.maintainChache(cacheKeyForUser, result);
                publishEvent(new EventModel(eventregisterUser));
                log.info("User {} saved in Database", user.getName());
                JedisObj.getJedis().hdel(user.getName(), cacheKeyForOTP);
                return Response.builder().date(new Date()).status(HttpStatus.CREATED).msg("Successfully Registered").data(result).build();
            } else {
                log.warn("User {} OTP Missmatch", user.getName());
                return Response.builder().date(new Date()).status(HttpStatus.BAD_GATEWAY).msg("OTP Not Matched").data(null).build();
            }
        } else {
            log.error("User {} failed saved in Database TimeOut", user.getName());
           return Response.builder().date(new Date()).status(HttpStatus.GATEWAY_TIMEOUT).msg("Time Out").data(null).build();
           
        }
    }

    public Response OTP(User user) {
        Random random = new Random();
        String otp = String.format("%04d", random.nextInt(10000));
        JedisObj.getJedis().hset(user.getName(), cacheKeyForOTP, otp);
        JedisObj.getJedis().expire(user.getName(), credentialsexpireTime);
        publishEvent(new EventModel(eventotp));
        log.info("OTP {} send for User {} for 1 Minute", otp, user.getName());
        return Response.builder().date(new Date()).status(HttpStatus.CREATED).msg("OTP "+otp+" Generated for user "+user.getName()+" Successfully").data(null).build();
    }

    public ResponseEntity updateUser(User user, int id) {

        if (userRepository.existsById(id)) {
            JedisObj.deleteFromCache(cacheKeyForUser, id + "");
            result = new User(id, user.getName(), user.getPassword());
            JedisObj.maintainChache(cacheKeyForUser, result);
            result = userRepository.save(result);
            if (checkResult(result)) {
                log.info("User {} updated in Database", user.getName());
                publishEvent(new EventModel(eventupdateUser));
                return ResponseEntity.ok("User " + user.getName() + " updated in Database Successfully");
            } else {
                log.error("User {} failed update in Database", user.getName());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } else {
            log.error("User not found in Database");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

    }

    public ResponseEntity<String> deleteUser(int id) {
        if (userRepository.existsById(id)) {
            JedisObj.deleteFromCache(cacheKeyForUser, id + "");
            userRepository.deleteById(id);
            log.info("User of Id: {} Deleted Successfully", id);
            publishEvent(new EventModel(eventdeleteUser));
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } else {
            log.error("No User have Id: {}", id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

    public ResponseEntity<List<User>> getAllUsers() throws JsonProcessingException, InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();
        if (JedisObj.getFromCache(cacheKeyForUser).isEmpty()) {
            List<User> s = (List<User>) userRepository.findAll();
            if (s.isEmpty()) {
                log.warn("Database is Empty");
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } else {
                for (User data : s) {
                    JedisObj.maintainChache(cacheKeyForUser, data);
                }
                publishEvent(new EventModel(eventgetAllUser));
                log.info("Populating User Data rom DB");
                return ResponseEntity.ok(s);
            }
        } else {
            log.info("Populating User Data from Cache");
            return ResponseEntity.ok(JedisObj.getFromCache(cacheKeyForUser));
        }
    }

    public ResponseEntity<User> findById(int id) throws JsonProcessingException {
        result = userRepository.findById(id).orElse(null);
        
        Optional<User> user=userRepository.findById(id);
        if(user.isPresent()){
            publishEvent(new EventModel(eventgetByID));
            log.info("Populating User Data");
            return ResponseEntity.ok(result);            
        }else
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private boolean checkResult(User val) {
        if (val != null) {
            return true;
        } else {
            return false;
        }
    }

    private void publishEvent(EventModel e) {
        rabbitTemplate.convertAndSend(exchange, routingkey, e);
    }

}

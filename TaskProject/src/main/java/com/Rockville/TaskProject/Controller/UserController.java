/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.Rockville.TaskProject.Controller;

import com.Rockville.TaskProject.Model.User;
import com.Rockville.TaskProject.Service.UserServices;
import com.Rockville.TaskProject.response.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author abubakar
 */
@Slf4j
@RestController
@RequestMapping("/User")
public class UserController {

    @Autowired
    private UserServices services;
    
   
    @PostMapping("/Register")
    public Response registerUser(@RequestBody User user) { 
        log.info("User Register Request Recieved");
        return services.OTP(user);
    }

    @PostMapping("/OTP/{otp}")
    public Response OTP(@RequestBody User user, @PathVariable String otp) throws JsonProcessingException, InterruptedException {
        log.info("OTP {} for registraion of user {} has recieved", otp, user.getName());
        return services.registerUser(user, otp);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity UpdateUser(@RequestBody User user, @PathVariable int id) {
        log.info("User Update Request Recieved");
        return services.updateUser(user,id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteUser(@PathVariable int id) {
        log.info("User Delete Request Recieved");
        return services.deleteUser(id);
    }

    @GetMapping("/")
    public ResponseEntity getAllUsers() throws JsonProcessingException, InterruptedException {
        log.info("Fetching all Users Request Recieved");
        return services.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity findById(@PathVariable int id) throws JsonProcessingException {
        log.info("Fetch by Id Request Recieved");
        return services.findById(id);
    }
}

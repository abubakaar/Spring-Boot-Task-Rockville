/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.Rockville.RabitListnerClient;

import com.Rockville.RabitListnerClient.Model.EventModel;
import com.Rockville.RabitListnerClient.Repository.EventRepo;
import jdk.javadoc.doclet.Reporter;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author abubakar
 */
@Component
public class Consumer {

    @Autowired
    private EventRepo repo;

    @RabbitListener(queues = "my_queue")
    public void consumerFromQueue(EventModel event) {
        
       repo.save(new EventModel(event.getEvent()));

    }

}

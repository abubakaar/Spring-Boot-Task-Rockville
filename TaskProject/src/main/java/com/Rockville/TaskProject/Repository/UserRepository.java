/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.Rockville.TaskProject.Repository;

import com.Rockville.TaskProject.Model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author abubakar
 */
@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
   
}

package com.qingqiao.springsecuritydatajpa.dao;

import com.qingqiao.springsecuritydatajpa.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDao extends JpaRepository<User,Integer> {
    User findUserByUsername(String username);
}

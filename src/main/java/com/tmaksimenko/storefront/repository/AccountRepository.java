package com.tmaksimenko.storefront.repository;

import com.tmaksimenko.storefront.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUsername (String username);
    List<Account> findByEmail (String email);
}

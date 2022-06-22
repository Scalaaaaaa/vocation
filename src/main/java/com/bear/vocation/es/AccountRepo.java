package com.bear.vocation.es;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepo extends ElasticsearchRepository<Account,String> {
    void deleteAccountsByLastname(String lastName);

    Account getAccountByLastname(String lastName);

    List<Account> getAccountsByAgeAfter(Integer age);

    Account getAccountByLastnameAndCity(String lastName, String city);

    List<Account> getAccountsByAgeBetweenOrderByAgeAsc(int min, int max);
}

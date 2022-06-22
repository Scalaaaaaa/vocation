package com.bear.vocation.es;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

@Data
@Document(indexName = "account")
public class Account {
    @Id
    private String id;

    @Field(name = "account_number")
    private String accountNumber;

    private Long balance;

    private String firstname;

    private String lastname;

    private Integer age;

    private String gender;

    private String address;

    private String employer;

    private String email;

    private String city;

    private String state;

}

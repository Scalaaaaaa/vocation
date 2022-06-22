package com.bear.vocation.es;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

@Configuration
public class Config {
    @Bean(name = "elasticsearchTemplate")
    public ElasticsearchRestTemplate elasticsearchRestTemplate(RestHighLevelClient client){
        ElasticsearchRestTemplate restTemplate = new ElasticsearchRestTemplate(client);
        return restTemplate;
    }
}

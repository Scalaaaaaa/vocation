package com.bear.vocation.es;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionRepository extends ElasticsearchRepository<Mission, String> {

    List<Mission> getMissionsByCreateDateTimeBetweenOrderByCreateDateTimeAsc(Long start,Long end);
}

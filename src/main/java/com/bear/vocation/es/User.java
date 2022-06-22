package com.bear.vocation.es;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * @Description: 距离交友,测试地理位置类型
 * @Author: yangyy
 * @Date: 2022/6/18 下午8:01
 **/
@Data
@Document(indexName = "user")
public class User {
}

package com.bear.vocation.es;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
@Data
@Document(indexName = "mission", shards = 1, replicas = 1)
/**
 * @Description:汽车金融的任务, 任务是一个线索的全流程,创建人/执行人,一个任务是一个购车意向
 *
 * @Author: yangyy
 * @Date: 2022/6/18 下午7:23
 **/
public class Mission {
    // 用自动生成策略
    @Id
    private String id;
    // 版本号,用来乐观锁并发更新,分配或领取任务时, 更新文档时加上这个条件,如果更新失败,则提示任务已经被分配
    // 如果是其他的状态更新,则失败时直接返回失败
    @Version
    private Long version;
    // 按照省市去汇总展示
    @Field(type = FieldType.Keyword)
    private String provinceName;

    @Field(type = FieldType.Keyword)
    private String cityName;

    @Field(type = FieldType.Date)
    private Long createDateTime;
    // 0:线索创建, 1,任务已分配/领取,2:已联系意向人,3:已购车,4:未购车
    @Field(type = FieldType.Integer)
    private Integer status;

    // 按照区域去获取任务列表,然后分配任务
    @Field(type = FieldType.Keyword)
    private String areaName;

    @Field(type = FieldType.Keyword)
    private String createBy;

    // 任务负责人,谁领的或者分配给谁的
    @Field(type = FieldType.Keyword)
    private String missionOwner;
    // 分配任务的人 不需要作为查询条件,只用来展示
    @Field(type = FieldType.Keyword,index = false)
    private String assigner;
    // 会生效, 只在创建的时候. 当创建后,在追加分词器/字段 就不会生效, es不支持删除字段,要重建
    @Field(type = FieldType.Text,analyzer = "ik_smart",searchAnalyzer = "ik_smart")
    private String remark;
}

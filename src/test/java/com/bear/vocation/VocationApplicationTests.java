package com.bear.vocation;

import com.alibaba.fastjson.JSON;
import com.bear.vocation.es.Account;
import com.bear.vocation.es.AccountRepo;
import com.bear.vocation.es.MissionRepository;
import com.bear.vocation.es.Mission;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.*;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ParsedAvg;
import org.elasticsearch.search.aggregations.metrics.ParsedValueCount;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.*;

@SpringBootTest
@Slf4j
class VocationApplicationTests {
    @Autowired
    MissionRepository missionRepository;
    @Autowired
    AccountRepo accountRepo;
    @Autowired
    RestHighLevelClient client;
    public  static Map<String, String[]> provCitys = new HashMap<>();
    public Object[] provs = provCitys.keySet().toArray();
    public static final int RAND = 200000000;


    @Test
    public void search() throws IOException {
        // 多条件相等
        /*Account account = accountRepo.getAccountByLastnameAndCity("yiyun", "徐州");
        if (account != null) {
            log.info("-------" + account.getId());
        }*/
        // 查数字范围 排序
        /*List<Account> ageAsc = accountRepo.getAccountsByAgeBetweenOrderByAgeAsc(31, 36);
        log.info(JSON.toJSONString(ageAsc));*/

        // 查时间范围
        /*List<Mission> missions = missionRepository.getMissionsByCreateDateTimeBetweenOrderByCreateDateTimeAsc(new Date().getTime()-20000000,
                new Date().getTime() + 1000000);
        log.info(JSON.toJSONString(missions));*/

        // 分页

        // 查聚合 排序
        //设置索引
        SearchRequest searchRequest = new SearchRequest("mission");
        //构建查询
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();

        TermQueryBuilder city = QueryBuilders.termQuery("provinceName", "江苏");
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("createDateTime");
        //起始时间
        rangeQueryBuilder.gte(new Date().getTime());
        //结束时间
        rangeQueryBuilder.lte(new Date().getTime() + (int)Math.random()*RAND/3);
        boolBuilder.filter(city).filter(rangeQueryBuilder);
        //sourceBuilder.query(boolBuilder);
        /**
         * 不输出原始数据
         */
        sourceBuilder.size(0);
        //按时间聚合，求TX的和
        //DateHistogramInterval.minutes(5)是指按5分钟聚合
        //format("yyyy-MM-dd HH:mm")是指聚合的结果的Time的格式
        //BucketOrder.aggregation("tx_sum", false)对聚合结果的排序 true为正序 false为倒序
        /*AggregationBuilder aggregation = AggregationBuilders.dateHistogram("time_count").field("Time")
                .fixedInterval(DateHistogramInterval.minutes(5)).format("yyyy-MM-dd HH:mm")
                .order(BucketOrder.aggregation("tx_sum", false));
        aggregation.subAggregation(AggregationBuilders.sum("tx_sum").field("Tx"));*/
        // 聚合的自定义名称是city, 按照cityName聚合
        AggregationBuilder aggregation = getHistogramAgg();
        sourceBuilder.aggregation(aggregation);

        searchRequest.source(sourceBuilder);
        log.info("dsl:" + sourceBuilder.toString());
        log.info("beforeSearch");
        //发送请求
        SearchResponse searchResponse = client.search(searchRequest,RequestOptions.DEFAULT);
        log.info("afterSearch");
        //获取聚合的结果
        Aggregations aggregations = searchResponse.getAggregations();
        Aggregation city1 = aggregations.get("timeSlice");
        dealHistogramAgg((ParsedDateHistogram) city1);
        log.info("success");
        /*Aggregation aggregation1 = aggregations.get("city");
        List<? extends Histogram.Bucket> buckets = ((Histogram)aggregation1).getBuckets();
        for (Histogram.Bucket bucket : buckets){
            String keyAsString = bucket.getKeyAsString();
            Sum sum = bucket.getAggregations().get("count");
            map.put(keyAsString,sum.getValue());
        }*/

        // 查分词
    }

    private void dealCountRes(ParsedStringTerms city1) {
        city1.getBuckets().forEach(item ->{
            Object cityName = item.getKey();
            long docCount = item.getDocCount();
            log.info("cityName:{}, decCount:{}, count:{}",cityName,
                    docCount, JSON.toJSONString(item.getAggregations().getAsMap().get("cnt")));
        });
    }
    private void dealAvgRes(ParsedStringTerms city1) {
        city1.getBuckets().forEach(item ->{
            Object cityName = item.getKey();
            long docCount = item.getDocCount();
            ParsedAvg parsedAvg = (ParsedAvg)item.getAggregations().getAsMap().get("avgBalance");
            log.info("cityName:{}, decCount:{}, avgBalanceValue:{}",cityName,
                    docCount, parsedAvg.getValue());
        });
    }

    private TermsAggregationBuilder getCountAggregation() {
        return AggregationBuilders.terms("city").field("cityName")
                .subAggregation(AggregationBuilders.count("cnt").field("_id"));
    }

    private TermsAggregationBuilder getAvgAggregation() {
        return AggregationBuilders.terms("stateName").field("state.keyword")
                .subAggregation(AggregationBuilders.avg("avgBalance").field("balance"));
    }

    private DateHistogramAggregationBuilder getHistogramAgg(){
        DateHistogramAggregationBuilder aggregation = AggregationBuilders.dateHistogram("timeSlice")
                .field("createDateTime")
                .fixedInterval(DateHistogramInterval.minutes(5)).format("yyyy-MM-dd HH:mm:ss")
                .order(BucketOrder.aggregation("cnt", false));
        aggregation.subAggregation(AggregationBuilders.count("cnt").field("_id"));
        return aggregation;
    }

    private void dealHistogramAgg(ParsedDateHistogram city1){
        city1.getBuckets().forEach(item ->{
            Object cityName = item.getKey();
            long docCount = item.getDocCount();
            ParsedValueCount parsedAvg = (ParsedValueCount)item.getAggregations().getAsMap().get("cnt");
            log.info("cityName:{}, decCount:{}, cnt:{}",cityName,
                    docCount, parsedAvg.getValue());
        });
    }
    // 设置分词器/映射信息
    @Test
    public void tokenizerMapping() throws IOException {
        //client
        //CreateIndexRequest request = new CreateIndexRequest("mission");
        // 1, mission里追加一个tags,类型是text,2, 设置 remark的分词器
        /* 追加字段,并设置分词器, POST /mission/_mapping, 不支持对已有字段 进行修改分词器
        {
            "properties":{
                "tags":{
                        "type":"text",
                        "analyzer":"ik_smart",
                        "search_analyzer":"ik_smart"
            }
        }
        }*/
        PutMappingRequest putMappingRequest = new PutMappingRequest("mission");
        // api的写法就是往PutMappingRequest设置source, source就是httpPost里的body,既可以是bean,也可以是map
        Map<String, Object> source = new HashMap<String,Object>(){{
            put("properties",new HashMap<String,Object>(){{
                put("tags",new HashMap<String,String>(){{
                    put("type", "text");
                    put("analyzer", "ik_smart");
                    put("search_analyzer", "ik_smart");
                }});
            }});
        }};

        putMappingRequest.source(source);
        client.indices().putMapping(putMappingRequest, RequestOptions.DEFAULT);
        log.info("afterPutMapping");
    }
    @Test
    public void saveToEs() {
        int provLen = provs.length;
        for (int i = 0; i < 2000; i++) {
            Mission m = new Mission();
            String prov = provs[(int)(Math.random() * provLen)].toString();
            String[] citys = provCitys.get(prov);
            int length = citys.length;

            m.setAreaName(String.valueOf((int)Math.random()*6));
            m.setCityName(citys[(int)Math.random() * length]);
            m.setCreateBy(m.getAreaName()+(int)Math.random()*10);
            m.setProvinceName(prov);
            m.setStatus(0);
            m.setCreateDateTime(new Date().getTime() + (int)Math.random()* RAND);
            missionRepository.save(m);
        }
        log.info("success");
    }
    @Test
    public void insertEs(){
        Account data = new Account();
        data.setAccountNumber("123121");
        data.setAddress("32  433 43");
        data.setAge(32);
        data.setBalance(12312l);
        data.setCity("nanjing");
        data.setEmail("1515197756@qq.com");
        data.setFirstname("yang");
        data.setLastname("yiyun");
        data.setGender("M");
        data.setState("jiangsu");
        data.setEmployer("zhangsan");
        System.out.println("beforeSave ");
        log.info("beforeSave ");
        Account save = accountRepo.save(data);
        log.info("afterSave id={}" , save.getId());
        System.out.println("afterSave "+save.getId());
    }

    @Test
    public void deleteEs(){
        try{
            log.info("beforeDelete");
            accountRepo.deleteAccountsByLastname("yiyun");
            log.info("afterDelete");
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    @Test
    public void update(){
        Account account = accountRepo.getAccountByLastname("yiyun");
        if (account != null) {
            account.setCity("徐州");
        }
        accountRepo.save(account);
    }
    static{
        provCitys.put("北京", new String[]{"北京"});
        provCitys.put("上海", new String[]{"上海"});
        provCitys.put("天津", new String[]{"天津"});
        provCitys.put("重庆", new String[]{"重庆"});
        provCitys.put("黑龙江", new String[]{"哈尔滨","齐齐哈尔","牡丹江","大庆","伊春","双鸭山","鹤岗","鸡西","佳木斯","七台河","黑河","绥化","大兴安岭"});
        provCitys.put("吉林", new String[]{"长春","延边","吉林","白山","白城","四平","松原","辽源","大安","通化"});
        provCitys.put("辽宁", new String[]{"沈阳","大连","葫芦岛","旅顺","本溪","抚顺","铁岭","辽阳","营口","阜新","朝阳","锦州","丹东","鞍山"});
        provCitys.put("内蒙古", new String[]{"呼和浩特","呼伦贝尔","锡林浩特","包头","赤峰","海拉尔","乌海","鄂尔多斯","通辽"});
        provCitys.put("河北", new String[]{"石家庄","唐山","张家口","廊坊","邢台","邯郸","沧州","衡水","承德","保定","秦皇岛"});
        provCitys.put("河南", new String[]{"郑州","开封","洛阳","平顶山","焦作","鹤壁","新乡","安阳","濮阳","许昌","漯河","三门峡","南阳","商丘","信阳","周口","驻马店"});
        provCitys.put("山东", new String[]{"济南","青岛","淄博","威海","曲阜","临沂","烟台","枣庄","聊城","济宁","菏泽","泰安","日照","东营","德州","滨州","莱芜","潍坊"});
        provCitys.put("山西", new String[]{"太原","阳泉","晋城","晋中","临汾","运城","长治","朔州","忻州","大同","吕梁"});
        provCitys.put("江苏", new String[]{"南京","苏州","昆山","南通","太仓","吴县","徐州","宜兴","镇江","淮安","常熟","盐城","泰州","无锡","连云港","扬州","常州","宿迁"});
        provCitys.put("安徽", new String[]{"合肥","巢湖","蚌埠","安庆","六安","滁州","马鞍山","阜阳","宣城","铜陵","淮北","芜湖","毫州","宿州","淮南","池州"});
        provCitys.put("陕西", new String[]{"西安","韩城","安康","汉中","宝鸡","咸阳","榆林","渭南","商洛","铜川","延安"});
        provCitys.put("宁夏", new String[]{"银川","固原","中卫","石嘴山","吴忠"});
        provCitys.put("甘肃", new String[]{"兰州","白银","庆阳","酒泉","天水","武威","张掖","甘南","临夏","平凉","定西","金昌"});
        provCitys.put("青海", new String[]{"西宁","海北","海西","黄南","果洛","玉树","海东","海南"});
        provCitys.put("湖北", new String[]{"武汉","宜昌","黄冈","恩施","荆州","神农架","十堰","咸宁","襄樊","孝感","随州","黄石","荆门","鄂州"});
        provCitys.put("湖南", new String[]{"长沙","邵阳","常德","郴州","吉首","株洲","娄底","湘潭","益阳","永州","岳阳","衡阳","怀化","韶山","张家界"});
        provCitys.put("浙江", new String[]{"杭州","湖州","金华","宁波","丽水","绍兴","雁荡山","衢州","嘉兴","台州","舟山","温州"});
        provCitys.put("江西", new String[]{"南昌","萍乡","九江","上饶","抚州","吉安","鹰潭","宜春","新余","景德镇","赣州"});
        provCitys.put("福建", new String[]{"福州","厦门","龙岩","南平","宁德","莆田","泉州","三明","漳州"});
        provCitys.put("贵州", new String[]{"贵阳","安顺","赤水","遵义","铜仁","六盘水","毕节","凯里","都匀"});
        provCitys.put("四川", new String[]{"成都","泸州","内江","凉山","阿坝","巴中","广元","乐山","绵阳","德阳","攀枝花","雅安","宜宾","自贡","甘孜州","达州","资阳","广安","遂宁","眉山","南充"});
        provCitys.put("广东", new String[]{"广州","深圳","潮州","韶关","湛江","惠州","清远","东莞","江门","茂名","肇庆","汕尾","河源","揭阳","梅州","中山","德庆","阳江","云浮","珠海","汕头","佛山"});
        provCitys.put("广西", new String[]{"南宁","桂林","阳朔","柳州","梧州","玉林","桂平","贺州","钦州","贵港","防城港","百色","北海","河池","来宾","崇左"});
        provCitys.put("云南", new String[]{"昆明","保山","楚雄","德宏","红河","临沧","怒江","曲靖","思茅","文山","玉溪","昭通","丽江","大理"});
        provCitys.put("海南", new String[]{"海口","三亚","儋州","琼山","通什","文昌"});
        provCitys.put("新疆", new String[]{"乌鲁木齐","阿勒泰","阿克苏","昌吉","哈密","和田","喀什","克拉玛依","石河子","塔城","库尔勒","吐鲁番","伊宁"});
    }
}

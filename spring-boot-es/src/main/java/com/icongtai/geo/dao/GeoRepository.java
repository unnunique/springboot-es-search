//package com.icongtai.geo.dao;
//
//import com.icongtai.geo.config.ElasticSearchHelper;
//import com.icongtai.geo.model.*;
//import com.icongtai.geo.utils.NumberUtil;
//import com.icongtai.geo.utils.StringUtil;
//import org.apache.lucene.search.join.ScoreMode;
//import org.elasticsearch.action.search.SearchRequestBuilder;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.client.transport.TransportClient;
//import org.elasticsearch.common.geo.GeoPoint;
//import org.elasticsearch.index.query.BoolQueryBuilder;
//import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
//import org.elasticsearch.index.query.QueryBuilder;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.search.SearchHit;
//import org.elasticsearch.search.SearchHits;
//import org.elasticsearch.search.aggregations.AggregationBuilder;
//import org.elasticsearch.search.aggregations.AggregationBuilders;
//import org.elasticsearch.search.aggregations.bucket.terms.Terms;
//import org.elasticsearch.search.sort.SortBuilder;
//import org.elasticsearch.search.sort.SortBuilders;
//import org.elasticsearch.search.sort.SortOrder;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CopyOnWriteArrayList;
//
///**
// * elasticsearch 数据处理
// */
//@Configuration
//@Component
//public class GeoRepository {
//
//    // 需要搜索的索引
//    @Value("${elasticsearch.index}")
//    private String index;
//    // 需要搜索的索引类型
//
//    @Value("${elasticsearch.type}")
//    private String types;
//
//    @Autowired
//    private ElasticSearchHelper  helper;
//
//    public List<GeoReGeoInfo> getGeoReGeoInfo(String locations, String distance,
//                                              String typeNames, String extensions,
//                                              boolean searchGaoDe) {
//        // 用于封装最终的返回结果信息
//        List<GeoReGeoInfo> geoReGeoInfos = new CopyOnWriteArrayList<>();
//        GeoReGeoInfo geoReGeoInfo = new GeoReGeoInfo();
//        if (searchGaoDe) {
//            // TODO 下个版本可能需要完成的功能
//            geoReGeoInfo.setStatus(Constance.STATUS_FAILED);
//            geoReGeoInfo.setInfo("搜索高德功能暂时未支持。");
//            geoReGeoInfos.add(geoReGeoInfo);
//            return  geoReGeoInfos;
//        }
//       // location 的值和distance 的值是否为空时
//        if (locations == null || "".equals(locations) || distance == null || "".equals(distance)) {
//            geoReGeoInfo.setStatus(Constance.STATUS_FAILED);
//            geoReGeoInfo.setInfo("地址坐标或者搜索范围为空。");
//            geoReGeoInfos.add(geoReGeoInfo);
//            return  geoReGeoInfos;
//        }
//        // 存储传过来的points
//        List<String> points = new CopyOnWriteArrayList<>();
//        // 搜索范围，范围间的数据用"-"分隔
//        String[] ranges = distance.split(Constance.SPLIT_SEARCH_RANGE);
//        String[] pointArr = locations.split(Constance.SPLIT_POPINTS);
//        for(String point : pointArr) {  // 点与点之间的分隔线"|"
//            String []matLon =  point.split(Constance.SPLIT_POINT);
//            if (!NumberUtil.isNumeric(matLon[0])) {
//                geoReGeoInfo.setStatus(Constance.STATUS_FAILED);
//                // 返回错误信息，经纬度信息或者范围搜索的数据格式错误信息
//                geoReGeoInfo.setInfo("经度数值有误");
//                geoReGeoInfos.add(geoReGeoInfo);
//                return  geoReGeoInfos;
//            }
//            if (point.split(Constance.SPLIT_POINT).length > 1 &&
//                    !NumberUtil.isNumeric(matLon[1]) ) {
//                geoReGeoInfo.setStatus(Constance.STATUS_FAILED);
//                // 返回错误信息，经纬度信息或者范围搜索的数据格式错误信息
//                geoReGeoInfo.setInfo("经度数值有误");
//                geoReGeoInfos.add(geoReGeoInfo);
//                return  geoReGeoInfos;
//            }
//            points.add(point);
//        }
//        // 检查范围是否是数字,
//        for (String range : ranges) {
//            if (!NumberUtil.isNumeric(range)) {
//                geoReGeoInfo.setStatus(Constance.STATUS_FAILED);
//                // 返回错误信息，经纬度信息或者范围搜索的数据格式错误信息
//                geoReGeoInfo.setInfo("范围搜索数值有误。");
//                geoReGeoInfos.add(geoReGeoInfo);
//                return  geoReGeoInfos;
//            }
//        }
//
//        //多个点的条件下的公共查询条件，
//        // type_name 列表
//        BoolQueryBuilder typeNamesBoolQuery = QueryBuilders.boolQuery();
//        if (typeNames != null) {
//            for (String type : typeNames.split(Constance.SPLIT_TYPES)) { // 类型之间用逗号分隔
//                typeNamesBoolQuery.should(QueryBuilders.termQuery(Constance.FIELD_ZEBRA_TYPE, type));
//            }
//        }
//
//        // 多个点分别根据传过来的条件进行查询
//        // geo 逆编码后的基本信息
//        GeoReGeoBaseInfo geoReGeoBaseInfo;
//        for(String point:points) {
//            geoReGeoBaseInfo = new GeoReGeoBaseInfo();
//            // 按照范围查询进行query
//            GeoDistanceQueryBuilder geoDistanceQueryBuilder1;
//            GeoDistanceQueryBuilder geoDistanceQueryBuilder2 = null;
//            geoDistanceQueryBuilder1 = QueryBuilders
//                    .geoDistanceQuery(Constance.FIELD_ZEBRA_LOCATION)
//                    .point(new GeoPoint(point)).distance(Double.parseDouble(ranges[0])/1000 + "km");
//            if (ranges.length >= 2 && Double.parseDouble(ranges[0]) < Double.parseDouble(ranges[1])) {
//                geoDistanceQueryBuilder2 = QueryBuilders
//                        .geoDistanceQuery(Constance.FIELD_ZEBRA_LOCATION)
//                    .point(new GeoPoint(point)).distance(Double.parseDouble(ranges[1])/1000 + "km");
//            } else if(ranges.length >= 2 && Double.parseDouble(ranges[0]) >= Double.parseDouble(ranges[1])) {
//                geoDistanceQueryBuilder1 = QueryBuilders
//                        .geoDistanceQuery(Constance.FIELD_ZEBRA_LOCATION)
//                        .point(new GeoPoint(point)).distance(Double.parseDouble(ranges[1])/1000 + "km");
//                geoDistanceQueryBuilder2 = QueryBuilders
//                        .geoDistanceQuery(Constance.FIELD_ZEBRA_LOCATION)
//                        .point(new GeoPoint(point)).distance(Double.parseDouble(ranges[0])/1000 + "km");
//            }
//
//            // 按照距离进行排序query 封装
//            SortBuilder sortBuilder = SortBuilders
//                    .geoDistanceSort(Constance.FIELD_ZEBRA_LOCATION,
//                    new GeoPoint(point)).order(SortOrder.ASC);
//
//            // 总的query 对象
//            BoolQueryBuilder totalBoolQuery = QueryBuilders.boolQuery();
//            totalBoolQuery.must(typeNamesBoolQuery);
//            if (ranges.length >= 2) {
//                totalBoolQuery.mustNot(geoDistanceQueryBuilder1).must(geoDistanceQueryBuilder2);
//            } else {
//                totalBoolQuery.must(geoDistanceQueryBuilder1);
//            }
//
//
//            // 封装请求对象,第一版本中只需要返回其最近的一条内容
//            SearchRequestBuilder searchRequestBuilder = null;
//            try {
//                searchRequestBuilder = helper.getClient().prepareSearch(index)
//                        .setTypes(types).setQuery(totalBoolQuery)
//                        .addSort(sortBuilder).setSize(1);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            // 获取response 对象,进行数据返回并进行封装
//            SearchResponse searchResponse = searchRequestBuilder.get();
//            // 获取数据并进行封装
//            SearchHits searchHits = searchResponse.getHits();
//            // 实际数据存储的数据
//            SearchHit[] hits = searchHits.getHits();
//            // 返回实际存储最近的一条数据
//            if (hits.length > 0) {
//                //TODO  下个版本可能需要做的功能
//                if (extensions !=null && !"".equals(extensions)
//                        && Constance.EXTENSIONS_AOI.equals(extensions)) {
//                    geoReGeoInfo.setStatus(Constance.STATUS_FAILED);
//                    geoReGeoInfo.setInfo("返回Aoi 信息暂时未支持。");
//                    geoReGeoInfos.add(geoReGeoInfo);
//                    return  geoReGeoInfos;
//                }
//                if(StringUtil.isNotEmptyOrNull(hits[0].getSource()
//                        .get(Constance.FIELD_ZEBRA_NAME))) {
//                    geoReGeoBaseInfo.setName((String) hits[0].getSource().
//                            get(Constance.FIELD_ZEBRA_NAME));
//                }
//                if(StringUtil.isNotEmptyOrNull(hits[0].getSource()
//                        .get(Constance.FIELD_ZEBRA_FIRSTLY_CLASSIFICATION))) {
//                    geoReGeoBaseInfo.setFirstly_classification((String) hits[0].getSource().
//                            get(Constance.FIELD_ZEBRA_FIRSTLY_CLASSIFICATION));
//                }
//                if(StringUtil.isNotEmptyOrNull(hits[0].getSource()
//                        .get(Constance.FIELD_ZEBRA_SECONDARY_CLASSIFICATION))) {
//                    geoReGeoBaseInfo.setSecondary_classification((String) hits[0].getSource().
//                            get(Constance.FIELD_ZEBRA_SECONDARY_CLASSIFICATION));
//                }
//                if(StringUtil.isNotEmptyOrNull(hits[0].getSource()
//                        .get(Constance.FIELD_ZEBRA_TYPE))) {
//                    geoReGeoBaseInfo.setType((String) hits[0].getSource().
//                            get(Constance.FIELD_ZEBRA_TYPE));
//                }
//                if(StringUtil.isNotEmptyOrNull(hits[0].getSource()
//                        .get(Constance.FIELD_ZEBRA_PROVINCE))) {
//                    geoReGeoBaseInfo.setProvince((String) hits[0].getSource().
//                            get(Constance.FIELD_ZEBRA_PROVINCE));
//                }
//                if(StringUtil.isNotEmptyOrNull(hits[0].getSource()
//                        .get(Constance.FIELD_ZEBRA_CITY))) {
//                    geoReGeoBaseInfo.setCity((String) hits[0].getSource().
//                            get(Constance.FIELD_ZEBRA_CITY));
//                }
//                if(StringUtil.isNotEmptyOrNull(hits[0].getSource()
//                        .get(Constance.FIELD_ZEBRA_CITYCODE))) {
//                    geoReGeoBaseInfo.setCitycode((String) hits[0].getSource().
//                            get(Constance.FIELD_ZEBRA_CITYCODE));
//                }
//                if(StringUtil.isNotEmptyOrNull(hits[0].getSource()
//                        .get(Constance.FIELD_ZEBRA_DISTRICT))) {
//                    geoReGeoBaseInfo.setDistrict((String) hits[0].getSource().
//                            get(Constance.FIELD_ZEBRA_DISTRICT));
//                }
//                if(StringUtil.isNotEmptyOrNull(hits[0].getSource()
//                        .get(Constance.FIELD_ZEBRA_ADCODE))) {
//                    geoReGeoBaseInfo.setAdcode((String) hits[0].getSource().
//                            get(Constance.FIELD_ZEBRA_ADCODE));
//                }
//                if(StringUtil.isNotEmptyOrNull(hits[0].getSource()
//                        .get(Constance.FIELD_ZEBRA_TOWNSHIP))) {
//                    geoReGeoBaseInfo.setTownship((String) hits[0].getSource().
//                            get(Constance.FIELD_ZEBRA_TOWNSHIP));
//                }
//                if(StringUtil.isNotEmptyOrNull(hits[0].getSource()
//                        .get(Constance.FIELD_ZEBRA_BUSINESS_CIRCLE))) {
//                    geoReGeoBaseInfo.setBusiness_circle((String) hits[0].getSource().
//                            get(Constance.FIELD_ZEBRA_BUSINESS_CIRCLE));
//                }
//                if(StringUtil.isNotEmptyOrNull(hits[0].getSource()
//                        .get(Constance.FIELD_ZEBRA_FORMATTED_ADDRESS))) {
//                    geoReGeoBaseInfo.setFormatted_address((String) hits[0].getSource().
//                            get(Constance.FIELD_ZEBRA_FORMATTED_ADDRESS));
//                }
//
//
//                List<PoiInfo> poiInfos = new CopyOnWriteArrayList<>();
//                if (extensions !=null && !"".equals(extensions)
//                        && Constance.EXTENSIONS_POI.equals(extensions)) {
//                    PoiInfo poiInfo = new PoiInfo();
//                    poiInfo.setGeoReGeoBaseInfo(geoReGeoBaseInfo);
//
//                    Map<String, Object> otherInfo = (Map<String, Object>) hits[0].getSource()
//                            .get(Constance.FIELD_ZEBRA_EXTENSIONS);
//                    if(StringUtil.isNotEmptyOrNull(otherInfo.get(Constance.FIELD_ZEBRA_AVG_PRICE))) {
//                        poiInfo.setAvgPrice((Double) otherInfo
//                                .get(Constance.FIELD_ZEBRA_AVG_PRICE));
//                    }
//                    if(StringUtil.isNotEmptyOrNull(otherInfo.get(Constance.FIELD_ZEBRA_SHOPS))) {
//                        poiInfo.setShops((Integer) otherInfo
//                                .get(Constance.FIELD_ZEBRA_SHOPS));
//                    }
//                    if(StringUtil.isNotEmptyOrNull(otherInfo.get(Constance.FIELD_ZEBRA_GOOD_COMMENTS))) {
//                        poiInfo.setGoodComments((Integer) otherInfo
//                               .get(Constance.FIELD_ZEBRA_GOOD_COMMENTS));
//                    }
//                    if(StringUtil.isNotEmptyOrNull(otherInfo.get(Constance.FIELD_ZEBRA_LVL))) {
//                        poiInfo.setLevel((Integer) otherInfo
//                               .get(Constance.FIELD_ZEBRA_LVL));
//                    }
//                    if(StringUtil.isNotEmptyOrNull(otherInfo.get(Constance.FIELD_ZEBRA_OTHER_TYPE))) {
//                        poiInfo.setOtherType((String) otherInfo
//                               .get(Constance.FIELD_ZEBRA_OTHER_TYPE));
//                    }
//                    if(StringUtil.isNotEmptyOrNull(otherInfo.get(Constance.FIELD_ZEBRA_NUMBERS))) {
//                        poiInfo.setNumbers((Integer) otherInfo
//                               .get(Constance.FIELD_ZEBRA_NUMBERS));
//                    }
//                    poiInfos.add(poiInfo);
//                }
//                geoReGeoInfo.setStatus(Constance.STATUS_SUCCESS);
//                geoReGeoInfo.setInfo("ok");
//                geoReGeoInfo.setGeoReGeoBaseInfo(geoReGeoBaseInfo);
//                geoReGeoInfo.setPoiInfos(poiInfos);
//                geoReGeoInfos.add(geoReGeoInfo);
//            } else {
//                geoReGeoInfo.setStatus(Constance.STATUS_SUCCESS);
//                geoReGeoInfo.setInfo("ok");
//                geoReGeoInfos.add(geoReGeoInfo);
//            }
//        }
//        return geoReGeoInfos;
//    }
//
//
//    public List<GeoReGeoInfo> getGeoReGeoQuery(String name, String locations, String distances, String types,
//                                               String firstly_classification, String secondary_classification,
//                                               String province, String city, String district,
//                                               String township, String business_circle,
//                                               String avg_price, String shops, String good_comments,
//                                               String lvl, String other_type, String numbers, String from,
//                                               String size, String extentions, boolean searchGaoDe) {
//
//        // 开始封装Query 查询
//        // 在传入多个点的时候，所有公共字段的查询
//        BoolQueryBuilder totalComonFiledQuery = QueryBuilders.boolQuery();
//
//        //多个点的条件下的公共查询条件，
//        BoolQueryBuilder tmpQuery = QueryBuilders.boolQuery();
//
//        // name 商店名字,医院名字，公司名字，教育机构名字
//        if (StringUtil.isNotEmptyOrNull(name)) {
//            totalComonFiledQuery.must(QueryBuilders.matchQuery(Constance.FIELD_ZEBRA_NAME,
//                    name));
//        }
//
//        // type_name 列表
//        if (StringUtil.isNotEmptyOrNull(types)) {
//            for (String type : types.split(Constance.SPLIT_TYPES)) { // 类型之间用逗号分隔
//                tmpQuery.should(QueryBuilders.termQuery(Constance.FIELD_ZEBRA_TYPE, type));
//            }
//            totalComonFiledQuery.must(tmpQuery);
//        }
//
//        // firstly_classification 一级地址
//        if (StringUtil.isNotEmptyOrNull(firstly_classification)) {
//            totalComonFiledQuery.must(
//                    QueryBuilders.termQuery(Constance.FIELD_ZEBRA_FIRSTLY_CLASSIFICATION,
//                    firstly_classification));
//        }
//
//        // secondary_classification 二级地址
//        if (StringUtil.isNotEmptyOrNull(secondary_classification)) {
//            totalComonFiledQuery.must(
//                    QueryBuilders.termQuery(Constance.FIELD_ZEBRA_SECONDARY_CLASSIFICATION,
//                    secondary_classification));
//        }
//
//        // secondary_classification 二级地址
//        if (StringUtil.isNotEmptyOrNull(types)) {
//            totalComonFiledQuery.must(QueryBuilders.termQuery(Constance.FIELD_ZEBRA_TYPE,
//                    types));
//        }
//
//        // province
//        if (StringUtil.isNotEmptyOrNull(province)) {
//            totalComonFiledQuery.must(QueryBuilders.termQuery(Constance.FIELD_ZEBRA_PROVINCE,
//                    province));
//        }
//
//        // city
//        if (StringUtil.isNotEmptyOrNull(city)) {
//            totalComonFiledQuery.must(QueryBuilders.termQuery(Constance.FIELD_ZEBRA_CITY,
//                    city));
//        }
//
//        // district
//        if (StringUtil.isNotEmptyOrNull(district)) {
//            totalComonFiledQuery.must(QueryBuilders.termQuery(Constance.FIELD_ZEBRA_DISTRICT,
//                    district));
//        }
//
//        // township
//        if (StringUtil.isNotEmptyOrNull(township)) {
//            totalComonFiledQuery.must(QueryBuilders.matchQuery(Constance.FIELD_ZEBRA_TOWNSHIP,
//                    township));
//        }
//
//        // business_circle
//        if (StringUtil.isNotEmptyOrNull(business_circle)) {
//            totalComonFiledQuery.must(QueryBuilders.termQuery(Constance.FIELD_ZEBRA_BUSINESS_CIRCLE,
//                    business_circle));
//        }
//
//        // 嵌套对象里面内容的查询
//        BoolQueryBuilder nestBoolQuery = QueryBuilders.boolQuery();
//        // avg_priace 范围
//        if (StringUtil.isNotEmptyOrNull(avg_price)) {
//            nestBoolQuery.must(QueryBuilders
//                    .rangeQuery(Constance.FIELD_ZEBRA_AVG_PRICE)
//                    .gte(avg_price.split(Constance.SPLIT_SEARCH_RANGE)[0])
//                    .lte(avg_price.split(Constance.SPLIT_SEARCH_RANGE)[1]));
//        }
//        // shops 范围
//        if (StringUtil.isNotEmptyOrNull(shops)) {
//            nestBoolQuery.must(QueryBuilders
//                    .rangeQuery(Constance.FIELD_ZEBRA_SHOPS)
//                    .gte(shops.split(Constance.SPLIT_SEARCH_RANGE)[0])
//                    .lte(shops.split(Constance.SPLIT_SEARCH_RANGE)[1]));
//        }
//        // good_comments 范围
//        if (StringUtil.isNotEmptyOrNull(good_comments)) {
//            nestBoolQuery.must(QueryBuilders
//                    .rangeQuery(Constance.FIELD_ZEBRA_GOOD_COMMENTS)
//                    .gte(good_comments.split(Constance.SPLIT_SEARCH_RANGE)[0])
//                    .lte(good_comments.split(Constance.SPLIT_SEARCH_RANGE)[1]));
//        }
//        // lvl 范围
//        if (StringUtil.isNotEmptyOrNull(lvl)) {
//            nestBoolQuery.must(QueryBuilders
//                    .rangeQuery(Constance.FIELD_ZEBRA_LVL)
//                    .gte(lvl.split(Constance.SPLIT_SEARCH_RANGE)[0])
//                    .lte(lvl.split(Constance.SPLIT_SEARCH_RANGE)[1]));
//        }
//        // other_type
//        if (StringUtil.isNotEmptyOrNull(other_type)) {
//            nestBoolQuery.must(QueryBuilders
//                    .termQuery(Constance.FIELD_ZEBRA_OTHER_TYPE, other_type));
//        }
//
//        // numbers
//        if (StringUtil.isNotEmptyOrNull(numbers)) {
//            nestBoolQuery.must(QueryBuilders
//                    .rangeQuery(Constance.FIELD_ZEBRA_NUMBERS)
//                    .gte(numbers.split(Constance.SPLIT_SEARCH_RANGE)[0])
//                    .lte(numbers.split(Constance.SPLIT_SEARCH_RANGE)[1]));
//        }
//        QueryBuilder nestQuery = QueryBuilders.nestedQuery(Constance.FIELD_ZEBRA_EXTENSIONS,
//                nestBoolQuery, ScoreMode.Avg);
//
//        totalComonFiledQuery.must(nestQuery);
//
//        //封装好公共的查询后，接下来进行对各个点进行查，进行查询，并且封装数据进行返回。
//        String[] points = locations.split(Constance.SPLIT_POPINTS);
//        GeoReGeoInfo geoReGeoInfo;
//        List<GeoReGeoInfo> geoReGeoInfos = new CopyOnWriteArrayList<>();
//        GeoReGeoBaseInfo geoReGeoBaseInfo;
//        String []ranges = distances.split(Constance.SPLIT_SEARCH_RANGE);
//        for (String point:points) {
//            // 按照范围查询进行query
//            GeoDistanceQueryBuilder geoDistanceQueryBuilder1;
//            GeoDistanceQueryBuilder geoDistanceQueryBuilder2 = null;
//            geoDistanceQueryBuilder1 = QueryBuilders
//                    .geoDistanceQuery(Constance.FIELD_ZEBRA_LOCATION)
//                    .point(new GeoPoint(point)).distance(Double.parseDouble(ranges[0])/1000 + "km");
//            if (ranges.length >= 2 && Double.parseDouble(ranges[0]) < Double.parseDouble(ranges[1])) {
//                geoDistanceQueryBuilder2 = QueryBuilders
//                        .geoDistanceQuery(Constance.FIELD_ZEBRA_LOCATION)
//                        .point(new GeoPoint(point)).distance(Double.parseDouble(ranges[1])/1000 + "km");
//            } else if(ranges.length >= 2 && Double.parseDouble(ranges[0]) >= Double.parseDouble(ranges[1])) {
//                geoDistanceQueryBuilder1 = QueryBuilders
//                        .geoDistanceQuery(Constance.FIELD_ZEBRA_LOCATION)
//                        .point(new GeoPoint(point)).distance(Double.parseDouble(ranges[1])/1000 + "km");
//                geoDistanceQueryBuilder2 = QueryBuilders
//                        .geoDistanceQuery(Constance.FIELD_ZEBRA_LOCATION)
//                        .point(new GeoPoint(point)).distance(Double.parseDouble(ranges[0])/1000 + "km");
//            }
//            // 按照距离进行排序query 封装
//            SortBuilder sortBuilder = SortBuilders
//                    .geoDistanceSort(Constance.FIELD_ZEBRA_LOCATION,
//                            new GeoPoint(point)).order(SortOrder.ASC);
//
//            // 总的query 对象
//            BoolQueryBuilder totalBoolQuery = QueryBuilders.boolQuery();
//            totalBoolQuery.must(totalComonFiledQuery);
//            if (ranges.length >= 2) {
//                totalBoolQuery.mustNot(geoDistanceQueryBuilder1).must(geoDistanceQueryBuilder2);
//            } else {
//                totalBoolQuery.must(geoDistanceQueryBuilder1);
//            }
//
//
//            // 封装请求对象
//            SearchRequestBuilder searchRequestBuilder = null;
//            try {
//                searchRequestBuilder = helper.getClient().prepareSearch(index)
//                        .setTypes(this.types).setQuery(totalBoolQuery)
//                        .addSort(sortBuilder);
//                if (StringUtil.isNotEmptyOrNull(from) && StringUtil.isNotEmptyOrNull(size)) {
//                    searchRequestBuilder.setFrom(Integer.parseInt(from)).setSize(Integer.parseInt(size));
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            // 获取response 对象,进行数据返回并进行封装
//            SearchResponse searchResponse = searchRequestBuilder.get();
//            // 获取数据并进行封装
//            SearchHits searchHits = searchResponse.getHits();
//            // 实际数据存储的数据
//            SearchHit[] hits = searchHits.getHits();
//
//            // 进行数据的返回
//            List<PoiInfo> poiInfos = new CopyOnWriteArrayList<>();
//            geoReGeoInfo = new GeoReGeoInfo();
//            for(int i = 0; i < hits.length; i++) {
//                geoReGeoBaseInfo = new GeoReGeoBaseInfo();
//                if(StringUtil.isNotEmptyOrNull(hits[i].getSource()
//                        .get(Constance.FIELD_ZEBRA_NAME))) {
//                    geoReGeoBaseInfo.setName((String) hits[i].getSource().
//                            get(Constance.FIELD_ZEBRA_NAME));
//                }
//                if(StringUtil.isNotEmptyOrNull(hits[i].getSource()
//                        .get(Constance.FIELD_ZEBRA_FIRSTLY_CLASSIFICATION))) {
//                    geoReGeoBaseInfo.setFirstly_classification((String) hits[i].getSource().
//                            get(Constance.FIELD_ZEBRA_FIRSTLY_CLASSIFICATION));
//                }
//                if(StringUtil.isNotEmptyOrNull(hits[i].getSource()
//                        .get(Constance.FIELD_ZEBRA_SECONDARY_CLASSIFICATION))) {
//                    geoReGeoBaseInfo.setSecondary_classification((String) hits[i].getSource().
//                            get(Constance.FIELD_ZEBRA_SECONDARY_CLASSIFICATION));
//                }
//                if(StringUtil.isNotEmptyOrNull(hits[i].getSource()
//                        .get(Constance.FIELD_ZEBRA_TYPE))) {
//                    geoReGeoBaseInfo.setType((String) hits[i].getSource().
//                            get(Constance.FIELD_ZEBRA_TYPE));
//                }
//                if(StringUtil.isNotEmptyOrNull(hits[i].getSource()
//                        .get(Constance.FIELD_ZEBRA_PROVINCE))) {
//                    geoReGeoBaseInfo.setProvince((String) hits[i].getSource().
//                            get(Constance.FIELD_ZEBRA_PROVINCE));
//                }
//                if(StringUtil.isNotEmptyOrNull(hits[i].getSource()
//                        .get(Constance.FIELD_ZEBRA_CITY))) {
//                    geoReGeoBaseInfo.setCity((String) hits[i].getSource().
//                            get(Constance.FIELD_ZEBRA_CITY));
//                }
//                if(StringUtil.isNotEmptyOrNull(hits[i].getSource()
//                        .get(Constance.FIELD_ZEBRA_CITYCODE))) {
//                    geoReGeoBaseInfo.setCitycode((String) hits[i].getSource().
//                            get(Constance.FIELD_ZEBRA_CITYCODE));
//                }
//                if(StringUtil.isNotEmptyOrNull(hits[i].getSource()
//                        .get(Constance.FIELD_ZEBRA_DISTRICT))) {
//                    geoReGeoBaseInfo.setDistrict((String) hits[i].getSource().
//                            get(Constance.FIELD_ZEBRA_DISTRICT));
//                }
//                if(StringUtil.isNotEmptyOrNull(hits[i].getSource()
//                        .get(Constance.FIELD_ZEBRA_ADCODE))) {
//                    geoReGeoBaseInfo.setAdcode((String) hits[i].getSource().
//                            get(Constance.FIELD_ZEBRA_ADCODE));
//                }
//                if(StringUtil.isNotEmptyOrNull(hits[i].getSource()
//                        .get(Constance.FIELD_ZEBRA_TOWNSHIP))) {
//                    geoReGeoBaseInfo.setTownship((String) hits[i].getSource().
//                            get(Constance.FIELD_ZEBRA_TOWNSHIP));
//                }
//                if(StringUtil.isNotEmptyOrNull(hits[i].getSource()
//                        .get(Constance.FIELD_ZEBRA_BUSINESS_CIRCLE))) {
//                    geoReGeoBaseInfo.setBusiness_circle((String) hits[i].getSource().
//                            get(Constance.FIELD_ZEBRA_BUSINESS_CIRCLE));
//                }
//                if(StringUtil.isNotEmptyOrNull(hits[i].getSource()
//                        .get(Constance.FIELD_ZEBRA_FORMATTED_ADDRESS))) {
//                    geoReGeoBaseInfo.setFormatted_address((String) hits[i].getSource().
//                            get(Constance.FIELD_ZEBRA_FORMATTED_ADDRESS));
//                }
//                //TODO  下个版本可能需要做的功能
//                if (extentions !=null && !"".equals(extentions)
//                        && Constance.EXTENSIONS_AOI.equals(extentions)) {
//                    geoReGeoInfo.setStatus(Constance.STATUS_FAILED);
//                    geoReGeoInfo.setInfo("返回Aoi 信息暂时未支持。");
//                    geoReGeoInfos.add(geoReGeoInfo);
//                    return  geoReGeoInfos;
//                }
//
//                if (extentions !=null && !"".equals(extentions)
//                        && Constance.EXTENSIONS_POI.equals(extentions)) {
//                    PoiInfo poiInfo = new PoiInfo();
//                    poiInfo.setGeoReGeoBaseInfo(geoReGeoBaseInfo);
//
//                    Map<String, Object> otherInfo = (Map<String, Object>) hits[i].getSource()
//                            .get(Constance.FIELD_ZEBRA_EXTENSIONS);
//                    if(StringUtil.isNotEmptyOrNull(otherInfo.get(Constance.FIELD_ZEBRA_AVG_PRICE))) {
//                        poiInfo.setAvgPrice((Double) otherInfo
//                                .get(Constance.FIELD_ZEBRA_AVG_PRICE));
//                    }
//                    if(StringUtil.isNotEmptyOrNull(otherInfo.get(Constance.FIELD_ZEBRA_SHOPS))) {
//                        poiInfo.setShops((Integer) otherInfo
//                                .get(Constance.FIELD_ZEBRA_SHOPS));
//                    }
//                    if(StringUtil.isNotEmptyOrNull(otherInfo.get(Constance.FIELD_ZEBRA_GOOD_COMMENTS))) {
//                        poiInfo.setGoodComments((Integer) otherInfo
//                                .get(Constance.FIELD_ZEBRA_GOOD_COMMENTS));
//                    }
//                    if(StringUtil.isNotEmptyOrNull(otherInfo.get(Constance.FIELD_ZEBRA_LVL))) {
//                        poiInfo.setLevel((Integer) otherInfo
//                                .get(Constance.FIELD_ZEBRA_LVL));
//                    }
//                    if(StringUtil.isNotEmptyOrNull(otherInfo.get(Constance.FIELD_ZEBRA_OTHER_TYPE))) {
//                        poiInfo.setOtherType((String) otherInfo
//                                .get(Constance.FIELD_ZEBRA_OTHER_TYPE));
//                    }
//                    if(StringUtil.isNotEmptyOrNull(otherInfo.get(Constance.FIELD_ZEBRA_NUMBERS))) {
//                        poiInfo.setNumbers((Integer) otherInfo
//                                .get(Constance.FIELD_ZEBRA_NUMBERS));
//                    }
//                    poiInfos.add(poiInfo);
//                }
//                if (i==0) {
//                    geoReGeoInfo.setGeoReGeoBaseInfo(geoReGeoBaseInfo);
//                }
//            }
//            geoReGeoInfo.setStatus(Constance.STATUS_SUCCESS);
//            geoReGeoInfo.setInfo("ok");
//            geoReGeoInfo.setPoiInfos(poiInfos);
//            geoReGeoInfos.add(geoReGeoInfo);
//        }
//
//        return geoReGeoInfos;
//    }
//
//    public AggregationValues getAggregationValues(String province,
//                                                  String city, String district,
//                                                  String firstly_classification,
//                                                  String secondary_classification,
//                                                  String aggrationField,
//                                                  String from,
//                                                  String size) {
//        //开始封装query查询
//        //总的query 对象
//        BoolQueryBuilder totalBoolQuery = QueryBuilders.boolQuery();
//        //针对 province 的查询
//        if (StringUtil.isNotEmptyOrNull(province)) {
//            totalBoolQuery.must(QueryBuilders.termQuery(Constance.FIELD_ZEBRA_PROVINCE, province));
//        }
//        //针对 city 的查询
//        if (StringUtil.isNotEmptyOrNull(city)) {
//            totalBoolQuery.must(QueryBuilders.termQuery(Constance.FIELD_ZEBRA_CITY, city));
//        }
//        //针对 district 的查询
//        if (StringUtil.isNotEmptyOrNull(district)) {
//            totalBoolQuery.must(QueryBuilders.termQuery(Constance.FIELD_ZEBRA_DISTRICT, district));
//        }
//        //针对 district 的查询
//        if (StringUtil.isNotEmptyOrNull(firstly_classification)) {
//            totalBoolQuery.must(QueryBuilders.termQuery(Constance.FIELD_ZEBRA_FIRSTLY_CLASSIFICATION,
//                    firstly_classification));
//        }
//        //针对 district 的查询
//        if (StringUtil.isNotEmptyOrNull(secondary_classification)) {
//            totalBoolQuery.must(QueryBuilders.termQuery(Constance.FIELD_ZEBRA_SECONDARY_CLASSIFICATION,
//                    secondary_classification));
//        }
//
//        // 聚合查询
//        AggregationBuilder aggregationBuilder = null;
//        if (StringUtil.isNotEmptyOrNull(aggrationField)) {
//            aggregationBuilder = AggregationBuilders.terms("all_values")
//                    .field(aggrationField);
//        }
//
//        // 开始封装查询对象
//        SearchRequestBuilder searchRequestBuilder;
//
//        searchRequestBuilder = helper.getClient().prepareSearch(index)
//                .setTypes(types)
//                .setQuery(totalBoolQuery)
//                .addAggregation(aggregationBuilder)
//                .setFrom(Integer.parseInt(from))
//                .setSize(Integer.parseInt(size));
//
//        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
//        Terms terms = searchResponse.getAggregations().get("all_values");
//
//        AggregationValues aggregationValues = new AggregationValues();
//        List<String> aggreValues = new CopyOnWriteArrayList<>();
//        for (Terms.Bucket bucket:terms.getBuckets()) {
//            aggreValues.add(bucket.getKeyAsString());
//        }
//        aggregationValues.setStatus(Constance.STATUS_SUCCESS);
//        aggregationValues.setStatusInfo("ok");
//        aggregationValues.setAggregationValuses(aggreValues);
//
//        return aggregationValues;
//    }
//}

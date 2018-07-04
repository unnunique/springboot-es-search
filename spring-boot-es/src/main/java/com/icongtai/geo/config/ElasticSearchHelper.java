package com.icongtai.geo.config;//package com.icongtai.geo.esconfig;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

@Component
public class ElasticSearchHelper {

//    /**
//     * es集群地址
//     */
//    @Value("${elasticsearch.ip}")
//    private String hostName;
//    /**
//     * 端口
//     */
//    @Value("${elasticsearch.port}")
//    private String port;
//    /**
//     * 集群名称
//     */
//    @Value("${elasticsearch.cluster.name}")
//    private String clusterName;
//
//    /**
//     * 连接池
//     */
//    @Value("${elasticsearch.pool}")
//    private String poolSize;

//    @Autowired
    private ElasticSearchClusterConf elasticSearchClusterConf;

    private TransportClient client;

    public TransportClient getClient() {
        return client;
    }

    private static transient ElasticSearchHelper instance;

//    public static ElasticSearchHelper getInstance() {
//        if(instance == null) {
//            synchronized(ElasticSearchHelper.class) {
//                if(instance == null) {
//                    instance = new ElasticSearchHelper(this.hostName, this.port, this.clusterName, this.poolSize);
//                }
//            }
//        }
//        return instance;
//    }

    public ElasticSearchHelper(ElasticSearchClusterConf elasticSearchClusterConf) {
        try {
            System.out.println(elasticSearchClusterConf);
            System.out.println(elasticSearchClusterConf.getHostName() +
                    "----------:-------" + elasticSearchClusterConf.getClusterName()
                    + "---:-----" + elasticSearchClusterConf.getPoolSize());
            // 配置信息
            Settings esSetting = Settings.builder()
                    .put("cluster.name", elasticSearchClusterConf.getClusterName())
                    // 增加嗅探机制，找到ES集群
                    .put("client.transport.sniff", true)
                    // 增加线程个数，默认为设置成5
                    .put("thread_pool.search.size",
                            Integer.parseInt(elasticSearchClusterConf.getPoolSize()))
                    .build();
            client = new PreBuiltTransportClient(esSetting);
            InetSocketTransportAddress inetSocketTransportAddress =
                    new InetSocketTransportAddress(InetAddress.getByName(elasticSearchClusterConf.getHostName()),
                            Integer.valueOf(elasticSearchClusterConf.getPort()));
            client.addTransportAddresses(inetSocketTransportAddress);

        } catch (Exception e) {
            System.out.println("elasticsearch TransportClient create error!!!");
        }
    }

}

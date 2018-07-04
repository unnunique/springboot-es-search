package com.icongtai.geo.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ElasticSearchClusterConf {
    /**
     * es集群地址
     */
    @Value("${elasticsearch.ip}")
    private String hostName;
    /**
     * 端口
     */
    @Value("${elasticsearch.port}")
    private String port;
    /**
     * 集群名称
     */
    @Value("${elasticsearch.cluster.name}")
    private String clusterName;

    /**
     * 连接池
     */
    @Value("${elasticsearch.pool}")
    private String poolSize;


    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(String poolSize) {
        this.poolSize = poolSize;
    }

    public ElasticSearchClusterConf() {
        System.out.println("init es conf.....");
    }

    @PostConstruct //加上该注解表明该方法会在bean初始化后调用
    private void init(){
        //这里便可以获取到environment
        String path="/config  "+clusterName+"  config.xml";
        System.out.println("path............................." + path);
    }

    @Override
    public String toString() {
        return "ElasticSearchClusterConf{" +
                "hostName='" + hostName + '\'' +
                ", port='" + port + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", poolSize='" + poolSize + '\'' +
                '}';
    }
}

package com.atguigu.gmall.list.api;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.model.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/list")
public class ListApiController {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;


    //创建商品索引库
    @GetMapping("/inner/createIndex")
    public Result createIndex() {
        //Elasticsearch的Java客户端工具包中的一个方法，用于向 Elasticsearch 集群发送创建 Goods 索引请求
        restTemplate.createIndex(Goods.class);
        //Elasticsearch的Java客户端工具包中的一个方法，用于向 Elasticsearch 集群发送设置 Goods 类型的索引映射请求
        restTemplate.putMapping(Goods.class);
        return Result.ok();
    }

}

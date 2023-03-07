package com.atguigu.gmall.list.api;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.model.SearchParam;
import com.atguigu.gmall.list.model.SearchResponseVo;
import com.atguigu.gmall.list.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/list")
public class ListApiController {

    @Autowired
    private SearchService searchService;


    //更新商品的热度排名分值
    @GetMapping("/inner/incrHotScore/{skuId}")
    public Result incrHotScore(@PathVariable("skuId") Long skuId){
        searchService.incrHotScore(skuId);
        return Result.ok();
    }

    //商品检索
    @PostMapping("/inner")
    public Result search(@RequestBody SearchParam searchParam) {
        SearchResponseVo responseVo = searchService.search(searchParam);
        return Result.ok(responseVo);
    }
}

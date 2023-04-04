package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.model.BaseCategoryTrademark;
import com.atguigu.gmall.product.mapper.BaseCategoryTrademarkMapper;
import com.atguigu.gmall.product.model.BaseTrademark;
import com.atguigu.gmall.product.model.CategoryTrademarkVo;
import com.atguigu.gmall.product.service.BaseCategoryTrademarkService;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jodd.util.CollectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ConnectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类品牌中间表 业务实现类
 */
@Service
public class BaseCategoryTrademarkServiceImpl extends ServiceImpl<BaseCategoryTrademarkMapper, BaseCategoryTrademark> implements BaseCategoryTrademarkService {

    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;

    @Autowired
    private BaseCategoryTrademarkMapper baseCategoryTrademarkMapper;


    //根据category3Id查询品牌列表
    @Override
    public List<BaseTrademark> findTrademarkList(Long category3Id) {
        //1.先查询种类-品牌中间表内的所有该种类的品牌ID，生成集合
        LambdaQueryWrapper<BaseCategoryTrademark> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseCategoryTrademark::getCategory3Id, category3Id);
        List<BaseCategoryTrademark> baseCategoryTrademarks = super.list(queryWrapper);

        //2.抽取品牌ID为集合
        //判断是否为空，不为空才处理
        if(!CollectionUtils.isEmpty(baseCategoryTrademarks)){
            List<Long> trademarksIdList = baseCategoryTrademarks.stream().map(BaseCategoryTrademark::getTrademarkId).collect(Collectors.toList());

            //3.根据品牌id集合，批处理获取品牌对象
            List<BaseTrademark> baseTrademarks = baseTrademarkMapper.selectBatchIds(trademarksIdList);
            return baseTrademarks;
        }
        return null;
    }


    //查询当前分类可选品牌列表
    @Override
    public List<BaseTrademark> findCurrentTrademarkList(Long category3Id) {
        return baseTrademarkMapper.findCurrentTrademarkList(category3Id);
    }

    //将品牌关联到分类，多选批量添加p品牌的接口
    @Override
    public void saveBasecategoryTrademark(CategoryTrademarkVo categoryTrademarkVo) {
        //1.获取品牌ID集合，TrademarkIdList是其一个属性
        List<Long> trademarkIdList = categoryTrademarkVo.getTrademarkIdList();
        //2.遍历品牌ID集合构建品牌分类中间对象 执行新增
        //判断获取的品牌ID集合是否为空，不为空就用Stream流进行新增
        if (!CollectionUtils.isEmpty(trademarkIdList)) {
            //采用Stream流将集合泛型 由  Long 转为 BaseCategoryTrademark
            List<BaseCategoryTrademark> baseCategoryTrademarkList = trademarkIdList.stream().map(tmId -> {
                BaseCategoryTrademark baseCategoryTrademark = new BaseCategoryTrademark();
                //tmId就是品牌ID，为每个中间表设置品牌ID和类型ID
                baseCategoryTrademark.setTrademarkId(tmId);
                baseCategoryTrademark.setCategory3Id(categoryTrademarkVo.getCategory3Id());
                return baseCategoryTrademark;
            }).collect(Collectors.toList());
            //执行当前关系业务父类中方法 批量保存
            this.saveBatch(baseCategoryTrademarkList);
        }
    }

    //删除分类品牌关联，删除品牌的接口
    @Override
    public void removeCategoryTrademark(Long category3Id, Long trademarkId) {
        //方式一：逻辑删除
        LambdaQueryWrapper<BaseCategoryTrademark> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseCategoryTrademark::getCategory3Id, category3Id);
        queryWrapper.eq(BaseCategoryTrademark::getTrademarkId, trademarkId);
        this.remove(queryWrapper);
        //方式二：执行物理删除
        baseCategoryTrademarkMapper.removeCategoryTrademark(category3Id, trademarkId);
    }


}

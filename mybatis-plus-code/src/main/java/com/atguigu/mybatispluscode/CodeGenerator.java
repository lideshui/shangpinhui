package com.atguigu.mybatispluscode;

import com.atguigu.gmall.base.model.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.*;

/**
 * 可参考：https://blog.csdn.net/weixin_44541136/article/details/121202871
 */

public class CodeGenerator {


    public static void main(String[] args) {
        // 设置代码生成的位置
        // TODO String projectPath = System.getProperty("user.dir");⚠️
        String projectPath = "/Users/shuaigouzi/shangpinhui/gmall-parent/gmall-service";
        // 设置父模块名称
        String parentModuleName = "com.atguigu.gmall";
        // 设置子模块名称⚠️
        String moduleName = "user";
        String subPath = "/service-" + moduleName;
        // 设置数据库连接⚠️
        String databaseUrl = "jdbc:mysql://101.200.162.17:3306/gmall_" + moduleName + "?useUnicode=true&useSSL=false&characterEncoding=utf8";
        // 数据库用户名
        String username = "root";
        //TODO 数据库密码⚠️
        String password = "123456";
        // 设置表名前缀，例如表为tb_UserInfo，这里设置表前缀为"tb_",生成实体类的时候会自动去除前缀，最终生成UserInfo
        String tablePrefix = "";
        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();
        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        // 最终输出目录
        gc.setOutputDir(projectPath + subPath + "/src/main/java");
        gc.setAuthor("atguigu"); //作者
        gc.setOpen(false); //是否打开输出目录
        gc.setSwagger2(true); //实体属性 Swagger2 注解
        gc.setDateType(DateType.ONLY_DATE); //时间类型为 Date LocalDateTime
        // 设置主键类型  ASSIGN_ID为分布式全局唯一ID  AUTO:数据库自增
        gc.setIdType(IdType.AUTO);
        //gc.setIdType(IdType.ASSIGN_ID);
        // 是否覆盖已有文件
        gc.setFileOverride(false);
        //去掉Service接口的首字母I
        gc.setServiceName("%sService");
        mpg.setGlobalConfig(gc);

        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl(databaseUrl);
        // dsc.setSchemaName("public");
        dsc.setDriverName("com.mysql.jdbc.Driver");
        dsc.setUsername(username);
        dsc.setPassword(password);
        mpg.setDataSource(dsc);

        // 包配置
        PackageConfig pc = new PackageConfig();
//        pc.setModuleName(scanner("模块名"));
        //设置实体类包名
        pc.setEntity("model");
        pc.setParent(parentModuleName);
        pc.setModuleName(moduleName);

        mpg.setPackageInfo(pc);

        // 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            //自定义属性注入
            //在.ftl(或者是.vm)模板中，通过${cfg.abc}获取属性
            @Override
            public void initMap() {
                Map<String, Object> map = new HashMap<>();
                map.put("parentName", parentModuleName);
                map.put("moduleName", moduleName);
                this.setMap(map);
            }
        };
        mpg.setCfg(cfg);

        // 配置模板
        TemplateConfig templateConfig = new TemplateConfig();
        templateConfig.setEntity("templates/entity2.java");
        //指定自定义模板路径, 位置：/resources/templates/entity2.java.ftl(或者是.vm)
        templateConfig.setMapper("templates/mapper2.java");
        templateConfig.setService("templates/service2.java");
        templateConfig.setServiceImpl("templates/serviceImpl2.java");
        templateConfig.setController("templates/controller2.java");
        mpg.setTemplate(templateConfig);

        //TODO 根据需要来设置！！！例如：禁用模版的方式禁止生成实体类⚠️
        //TemplateType.CONTROLLER
        templateConfig.disable(TemplateType.ENTITY,TemplateType.CONTROLLER);
        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setSuperEntityClass(BaseEntity.class);
        //写于父类中的公共字段
        strategy.setSuperEntityColumns("id", "create_time", "update_time", "is_deleted");
        strategy.setNaming(NamingStrategy.underline_to_camel);  //数据库表映射到实体的命名策略
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);    //数据库表字段映射到实体的命名策略
        strategy.setEntityLombokModel(true);    //【实体】是否为lombok模型（默认 false）
        strategy.setRestControllerStyle(true);
        strategy.setEntityTableFieldAnnotationEnable(true); //是否生成实体时，生成字段注解


        strategy.setInclude(scanner("表名，多个英文逗号分割").split(","));
        strategy.setControllerMappingHyphenStyle(true);
        strategy.setTablePrefix(tablePrefix);
        mpg.setStrategy(strategy);
        mpg.setTemplateEngine(new FreemarkerTemplateEngine());
        mpg.execute();
    }

    /**
     * <p>
     * 读取控制台内容
     * </p>
     */
    public static String scanner(String tip) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder help = new StringBuilder();
        help.append("请输入" + tip + "：");
        System.out.println(help.toString());
        if (scanner.hasNext()) {
            String ipt = scanner.next();
            if (StringUtils.isNotBlank(ipt)) {
                return ipt;
            }
        }
        throw new MybatisPlusException("请输入正确的" + tip + "！");
    }

}

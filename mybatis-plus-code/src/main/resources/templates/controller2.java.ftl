package ${package.Controller};


import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ${package.Service}.${table.serviceName};

<#if restControllerStyle>
import org.springframework.web.bind.annotation.RestController;
<#else>
import org.springframework.stereotype.Controller;
</#if>
<#if superControllerClassPackage??>
import ${superControllerClassPackage};
</#if>

/**
 * ${table.comment!} 前端控制器
 *
 * @author ${author}
 * @since ${date}
 */
<#if swagger2>
@Api(tags = "${table.comment!}控制器")
</#if>
<#if restControllerStyle>
@RestController
<#else>
@Controller
</#if>
<#if package.ModuleName == table.entityPath>
@RequestMapping("<#if package.ModuleName??>/${package.ModuleName}</#if>")
<#else>
<#--@RequestMapping("/api/v1/${table.name?keep_after("_")}")-->
@RequestMapping("") //TODO 填写基础映射URL
</#if>
<#if kotlin>
class ${table.controllerName}<#if superControllerClass??> : ${superControllerClass}()</#if>
<#else>
<#if superControllerClass??>
public class ${table.controllerName} extends ${superControllerClass} {
<#else>
<#--@CrossOrigin-->
public class ${table.controllerName} {

    @Autowired
    private ${table.serviceName} ${table.entityPath}Service;


</#if>

}
</#if>

<template>
  <div class="app-container">

    <!--三级下拉列表-->
    <CategorySelector v-show="!showSkuForm" @listenOnSelect="getSpuList" />

    <!-- banner列表 -->
    <el-table
      v-show="!showSkuForm"
      v-loading="listLoading"
      :data="list"
      element-loading-text="数据正在加载......"
      border
      fit
      highlight-current-row>

      <el-table-column
        label="序号"
        width="70"
        align="center">
        <template slot-scope="scope">
          {{ (page - 1) * limit + scope.$index + 1 }}
        </template>
      </el-table-column>

      <el-table-column prop="id" label="SKU ID" width="100"/>

      <el-table-column label="banner" width="320" align="center">
        <template slot-scope="scope">
          <div class="info">
            <div class="pic">
              <img :src="scope.row.skuDefaultImg" alt="scope.row.title" style="width: 50px;">
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="skuName" label="名称" />

      <el-table-column prop="price" label="价格" width="70"/>

      <el-table-column label="操作" width="230" align="center">
        <template slot-scope="scope">
          <el-button type="primary" size="mini" icon="el-icon-plus" @click="showSkuUpdate(scope.row)">修改SKU</el-button>
          <el-button v-if="scope.row.isSale == 0" size="mini" icon="el-icon-edit" @click="onSale(scope.row.id)">上架</el-button>
          <el-button v-if="scope.row.isSale == 1" type="danger" size="mini" icon="el-icon-delete" @click="cancelSale(scope.row.id)">下架</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <el-pagination
      v-show="!showSkuForm"
      :current-page="page"
      :total="total"
      :page-size="limit"
      :page-sizes="[5, 10, 20, 30, 40, 50, 100]"
      style="padding: 30px 0; text-align: center;"
      layout="sizes, prev, pager, next, jumper, ->, total, slot"
      @current-change="fetchData"
      @size-change="changeSize"
    />

    <!--sku表单-->
    <SkuForm
      v-show="showSkuForm"
      ref="skuForm"
      :category-id="categoryId"
      :spu-id="spuId"
      :spu-name="spuName"
      @listenOnSave="onSkuSave()"
      @listenOnClose="onSkuClose()"/>
  </div>
</template>

<script>
import sku from '@/api/product/sku'
import SkuForm from '@/views/product/components/SkuForm'
import CategorySelector from '@/views/components/CategorySelector'

export default {
  components: { CategorySelector, SkuForm },

  data() {
    return {
      listLoading: false, // 数据是否正在加载
      list: null, // banner列表
      total: 0, // 数据库中的总记录数
      page: 1, // 默认页码
      limit: 10, // 每页记录数
      searchObj: {}, // 查询表单对象
      multipleSelection: [], // 批量选择中选择的记录列表

      // 属性所属分类
      category1Id: 0,
      category2Id: 0,
      category3Id: 0,
      categoryLevel: 1,

      // sku表单显示
      showSkuForm: false,
      spuId: null,
      spuName: null
    }
  },

  // 生命周期函数：内存准备完毕，页面尚未渲染
  created() {
    console.log('list created......')
    // this.fetchData()
  },

  // 生命周期函数：内存准备完毕，页面渲染成功
  mounted() {
    console.log('list mounted......')
  },

  methods: {
    // 获取spu列表
    getSpuList(categoryId = 1, categoryLevel) {
      debugger
      this.categoryLevel = categoryLevel
      if (categoryLevel === 1) {
        this.category1Id = categoryId
        this.category2Id = 0
        this.category3Id = 0
      }
      if (categoryLevel === 2) {
        this.category2Id = categoryId
        this.category3Id = 0
      }
      if (categoryLevel === 3) {
        this.category3Id = categoryId
         this.searchObj.category3Id = categoryId
        // 查询数据
        this.fetchData(1)
      }
    },

    // 当页码发生改变的时候
    changeSize(size) {
      console.log(size)
      this.limit = size
      this.fetchData(1)
    },

    // 加载banner列表数据
    fetchData(page = 1) {
      this.listLoading = true
      console.log('翻页。。。' + page)
      // 异步获取远程数据（ajax）
      this.page = page

      sku.getPageList(this.page, this.limit, this.searchObj).then(response => {
        this.list = response.data.records
        this.total = response.data.total

        // 数据加载并绑定成功
        this.listLoading = false
      })
    },

    onSale(skuId) {
      sku.onSale(skuId).then(response => {
        this.$message({
          type: 'success',
          message: '操作成功!'
        })
        this.fetchData(this.page)
      })
    },

    cancelSale(skuId) {
      sku.cancelSale(skuId).then(response => {
        this.$message({
          type: 'success',
          message: '操作成功!'
        })
        this.fetchData(this.page)
      })
    },

    // 重置查询表单
    resetData() {
      console.log('重置查询表单')
      this.searchObj = {}
      this.fetchData()
    },

    showSkuUpdate(row) {
      this.spuId = row.spuId
      this.spuName = row.skuName

      // 初始化表单
      this.$refs.skuForm.init(row.id, row.spuId, this.category1Id, this.category2Id, this.category3Id, row.tmId)

      // 显示表单
      this.showSkuForm = true
    },

    onSkuSave() {
      // 隐藏表单
      this.showSkuForm = false
    },

    // 关闭sku表单
    onSkuClose() {
      // 隐藏表单
      this.showSkuForm = false
    }
  }
}
</script>


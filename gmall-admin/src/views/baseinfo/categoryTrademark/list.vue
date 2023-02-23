<template>
  <div class="app-container">

    <!--三级下拉列表-->
    <CategorySelector @listenOnSelect="getAttrInfoList" />

    <div v-show="!showAttrInfoForm">
      <div style="margin-bottom:10px;">
        <el-button type="primary" icon="el-icon-plus" size="mini" @click="add()">添加品牌</el-button>
      </div>
      <el-table
        v-loading="listLoading"
        :data="list"
        stripe
        style="width: 100%">

        <el-table-column
          label="序号"
          width="70"
          align="center">
          <template slot-scope="scope">
            {{ scope.$index + 1 }}
          </template>
        </el-table-column>

        <el-table-column label="logo" width="320" align="center">
          <template slot-scope="scope">
            <div class="info">
              <div class="pic">
                <img :src="scope.row.logoUrl" alt="scope.row.title" width="100px">
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="tmName" label="名称" />

        <el-table-column label="操作" width="200" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="mini" icon="el-icon-delete" @click="removeDataById(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog title="添加品牌" :visible.sync="dialogVisible" width="40%" >
      <el-form ref="flashPromotionForm" label-width="150px" size="small" style="padding-right: 40px;">

        <el-table
          v-loading="listLoading"
          :data="trademarkList"
          stripe
          style="width: 100%"
          @selection-change="handleSelectionChange">

          <el-table-column
            type="selection"
            width="55"/>

          <el-table-column
            label="序号"
            width="70"
            align="center">
            <template slot-scope="scope">
              {{ scope.$index + 1 }}
            </template>
          </el-table-column>
          <el-table-column prop="tmName" label="名称" />
        </el-table>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false" size="small">取 消</el-button>
        <el-button type="primary" @click="save()" size="small">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import categoryTrademarkApi from '@/api/baseinfo/categoryTrademark'
import CategorySelector from '@/views/components/CategorySelector'

const defaultForm = {
  category3Id: null,
  trademarkIdList: []
}
export default {
  components: { CategorySelector },

  data() {
    return {
      listLoading: false, // 数据是否正在加载
      list: null, // banner列表

      showAttrInfoForm: false,
      category3Id: null,

      trademarkList: [],
      categoryTrademarkVo: defaultForm,

      multipleSelection: [], // 批量选择中选择的记录列表
      dialogVisible: false,
      saveBtnDisabled: false
    }
  },

  // 生命周期函数：内存准备完毕，页面尚未渲染
  created() {
    console.log('list created......')
  },

  // 生命周期函数：内存准备完毕，页面渲染成功
  mounted() {
    console.log('list mounted......')
  },

  methods: {
    getAttrInfoList(categoryId, categoryLevel) {
      if (categoryLevel === 3) {
        this.category3Id = categoryId
        // 查询数据
        this.fetchData()
      }
    },

    add() {
      if (this.category3Id == null) {
        this.$message({
          type: 'info',
          message: '请选择分类!'
        })
        return
      }
      this.getTrademarkList()
    },

    /*
   当表格复选框选项发生变化的时候触发
   */
    handleSelectionChange(selection) {
      this.multipleSelection = selection
    },

    save() {
      var idList = []
      this.multipleSelection.forEach(item => {
        idList.push(item.id)
      })
      this.categoryTrademarkVo.trademarkIdList = idList
      this.categoryTrademarkVo.category3Id = this.category3Id
      categoryTrademarkApi.save(this.categoryTrademarkVo).then(response => {
        if (response.code) {
          this.$message({
            type: 'success',
            message: response.message
          })
          this.dialogVisible = false
          this.fetchData()
        }
      })
    },

    // 加载banner列表数据
    fetchData() {
      this.listLoading = true
      categoryTrademarkApi.findTrademarkList(this.category3Id).then(
        response => {
          debugger
          this.list = response.data

          // 数据加载并绑定成功
          this.listLoading = false
        }
      )
    },

    getTrademarkList() {
      // 查询数据
      categoryTrademarkApi.findCurrentTrademarkList(this.category3Id).then(response => {
        this.trademarkList = response.data
        this.dialogVisible = true
      })
    },

    // 重置查询表单
    resetData() {
      console.log('重置查询表单')
      this.searchObj = {}
      this.fetchData()
    },

    // 根据id删除数据
    removeDataById(id) {
      // debugger
      this.$confirm('此操作将永久删除该记录, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => { // promise
        // 点击确定，远程调用ajax
        return categoryTrademarkApi.remove(this.category3Id, id)
      }).then((response) => {
        this.fetchData()
        if (response.code) {
          this.$message({
            type: 'success',
            message: '删除成功!'
          })
        }
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        })
      })
    }
  }
}
</script>


import request from '@/utils/request'

const api_name = '/admin/product/baseCategoryTrademark'

export default {

  findTrademarkList(category3Id) {
    return request({
      url: `${api_name}/findTrademarkList/${category3Id}`,
      method: 'get'
    })
  },

  findCurrentTrademarkList(category3Id) {
    return request({
      url: `${api_name}/findCurrentTrademarkList/${category3Id}`,
      method: 'get'
    })
  },

  save(categoryTrademarkVo) {
    return request({
      url: `${api_name}/save`,
      method: 'post',
      data: categoryTrademarkVo
    })
  },

  remove(category3Id, trademarkId) {
    return request({
      url: `${api_name}/remove/${category3Id}/${trademarkId}`,
      method: 'delete'
    })
  }
}

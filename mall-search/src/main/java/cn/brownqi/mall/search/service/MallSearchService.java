package cn.brownqi.mall.search.service;

import cn.brownqi.mall.search.vo.SearchParamVO;
import cn.brownqi.mall.search.vo.SearchResponseVO;

public interface MallSearchService {
    /**
     *
     * @param param 检索的所有参数
     * @return 返回检索的结果，里面包含页面所需要的所有信息
     */
    SearchResponseVO search(SearchParamVO param);
}

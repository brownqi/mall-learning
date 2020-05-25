package cn.brownqi.mall.search.controller;

import cn.brownqi.mall.search.service.MallSearchService;
import cn.brownqi.mall.search.vo.SearchParamVO;
import cn.brownqi.mall.search.vo.SearchResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    /**
     * 自动将页面提交过来的所有请求查询参数，封装成指定的对象-
     * @param param
     * @return
     */
    @GetMapping("/list.html")
    public String listPage(SearchParamVO param, Model model){

        // 1、根据传递来的页面的擦汗寻参数，去es中检索商品
        SearchResponseVO result = mallSearchService.search(param);
        model.addAttribute("result",result);

        return "list";
    }

}

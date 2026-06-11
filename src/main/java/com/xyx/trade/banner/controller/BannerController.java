package com.xyx.trade.banner.controller;

import com.xyx.trade.banner.domain.Banner;
import com.xyx.trade.banner.service.BannerService;
import com.xyx.trade.user.util.AjaxResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 轮播图Controller
 */
@Api(tags = "轮播图模块")
@RestController
@RequestMapping("/api/banner")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    /**
     * 获取轮播图列表
     * 
     * @param count 数量
     * @return 结果
     */
    @ApiOperation(value = "获取轮播图列表", notes = "获取首页轮播图")
    @GetMapping("/list")
    public AjaxResult list(@ApiParam(value = "数量", defaultValue = "5") @RequestParam(defaultValue = "5") int count) {
        return bannerService.getBannerList(count);
    }

    /**
     * 添加轮播图
     * 
     * @param banner 轮播图
     * @return 结果
     */
    @ApiOperation(value = "添加轮播图", notes = "添加新的轮播图")
    @PostMapping("/add")
    public AjaxResult add(@RequestBody Banner banner) {
        return bannerService.addBanner(banner);
    }

    /**
     * 修改轮播图
     * 
     * @param banner 轮播图
     * @return 结果
     */
    @ApiOperation(value = "修改轮播图", notes = "修改轮播图信息")
    @PutMapping("/update")
    public AjaxResult update(@RequestBody Banner banner) {
        return bannerService.updateBanner(banner);
    }

    /**
     * 删除轮播图
     * 
     * @param id ID
     * @return 结果
     */
    @ApiOperation(value = "删除轮播图", notes = "根据ID删除轮播图")
    @DeleteMapping("/delete/{id}")
    public AjaxResult delete(@PathVariable Long id) {
        return bannerService.deleteBanner(id);
    }

    /**
     * 获取轮播图详情
     * 
     * @param id ID
     * @return 结果
     */
    @ApiOperation(value = "获取轮播图详情", notes = "根据ID获取轮播图详情")
    @GetMapping("/detail/{id}")
    public AjaxResult detail(@PathVariable Long id) {
        return bannerService.getBannerDetail(id);
    }
}


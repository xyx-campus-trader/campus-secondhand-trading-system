package com.xyx.trade.banner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xyx.trade.banner.domain.Banner;
import com.xyx.trade.user.util.AjaxResult;

import java.util.List;

/**
 * 轮播图服务接口
 */
public interface BannerService extends IService<Banner> {
    /**
     * 获取轮播图列表
     * 
     * @param count 数量
     * @return 结果
     */
    AjaxResult<List<Banner>> getBannerList(int count);

    /**
     * 添加轮播图
     * 
     * @param banner 轮播图
     * @return 结果
     */
    AjaxResult<Banner> addBanner(Banner banner);

    /**
     * 修改轮播图
     * 
     * @param banner 轮播图
     * @return 结果
     */
    AjaxResult<String> updateBanner(Banner banner);

    /**
     * 删除轮播图
     * 
     * @param id ID
     * @return 结果
     */
    AjaxResult<String> deleteBanner(Long id);

    /**
     * 获取轮播图详情
     * 
     * @param id ID
     * @return 结果
     */
    AjaxResult<Banner> getBannerDetail(Long id);

    /**
     * 获取所有轮播图（管理员用）
     */
    AjaxResult<List<Banner>> getAllBanners();
}

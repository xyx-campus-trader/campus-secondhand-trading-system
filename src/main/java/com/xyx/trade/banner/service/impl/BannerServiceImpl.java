package com.xyx.trade.banner.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyx.trade.banner.domain.Banner;
import com.xyx.trade.banner.mapper.BannerMapper;
import com.xyx.trade.banner.service.BannerService;
import com.xyx.trade.user.util.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 轮播图服务实现类
 */
@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    /**
     * 获取轮播图列表（带缓存逻辑）
     */
    @SuppressWarnings("unchecked")
    @Override
    public AjaxResult<List<Banner>> getBannerList(int count) {
        List<Banner> list = baseMapper.selectActiveBanners();
        return AjaxResult.success(list);
    }

    /**
     * 添加轮播图
     */
    @Override
    public AjaxResult<Banner> addBanner(Banner banner) {
        banner.setId(null);
        banner.setCreateTime(null);

        baseMapper.insert(banner);
        return AjaxResult.success("添加成功", banner);
    }

    /**
     * 修改轮播图信息
     */
    @Override
    public AjaxResult<String> updateBanner(Banner banner) {
        banner.setCreateTime(null);

        baseMapper.update(banner);
        return AjaxResult.success("修改成功");
    }

    /**
     * 删除轮播图
     */
    @Override
    public AjaxResult<String> deleteBanner(Long id) {
        baseMapper.deleteById(id);
        return AjaxResult.success("删除成功");
    }

    /**
     * 根据 ID 查询轮播图详情
     */
    @Override
    public AjaxResult<Banner> getBannerDetail(Long id) {
        Banner banner = baseMapper.selectById(id);
        if (banner != null) {
            return AjaxResult.success(banner);
        }
        return AjaxResult.error("轮播图不存在");
    }

    /**
     * 查询所有轮播图列表（管理端使用）
     */
    @Override
    public AjaxResult getAllBanners() {
        List<Banner> banners = baseMapper.selectAllBanners();
        return AjaxResult.success(banners);
    }
}


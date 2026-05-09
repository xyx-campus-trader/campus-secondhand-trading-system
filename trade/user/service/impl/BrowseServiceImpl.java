package com.xyx.trade.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyx.trade.user.domain.Browse;
import com.xyx.trade.user.mapper.BrowseMapper;
import com.xyx.trade.user.service.BrowseService;
import org.springframework.stereotype.Service;

@Service
public class BrowseServiceImpl extends ServiceImpl<BrowseMapper, Browse> implements BrowseService {

    @Override
    public void addBrowse(Long userId, Long productId) {
        Browse browse = new Browse();
        browse.setUserId(userId);
        browse.setProductId(productId);
        save(browse);
    }
}
package com.xyx.trade.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyx.trade.user.domain.Qa;
import com.xyx.trade.user.mapper.QaMapper;
import com.xyx.trade.user.service.QaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QaServiceImpl extends ServiceImpl<QaMapper, Qa> implements QaService {

    @Override
    public String answer(String msg) {
        LambdaQueryWrapper<Qa> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Qa::getQuestion, msg);
        wrapper.eq(Qa::getStatus, 1);

        List<Qa> list = this.list(wrapper);

        if (list.isEmpty()) {
            return "你好，我是AI客服，你可以问我：订单、付款、发货、退款、取消、商品发布等问题";
        }

        return list.get(0).getAnswer();
    }
}

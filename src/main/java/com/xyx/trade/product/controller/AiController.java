package com.xyx.trade.product.controller;

import com.xyx.trade.product.domain.Product;
import com.xyx.trade.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private ProductService productService;

    @GetMapping("/chat")
    public Map<String, Object> chat(@RequestParam String msg) {
        String answer = getAiAnswer(msg);
        return Map.of("code", 200, "msg", "获取成功", "data", Map.of("answer", answer));
    }

    @GetMapping("/search/suggest")
    public Map<String, Object> getSearchSuggest(@RequestParam String keyword) {
        Map<String, Object> result = productService.getProductList(
                null, keyword, null, null, null, null, null, null, 1, 5);

        @SuppressWarnings("unchecked")
        List<Product> list = (List<Product>) result.get("list");

        List<String> suggests = list.stream()
                .map(Product::getTitle)
                .limit(3)
                .collect(Collectors.toList());

        return Map.of("code", 200, "msg", "获取成功", "data", suggests);
    }

    private String getAiAnswer(String msg) {
        if (msg.contains("付款") || msg.contains("支付")) {
            return "下单后请在15分钟内完成支付，支持支付宝、微信支付。";
        } else if (msg.contains("发货")) {
            return "校内当面交易，下单后可直接联系卖家确认时间地点。";
        } else if (msg.contains("取消")) {
            return "未支付的订单可以随时取消，已支付请联系卖家退款。";
        } else if (msg.contains("退款")) {
            return "未发货前可申请退款，商家同意后立即到账。";
        } else if (msg.contains("订单")) {
            return "您可以在个人中心-我的订单中查看所有订单状态。";
        } else if (msg.contains("商品") || msg.contains("发布")) {
            return "点击发布闲置按钮，填写商品信息即可发布商品。";
        } else if (msg.contains("校區") || msg.contains("校區")) {
            return "我们支持东校区、西校区、南校区、北校区等多个校区。";
        } else if (msg.contains("你好") || msg.contains("您好")) {
            return "您好！我是AI客服，请问有什么可以帮助您的？";
        } else if (msg.contains("谢谢")) {
            return "不客气！很高兴能帮助到您，祝您购物愉快！";
        } else {
            return "你好，我是AI客服，你可以问我：付款、发货、取消、退款、订单、商品发布等问题。";
        }
    }
}

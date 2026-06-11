package com.xyx.trade.product.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteVO {
    private Long id;          // 收藏ID
    private String userNickname; // 收藏用户昵称
    private String productTitle; // 收藏商品标题
    private String createTime;   // 收藏时间
}

package com.hixtrip.sample.client.order.dto.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 这是返回值的示例
 */
@Data
@Builder
public class OrderCreateVO {
    private String id;
    private String name;
    private String code;
    private String msg;
}

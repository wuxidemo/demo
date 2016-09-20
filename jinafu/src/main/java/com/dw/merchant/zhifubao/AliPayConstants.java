package com.dw.merchant.zhifubao;

/**
 * @Author: lvyongxu
 * @Date: 14:31 2015/12/22
 */
public class AliPayConstants {

    public static final String APP_ID = "2015110600707517";

    // 商户私钥，pkcs8格式
    public static final String RSA_PRIVATE = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAK5367p+RchoyzKBLXtzLAj/CoiQfgnEUUKRjomxvdwvZkL8TEfie6ZUY2WhVObYVELkup7l7PYUtAZRn8UkUfSDkyhuTL4LV/UDlggbOg68B/s6PnTd6Ldk7XnjY+UfyVnhpUUnHdhtXPIYuj83Hmndqj4xHXiyqC/hRYZBsE11AgMBAAECgYBAXXmX+dHg19hvL30KGDlcsErAix4UmFqqRmzhm7NBsjL174Js2r9nY6av7c8WwySC8UNwL782Ifkwg5h/8KBTCtQgvPYapF8PSyD1/cHzF6Tcb32XoZz7/uypnVNVt6kPraTGfXa0jDe+CRCGk09vmnranV/Mev8U/yJ9sIp62QJBANtYuQJYodJuZLC+qld9vZrrsGHD3FYoBKNMOq6gTm7RLX+Tihb9Rithds54AoifHCXtBJfeiupYAOp9Hu9SROMCQQDLn2MbU5koRV3Q8XxmKITIVw5Brq11+rRIMrjilws8JyDQOTOuN3Z29lqw9S4teFkciIxKIHqYpmnPgZpCzQvHAkB82LUFJtmEYp0hFIT0I3emE/xiyQ5CY6iwIZVNC6VY4eqZsKpqh2JHEsSCpEAc7yMgWxXAM0SyOcDbtrfC0/qtAkB50M2yoG2k+PKqOH3qg90EGYiu5LhjN2u5MZcH/8K55tKrnzz6wbV+b91LtjI9A52UA2CiTBHr1srAWFGYGyErAkEAhBw+z7JpGhS8fxLeoTxVrajqpeV31paHywb2fYTotT5eHPoH7qbG8YeGVcCv7YHygxjhY0P7WAcaCtpeIS9nww==";
    // 卖家支付宝用户ID,如果该值为空，则默认为商户签约账号对应的支付宝用户ID	2088102146225135 高龙；
    // 2088302038788861 吕永序；2088012896752633 陈翔宇
    public static final String SELLER_ID = "2088012896752633";//合作者身份（PID）
    public static final String NOTIFY_URL = "http://ts.do-wi.cn/nsh/appapi/alipaycallback/";
    // 支付场景,条码支付
    private String scene = "bar_code";

}

package com.bright.course.utils

import android.text.TextUtils

import java.util.regex.Pattern

/**
 * Created by Administrator on 2017/4/20.
 */

class RegularUtils private constructor() {

    init {
        throw UnsupportedOperationException("u can't fuck me...")
    }

    companion object {

        private val REGEX_PHONE = "^1|^1[0-9]{10}$"

        // 验证手机号（现在手机号码水太深，来个短小粗暴点了只能）
        private val REGEX_MOBILE = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(17([0-6]|[5-9]))|(18[0,5-9]))\\d{8}$"

        // 验证座机号,正确格式：xxx/xxxx-xxxxxxx/xxxxxxxx
        private val REGEX_TEL = "^0\\d{2,3}[- ]?\\d{7,8}"

        // 验证邮箱
        private val REGEX_EMAIL = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$"

        // 验证url
        private val REGEX_URL = "http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?"

        // 验证汉字
        private val REGEX_CHZ = "^[\\u4e00-\\u9fa5]+$"

        // 验证用户名,取值范围为a-z,A-Z,0-9,"_",汉字，不能以"_"结尾,用户名必须是6-20位
        private val REGEX_USERNAME = "^[\\w\\u4e00-\\u9fa5]{6,20}(?"

        // 验证IP地址
        private val REGEX_IP = "((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)"


        //If u want more please visit http://toutiao.com/i6231678548520731137/
        /**
         * 正则表达式:验证身份证
         */
        val REGEX_ID_CARD = "(^\\d{15}$)|(^\\d{17}([0-9]|X)$)"

        /**
         * 校验身份证
         *
         * @param idCard
         * @return 校验通过返回true，否则返回false
         */
        fun isIDCard(idCard: String): Boolean {
            return Pattern.matches(REGEX_ID_CARD, idCard)
        }

        /**
         * @param string 待验证文本
         * @return 是否符合手机号格式
         */
        fun isMobile(string: String): Boolean {
            return isMatch(REGEX_MOBILE, string)
        }

        fun isPhone(s: String): Boolean {
            return isMatch(REGEX_PHONE, s)
        }

        /**
         * @param string 待验证文本
         * @return 是否符合座机号码格式
         */
        fun isTel(string: String): Boolean {
            return isMatch(REGEX_TEL, string)
        }


        /**
         * @param string 待验证文本
         * @return 是否符合邮箱格式
         */
        fun isEmail(string: String): Boolean {
            return isMatch(REGEX_EMAIL, string)
        }


        /**
         * @param string 待验证文本
         * @return 是否符合网址格式
         */
        fun isURL(string: String): Boolean {
            return isMatch(REGEX_URL, string)
        }


        /**
         * @param string 待验证文本
         * @return 是否符合汉字
         */
        fun isChz(string: String): Boolean {
            return isMatch(REGEX_CHZ, string)
        }


        /**
         * @param string 待验证文本
         * @return 是否符合用户名
         */
        fun isUsername(string: String): Boolean {
            return isMatch(REGEX_USERNAME, string)
        }


        /**
         * @param regex  正则表达式字符串
         * @param string 要匹配的字符串
         * @return 如果str 符合 regex的正则表达式格式,返回true, 否则返回 false;
         */
        fun isMatch(regex: String, string: String): Boolean {
            return !TextUtils.isEmpty(string) && Pattern.matches(regex, string)
        }
    }
}

package com.example.depressive.util;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

@Component
public class IpLocationUtil {
    private static final Logger logger = LoggerFactory.getLogger(IpLocationUtil.class);
    private DatabaseReader reader;

    public IpLocationUtil() {
        try {
            File database = new ClassPathResource("GeoLite2-City.mmdb").getFile();
            if (!database.exists()) {
                logger.error("GeoLite2-City.mmdb 檔案不存在於 resources/ 目錄，IP 屬地功能將禁用");
                return;
            }
            this.reader = new DatabaseReader.Builder(database).build();
            logger.info("GeoIP 資料庫成功載入");
        } catch (IOException e) {
            logger.error("無法載入 GeoIP 資料庫: {}", e.getMessage());
        }
    }

    public String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // 取 X-Forwarded-For 中的第一個 IP（代理鏈的第一個）
            ip = ip.split(",")[0].trim();
        } else {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        logger.debug("獲取客戶端 IP: {}", ip);
        return ip;
    }

    public String getIpLocation(String ip) {
        System.out.println("ip:"+ip);
        if (reader == null) {
            logger.warn("GeoIP 資料庫未初始化，返回預設值");
            return "未知地區（GeoIP 資料庫未初始化）";
        }
        try {
            if (ip == null || ip.isEmpty() || "localhost".equals(ip) || "127.0.0.1".equals(ip)) {
                logger.debug("本地 IP: {}", ip);
                return "本地地址";
            }
            InetAddress ipAddress = InetAddress.getByName(ip);
            CityResponse response = reader.city(ipAddress);
            String country = response.getCountry().getNames().get("zh-CN");
            String region = response.getMostSpecificSubdivision().getNames().get("zh-CN");
            String city = response.getCity().getNames().get("zh-CN");
            String location = (country != null ? country : "未知國家") + " " +
                    (region != null ? region : "") + " " +
                    (city != null ? city : "");
            logger.debug("IP {} 查詢屬地: {}", ip, location);
            return location.trim();
        } catch (IOException | GeoIp2Exception e) {
            logger.error("IP 屬地查詢失敗，IP: {}, 錯誤: {}", ip, e.getMessage());
            return "未知地區（查詢失敗）";
        }
    }
}
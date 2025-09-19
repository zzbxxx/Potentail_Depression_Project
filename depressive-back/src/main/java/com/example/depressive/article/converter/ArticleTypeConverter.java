package com.example.depressive.article.converter;

import com.example.depressive.article.entity.ArticleType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ArticleTypeConverter implements AttributeConverter<ArticleType, String> {
    @Override
    public String convertToDatabaseColumn(ArticleType attribute) {
        if (attribute == null) return null;
        return attribute.name().toLowerCase(); // Convert to lowercase for DB
    }

    @Override
    public ArticleType convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return ArticleType.valueOf(dbData.toUpperCase()); // Convert to uppercase for enum
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("未知的文章类型: " + dbData);
        }
    }
}
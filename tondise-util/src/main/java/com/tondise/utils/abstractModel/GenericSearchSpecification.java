package com.tondise.utils.abstractModel;

import jakarta.persistence.ManyToOne;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GenericSearchSpecification<T> {

    public static <T> Specification<T> searchByKeywordAndAppId(
            UUID applicationProductId,
            String keyword,
            String appFieldName,   // ex: "applicationProduct"
            Class<T> entityClass
    ) {
        return (root, query, cb) -> {

            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // Filtre obligatoire : applicationProductId
            predicates.add(
                    cb.equal(root.get(appFieldName).get("id"), applicationProductId)
            );

            if (keyword != null && !keyword.trim().isEmpty()) {
                String like = "%" + keyword.toLowerCase() + "%";
                List<jakarta.persistence.criteria.Predicate> searchPredicates = new ArrayList<>();

                for (Field field : entityClass.getDeclaredFields()) {

                    // Ignore les relations complexes
                    if (field.isAnnotationPresent(ManyToOne.class)) continue;
                    if (field.getType().getName().contains("java.util.List")) continue;

                    String fieldName = field.getName();
                    Class<?> fieldType = field.getType();

                    // String
                    if (fieldType.equals(String.class)) {
                        searchPredicates.add(
                                cb.like(cb.lower(root.get(fieldName)), like)
                        );
                    }

                    // BigDecimal, Integer, Long → convertis en String
                    else if (fieldType.equals(BigDecimal.class) ||
                            fieldType.equals(Integer.class) ||
                            fieldType.equals(Long.class)) {

                        searchPredicates.add(
                                cb.like(root.get(fieldName).as(String.class), "%" + keyword + "%")
                        );
                    }

                    // Enum → converti en String
                    else if (fieldType.isEnum()) {
                        searchPredicates.add(
                                cb.like(
                                        cb.lower(root.get(fieldName).as(String.class)),
                                        like
                                )
                        );
                    }
                }

                predicates.add(
                        cb.or(searchPredicates.toArray(new jakarta.persistence.criteria.Predicate[0]))
                );
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}

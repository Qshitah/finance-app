package com.marouan.finance_app.service;

import com.marouan.finance_app.domain.Category;
import com.marouan.finance_app.domain.CategoryType;
import com.marouan.finance_app.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public Category create(UUID userId, String name, CategoryType type) {
        var category = new Category(userId, name, type);
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public List<Category> listForUser(UUID userId) {
        return categoryRepository.findByUserId(userId);
    }
}
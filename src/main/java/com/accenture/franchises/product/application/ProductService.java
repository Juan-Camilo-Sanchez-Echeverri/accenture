package com.accenture.franchises.product.application;

import com.accenture.franchises.common.exception.DuplicateNameException;
import com.accenture.franchises.common.exception.ResourceNotFoundException;
import com.accenture.franchises.common.pagination.PageQuery;
import com.accenture.franchises.common.pagination.PageResult;
import com.accenture.franchises.product.domain.Product;
import com.accenture.franchises.product.domain.ProductRepositoryPort;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private static final String TYPE = "Product";

    private final ProductRepositoryPort products;

    public ProductService(ProductRepositoryPort products) {
        this.products = products;
    }

    @Transactional
    public Product create(String name) {
        if (products.existsByNameIgnoreCase(name)) {
            throw new DuplicateNameException(TYPE, name);
        }
        return products.save(Product.create(name));
    }

    public PageResult<Product> findAll(PageQuery query) {
        return products.findAll(query);
    }

    public Product findById(UUID id) {
        return getExisting(id);
    }

    @Transactional
    public Product rename(UUID id, String name) {
        Product product = getExisting(id);

        if (!product.getName().equalsIgnoreCase(name) && products.existsByNameIgnoreCase(name)) {
            throw new DuplicateNameException(TYPE, name);
        }

        return products.save(product.rename(name));
    }

    @Transactional
    public void delete(UUID id) {
        getExisting(id);
        products.delete(id);
    }

    private Product getExisting(UUID id) {
        return products.findById(id).orElseThrow(() -> new ResourceNotFoundException(TYPE, id));
    }
}
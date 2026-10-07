package com.accenture.franchises.product.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accenture.franchises.common.exception.DuplicateNameException;
import com.accenture.franchises.common.exception.ResourceNotFoundException;
import com.accenture.franchises.common.pagination.PageQuery;
import com.accenture.franchises.common.pagination.PageResult;
import com.accenture.franchises.product.domain.Product;
import com.accenture.franchises.product.domain.ProductRepositoryPort;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepositoryPort products;

    @InjectMocks
    private ProductService service;

    private final UUID id = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-01-01T10:00:00Z");

    @Test
    void createSavesTheProduct() {
        when(products.existsByNameIgnoreCase("Hamburguesa")).thenReturn(false);
        when(products.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Product created = service.create("Hamburguesa");

        assertThat(created.getName()).isEqualTo("Hamburguesa");
        assertThat(created.getId()).isNull();
    }

    @Test
    void createRejectsDuplicatedNameIgnoringCase() {
        when(products.existsByNameIgnoreCase("hamburguesa")).thenReturn(true);

        assertThatThrownBy(() -> service.create("hamburguesa")).isInstanceOf(DuplicateNameException.class);

        verify(products, never()).save(any());
    }

    @Test
    void findAllDelegatesTheQueryToThePort() {
        PageQuery query = PageQuery.of(1, 10);
        PageResult<Product> expected = PageResult.of(List.of(), 1, 10, 0);
        when(products.findAll(query)).thenReturn(expected);

        assertThat(service.findAll(query)).isSameAs(expected);
    }

    @Test
    void findByIdReturnsTheProduct() {
        Product product = Product.restore(id, "Hamburguesa", now, now);
        when(products.findById(id)).thenReturn(Optional.of(product));

        assertThat(service.findById(id)).isSameAs(product);
    }

    @Test
    void findByIdFailsWhenMissing() {
        when(products.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void renameReturnsTheUpdatedProduct() {
        Product product = Product.restore(id, "Hamburguesa", now, now);
        when(products.findById(id)).thenReturn(Optional.of(product));
        when(products.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Product renamed = service.rename(id, "Hamburguesa XL");

        assertThat(renamed.getName()).isEqualTo("Hamburguesa XL");
        assertThat(renamed.getId()).isEqualTo(id);
        assertThat(renamed.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void renameKeepsTheSameNameWhenOnlyCaseChanges() {
        when(products.findById(id)).thenReturn(Optional.of(Product.restore(id, "Hamburguesa", now, now)));
        when(products.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Product renamed = service.rename(id, "HAMBURGUESA");

        assertThat(renamed.getName()).isEqualTo("HAMBURGUESA");
        verify(products, never()).existsByNameIgnoreCase(any());
    }

    @Test
    void renameRejectsNameTakenByAnotherProduct() {
        when(products.findById(id)).thenReturn(Optional.of(Product.restore(id, "Hamburguesa", now, now)));
        when(products.existsByNameIgnoreCase("Pizza")).thenReturn(true);

        assertThatThrownBy(() -> service.rename(id, "Pizza"))
                .isInstanceOf(DuplicateNameException.class);

        verify(products, never()).save(any());
    }

    @Test
    void deleteRemovesTheProduct() {
        when(products.findById(id)).thenReturn(Optional.of(Product.restore(id, "Hamburguesa", now, now)));

        service.delete(id);

        verify(products).delete(id);
    }

    @Test
    void deleteFailsWhenMissing() {
        when(products.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id)).isInstanceOf(ResourceNotFoundException.class);

        verify(products, never()).delete(any());
    }
}
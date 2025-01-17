package com.codesoom.assignment.controller;

import com.codesoom.assignment.application.ProductService;
import com.codesoom.assignment.domain.Product;
import com.codesoom.assignment.dto.ProductDto;
import com.codesoom.assignment.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * 단순 의존 관계를 테스트하기 위한 mock 테스트입니다.
 */
class ProductControllerTest {

    ProductService service;
    ProductController controller;
    ModelMapper modelMapper = new ModelMapper();

    private final long ID = 1L;
    private final long NOT_FOUND_ID = 100000L;
    private final String MAKER = "KOREAN SHORT CAT";
    private final int PRICE = 20000;
    private final String NAME = "CAT FISHING ROD";
    private final String IMAGE = "https://www.zoostore.de/media/image/product/4598/sm/katzenspielzeug-katzenangel-spielangel-zum-zusammenschrauben-mit-heuschrecke~2.jpg";
    Product PRODUCT;

    @BeforeEach
    void setUp() {
        service = mock(ProductService.class);
        controller = new ProductController(service);

        List<Product> products = new ArrayList<>();

        PRODUCT = new Product();
        PRODUCT.setId(ID);
        PRODUCT.setMaker(MAKER);
        PRODUCT.setPrice(PRICE);
        PRODUCT.setName(NAME);
        PRODUCT.setImageUrl(IMAGE);
        products.add(PRODUCT);

        given(service.getProducts()).willReturn(products);
        given(service.getProduct(ID)).willReturn(PRODUCT);
        given(service.getProduct(NOT_FOUND_ID)).willThrow(ProductNotFoundException.class);
        given(service.createProduct(any(ProductDto.class))).will(invocation -> {
            ProductDto productDto = invocation.getArgument(0);
            Product product = modelMapper.map(productDto, Product.class);
            product.setId(ID);
            return product;
        });
        given(service.updateProduct(eq(ID), any(ProductDto.class))).will(invocation -> {
            ProductDto productDto = invocation.getArgument(1);

            Product product = new Product();
            product.setId(ID);
            product.setMaker(productDto.getMaker());
            product.setPrice(productDto.getPrice());
            product.setName(productDto.getName());
            product.setImageUrl(productDto.getImageUrl());

            return product;
        });
    }

    @Test
    void getProducts() {
        List<Product> products = controller.list();
        verify(service).getProducts();
        assertThat(products).isNotEmpty();
    }

    @Test
    void getEmptyProducts() {
        given(service.getProducts()).willReturn(new ArrayList<>());

        List<Product> products = controller.list();
        verify(service).getProducts();
        assertThat(products).isEmpty();
    }

    @Test
    void getProduct() {
        Product product = controller.detail(ID);
        verify(service).getProduct(ID);
        verifyProduct(product);
    }

    @Test
    void getNotFoundProduct() {
        assertThatThrownBy(() -> {
            controller.detail(NOT_FOUND_ID);
        }).isInstanceOf(ProductNotFoundException.class);

        verify(service).getProduct(NOT_FOUND_ID);
    }

    @Test
    void createProduct() {
        ProductDto newProduct = new ProductDto();
        newProduct.setImageUrl(IMAGE);
        newProduct.setPrice(PRICE);
        newProduct.setMaker(MAKER);
        newProduct.setName(NAME);

        Product product = controller.create(newProduct);
        verify(service).createProduct(any(ProductDto.class));
        verifyProduct(product);
    }

    @Test
    void updateProduct() {
        ProductDto productDto = new ProductDto();
        productDto.setName("updated" + NAME);
        productDto.setImageUrl("updated" + IMAGE);
        productDto.setMaker("updated" + MAKER);
        productDto.setPrice(1000 + PRICE);

        Product updateProduct = controller.update(ID, productDto);

        verify(service).updateProduct(ID, productDto);
        verifyUpdateProduct(updateProduct);
    }

    @Test
    void removeProduct() {
        controller.remove(ID);

        verify(service).removeProduct(any(Product.class));
    }

    private void verifyProduct(Product product) {
        assertThat(product.getId()).isEqualTo(ID);
        assertThat(product.getMaker()).isEqualTo(MAKER);
        assertThat(product.getPrice()).isEqualTo(PRICE);
        assertThat(product.getImageUrl()).isEqualTo(IMAGE);
        assertThat(product.getName()).isEqualTo(NAME);
    }

    private void verifyUpdateProduct(Product product) {
        assertThat(product.getId()).isEqualTo(ID);
        assertThat(product.getMaker()).isEqualTo("updated" + MAKER);
        assertThat(product.getPrice()).isEqualTo(1000 + PRICE);
        assertThat(product.getImageUrl()).isEqualTo("updated" + IMAGE);
        assertThat(product.getName()).isEqualTo("updated" + NAME);
    }
}
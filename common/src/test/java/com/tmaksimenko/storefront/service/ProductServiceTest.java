package com.tmaksimenko.storefront.service;

import com.tmaksimenko.storefront.dto.order.CartDto;
import com.tmaksimenko.storefront.dto.order.CartItemDto;
import com.tmaksimenko.storefront.dto.payment.PaymentCreateDto;
import com.tmaksimenko.storefront.dto.product.ProductCreateDto;
import com.tmaksimenko.storefront.enums.Role;
import com.tmaksimenko.storefront.enums.payment.PaymentProvider;
import com.tmaksimenko.storefront.enums.payment.PaymentStatus;
import com.tmaksimenko.storefront.model.Product;
import com.tmaksimenko.storefront.model.account.Account;
import com.tmaksimenko.storefront.model.account.Address;
import com.tmaksimenko.storefront.model.account.Cart;
import com.tmaksimenko.storefront.model.base.Audit;
import com.tmaksimenko.storefront.model.discount.GeneralDiscount;
import com.tmaksimenko.storefront.model.discount.ProductDiscount;
import com.tmaksimenko.storefront.model.payment.ExpiryDate;
import com.tmaksimenko.storefront.model.payment.PaymentInfo;
import com.tmaksimenko.storefront.repository.ProductRepository;
import com.tmaksimenko.storefront.service.account.AccountService;
import com.tmaksimenko.storefront.service.discount.DiscountService;
import com.tmaksimenko.storefront.service.product.ProductService;
import com.tmaksimenko.storefront.service.product.ProductServiceImplementation;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Mock
    ProductRepository productRepository;

    @Mock
    DiscountService discountService;

    @Mock
    AccountService accountService;

    ProductService productService;

    ProductService spyProductService;

    Product product;

    ProductCreateDto productCreateDto;

    Account account;

    GeneralDiscount generalDiscount;

    ProductDiscount productDiscount;

    CartDto cartDto;

    @BeforeEach
    public void setup () {
        productService = new ProductServiceImplementation(productRepository, discountService, accountService);
        spyProductService = Mockito.spy(productService);
        product = Product.builder()
                .id(1000L)
                .name("testName")
                .brand("testBrand")
                .price(10.0)
                .weight(1.0).build();
        productCreateDto = new ProductCreateDto(
                product.getName(), product.getBrand(), product.getPrice(), product.getWeight());
        account = Account.builder()
                .id(1L)
                .audit(new Audit("Test"))
                .username("testUser")
                .email("testEmail@mail.com")
                .password(passwordEncoder.encode("testPassword"))
                .role(Role.ROLE_USER)
                .address(Address.builder()
                        .streetAddress("1 Street St")
                        .country("Canada")
                        .postalCode("M1M1M1")
                        .build())
                .build();
        generalDiscount = GeneralDiscount.builder()
                .id(1L)
                .audit(new Audit("Test"))
                .percent(10.0)
                .role(Role.ROLE_STAFF)
                .build();
        productDiscount = ProductDiscount.builder()
                .id(2L)
                .audit(new Audit("Test"))
                .percent(15.0)
                .product(product)
                .build();
        cartDto = CartDto.builder()
                        .paymentCreateDto(PaymentCreateDto.builder()
                                .paymentProvider(PaymentProvider.VISA)
                                .paymentInfo(PaymentInfo.builder()
                                        .cardNumber(1111111111111111L)
                                        .expiry(new ExpiryDate(11, 23))
                                        .securityCode(111)
                                        .postalCode(account.getAddress().getPostalCode())
                                        .build())
                                .build())
                        .cartItemDtos(List.of(new CartItemDto(product.getId())))
                        .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(account.getUsername(), account.getPassword()));
    }

    @Test
    @DisplayName("Successful findAll")
    public void test_successful_findAll () {
        //given
        Product product1 = product.toBuilder()
                .id(1001L)
                .name("testName1")
                .brand("testBrand1")
                .price(15.0)
                .weight(1.5)
                .build();
        given(productRepository.findAll()).willReturn(List.of(product, product1));

        // when
        List<Product> products = productService.findAll();

        // then
        assertThat(products).hasSize(2).contains(product).contains(product1);
    }

    @Test
    @DisplayName("Failed findAll")
    public void test_failed_findAll () {
        //given
        given(productRepository.findAll()).willReturn(List.of());

        // when
        List<Product> products = productService.findAll();

        // then
        assertThat(products).isEmpty();
    }

    @Test
    @DisplayName("Successful findById")
    public void test_successful_findById () {
        //given
        given(productRepository.findById(product.getId())).willReturn(Optional.of(product));

        // when
        Optional<Product> product1 = productService.findById(product.getId());

        // then
        assertThat(product1).isPresent().get().isSameAs(product);
    }

    @Test
    @DisplayName("Failed findById")
    public void test_failed_findById () {
        //given
        given(productRepository.findById(product.getId())).willReturn(Optional.empty());

        // when
        Optional<Product> product1 = productService.findById(product.getId());

        // then
        assertThat(product1).isEmpty();
    }


    @Test
    @DisplayName("Successful real createCart - no discount")
    public void test_realCreateCart_noDiscount () {
        //given
        given(accountService.findByUsername(account.getUsername())).willReturn(Optional.of(account));
        given(discountService.findByRole(account.getRole())).willReturn(List.of());
        given(discountService.findByProductId(product.getId())).willReturn(Optional.empty());
        given(spyProductService.findById(product.getId())).willReturn(Optional.of(product));

        // when
        Cart cart = spyProductService.createCart(cartDto, account.getUsername());

        // then
        Map<Long, Integer> itemMap = cartDto.getCartItemDtos().stream().collect(
                Collectors.toMap(CartItemDto::getProductId, CartItemDto::getQuantity));
        assertThat(cart.getPrice()).isEqualTo(product.getPrice() + (product.getWeight() * 0.1));
        assertThat(cart.getPayment()).isEqualTo(cartDto.getPaymentCreateDto().toPayment(PaymentStatus.NOT_PAID));
        assertThat(cart.getItems()).isEqualTo(itemMap);
    }

    @Test
    @DisplayName("Successful real createCart - general discount")
    public void test_realCreateCart_generalDiscount () {
        //given
        given(accountService.findByUsername(account.getUsername())).willReturn(Optional.of(account));
        given(discountService.findByRole(account.getRole())).willReturn(List.of(generalDiscount));
        given(discountService.findByProductId(product.getId())).willReturn(Optional.empty());
        given(spyProductService.findById(product.getId())).willReturn(Optional.of(product));

        // when
        Cart cart = spyProductService.createCart(cartDto, account.getUsername());

        // then
        Map<Long, Integer> itemMap = cartDto.getCartItemDtos().stream().collect(
                Collectors.toMap(CartItemDto::getProductId, CartItemDto::getQuantity));
        assertThat(cart.getPrice()).isEqualTo((product.getPrice() + (product.getWeight() * 0.1))
                * ((100.0 - generalDiscount.getPercent())/100.0));
        assertThat(cart.getPayment()).isEqualTo(cartDto.getPaymentCreateDto().toPayment(PaymentStatus.NOT_PAID));
        assertThat(cart.getItems()).isEqualTo(itemMap);
    }

    @Test
    @DisplayName("Successful real createCart - multiple general discounts")
    public void test_realCreateCart_multipleGeneralDiscounts () {
        //given
        GeneralDiscount generalDiscount1 = generalDiscount.toBuilder().percent(12.0).build();

        given(accountService.findByUsername(account.getUsername())).willReturn(Optional.of(account));
        given(discountService.findByRole(account.getRole())).willReturn(List.of(generalDiscount1, generalDiscount));
        given(discountService.findByProductId(product.getId())).willReturn(Optional.empty());
        given(spyProductService.findById(product.getId())).willReturn(Optional.of(product));

        // when
        Cart cart = spyProductService.createCart(cartDto, account.getUsername());

        // then
        Map<Long, Integer> itemMap = cartDto.getCartItemDtos().stream().collect(
                Collectors.toMap(CartItemDto::getProductId, CartItemDto::getQuantity));
        assertThat(cart.getPrice()).isEqualTo((product.getPrice() + (product.getWeight() * 0.1))
                * ((100.0 - generalDiscount1.getPercent())/100.0));
        assertThat(cart.getPayment()).isEqualTo(cartDto.getPaymentCreateDto().toPayment(PaymentStatus.NOT_PAID));
        assertThat(cart.getItems()).isEqualTo(itemMap);
    }

    @Test
    @DisplayName("Successful real createCart - product discount")
    public void test_realCreateCart_productDiscount () {
        //given
        given(accountService.findByUsername(account.getUsername())).willReturn(Optional.of(account));
        given(discountService.findByRole(account.getRole())).willReturn(List.of());
        given(discountService.findByProductId(product.getId())).willReturn(Optional.of(productDiscount));
        given(spyProductService.findById(product.getId())).willReturn(Optional.of(product));

        // when
        Cart cart = spyProductService.createCart(cartDto, account.getUsername());

        // then
        Map<Long, Integer> itemMap = cartDto.getCartItemDtos().stream().collect(
                Collectors.toMap(CartItemDto::getProductId, CartItemDto::getQuantity));
        assertThat(cart.getPrice()).isEqualTo((product.getPrice() * ((100.0 - productDiscount.getPercent())/100.0)
                + (product.getWeight() * 0.1)));
        assertThat(cart.getPayment()).isEqualTo(cartDto.getPaymentCreateDto().toPayment(PaymentStatus.NOT_PAID));
        assertThat(cart.getItems()).isEqualTo(itemMap);
    }

    @Test
    @DisplayName("Successful real createCart - both discounts")
    public void test_realCreateCart_bothDiscounts () {
        //given
        given(accountService.findByUsername(account.getUsername())).willReturn(Optional.of(account));
        given(discountService.findByRole(account.getRole())).willReturn(List.of(generalDiscount));
        given(discountService.findByProductId(product.getId())).willReturn(Optional.of(productDiscount));
        given(spyProductService.findById(product.getId())).willReturn(Optional.of(product));

        // when
        Cart cart = spyProductService.createCart(cartDto, account.getUsername());

        // then
        Map<Long, Integer> itemMap = cartDto.getCartItemDtos().stream().collect(
                Collectors.toMap(CartItemDto::getProductId, CartItemDto::getQuantity));
        assertThat(cart.getPrice()).isEqualTo((product.getPrice() * ((100.0 - productDiscount.getPercent())/100.0)
                + (product.getWeight() * 0.1)) * ((100.0 - generalDiscount.getPercent())/100.0));
        assertThat(cart.getPayment()).isEqualTo(cartDto.getPaymentCreateDto().toPayment(PaymentStatus.NOT_PAID));
        assertThat(cart.getItems()).isEqualTo(itemMap);
    }


    @Test
    @DisplayName("Successful redirect createCart")
    public void test_redirectCreateCart () {
        //given
        Map<Long, Integer> itemMap = cartDto.getCartItemDtos().stream().collect(
                Collectors.toMap(CartItemDto::getProductId, CartItemDto::getQuantity));
        Cart cart = Cart.builder()
                        .price(product.getPrice() + (product.getWeight() * 0.1))
                        .payment(cartDto.getPaymentCreateDto().toPayment(PaymentStatus.NOT_PAID))
                        .items(itemMap)
                        .build();

        doReturn(cart).when(spyProductService).createCart(cartDto, account.getUsername());

        // when
        Cart cart1 = spyProductService.createCart(cartDto);

        // then
        assertThat(cart).isSameAs(cart1);
    }

    @Test
    @DisplayName("Successful createProduct")
    public void test_createProduct () {
        //given
        given(productRepository.save(Mockito.any(Product.class))).willAnswer(i -> i.getArgument(0, Product.class));

        // when
        Product product1 = productService.createProduct(productCreateDto);
        product1.setId(product.getId()); // in order to compare to product

        // then
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(product1).isSameAs(captor.getValue()).isEqualTo(product);
    }

    @Test
    @DisplayName("Successful updateProduct")
    public void test_successful_updateProduct () {
        //given
        ProductCreateDto productCreateDto1 =
                productCreateDto.toBuilder()
                .name(productCreateDto.getName() + "1")
                .brand(productCreateDto.getBrand() + "1")
                .price(productCreateDto.getPrice() + 1.0)
                .weight(productCreateDto.getWeight() + 1.0)
                .build();
        given(productRepository.findById(product.getId())).willReturn(Optional.of(product.toBuilder().build()));

        // when
        Product product1 = productService.updateProduct(product.getId(), productCreateDto1);

        // then
        assertThat(product1).isNotEqualTo(product);
    }

    @Test
    @DisplayName("Successful updateProduct - none changed")
    public void test_successful_updateProduct_noneChanged () {
        //given
        ProductCreateDto productCreateDto1 = ProductCreateDto.builder().build();

        given(productRepository.findById(product.getId())).willReturn(Optional.of(product.toBuilder().build()));

        // when
        Product product1 = productService.updateProduct(product.getId(), productCreateDto1);

        // then
        assertThat(product1).isEqualTo(product);
    }

    @Test
    @DisplayName("Failed updateProduct")
    public void test_failed_updateProduct () {
        //given
        given(productRepository.findById(Mockito.anyLong())).willReturn(Optional.empty());

        // when
        Exception exception = assertThrows(ResponseStatusException.class, () ->
                productService.updateProduct(1001L, productCreateDto));

        // then
        assertThat(exception).hasMessageContaining("PRODUCT NOT FOUND");
    }

    @Test
    @DisplayName("Successful deleteProduct")
    public void test_successful_deleteProduct () {
        //given
        given(productRepository.findById(product.getId())).willReturn(Optional.of(product));
        doNothing().when(productRepository).delete(product);

        // when
        Product product1 = productService.deleteProduct(product.getId());

        // then
        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).delete(captor.capture());
        assertThat(product1).isSameAs(captor.getValue()).isSameAs(product);
    }

    @Test
    @DisplayName("Failed deleteProduct")
    public void test_failed_deleteProduct () {
        // given
        given(productRepository.findById(Mockito.anyLong())).willReturn(Optional.empty());

        // when
        Exception exception = assertThrows(ResponseStatusException.class, () ->
                productService.deleteProduct(1001L));

        // then
        assertThat(exception).hasMessageContaining("PRODUCT NOT FOUND");
    }

}

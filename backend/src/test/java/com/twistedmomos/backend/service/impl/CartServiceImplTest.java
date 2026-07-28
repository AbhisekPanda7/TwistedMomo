package com.twistedmomos.backend.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.twistedmomos.backend.dto.request.AddCartItemRequest;
import com.twistedmomos.backend.dto.request.UpdateCartItemRequest;
import com.twistedmomos.backend.dto.response.CartResponse;
import com.twistedmomos.backend.entity.Cart;
import com.twistedmomos.backend.entity.CartItem;
import com.twistedmomos.backend.entity.Category;
import com.twistedmomos.backend.entity.MenuItem;
import com.twistedmomos.backend.entity.Role;
import com.twistedmomos.backend.entity.RoleName;
import com.twistedmomos.backend.entity.User;
import com.twistedmomos.backend.exception.ItemUnavailableException;
import com.twistedmomos.backend.exception.ResourceNotFoundException;
import com.twistedmomos.backend.repository.CartItemRepository;
import com.twistedmomos.backend.repository.CartRepository;
import com.twistedmomos.backend.repository.MenuItemRepository;
import com.twistedmomos.backend.repository.UserRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private MenuItemRepository menuItemRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private User user;
    private MenuItem menuItem;
    private Cart cart;

    @BeforeEach
    void setUp() {
        Role role = Role.builder().id(1L).name(RoleName.CUSTOMER).build();
        user = User.builder().id(1L).name("Cart Tester").email("cart.tester@example.com").role(role).build();

        Category category = Category.builder().id(1L).name("Steam").slug("steam").displayOrder(0).active(true).build();
        menuItem = MenuItem.builder()
                .id(5L).category(category).name("Veg Steam Momo").slug("veg-steam-momo")
                .price(new BigDecimal("80.00")).veg(true).available(true).displayOrder(0)
                .build();

        cart = Cart.builder().id(1L).user(user).items(new ArrayList<>()).build();
        lenient().when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));
    }

    @Test
    void addItem_onAFreshItem_addsItAtTheRequestedQuantity() {
        when(menuItemRepository.findById(5L)).thenReturn(Optional.of(menuItem));
        when(cartItemRepository.findByCartIdAndMenuItemId(1L, 5L)).thenReturn(Optional.empty());

        CartResponse response = cartService.addItem(1L, new AddCartItemRequest(5L, 3));

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).quantity()).isEqualTo(3);
        assertThat(response.subtotal()).isEqualByComparingTo("240.00");
    }

    @Test
    void addItem_onAnItemAlreadyInTheCart_mergesQuantitiesCappedAtTwenty() {
        CartItem existing = CartItem.builder().id(10L).cart(cart).menuItem(menuItem).quantity(15).build();
        cart.getItems().add(existing);
        when(menuItemRepository.findById(5L)).thenReturn(Optional.of(menuItem));
        when(cartItemRepository.findByCartIdAndMenuItemId(1L, 5L)).thenReturn(Optional.of(existing));

        CartResponse response = cartService.addItem(1L, new AddCartItemRequest(5L, 10));

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).quantity()).isEqualTo(20);
    }

    @Test
    void addItem_whenMenuItemIsUnavailable_rejectsWithConflict() {
        menuItem.setAvailable(false);
        when(menuItemRepository.findById(5L)).thenReturn(Optional.of(menuItem));

        assertThatThrownBy(() -> cartService.addItem(1L, new AddCartItemRequest(5L, 1)))
                .isInstanceOf(ItemUnavailableException.class);
    }

    @Test
    void addItem_whenMenuItemDoesNotExist_rejectsWithNotFound() {
        when(menuItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem(1L, new AddCartItemRequest(999L, 1)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void removeItem_removesTheLineFromTheCartsOwnCollection() {
        CartItem item = CartItem.builder().id(10L).cart(cart).menuItem(menuItem).quantity(2).build();
        cart.getItems().add(item);

        CartResponse response = cartService.removeItem(1L, 10L);

        // Regression guard: removal must go through cart.getItems() (relying on
        // orphanRemoval), not a repository.delete() call that Hibernate would silently
        // undo via the still-attached cascade=ALL collection — see removeItem's own
        // comment for the incident this covers.
        assertThat(cart.getItems()).isEmpty();
        assertThat(response.items()).isEmpty();
    }

    @Test
    void removeItem_whenTheItemIsNotInTheCart_rejectsWithNotFound() {
        assertThatThrownBy(() -> cartService.removeItem(1L, 404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateItem_changesTheStoredQuantity() {
        CartItem item = CartItem.builder().id(10L).cart(cart).menuItem(menuItem).quantity(2).build();
        cart.getItems().add(item);
        when(cartItemRepository.findByIdAndCartId(10L, 1L)).thenReturn(Optional.of(item));

        CartResponse response = cartService.updateItem(1L, 10L, new UpdateCartItemRequest(7));

        assertThat(response.items().get(0).quantity()).isEqualTo(7);
    }

    @Test
    void clearCart_emptiesTheCart() {
        cart.getItems().add(CartItem.builder().id(10L).cart(cart).menuItem(menuItem).quantity(2).build());

        CartResponse response = cartService.clearCart(1L);

        assertThat(cart.getItems()).isEmpty();
        assertThat(response.items()).isEmpty();
        assertThat(response.totalItems()).isZero();
        assertThat(response.subtotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}

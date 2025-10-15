package com.backend.jibli.cart;

import com.backend.jibli.product.IProductRepository;
import com.backend.jibli.product.Product;
import com.backend.jibli.user.IUserRepository;
import com.backend.jibli.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService implements ICartService {

    private final ICartRepository cartRepository;
    private final IUserRepository userRepository;
    private final IProductRepository productRepository;

    @Autowired
    public CartService(ICartRepository cartRepository,
                       IUserRepository userRepository,
                       IProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<CartDTO> getAllCarts() {
        return cartRepository.findAll().stream()
                .map(this::mapCartToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CartDTO> getCartById(Integer id) {
        return cartRepository.findById(id)
                .map(this::mapCartToDTO);
    }

    @Override
    public CartDTO createCart(CartDTO dto) {
        if (dto.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        if (dto.getCartItems() != null) {
            for (CartItemDTO item : dto.getCartItems()) {
                if (item.getProductId() == null) {
                    throw new IllegalArgumentException("Product ID is required for cart item");
                }
                if (!productRepository.existsById(item.getProductId())) {
                    throw new IllegalArgumentException("Product not found: " + item.getProductId());
                }
                if (item.getQuantity() == null || item.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Quantity must be greater than 0");
                }
            }
        }

        Cart cart = mapCartDTOToEntity(dto);
        Cart saved = cartRepository.save(cart);
        return mapCartToDTO(saved);
    }

    @Override
    public Optional<CartDTO> updateCart(Integer id, CartDTO dto) {
        if (dto.getUserId() != null && !userRepository.existsById(dto.getUserId())) {
            throw new IllegalArgumentException("User not found");
        }

        if (dto.getCartItems() != null) {
            for (CartItemDTO item : dto.getCartItems()) {
                if (item.getProductId() != null && !productRepository.existsById(item.getProductId())) {
                    throw new IllegalArgumentException("Product not found: " + item.getProductId());
                }
                if (item.getQuantity() != null && item.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Quantity must be greater than 0");
                }
            }
        }

        return cartRepository.findById(id)
                .map(cart -> {
                    if (dto.getUserId() != null) {
                        User user = new User();
                        user.setUserId(dto.getUserId());
                        cart.setUser(user);
                    }

                    if (dto.getCartItems() != null) {
                        cart.getCartItems().clear();
                        List<CartItem> cartItems = dto.getCartItems().stream()
                                .map(item -> {
                                    CartItem cartItem = new CartItem();
                                    cartItem.setCart(cart);
                                    Product product = new Product();
                                    product.setProductId(item.getProductId());
                                    cartItem.setProduct(product);
                                    cartItem.setQuantity(item.getQuantity());
                                    return cartItem;
                                })
                                .collect(Collectors.toList());
                        cart.setCartItems(cartItems);
                    }

                    Cart updated = cartRepository.save(cart);
                    return mapCartToDTO(updated);
                });
    }

    @Override
    public boolean deleteCart(Integer id) {
        if (cartRepository.existsById(id)) {
            cartRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public Optional<CartDTO> findByUserUserId(Integer userId) {
        // If repository returns Optional<Cart>, map it to CartDTO
        return cartRepository.findByUserUserId(userId)
                .map(this::mapCartToDTO);
    }

    // ---------------- Mapping methods ----------------

    private CartDTO mapCartToDTO(Cart cart) {
        List<CartItemDTO> cartItemDTOs = cart.getCartItems() != null
                ? cart.getCartItems().stream()
                .map(this::mapCartItemToDTO)
                .collect(Collectors.toList())
                : List.of();

        return new CartDTO(
                cart.getCartId(),
                cart.getUser() != null ? cart.getUser().getUserId() : null,
                cart.getCreatedAt(),
                cart.getLastUpdated(),
                cartItemDTOs,
                cart.getTotalPrice()
        );
    }

    private Cart mapCartDTOToEntity(CartDTO dto) {
        Cart cart = new Cart();

        if (dto.getUserId() != null) {
            User user = new User();
            user.setUserId(dto.getUserId());
            cart.setUser(user);
        }

        if (dto.getCartItems() != null) {
            List<CartItem> cartItems = dto.getCartItems().stream()
                    .map(item -> {
                        CartItem cartItem = new CartItem();
                        cartItem.setCart(cart);
                        Product product = new Product();
                        product.setProductId(item.getProductId());
                        cartItem.setProduct(product);
                        cartItem.setQuantity(item.getQuantity());
                        return cartItem;
                    })
                    .collect(Collectors.toList());
            cart.setCartItems(cartItems);
        }

        return cart;
    }

    private CartItemDTO mapCartItemToDTO(CartItem cartItem) {
        CartItemDTO dto = new CartItemDTO();
        dto.setCartItemId(cartItem.getCartItemId());
        dto.setCartId(cartItem.getCart() != null ? cartItem.getCart().getCartId() : null);
        dto.setProductId(cartItem.getProduct() != null ? cartItem.getProduct().getProductId() : null);
        dto.setQuantity(cartItem.getQuantity());
        dto.setProduct(cartItem.getProduct());
        return dto;
    }
}
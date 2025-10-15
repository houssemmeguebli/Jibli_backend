
package com.backend.jibli.cart;

import com.backend.jibli.product.IProductRepository;
import com.backend.jibli.product.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartItemService implements ICartItemService {

    private final ICartItemRepository cartItemRepository;
    private final ICartRepository cartRepository;
    private final IProductRepository productRepository;

    @Autowired
    public CartItemService(
            ICartItemRepository cartItemRepository,
            ICartRepository cartRepository,
            IProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<CartItemDTO> getAllCartItems() {
        return cartItemRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CartItemDTO> getCartItemsByCartId(Integer cartId) {
        return cartItemRepository.findByCartCartId(cartId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CartItemDTO> getCartItemById(Integer id) {
        return cartItemRepository.findById(id)
                .map(this::mapToDTO);
    }

    @Override
    public CartItemDTO createCartItem(CartItemDTO dto) {
        // Validate required fields
        if (dto.getCartId() == null) {
            throw new IllegalArgumentException("Cart ID is required");
        }
        if (dto.getProductId() == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        // Validate cart exists
        Cart cart = cartRepository.findById(dto.getCartId())
                .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + dto.getCartId()));

        // Validate product exists
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + dto.getProductId()));

        // Check if product already exists in cart
        Optional<CartItem> existingCartItem = cartItemRepository
                .findByCartCartIdAndProductProductId(dto.getCartId(), dto.getProductId());

        if (existingCartItem.isPresent()) {
            // Update quantity if product already in cart
            CartItem cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + dto.getQuantity());
            CartItem updated = cartItemRepository.save(cartItem);
            return mapToDTO(updated);
        } else {
            // Create new cart item
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(dto.getQuantity());
            CartItem saved = cartItemRepository.save(cartItem);
            return mapToDTO(saved);
        }
    }

    @Override
    public Optional<CartItemDTO> updateCartItem(Integer id, CartItemDTO dto) {
        return cartItemRepository.findById(id)
                .map(cartItem -> {
                    // Update cart if provided
                    if (dto.getCartId() != null) {
                        Cart cart = cartRepository.findById(dto.getCartId())
                                .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + dto.getCartId()));
                        cartItem.setCart(cart);
                    }

                    // Update product if provided
                    if (dto.getProductId() != null) {
                        Product product = productRepository.findById(dto.getProductId())
                                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + dto.getProductId()));
                        cartItem.setProduct(product);
                    }

                    // Update quantity if provided
                    if (dto.getQuantity() != null) {
                        if (dto.getQuantity() <= 0) {
                            throw new IllegalArgumentException("Quantity must be greater than 0");
                        }
                        cartItem.setQuantity(dto.getQuantity());
                    }

                    CartItem updated = cartItemRepository.save(cartItem);
                    return mapToDTO(updated);
                });
    }

    @Override
    public boolean deleteCartItem(Integer id) {
        if (cartItemRepository.existsById(id)) {
            cartItemRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public void deleteCartItemsByCartId(Integer cartId) {
        cartItemRepository.deleteByCartCartId(cartId);
    }

    private CartItemDTO mapToDTO(CartItem cartItem) {
        return new CartItemDTO(
                cartItem.getCartItemId(),
                cartItem.getCart() != null ? cartItem.getCart().getCartId() : null,
                cartItem.getProduct() != null ? cartItem.getProduct().getProductId() : null,
                cartItem.getQuantity(),
                cartItem.getProduct()
        );
    }
}
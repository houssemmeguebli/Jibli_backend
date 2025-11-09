
package com.backend.jibli.cart;

import com.backend.jibli.product.IProductRepository;
import com.backend.jibli.product.Product;
import com.backend.jibli.product.ProductDTO;
import com.backend.jibli.user.IUserRepository;
import com.backend.jibli.user.User;
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
    private final IUserRepository userRepository;

    @Autowired
    public CartItemService(
            ICartItemRepository cartItemRepository,
            ICartRepository cartRepository,
            IProductRepository productRepository,
            IUserRepository userRepository
            ) {
        this.cartItemRepository = cartItemRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
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
    public CartItemDTO addProductToUserCart(Integer userId, CartItemDTO dto) {
        if (dto.getProductId() == null || dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new IllegalArgumentException("ProductId and quantity are required");
        }

        // Find product
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + dto.getProductId()));

        Integer companyId = product.getCompany().getCompanyId(); // Assuming each product belongs to a company

        // Find existing cart for this user and company
        Optional<Cart> optionalCart = cartRepository.findByUserUserIdAndCompanyCompanyId(userId, companyId);

        Cart cart;
        if (optionalCart.isPresent()) {
            cart = optionalCart.get();
        } else {
            // Create new cart for this user & company
            cart = new Cart();
            cart.setUser(userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId)));
            cart.setCompany(product.getCompany());
            cart.setTotalPrice(0.0); // initially zero
            cart = cartRepository.save(cart);
        }

        // Check if product is already in the cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartCartIdAndProductProductId(cart.getCartId(), product.getProductId());

        CartItem cartItem;
        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + dto.getQuantity());
        } else {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(dto.getQuantity());
        }

        CartItem savedItem = cartItemRepository.save(cartItem);

        // Update cart total price
        double total = cartItemRepository.findByCartCartId(cart.getCartId())
                .stream()
                .mapToDouble(ci -> ci.getQuantity() * ci.getProduct().getProductPrice())
                .sum();
        cart.setTotalPrice(total);
        cartRepository.save(cart);

        return mapToDTO(savedItem);
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
        ProductDTO productDTO = null;

        if (cartItem.getProduct() != null) {
            Product product = cartItem.getProduct();
            productDTO = new ProductDTO(
                    product.getProductId(),
                    product.getProductName(),
                    product.getProductDescription(),
                    product.getProductPrice(),
                    product.getProductFinalePrice(),
                    product.isAvailable(),
                    product.getDiscountPercentage(),
                    product.getCategory() != null ? product.getCategory().getCategoryId() : null,
                    product.getUser() != null ? product.getUser().getUserId() : null,
                    product.getCompany() != null ? product.getCompany().getCompanyId() : null,
                    product.getCreatedAt(),
                    product.getLastUpdated(),
                    product.getAttachments() != null
                            ? product.getAttachments().stream().map(a -> a.getAttachmentId()).toList()
                            : null,
                    product.getReviews() != null
                            ? product.getReviews().stream().map(r -> r.getReviewId()).toList()
                            : null,
                    product.getOrderItems() != null
                            ? product.getOrderItems().stream().map(o -> o.getOrderItemId()).toList()
                            : null
            );
        }

        return new CartItemDTO(
                cartItem.getCartItemId(),
                cartItem.getCart() != null ? cartItem.getCart().getCartId() : null,
                cartItem.getProduct() != null ? cartItem.getProduct().getProductId() : null,
                cartItem.getQuantity(),
                productDTO
        );
    }
}
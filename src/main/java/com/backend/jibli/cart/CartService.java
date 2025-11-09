package com.backend.jibli.cart;

import com.backend.jibli.product.IProductRepository;
import com.backend.jibli.product.Product;
import com.backend.jibli.product.ProductDTO;
import com.backend.jibli.user.IUserRepository;
import com.backend.jibli.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    public List<Map<String, Object>> getUserCartsGroupedByCompany(Integer userId) {
        List<Map<String, Object>> results = cartRepository.findUserCartsGroupedByCompany(userId);

        ObjectMapper mapper = new ObjectMapper();

        return results.stream()
                .map(tuple -> {
                    // Make a mutable copy
                    Map<String, Object> cart = new HashMap<>(tuple);

                    // Parse JSON string into a list
                    Object items = cart.get("cartItems");
                    if (items instanceof String json) {
                        try {
                            List<Map<String, Object>> parsedItems =
                                    mapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
                            cart.put("cartItems", parsedItems);
                        } catch (Exception e) {
                            cart.put("cartItems", List.of());
                        }
                    }
                    return cart;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteByUserUserIdAndCompanyCompanyId(Integer userId,Integer  companyId) {
        cartRepository.deleteByUserUserIdAndCompanyCompanyId(userId,companyId);

    }


    private Map<String, Object> parseCartRow(Map<String, Object> row) {
        Object jsonItems = row.get("cartItems");
        if (jsonItems != null) {
            try {
                // Convert JSON array string into a real List<Map<String, Object>>
                List<Map<String, Object>> parsedItems = new ObjectMapper().readValue(
                        jsonItems.toString(),
                        new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
                );
                row.put("cartItems", parsedItems);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return row;
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

        if (cartItem.getProduct() != null) {
            Product product = cartItem.getProduct();
            ProductDTO productDTO = new ProductDTO();
            productDTO.setProductId(product.getProductId());
            productDTO.setProductName(product.getProductName());
            productDTO.setProductDescription(product.getProductDescription());
            productDTO.setProductPrice(product.getProductPrice());
            productDTO.setProductFinalePrice(product.getProductFinalePrice());
            productDTO.setAvailable(product.isAvailable());
            productDTO.setDiscountPercentage(product.getDiscountPercentage());
            productDTO.setCategoryId(product.getCategory() != null ? product.getCategory().getCategoryId() : null);
            productDTO.setUserId(product.getUser() != null ? product.getUser().getUserId() : null);
            productDTO.setCompanyId(product.getCompany() != null ? product.getCompany().getCompanyId() : null);
            productDTO.setCreatedAt(product.getCreatedAt());
            productDTO.setLastUpdated(product.getLastUpdated());
            // if you have relationships like attachments, reviews, etc., you can map them here if needed

            dto.setProduct(productDTO);
        }

        return dto;
    }

}
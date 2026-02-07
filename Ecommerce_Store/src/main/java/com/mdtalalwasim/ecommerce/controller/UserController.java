package com.mdtalalwasim.ecommerce.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mdtalalwasim.ecommerce.entity.Cart;
import com.mdtalalwasim.ecommerce.entity.Category;
import com.mdtalalwasim.ecommerce.entity.User;
import com.mdtalalwasim.ecommerce.service.CartService;
import com.mdtalalwasim.ecommerce.service.CategoryService;
import com.mdtalalwasim.ecommerce.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	CategoryService categoryService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	CartService cartService;
	
	//to track which user is login right Now
	//by default call this method when any request come to this controller because of @ModelAttribut
	@ModelAttribute 
	public void getUserDetails(Principal principal, Model model) {
		if(principal != null) {
			String currenLoggedInUserEmail = principal.getName();
			User currentUserDetails = userService.getUserByEmail(currenLoggedInUserEmail);
			//System.out.println("Current Logged In User is :: USER Controller :: "+currentUserDetails.toString());
			model.addAttribute("currentLoggedInUserDetails",currentUserDetails);
			
			//for showing user cart count
			Long countCartForUser = cartService.getCounterCart(currentUserDetails.getId());
			//System.out.println("User Cart Count :"+countCartForUser);
			model.addAttribute("countCartForUser", countCartForUser);
		}
		
		List<Category> allActiveCategory = categoryService.findAllActiveCategory();
		model.addAttribute("allActiveCategory",allActiveCategory);
		
	}
	
	
	@GetMapping("/")
	public String home(){
		return "user/user-home";
	}
	
	
	//ADD TO CART Module
	@GetMapping("/add-to-cart")
	String addToCart(@RequestParam Long productId, @RequestParam Long userId,
					 @RequestParam(defaultValue = "1") int quantity, HttpSession session,
					 HttpServletRequest request, Principal principal) {
		System.out.println("INSIDE ITS");

		String redirectTarget = "/product/" + productId;
		if (principal != null) {
			User currentUser = userService.getUserByEmail(principal.getName());
			if (currentUser != null && "ROLE_ADMIN".equals(currentUser.getRole())) {
				String referer = request.getHeader("Referer");
				if (!ObjectUtils.isEmpty(referer)) {
					redirectTarget = "redirect:" + referer;
				} else {
					redirectTarget = "redirect:" + redirectTarget;
				}
			} else {
				redirectTarget = "redirect:" + redirectTarget;
			}
		} else {
			redirectTarget = "redirect:" + redirectTarget;
		}

		if (quantity <= 0) {
			session.setAttribute("errorMsg", "Quantity must be at least 1.");
			return redirectTarget;
		}
		Cart saveCart = cartService.saveCart(productId, userId, quantity);

		//System.out.println("save Cart is :"+saveCart);
		if(ObjectUtils.isEmpty(saveCart)) {
			System.out.println("INSIDE Error");
			session.setAttribute("errorMsg", "Requested quantity is unavailable.");
		}else {
			session.setAttribute("successMsg", "Successfully, Product added to Cart");
		}
		System.out.println("pid"+productId+" uid:"+userId);
		return redirectTarget;
	}
	
	@GetMapping("/cart")
	String loadCartPage(Principal principal, Model model) {
		//when load cart, it is showing logged in user cart details:
		
		
		User user = getLoggedUserDetails(principal);
		List<Cart> carts = cartService.getCartsByUser(user.getId());
		model.addAttribute("carts", carts);
		if(carts.size() > 0) {
			Double totalOrderPrice = carts.get(carts.size()-1).getTotalOrderPrice();
			model.addAttribute("totalOrderPrice", totalOrderPrice);
		}
		
		
		return "/user/cart";
	}

	@GetMapping("/cart-quantity-update")
	public String updateCartQuantity(@RequestParam("symbol") String symbol , @RequestParam("cartId") Long cartId,
									 HttpSession session){
		System.out.println(symbol+ " " + cartId);
		Boolean f = cartService.updateCartQuantity(symbol, cartId);
		if (!Boolean.TRUE.equals(f) && symbol.equalsIgnoreCase("increase")) {
			session.setAttribute("errorMsg", "Requested quantity is unavailable.");
		}
		return "redirect:/user/cart";
	}

	private User getLoggedUserDetails(Principal principal) {
		String email = principal.getName();
		User user = userService.getUserByEmail(email);
		return user;
	}
	
	
	@GetMapping("/orders")
	public String orderPage(Principal principal, HttpSession session) {
		User user = getLoggedUserDetails(principal);
		boolean checkedOut = cartService.checkoutCart(user.getId());
		if (!checkedOut) {
			session.setAttribute("errorMsg", "Your cart is empty.");
			return "redirect:/user/cart";
		}
		session.setAttribute("successMsg", "Order placed successfully.");
		return "redirect:/user/order-success";
		}

	@GetMapping("/order-success")
	public String orderSuccess() {
		return "/user/order-success";
	}


	
}

package springbook.learningtest.spring.web.atmvc;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import springbook.learningtest.spring.web.AbstractDispatcherServletTest;

public class SessionAttributesTest extends AbstractDispatcherServletTest {
	@Test
	public void sessionAttr() throws ServletException, IOException {
		setClasses(UserController.class);
		initRequest("/user/edit").addParameter("id", "1");
		runService();
		
		HttpSession session = request.getSession();

		// 모델로 리턴된 user와 HttpSession에 저장된 user가 동일한 오브젝트인지 확인
		assertThat(session.getAttribute("user"), is(getModelAndView().getModel().get("user")));
		
		// 모델로 리턴된 order와 HttpSession에 저장된 order가 동일한 오브젝트인지 확인
		assertThat(session.getAttribute("order"), is(getModelAndView().getModel().get("order")));
		
		initRequest("/user/edit", "POST").addParameter("id", "1").addParameter("name", "Spring2");
		
		// 이전 요청을 세션을 가져와 설정하여 세션 상태가 유지된 채로 다음 요청을 보내도록 함.
		request.setSession(session);
		runService();
		
		// 두 번째 요청의 파라미터로는 전달되지 않았으나 세션에 저장되어 있던 user에 email이 반영되었는지 확인 
		assertThat(((User)getModelAndView().getModel().get("user")).getEmail(), is("mail@spring.com"));
		
		// SessionStatus 를 통해 세션에 저장된 user가 제거됐는지 확인
		assertThat(session.getAttribute("user"), is(nullValue()));
		
		// SessionStatus 를 통해 세션에 저장된 order가 제거됐는지 확인
		assertThat(session.getAttribute("order"), is(nullValue()));
	}
	
	@Controller	
	@SessionAttributes({"user", "order"})
	static class UserController {
		@RequestMapping(value="/user/edit", method=RequestMethod.GET) 
		public User form(@RequestParam int id, Model model) {
			model.addAttribute("order", new Order(id, 1, 5));
			return new User(1, "Spring", "mail@spring.com");
		}
		
		@RequestMapping(value="/user/edit", method=RequestMethod.POST) 
		public void submit(@ModelAttribute User user, SessionStatus sessionStatus) {
			sessionStatus.setComplete();
		}
	}
	
	static class User {
		int id; String name; String email;
		public User(int id, String name, String email) { this.id = id;			this.name = name;			this.email = email;		}
		public User() { }
		public int getId() { return id; }
		public void setId(int id) { this.id = id; }
		public String getName() { return name; }
		public void setName(String name) { this.name = name; }
		public String getEmail() { return email; }
		public void setEmail(String email) { this.email = email; }
		public String toString() { return "User [email=" + email + ", id=" + id + ", name=" + name + "]";	}		
	}
	
	// 테스트 용 주문 객체 
	static class Order {
		int userId; int productId; int quantity;			
		public Order(int userId, int productId, int quantity) { this.userId = userId; this.productId = productId; this.quantity = quantity; }
		public int getUserId() { return userId; }
		public void setUserId(int userId) { this.userId = userId; }
		public int getProductId() { return productId; }
		public void setProductId(int productId) { this.productId = productId; }
		public int getQuantity() { return quantity; }
		public void setQuantity(int quantity) { this.quantity = quantity; }
		public String toString() { return "User [userId=" + userId + ", productId=" + productId + ", quantity=" + quantity + "]";	}		
	}
}

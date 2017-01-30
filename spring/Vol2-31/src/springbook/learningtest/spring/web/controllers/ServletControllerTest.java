package springbook.learningtest.spring.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.SimpleServletHandlerAdapter;

import springbook.learningtest.spring.web.AbstractDispatcherServletTest;

public class ServletControllerTest extends AbstractDispatcherServletTest {
	@Test
	public void helloServletController() throws ServletException, IOException {
		// 핸들러 어댑터와 컨트롤러를 Bean으로 등록해 줌
		setClasses(SimpleServletHandlerAdapter.class, HelloServlet.class);
		initRequest("/hello").addParameter("name", "Spring");
		assertThat(runService().getContentAsString(), is("Hello Spring"));
	}
	
	@Component("/hello")
	static class HelloServlet extends HttpServlet {
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			// GET 파라미터를 HttpServletResponse에 넣어줌
			String name = req.getParameter("name");
			resp.getWriter().print("Hello " + name);
		}
	}
}

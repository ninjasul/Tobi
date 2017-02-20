package springbook.learningtest.spring.ioc;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import springbook.learningtest.spring.ioc.bean.AnnotatedHello;
import springbook.learningtest.spring.ioc.bean.AnnotatedHelloConfig;
import springbook.learningtest.spring.ioc.bean.Hello;
import springbook.learningtest.spring.ioc.bean.Printer;
import springbook.learningtest.spring.ioc.bean.StringPrinter;

public class ApplicationContextTest {
	private String basePath = StringUtils.cleanPath(ClassUtils.classPackageAsResourcePath(getClass())) + "/";
	
	@Test
	public void registerBean() {
		StaticApplicationContext ac = new StaticApplicationContext();
		ac.registerSingleton("hello1", Hello.class);
		
		Hello hello1 = ac.getBean("hello1", Hello.class);
		assertThat(hello1, is(notNullValue()));
		
		BeanDefinition helloDef = new RootBeanDefinition(Hello.class);
		helloDef.getPropertyValues().addPropertyValue("name", "Spring");
		ac.registerBeanDefinition("hello2", helloDef);
		
		Hello hello2 = ac.getBean("hello2", Hello.class);
		assertThat(hello2.sayHello(), is("Hello Spring"));
		assertThat(hello1, is(not(hello2)));
		
		assertThat(ac.getBeanFactory().getBeanDefinitionCount(), is(2));
	}
	
	@Test
	public void registerBeanWithDependency() {
		StaticApplicationContext ac = new StaticApplicationContext();
		
		ac.registerBeanDefinition("printer", new RootBeanDefinition(StringPrinter.class));
		
		BeanDefinition helloDef = new RootBeanDefinition(Hello.class);
		helloDef.getPropertyValues().addPropertyValue("name", "Spring");
		helloDef.getPropertyValues().addPropertyValue("printer", new RuntimeBeanReference("printer"));
		ac.registerBeanDefinition("hello", helloDef);

		Hello hello = ac.getBean("hello", Hello.class);
		hello.print();

		assertThat(ac.getBean("printer").toString(), is("Hello Spring"));
	}
	
	@Test
	public void genericApplicationContext() {
		GenericApplicationContext ac = new GenericApplicationContext();
		
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(ac);
		reader.loadBeanDefinitions("springbook/learningtest/spring/ioc/genericApplicationContext.xml");
		
		ac.refresh();
		
		Hello hello = ac.getBean("hello", Hello.class);
		hello.print();

		assertThat(ac.getBean("printer").toString(), is("Hello Spring"));
	}
	
	@Test
	public void genericXmlApplicationContext() {
		GenericApplicationContext ac = new GenericXmlApplicationContext(basePath + "genericApplicationContext.xml");
		
		Hello hello = ac.getBean("hello", Hello.class);
		hello.print();
		
		assertThat(ac.getBean("printer").toString(), is("Hello Spring"));
	}
	
	@Test(expected=BeanCreationException.class)
	public void createContextWithoutParent() {
		ApplicationContext child = new GenericXmlApplicationContext(basePath + "childContext.xml");
	}
	
	@Test
	public void contextHierachy() {
		// GenericXmlApplicationContext 는 XML 설정을 사용하는 루트 컨텍스트를 만들때 만 사용할 수 있음.
		ApplicationContext parent = new GenericXmlApplicationContext(basePath + "parentContext.xml");

		// 보다 세밀한 컨텍스트 설정을 위해서는 GenericApplicationContext를 이용해야 함.
		// parent 를 부모 컨텍스트로 지정
		GenericApplicationContext child = new GenericApplicationContext(parent);
		
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(child);
		reader.loadBeanDefinitions(basePath + "childContext.xml");
		
		// 설정 메타 정보를 읽은 다음 refresh() 를 호출하여 컨텍스트를 초기화 하면서 DI를 진행
		// 리더를 사용해서 설정을 읽은 경우에는 반드시 refresh() 를 통해 초기화 해 주어야 함.
		child.refresh();
		
		// printer 빈 오브젝트가 childContext.xml 에 설정되어 있지 않음에도 불구하고 
		// child 컨텍스트에서 printer 빈 오브젝트를 참조할 수 있음.
		Printer printer = child.getBean("printer", Printer.class);
		assertThat(printer, is(notNullValue()));
				
		// hello 오브젝트를 childContext.xml 에 설정된 값으로 읽어오도록 처리.
		Hello hello = child.getBean("hello", Hello.class);
		assertThat(hello, is(notNullValue()));

		// 인사말이 Hello Child 로 출력되는 지 확인
		hello.print();
		assertThat(printer.toString(), is("Hello Child"));
		
		// hello 오브젝트를 parentContext.xml 에 설정된 값으로 다시 읽어오도록 처리.
		// 인사말이 Hello Parent 로 변경되어 있는지 확인
		hello = parent.getBean("hello", Hello.class);
		hello.print();
		assertThat(printer.toString(), is("Hello Parent"));
	}
	
	@Test
	public void simpleBeanScanning() {
		ApplicationContext ctx = new AnnotationConfigApplicationContext("springbook.learningtest.spring.ioc.bean");
		AnnotatedHello hello = ctx.getBean("annotatedHello", AnnotatedHello.class);
		assertThat(hello, is(notNullValue()));
	}
	
	@Test
	public void filteredBeanScanning() {
		ApplicationContext ctx = new GenericXmlApplicationContext(basePath + "filteredScanningContext.xml");
		Hello hello = ctx.getBean("hello", Hello.class);
		assertThat(hello, is(notNullValue()));
	}
	
	@Test
	public void configurationBean() {
		ApplicationContext ctx = new AnnotationConfigApplicationContext(AnnotatedHelloConfig.class);
		
		AnnotatedHello hello = ctx.getBean("annotatedHello", AnnotatedHello.class);
		assertThat(hello, is(notNullValue()));

		AnnotatedHelloConfig config = ctx.getBean("annotatedHelloConfig", AnnotatedHelloConfig.class);
		assertThat(config, is(notNullValue()));
		
		assertThat(config.annotatedHello(), is(sameInstance(hello)));
		assertThat(config.annotatedHello(), is(config.annotatedHello()));
		
		System.out.println(ctx.getBean("systemProperties").getClass());
	}
	
	@Test
	public void constructorArgName() {
		ApplicationContext ac = new GenericXmlApplicationContext(basePath + "constructorInjection.xml");
		
		Hello hello = ac.getBean("hello", Hello.class);
		hello.print();
		
		assertThat(ac.getBean("printer").toString(), is("Hello Spring"));
	}
	
	@Test
	public void autowire() {
		ApplicationContext ac = new GenericXmlApplicationContext(basePath + "autowire.xml");
		
		Hello hello = ac.getBean("hello", Hello.class);
		hello.print();
		
		assertThat(ac.getBean("printer").toString(), is("Hello Spring"));
	}

}

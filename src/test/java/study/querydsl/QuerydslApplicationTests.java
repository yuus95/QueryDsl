package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Hello;
import study.querydsl.entity.QHello;

import javax.persistence.EntityManager;
import javax.swing.text.html.parser.Entity;


@Transactional
@SpringBootTest
class QuerydslApplicationTests {

	@Autowired
	EntityManager em;





	@Test
	void contextLoads() {

		Hello hello = new Hello();
		em.persist(hello);

		JPAQueryFactory query = new JPAQueryFactory(em);
		
		
		//QHello qHello = new QHello("h"); 밑에 문장과 같은 표현
		QHello qHello = QHello.hello.hello;
		
		
		
		Hello result = query
				.selectFrom(qHello) // 큐타입 넣기
				.fetchOne();

		Assertions.assertEquals(hello,result);
		Assertions.assertEquals(hello.getId(),result.getId());




	}

}

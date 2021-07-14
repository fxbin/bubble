package test.data.elasticsearch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import test.data.elasticsearch.constants.ElasticsearchConstant;
import test.data.elasticsearch.model.Person;
import test.data.elasticsearch.service.PersonService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * test.data.elasticsearch.SampleElasticsearchApplication
 *
 * @author fxbin
 * @version v1.0
 * @since 2020/5/6 11:07
 */
@Slf4j
@SpringBootApplication
public class SampleElasticsearchApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SampleElasticsearchApplication.class, "--debug").close();
    }


    @Resource
    private PersonService personService;

    @Override
    public void run(String... args) throws Exception {
        personService.deleteIndex(ElasticsearchConstant.INDEX_NAME);
        personService.createIndex(ElasticsearchConstant.INDEX_NAME);

        List<Person> addList = new ArrayList<>();
        addList.add(Person.builder().age(11).birthday(new Date()).country("CN").id(1L).name("哈哈").remark("test1").build());
        addList.add(Person.builder().age(22).birthday(new Date()).country("US").id(2L).name("hiahia").remark("test2").build());
        addList.add(Person.builder().age(33).birthday(new Date()).country("ID").id(3L).name("呵呵").remark("test3").build());

        personService.insert(ElasticsearchConstant.INDEX_NAME, addList);

        Person person = Person.builder().age(33).birthday(new Date()).country("ID_update").id(3L).name("呵呵update").remark("test3_update").build();
        List<Person> updateList = new ArrayList<>();
        updateList.add(person);
        personService.update(ElasticsearchConstant.INDEX_NAME, updateList);

        List<Person> personList = personService.searchList(ElasticsearchConstant.INDEX_NAME);
        log.info("person list data: {}", personList);
    }
}

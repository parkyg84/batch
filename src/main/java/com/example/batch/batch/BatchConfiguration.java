package com.example.batch.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.*;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class BatchConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private static final int chunkSize = 30;


    BatchConfiguration(JobBuilderFactory jobBuilderFactory,
                     StepBuilderFactory stepBuilderFactory,
                     DataSource dataSource) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.dataSource = dataSource;
    }


// 파일 로딩 시 사용!!
//    @Bean
//    public FlatFileItemReader<Person> reader() {
//        return new FlatFileItemReaderBuilder<Person>()
//                .name("personItemReader")
//                .resource(new ClassPathResource("sample-data.csv"))
//                .delimited()
//                .names(new String[]{"firstName", "lastName"})
//                .fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
//                    setTargetType(Person.class);
//                }})
//                .build();
//    }


    @Bean
    public FlatFileItemReader<Person> reader() {
        return new FlatFileItemReaderBuilder<Person>()
                .name("personItemReader")
                .resource(new ClassPathResource("sample-data.csv"))
                .delimited()
                .names(new String[]{"firstName", "lastName"})
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                    setTargetType(Person.class);
                }})
                .build();
    }


    @Bean
    public JdbcPagingItemReader<Person> jdbcPagingItemReader() throws Exception {
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("id", 1);

        return new JdbcPagingItemReaderBuilder<Person>()
                .pageSize(chunkSize)
                .fetchSize(chunkSize)
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(Person.class))
                .queryProvider(createQueryProvider())
                .parameterValues(parameterValues)
                .name("jdbcPagingItemReader")
                .build();




        //final StudentMapper studentMapper = new StudentMapper();
        //reader.setRowMapper(studentMapper);  -->> 이거 활용!!
    }



//    SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
//            .withProcedureName("storedProcName")
//            .returningResultSet("test", new CustomRowMapper());
//    Map<String, Object> out = jdbcCall.execute(parameterSource);
//    List<CustomObject> customObjects = (List<CustomObject>) out.get("test");


//    @Bean
//    @StepScope
//    public StoredProcedureItemReader<Customer> customerItemReader(DataSource dataSource,
//                                                                  @Value("#{jobParameters['city']}") String city){
//
//        return new StoredProcedureItemReaderBuilder<Customer>()
//                .name("customerItemReader")
//                .dataSource(DataSource)
//                .procedureName("customer_list")
//                .parameters(new SqlParameters[]{
//                        new SqlParameter("cityOption", Types.VARCHAR)})
//                .preparedStatementSetter(
//                        new ArgumentPreparedStatementSetter(new Object[] {city}))
//                .rowMapper(new CusstomerRowMapper())
//                .build();
//    }


    @Bean
    public PagingQueryProvider createQueryProvider() throws Exception {

        //만약 SP 호출 가능한가??
//https://github.com/AcornPublishing/definitive-spring-batch/tree/main/def-guide-spring-batch-master/Chapter07

        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource); // Database에 맞는 PagingQueryProvider를 선택하기 위해
        queryProvider.setSelectClause("id, first_name, last_name");
        queryProvider.setFromClause("from monkey.people2");
        queryProvider.setWhereClause("where id >= :id");

        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("id", Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);




        return queryProvider.getObject();
    }


    @Bean
    public PersonItemProcessor processor() {
        return new PersonItemProcessor();
    }

    @Bean
    public Job importUserJob(JobCompletionNotificationListener listener, Step step1) {
        return jobBuilderFactory.get("importUserJob3")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1)
                .end()
                .build();
    }

    @Bean
    public Step step1() throws  Exception  {
        return stepBuilderFactory.get("step3")
                .<Person, Person> chunk(30)
                //.reader(reader())
                .reader(jdbcPagingItemReader())
                .processor(processor())
                .writer(writer())
                .build();
    }


    public ItemWriter<Person> writer() throws  Exception{

        JdbcBatchItemWriter<Person> itemWriter = new JdbcBatchItemWriterBuilder<Person>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .dataSource(dataSource)
                .sql("INSERT INTO monkey.people3 (firstName, lastName) VALUES (:firstName, :lastName)")
                .beanMapped()
                .build();
        itemWriter.afterPropertiesSet();

        return itemWriter;
    }


}

# ElsticSearch搜索模块

## mvn依赖
1. elasticsearch-rest-high-level-client
```xml
        <dependency>
            <groupId>org.elasticsearch.client</groupId>
            <artifactId>elasticsearch-rest-high-level-client</artifactId>
            <version>7.4.2</version>
        </dependency>
```
2. elasticsearch版本（更改springboot默认elasticsearch版本）
```xml
    <properties>
        <elasticsearch.version>7.4.2</elasticsearch.version>
    </properties>
```
3. 依赖common模块(spring-cloud相关依赖)
```xml
        <dependency>
            <groupId>cn.brownqi.mall</groupId>
            <artifactId>mall-common</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
```
## 启动类
```java
@EnableDiscoveryClient
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class MallSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallSearchApplication.class, args);
    }

}
```
## 编写配置类
```java
@Configuration
public class MallElasitcSearchConfig {

    @Bean
    public RestHighLevelClient esRestClient(){
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("192.168.56.10", 9200, "http")
                )
        );
        return client; 
    }
    
        public static final RequestOptions COMMON_OPTIONS;
    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
//        builder.addHeader("Authorization", "Bearer " + TOKEN);
//        builder.setHttpAsyncResponseConsumerFactory(
//                new HttpAsyncResponseConsumerFactory
//                        .HeapBufferedResponseConsumerFactory(30 * 1024 * 1024 * 1024));
        COMMON_OPTIONS = builder.build();
    }
    
}
```
## 测试
### RestHighLevelClient
```java
@SpringBootTest
class MallElasitcSearchConfigTest {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Test
    void esRestClient() {
        System.out.println(restHighLevelClient);
    }
}
```
### api测试
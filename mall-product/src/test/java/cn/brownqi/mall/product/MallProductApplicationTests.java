package cn.brownqi.mall.product;

import cn.brownqi.mall.product.entity.BrandEntity;
import cn.brownqi.mall.product.service.BrandService;
//import com.aliyun.oss.OSS;
//import com.aliyun.oss.OSSClient;
//import com.aliyun.oss.OSSClientBuilder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

@SpringBootTest
class MallProductApplicationTests {

    @Autowired
    BrandService brandService;

//    @Autowired
//    OSSClient ossClient;

    @Test
    public void testUpload() throws FileNotFoundException {
//        // Endpoint以杭州为例，其它Region请按实际情况填写。
//        String endpoint = "oss-cn-hangzhou.aliyuncs.com";
//// 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建。
//        String accessKeyId = "LTAI4GCypjR8CFM7SXoJs8Ut";
//        String accessKeySecret = "GMkazeAEkMzjn8kX4TaOGR0l9OCIhr";
//
//// 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

//// 上传文件流。
////        InputStream inputStream = new FileInputStream("W:\\workSpace\\A_Projects\\mall-learning\\mall-product\\src\\test\\resources\\img\\brownqi_128.png");
//        InputStream inputStream = new FileInputStream("W:\\workSpace\\A_Projects\\mall-learning\\mall-product\\src\\test\\resources\\img\\mario.png");
//        ossClient.putObject("mall-brownqi", "mario.png", inputStream);
//
//// 关闭OSSClient。
//        ossClient.shutdown();
//
//        System.out.println("上传完成...");
    }

    @Test
    void contextLoads() {

        BrandEntity brandEntity = new BrandEntity();

//        brandEntity.setName("华为");
//        brandService.save(brandEntity);
//        System.out.println("保存成功");

//        brandEntity.setBrandId(1L);
//        brandEntity.setDescript("HUAWEI");
//        brandService.updateById(brandEntity);

        List<BrandEntity> brand_id = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));
        brand_id.forEach(System.out::println);

    }

}

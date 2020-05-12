# mall-learning

# 项目介绍

- 该项目为电商的后台管理系统。主要功能有商品系统的分类、品牌、属性、spu、sku的维护与管理；库存系统中的采购单的维护，以及订单与用户系统的管理；前端页面由renren-fast生成

# 开发环境及工具

- IDEA / JDK 8
- DataGrip / MySQL 5.7
- Kibana / ElasticSearch 7.4.2
- WebStorm
- CentOS7
- Docker

# 框架使用

- SpringBoot 2.2.6.RELEASE
- SpringCloud Hoxton.SR3 (Nacos / openFeign / Gateway)
- Mybatis-Plus
- Vue.js 2

# 开发内容

- 使用SpringBoot构建应用，将应用拆分为商品、会员、优惠信息、订单、仓储五个微服务。使用SpringCloudAlibabaNacos做调度中心与配置中心。微服务之间使用SpringCloudOpenFeign通信。网关层使用SpringCloudGateway过滤路由、负载均衡。完成了商品系统业务代码的编写。主要实现了以下功能：
    - 三级分类维护更新；
    - 品牌管理CRUD，其中使用了阿里云OSS进行图片的存储；
    - 属性的分组以及规格参数销售属性的关联；
    - 商品的spu管理、发布商品；以及库存系统的采购单维护功能。
    - 库存系统的的采购单维护，采购单的发布、分配、领取、完成等功能
    - 商品的上架功能，将sku信息保存到ElasticSearch中，可供之后的检索；
    - 编写了部分通用功能，如：枚举状态码、自定义异常状态码、自定义JSR303校验器及其注解的编写。


# 使用步骤：
本文将以spring boot 2.7为例，spring boot 3以上的，以后再说。

## 启动示例

1. 将demo项目导入idea
2.  修改宿主module中的application.yml，
```
sbp:  
 runtime-mode: development
```
    
3.   `mvn clean compile` 命令，生成class文件。
4.  使用idea上面的`run`按钮启动。

测试：
你可以直接调用或者使用浏览器访问那些controller中的接口地址即可，
比如
```

//此请求调用了宿主controller中的方法，并在方法中调用了插件的方法
http://localhost:8082/pf4j_demo/plugin/extensions/list

//此请求直接调用了插件的controller方法，是的，插件中的接口地址依旧是基于宿主的。
http://localhost:8082/pf4j_demo/admin2/profile
```

## 依赖：

- To match Spring Boot versions, sbp will use the major part and minor part of Spring Boot versions, and use micro part for sbp version.
- Before sbp version 18, Every sbp release will have wwo versions: `2.4.X` and `2.7.X`.
- ❗️After sbp [version 18](https://github.com/hank-cp/sbp/releases/tag/18), sbp will only supports Spring Boot 3.x.
- Table below shows the corresponding sbp version to different Spring Boot version.

| Spring Boot Version | sbp Version | Jvm | Gradle                                                                                                                                                                                                                  |
| ------------------- | ----------- | --- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| <= 2.4.x            | 2.4.17      | 8+  | implementation "org.springframework.boot:spring-boot-starter-web:2.4.13"  <br>implementation "org.springframework.boot:spring-boot-starter-aop:2.4.13"  <br>implementation 'org.laxture:sbp-spring-boot-starter:2.4.17' |
| >= 2.5.x, < 3.x     | 2.7.17      | 8+  | implementation "org.springframework.boot:spring-boot-starter-web:2.7.8"  <br>implementation "org.springframework.boot:spring-boot-starter-aop:2.7.8"  <br>implementation 'org.laxture:sbp-spring-boot-starter:2.7.17'   |
| >= 3.x              | 3.3.25      | 17+ | implementation "org.springframework.boot:spring-boot-starter-web:3.0.6"  <br>implementation "org.springframework.boot:spring-boot-starter-aop:3.0.6"  <br>implementation 'org.laxture:sbp-spring-boot-starter:3.0.18'   |
| >= 3.x              | -SNAPSHOT   | 17+ | 'org.laxture:sbp-spring-boot-starter:-SNAPSHOT'                                                                                                                                                                         |

# demo的项目结构：
```

sbp      //工程文件夹
  |--pom //工程的pom，引入了一些公用依赖，比如spring-boot的那些依赖
  |
  |--api //api接口，被宿主module和插件module共享
  |     |--pom
  |
  |--demo //宿主module，比如你的项目
  |     |--pom 
  |
  |--plugins //存放所有插件的文件夹/module
    |--pom //定义了一些plugin module可以共享的内容
    |    
    |--plugin1 //插件
	  |--pom 
```


### 宿主

首先，需要有一个module作为宿主，也就是选取你的可执行的spring boot工程、或者说承载业务逻辑的主module。
在这个module中需要引入`sbp-spring-boot-starter`依赖，同时，还需要引入api module作为依赖
```
<dependency>  
    <groupId>org.laxture</groupId>  
    <artifactId>sbp-spring-boot-starter</artifactId>  
    <version>2.7.14</version>  
</dependency>

<dependency>  
    <groupId>com.kiylx.sbp</groupId>  
    <artifactId>common-api</artifactId>  
    <version>0.0.1-SNAPSHOT</version>  
</dependency>

<dependency>  
    <groupId>org.pf4j</groupId>  
    <artifactId>pf4j</artifactId>  
    <version>3.8.0</version>  
    <scope>provided</scope>  
    <exclusions>  
        <exclusion>  
            <groupId>org.slf4j</groupId>  
            <artifactId>slf4j-log4j12</artifactId>  
        </exclusion>  
    </exclusions>  
</dependency>
```

#### `application.yml`文件

宿主的`application.yml`文件中需要添加如下配置，用于配置sbp的行为：

```
spring:
	sbp:  
#    runtime-mode: development  # 如果正在idea中开发，不打算打包，使用这个
    runtime-mode: deployment  #如果需要打包部署，使用这个
    enabled: true  
    classes-directories:  #这个是在development模式下，加载插件的class路径
      - "target/classes"  
      - "out/production/classes"  
      - "out/production/resources"  
      - "build/classes/java/main"  
      - "build/resources/main"  
    lib-directories:  #相对于上面插件路径而言的依赖库路径
      - "libs"  
    plugin-profiles:  
      - share_jta  
    plugin-properties:  
      spring:  
        jpa:  
          properties:  
            hibernate:  
              temp:  
                use_jdbc_metadata_defaults: false  
          database-platform: org.hibernate.dialect.PostgreSQL9Dialect  
    controller:  
      base-path: pf4j
```


### 公共的API module

这个module用来创建一些公用的interface接口，其他的插件module可以实现这些接口，提供统一的行为。
并且，宿主工程也需要引入此module。

API module 也可以引入一些公共依赖：

注意，引入pf4j时最好添加上`<scope>provided</scope>` 因此，你需要在宿主，api，插件等module都引入pf4j依赖。

```
<dependency>  
    <groupId>org.pf4j</groupId>  
    <artifactId>pf4j</artifactId>  
    <version>3.8.0</version>
    <scope>provided</scope>
</dependency>  
当然，也可以引入其他的一些公共依赖
<dependency>  
    <groupId>org.laxture</groupId>  
    <artifactId>spring-static-ctx</artifactId>  
    <version>0.1.2</version>  
</dependency>
```
### 插件 module

除了需要引入上面的api module作为依赖之外，还需要引入`sbp-core`依赖：

```
<dependency>  
    <groupId>org.laxture</groupId>  
    <artifactId>sbp-core</artifactId>  
    <version>2.7.14</version>  
</dependency>

  <dependency>  
    <groupId>org.pf4j</groupId>  
    <artifactId>pf4j</artifactId>  
    <version>3.8.0</version>  
    <scope>provided</scope>  
    <exclusions>  
        <exclusion>  
            <groupId>org.slf4j</groupId>  
            <artifactId>slf4j-log4j12</artifactId>  
        </exclusion>  
    </exclusions>  
</dependency> 
```

### 插件的配置

可以在插件module中正常使用`application.yml`文件，但是不要配置请求路径，因为配置了也不会起作用。
插件中的controller，访问时其地址也是基于宿主的baseurl的。

如下代码不要写入`application.yml`文件
```
server:  
   servlet.context-path: /pf4j_demo  
   port: 8082  
   max-http-header-size: 2MB  
   tomcat.max-http-post-size: 50MB
```

除了`application.yml`文件，还需要创建一个`application-plugin.yml`文件。
内容不多：
```
spring:  
  plugin: true
```

除了`application.yml`文件，还需要建立一个plugin.properties文件，里面的内容需要与上面的pom中的manifest一致,
因为我们用的是maven构建，因此，它只在开发模式下有用，打包构建jar包时，需要在pom中配置manifest。

```
plugin.id=some.sbp.demo  
plugin.class=com.kiylx.pf4j.plugin1.AdminPlugin  
plugin.version=0.0.1  
plugin.provider=Your Name  
plugin.dependencies=
```

# 启动和部署

首先，需要给插件module做一些配置：
## 1. 如果插件有第三方依赖

需要在插件module的pom中添加maven-shade-plugin插件，将第三方依赖打包进去。

来看一种情况：
宿主工程已经有了ABC三个依赖，而插件module除了ABC之外，还需要依赖D。
而你又不想把ABC依赖也打包进插件module（因为宿主module有了嘛，再打进插件module中其实没啥用），那么，可以引入maven-shade-plugin同时配置一下artifactSet，看下面示例即可。

示例：

```
<plugin>  
    <groupId>org.apache.maven.plugins</groupId>  
    <artifactId>maven-shade-plugin</artifactId>  
    <version>3.2.4</version>  
    <configuration>  
        <!--使用 artifactSet 中的includes ：仅打包指定依赖，排除所有未指定的依赖（依赖项及其传递依赖项）-->  
        <artifactSet>  
            <includes>  
                <include>org.eclipse.paho:org.eclipse.paho.client.mqttv3</include>  
            </includes>  
        </artifactSet>  
        <!-- 此处按需编写更具体的配置 -->  
    </configuration>  
    <executions>  
        <execution>  
            <!-- 和 package 阶段绑定 -->  
            <phase>package</phase>  
            <goals>  
                <goal>shade</goal>  
            </goals>  
        </execution>  
    </executions>  
</plugin>
```

## 2. 在插件的pom中添加manifest配置


注意，在manifestEntries标签中按照例子添加。

```
<plugin>  
    <groupId>org.apache.maven.plugins</groupId>  
    <artifactId>maven-jar-plugin</artifactId>  
    <configuration>  
        <archive>  
            <index>true</index>  
            <manifest>  
                <addClasspath>true</addClasspath>  
                <mainClass>com.kiylx.pf4j.plugin1.AdminPluginStarter</mainClass>  
                <addDefaultImplementationEntries>true</addDefaultImplementationEntries>  
                <addDefaultSpecificationEntries>true</addDefaultSpecificationEntries>  
            </manifest>  
            <manifestEntries>  
                <Plugin-Id>some.sbp.demo</Plugin-Id>  
                <Plugin-Class>com.kiylx.pf4j.plugin1.AdminPlugin</Plugin-Class>  //这个类是spring启动的那个main类
                <Plugin-Version>0.0.1</Plugin-Version>  
                <Plugin-Provider>kiylx</Plugin-Provider>  
                <Plugin-Dependencies/>  //这个不知道怎么用
            </manifestEntries>  
        </archive>  
    </configuration>  
</plugin>
```
## 3.1 在开发模式下启动项目：

在开发模式下，不需要将插件打包为jar进行部署。

操作步骤：
1. 修改宿主module中的application.yml，
```
sbp:  
 runtime-mode: development
```
    
2.  `mvn clean compile` 命令，生成class文件。
3. 使用idea上面的`run`按钮启动。

## 3.2 打包出jar进行部署：

操作步骤：
1. 修改宿主module中的application.yml，
```
sbp:  
 runtime-mode: deployment
```
    
2. 使用`mvn clean package`打包为jar
3. 部署
	例如将项目部署在"D:\TEST"
	那么，需要在TEST文件夹下建立名为“plugins”的文件夹，这个不能改为其他名称。
	将打包出来的插件jar包放入TEST/plugins文件夹下，将宿主jar包放入TEST文件夹。
	然后使用`java -jar 宿主.jar` 正常启动宿主工程的jar包即可。

### 问题：
#### 找不到类

1. 确保在插件module中使用了maven-shade-plugin将所需要依赖都打包进去了
2. 确保extensions.idx文件存在，且不是空白。具体看下面。

#### 可以运行，但是在宿主module中，找不到扩展点

首先你需要确保你使用了pf4j中的插件化功能

在api moudle
```
public interface PluginGreeting extends ExtensionPoint {  
  
    String weName();  
  
}
```

在插件module
```
@Extension  
public class AdminGreeting implements PluginGreeting {  
  
    public AdminGreeting() { }  
    
    @Override  
    public String weName() {  
        return "admin";  
    }  
  
}
```

在宿主module
```
List<PluginGreeting> registers = pluginManager.getExtensions(PluginGreeting.class);  
for (PluginGreeting register : registers) {  
    System.out.println(register.weName());  
}
```

非常大的可能是打包后的插件jar包中没有extensions.idx文件或者文件中的内容空白。
因此pf4j不认为插件中的AdminGreeting类是扩展点，
或者说pom没有配置好，pf4j的注解处理没有生成文件。


可行的几个解决办法：

1. 在idea或Eclipse设置中启用注解处理
2. 据pf4j文档中所说，添加注解处理（我尝试后没有成功，或许文档过时了？）
```
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <annotationProcessors>
                        <annotationProcessor>org.pf4j.processor.ExtensionAnnotationProcessor</annotationProcessor>-->
                    </annotationProcessors>
                </configuration>
            </plugin>

```
3. 手动在如下位置创建extensions.idx文件

```
插件module
	  |--src
		|--main
			|--java
			|--resource
				|--META-INF
					|--extensions.idx
```

extensions.idx文件写入内容

```
com.kiylx.sbp.plugin1.AdminGreeting
```

内容其实是上面AdminGreeting类在打包后的class文件的全路径，需要注意的是，你的项目打包出来的class文件可能不是原文件名。

比如在PF4J-spring的demo中，在打包出来的jar包中，找到extensions.idx文件，你会发现内容是这样的：

```
org.pf4j.demo.welcome.WelcomePlugin$WelcomeGreeting
```

或者这样的

```
org.pf4j.demo.hello.HelloPlugin$HelloGreeting
```

因为它打包出来后，他们的class文件就是`HelloPlugin$HelloGreeting.class` 和 `WelcomePlugin$WelcomeGreeting.class`

如何知道类的位置和名称：
在开发模式下，使用`mvn clean compile`命令，然后去插件module文件夹的target/calsses文件夹下找到class文件


# 其他


---
- Fat-jar format (jar in jar) is not supported at the moment. There are two walkaround solutions:  
    目前不支持 Fat-jar 格式（jar in jar）。有两种演练解决方案：
    - Flat lib jar, then pack all classes and resources into on jar file. (as the below gradle example)  
        扁平的 lib jar，然后将所有类和资源打包到 jar 文件中。（如下图所示）
    - Use [zip format](https://pf4j.org/doc/packaging.html), put all 3rd-parth libs jar under `/lib`, then pack into zip file with `/classes`.  
        使用 zip 格式，将所有第 3 部分 libs jar 放在 `/lib` 下面，然后用 `/classes` 打包到 zip 文件中。

可以在application.yml中配置一个`spring.sbp.lib-directories`，

>文档原文：
>where to load jar libs, relative to plugin folder.

看起来应该是在插件目录下建立这个文件夹，存放插件所需lib，但是似乎会把文件夹识别为插件而报错。看来是我没理解。

---

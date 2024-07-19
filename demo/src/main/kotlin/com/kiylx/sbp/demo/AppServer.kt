package com.kiylx.sbp.demo

import com.kiylx.sbp.api.IdsConverter
import org.laxture.spring.util.ApplicationContextProvider
import org.modelmapper.ModelMapper
import org.modelmapper.convention.NameTokenizers
import org.modelmapper.jooq.RecordValueReader
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@SpringBootApplication(
    scanBasePackages = ["com.kiylx.sbp"],exclude = [
        SecurityAutoConfiguration::class ,SecurityFilterAutoConfiguration::class,
    ]
)
open class AppServer : CommandLineRunner {

    @Throws(Exception::class)
    override fun run(vararg args: String) {
        println("start...")
    }

    /**
     * @return
     * @category 跨越处理
     */
    @Bean
    open fun corsFilter(): CorsFilter {
        val urlBasedCorsConfigurationSource = UrlBasedCorsConfigurationSource()
        val corsConfiguration = CorsConfiguration()
        corsConfiguration.allowCredentials = true
        corsConfiguration.addAllowedOrigin("*")
        corsConfiguration.addAllowedHeader("*")
        corsConfiguration.addAllowedMethod("*")
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration)
        return CorsFilter(urlBasedCorsConfigurationSource)
    }

    /**
     * 下面的modelMapper方法的形参需要此处的bean
     * 算了，用不到，注释吧
     */
    @Bean
    open fun multiApplicationContextProviderRegister(): ApplicationContextAware {
        return ApplicationContextAware { ctx: ApplicationContext? ->
            ApplicationContextProvider.registerApplicationContext(
                ctx
            )
        }
    }

    @Bean
    @ConditionalOnMissingBean
    //使用@DependsOn，确保注入到此方法的applicationContext先于此方法注入
//    @DependsOn("multiApplicationContextProviderRegister")
    open fun modelMapper(applicationContext: ApplicationContext?=null): ModelMapper {
        val mapper: ModelMapper = ModelMapper()
        mapper.getConfiguration().setSourceNameTokenizer(NameTokenizers.CAMEL_CASE)
            .addValueReader(RecordValueReader())
        mapper.addConverter(IdsConverter())
        return mapper
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val builder = SpringApplicationBuilder()
            builder.profiles("no_security")
            builder.sources(AppServer::class.java)
            builder.build().run()
//            SpringApplication.run(AppServer::class.java, *args)
        }
    }
}

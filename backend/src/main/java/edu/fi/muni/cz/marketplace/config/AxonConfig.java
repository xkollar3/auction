package edu.fi.muni.cz.marketplace.config;

import java.time.Clock;
import java.util.List;

import javax.sql.DataSource;

import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.ConfigurationScopeAwareProvider;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.DefaultDeadlineManagerSpanFactory;
import org.axonframework.deadline.dbscheduler.DbSchedulerDeadlineManager;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.jdbc.JdbcTokenStore;
import org.axonframework.messaging.ScopeAwareProvider;
import org.axonframework.serialization.Serializer;
import org.axonframework.spring.jdbc.SpringDataSourceConnectionProvider;
import org.axonframework.tracing.SpanFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.task.Task;

@Configuration
public class AxonConfig {

  @Bean
  public TokenStore tokenStore(Serializer serializer, DataSource source) {
    JdbcTokenStore jdbcTokenStore = JdbcTokenStore.builder()
        .serializer(serializer)
        .connectionProvider(new SpringDataSourceConnectionProvider(source))
        .build();

    return jdbcTokenStore;
  }

  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  public Scheduler scheduler(DataSource dataSource, List<Task<?>> tasks) {
    return Scheduler.create(dataSource, tasks)
        .threads(10)
        .registerShutdownHook()
        .build();
  }

  @Bean
  public DeadlineManager deadlineManager(
      Scheduler scheduler,
      org.axonframework.config.Configuration configuration,
      @Qualifier("eventSerializer") Serializer serializer,
      TransactionManager transactionManager,
      SpanFactory spanFactory) {
    ScopeAwareProvider scopeAwareProvider = new ConfigurationScopeAwareProvider(configuration);
    return DbSchedulerDeadlineManager.builder()
        .scheduler(scheduler)
        .scopeAwareProvider(scopeAwareProvider)
        .serializer(serializer)
        .transactionManager(transactionManager)
        .spanFactory(DefaultDeadlineManagerSpanFactory.builder()
            .spanFactory(spanFactory)
            .build())
        .startScheduler(true)
        .build();
  }
}

package com.test.oes.cache;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.cache")
public class CacheProperties {

    private boolean enabled = true;
    private Redis redis = new Redis();
    private LocalTtl examList = new LocalTtl(Duration.ofSeconds(15));
    private LocalTtl examDetail = new LocalTtl(Duration.ofSeconds(15));
    private TwoLevelTtl scopeExams = new TwoLevelTtl(Duration.ofSeconds(30), Duration.ofMinutes(2));
    private TwoLevelTtl examMeta = new TwoLevelTtl(Duration.ofSeconds(60), Duration.ofMinutes(10));
    private TwoLevelTtl paperAggregate = new TwoLevelTtl(Duration.ofSeconds(120), Duration.ofMinutes(10));
    private LocalTtl snapshot = new LocalTtl(Duration.ofSeconds(60));
    private LocalTtl questionBank = new LocalTtl(Duration.ofSeconds(30));
    private Draft draft = new Draft();

    @Getter
    @Setter
    public static class Redis {
        private boolean enabled = false;
        private boolean required = false;
    }

    @Getter
    @Setter
    public static class LocalTtl {
        private Duration localTtl;

        public LocalTtl() {
        }

        public LocalTtl(Duration localTtl) {
            this.localTtl = localTtl;
        }
    }

    @Getter
    @Setter
    public static class TwoLevelTtl extends LocalTtl {
        private Duration redisTtl;

        public TwoLevelTtl() {
        }

        public TwoLevelTtl(Duration localTtl, Duration redisTtl) {
            super(localTtl);
            this.redisTtl = redisTtl;
        }
    }

    @Getter
    @Setter
    public static class Draft {
        private Duration persistInterval = Duration.ofSeconds(30);
        private Duration postExamTtl = Duration.ofMinutes(30);
    }
}

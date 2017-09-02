import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;

@SuppressWarnings({"WeakerAccess", "unused"})
class ChallengeConfiguration extends Configuration {
    private String dbDriverClassName = "com.mysql.jdbc.Driver";

    @NotEmpty
    private String dbUrl = "jdbc:mysql://localhost:3306/challenge";

    @NotEmpty
    private String dbUserName = "root";

    @NotEmpty
    private String dbPassword = "root";

    @Min(1000)
    private int serverPort = 18000;

    @Min(1024*1024)
    private int maxHttpContentLength = 100 * 1024 * 1024;

    @Min(1)
    private int ioThreads = 4;

    @Min(10)
    private int workerThreads = 300;

    private boolean tcpNoDelay = true;

    @Min(5)
    private int poolSize = 10;

    @JsonProperty
    public String getDbUserName() {
        return dbUserName;
    }

    @JsonProperty
    public int getPoolSize() {
        return poolSize;
    }

    @JsonProperty
    public String getDbDriverClassName() {
        return dbDriverClassName;
    }

    @JsonProperty
    public void setDbDriverClassName(String dbDriverClassName) {
        this.dbDriverClassName = dbDriverClassName;
    }

    @JsonProperty
    public String getDbUrl() {
        return dbUrl;
    }

    @JsonProperty
    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    @JsonProperty
    public String getDbPassword() {
        return dbPassword;
    }

    @JsonProperty
    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    @JsonProperty
    public int getServerPort() {
        return serverPort;
    }

    @JsonProperty
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    @JsonProperty
    public int getMaxHttpContentLength() {
        return maxHttpContentLength;
    }

    @JsonProperty
    public void setMaxHttpContentLength(int maxHttpContentLength) {
        this.maxHttpContentLength = maxHttpContentLength;
    }

    @JsonProperty
    public int getIoThreads() {
        return ioThreads;
    }

    @JsonProperty
    public void setIoThreads(int ioThreads) {
        this.ioThreads = ioThreads;
    }

    @JsonProperty
    public int getWorkerThreads() {
        return workerThreads;
    }

    @JsonProperty
    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }

    @JsonProperty
    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    @JsonProperty
    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }
}

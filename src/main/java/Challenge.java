import endpoint.DataService;
import io.dropwizard.Application;
import org.apache.commons.dbcp.BasicDataSource;
import webservice.Server;
import io.dropwizard.setup.Environment;

public class Challenge extends Application<ChallengeConfiguration>{
    private Server server;

    @Override
    public void run(ChallengeConfiguration conf, Environment env){
        try{
            try {
                BasicDataSource dataSource = new BasicDataSource();
                dataSource.setDriverClassName(conf.getDbDriverClassName());
                dataSource.setUrl(conf.getDbUrl());
                dataSource.setUsername(conf.getDbUserName());
                dataSource.setPassword(conf.getDbPassword());
                dataSource.setMaxActive(conf.getPoolSize());
                dataSource.setMaxIdle(conf.getPoolSize());
                server = new Server(conf.getServerPort(), conf.getMaxHttpContentLength(), new DataService(dataSource),
                        conf.getIoThreads(), conf.getWorkerThreads(), conf.isTcpNoDelay());
                server.start();
            } finally {
                if(server !=null) {
                    server.stop();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        new Challenge().run(args);
    }
}
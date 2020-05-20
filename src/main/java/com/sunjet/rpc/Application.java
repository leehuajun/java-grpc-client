package com.sunjet.rpc;

import com.sunjet.rpc.api.Hello;
import com.sunjet.rpc.api.HelloServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.ResourceUtils;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.FileNotFoundException;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        try {
            testTls();
        } catch (SSLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 不使用安全认证访问服务器
     */
    private static void test01(){
        io.grpc.Channel channel = NettyChannelBuilder.forAddress("127.0.0.1", 8081)
                .negotiationType(NegotiationType.PLAINTEXT)
                .build();
        Hello.HelloRequest request = Hello.HelloRequest.newBuilder().setName("LeeHuajun").build();
        HelloServiceGrpc.HelloServiceBlockingStub stub = HelloServiceGrpc.newBlockingStub(channel);
        Hello.HelloResponse response = stub.sayHello(request);
        System.out.println(response.getMessage());
    }
    private static void testTls() throws SSLException, FileNotFoundException {
        String path = ResourceUtils.getURL("classpath:").getPath();
        System.out.println(path);
        ManagedChannel channel = NettyChannelBuilder.forAddress("localhost", 8081)
                .negotiationType(NegotiationType.TLS)
                .overrideAuthority("localhost")
                .sslContext(buildSslContext(path + "cert/ca.pem"
                        , path + "cert/client.pem"
                        , path + "cert/client.key"))
                .build();
        Hello.HelloRequest request = Hello.HelloRequest.newBuilder().setName("LeeHuajun").build();
        HelloServiceGrpc.HelloServiceBlockingStub stub = HelloServiceGrpc.newBlockingStub(channel);
        Hello.HelloResponse response = stub.sayHello(request);
        System.out.println(response.getMessage());
    }

    private static SslContext buildSslContext(String trustCertCollectionFilePath,
                                       String clientCertChainFilePath,
                                       String clientPrivateKeyFilePath) throws SSLException {
        SslContextBuilder builder = GrpcSslContexts.forClient();
        if (trustCertCollectionFilePath != null) {
            builder.trustManager(new File(trustCertCollectionFilePath));
        }
        if (clientCertChainFilePath != null && clientPrivateKeyFilePath != null) {
            builder.keyManager(new File(clientCertChainFilePath), new File(clientPrivateKeyFilePath));
        }
        return builder.build();
    }

}

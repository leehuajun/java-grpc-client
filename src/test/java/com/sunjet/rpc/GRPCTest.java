package com.sunjet.rpc;

import com.sunjet.rpc.api.Hello;
import com.sunjet.rpc.api.HelloServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.FileNotFoundException;

public class GRPCTest {

    @Test
    void testTls() throws SSLException, FileNotFoundException {
        String path = ResourceUtils.getURL("classpath:").getPath() + "cert/";
//        System.out.println(path);
        ManagedChannel channel = NettyChannelBuilder.forAddress("localhost", 8081)
                .negotiationType(NegotiationType.TLS)
                .overrideAuthority("localhost")
                .sslContext(buildSslContext(path + "ca.pem"
                        , path + "client.pem"
                        , path + "client.pk8"))
                .build();
        Hello.HelloRequest request = Hello.HelloRequest.newBuilder().setName("LeeHuajun").build();
        HelloServiceGrpc.HelloServiceBlockingStub stub = HelloServiceGrpc.newBlockingStub(channel);
        Hello.HelloResponse response = stub.sayHello(request);
        System.out.println(response.getMessage());
    }


    private SslContext buildSslContext(String trustCertCollectionFilePath,
                                              String clientCertChainFilePath,
                                              String clientPrivateKeyFilePath) throws SSLException {
        SslContextBuilder builder = GrpcSslContexts.forClient();
        if (trustCertCollectionFilePath != null) {
            builder.trustManager(new File(trustCertCollectionFilePath));
        }
        if (clientCertChainFilePath != null && clientPrivateKeyFilePath != null) {
            builder.keyManager(new File(clientCertChainFilePath), new File(clientPrivateKeyFilePath),"1234");
        }
        return builder.build();
    }

}
